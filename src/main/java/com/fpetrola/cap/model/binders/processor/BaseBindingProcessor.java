package com.fpetrola.cap.model.binders.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.helpers.YamlHelper;
import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.BinderModMaker;
import com.fpetrola.cap.model.binders.BinderModUnmaker;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.binders.ModelBinder;
import com.fpetrola.cap.model.binders.TraverseListener;
import com.fpetrola.cap.model.binders.sync.ChangesLinker;
import com.fpetrola.cap.model.binders.sync.DummyChangesLinker;
import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.DummySourceChangesListener;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.fpetrola.cap.model.source.SourceCodeModification;
import com.github.javaparser.Position;
import com.github.javaparser.Range;

public abstract class BaseBindingProcessor {

	public SourceChangesListener sourceChangesListener;
	protected String configURI;
	protected List<CodeProposal> sourceChanges = new ArrayList<>();
	protected ModelBinder modelBinder;

	public BaseBindingProcessor() {
		super();
	}

	protected void addChangeProposalToBinder(Binder<?, ?> binder, String message, BinderModMaker doer, BinderModUnmaker undoer) {
		String modelSerialization = YamlHelper.serializeModel(modelBinder);
		doer.doMod(binder);
		String updatedModelSerialization = YamlHelper.serializeModel(modelBinder);

		List<SourceCodeModification> sourceCodeModifications = JavaSourceChangesHandler.createModifications(modelSerialization, updatedModelSerialization);

		Range range = new Range(new Position(1, 0), new Position(1, 100));
		if (binder != modelBinder) {
			Range findPositionOf = findPositionOfBinderBasedInYamlSerialization(binder);
			range = new Range(new Position(findPositionOf.begin.line, 0), new Position(findPositionOf.begin.line, 100));
		}
		CodeProposal codeProposal = new CodeProposal(configURI, range, message);
		codeProposal.getSourceChange().setInsertions(sourceCodeModifications);
		sourceChanges.add(codeProposal);

		undoer.undoMod(binder);
	}

	private Range findPositionOfBinderBasedInYamlSerialization(Binder<?, ?> binder) {
		Range result = new Range(new Position(0, 0), new Position(0, 1));

		Binder<?, ?> parent = binder.getParent();
		if (parent != null) {
			List<Binder> lastBinderChain = parent.getChain();

			List<Binder> binders = new ArrayList<>();
			binders.addAll(parent.getChain());
			int indexOf = binders.indexOf(binder);

			DefaultBinder<Object, Object> temporalBinder = new DefaultBinder<>();
			binders.add(indexOf + 1, temporalBinder);
			parent.setChain(binders);

			String modelSerialization = YamlHelper.serializeModel(modelBinder);
			parent.getChain().remove(binder);
			String modelSerialization2 = YamlHelper.serializeModel(modelBinder);
			parent.removeBinder(temporalBinder);
			List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization2, modelSerialization);
			parent.setChain(lastBinderChain);

			try {
				Position begin = result.begin;

				for (SourceCodeModification sourceCodeModification : createInsertions)
					begin = sourceCodeModification.range.begin;

				return new Range(new Position(begin.line + 1, 100), new Position(begin.line + 1, 100));
			} catch (Exception e) {
				return result;
			}
		}
		return result;
	}

	protected void bindModel(boolean doLoop, Binder<?, ?> aModelManagement, TraverseListener traverseListener, boolean listenChanges) {
		do {
			try {

				modelBinder.setSourceChangesListener(!listenChanges ? new DummySourceChangesListener() : sourceChangesListener);
				modelBinder.setTraverseListener(traverseListener);

				aModelManagement.solve(null);

			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				modelBinder.setTraverseListener(null);
				modelBinder.setSourceChangesListener(null);
			}
		} while (doLoop);
	}

	public void setConfigUri(String uri) {
		this.configURI = uri;
	}

	abstract public void bind(boolean doLoop);

	public static Void createVoid() {
		try {
			Constructor<Void> c = Void.class.getDeclaredConstructor();
			c.setAccessible(true);
			Void v = c.newInstance();
			return v;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}