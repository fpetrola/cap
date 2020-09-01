package com.fpetrola.cap.model.binders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.helpers.YamlHelper;
import com.fpetrola.cap.model.source.DummySourceChangesListener;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.fpetrola.cap.model.source.SourceCodeModification;
import com.github.javaparser.Position;
import com.github.javaparser.Range;

public abstract class BaseBindingProcessor {

	public SourceChangesListener sourceChangesListener;
	protected String configURI;
	protected List<SourceChange> sourceChanges = new ArrayList<SourceChange>();
	protected ModelManagement modelManagement;

	public BaseBindingProcessor() {
		super();
	}

	protected void addChangeProposalToBinder(Binder<?, ?> binder, String message, BinderModMaker doer, BinderModUnmaker undoer) {
		String modelSerialization = YamlHelper.serializeModel(modelManagement);
		doer.doMod(binder);
		String updatedModelSerialization = YamlHelper.serializeModel(modelManagement);

		List<SourceCodeModification> sourceCodeModifications = JavaSourceChangesHandler.createModifications(modelSerialization, updatedModelSerialization);

		Range range = new Range(new Position(1, 0), new Position(1, 100));
		if (binder != modelManagement) {
			Range findPositionOf = findPositionOfBinderBasedInYamlSerialization(binder);
			range = new Range(new Position(findPositionOf.begin.line, 0), new Position(findPositionOf.begin.line, 100));
		}
		SourceChange sourceChange = new SourceChange(configURI, range, message);
		sourceChange.insertions = sourceCodeModifications;
		sourceChanges.add(sourceChange);

		undoer.undoMod(binder);
	}

	private Range findPositionOfBinderBasedInYamlSerialization(Binder<?, ?> bidirectionalBinder) {
		Range range = new Range(new Position(0, 0), new Position(0, 1));
		Binder<?, ?> parent = bidirectionalBinder.getParent();

		if (parent != null) {
			List<Binder> lastBinderChain = parent.getChain();
			DefaultBinder<Object, Object> aBinder = new DefaultBinder<>();
			List<Binder> binders = new ArrayList<>();
			binders.addAll(parent.getChain());
			int indexOf = binders.indexOf(bidirectionalBinder);
			binders.add(indexOf + 1, aBinder);
			parent.setChain(binders);

			String modelSerialization = YamlHelper.serializeModel(modelManagement);
			parent.getChain().remove(bidirectionalBinder);
			String modelSerialization2 = YamlHelper.serializeModel(modelManagement);
			parent.removeBinder(aBinder);
			List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization2, modelSerialization);
			parent.setChain(lastBinderChain);

			try {
				Position begin = range.begin;

				for (SourceCodeModification sourceCodeModification : createInsertions) {
					begin = sourceCodeModification.range.begin;
				}
				return new Range(new Position(begin.line + 1, 100), new Position(begin.line + 1, 100));
			} catch (Exception e) {
				return range;
			}
		}
		return range;
	}

	protected void bindModel(boolean doLoop, Binder<?, ?> aModelManagement, TraverseListener traverseListener, boolean listenChanges) {
		do {
			SourceChangesListener lastSourceChangesListener;
			lastSourceChangesListener = sourceChangesListener;
			try {

				if (!listenChanges)
					sourceChangesListener = new DummySourceChangesListener();

				aModelManagement.accept(new BinderVisitor() {
					public void visitChainedBinder(Binder binder) {
						binder.setSourceChangesListener(sourceChangesListener);
						binder.setTraverserListener(traverseListener);
					}
				});
				aModelManagement.solve(null);
			} catch (Exception e) {
				e.printStackTrace();
			}

			aModelManagement.accept(new BinderVisitor() {
				public void visitChainedBinder(Binder binder) {
					binder.setSourceChangesListener(null);
					binder.setTraverserListener(null);
				}
			});

			sourceChangesListener = lastSourceChangesListener;
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