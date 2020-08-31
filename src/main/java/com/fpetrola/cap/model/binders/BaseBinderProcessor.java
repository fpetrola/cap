package com.fpetrola.cap.model.binders;

import java.lang.reflect.Type;
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

public class BaseBinderProcessor {

	public SourceChangesListener sourceChangesListener;
	protected String configURI;
	protected List<SourceChange> sourceChanges = new ArrayList<SourceChange>();
	protected ModelManagement modelManagement;

	public BaseBinderProcessor() {
		super();
	}

	protected void addChangeProposalToBinder(Binder<?, ?> binder, String message, BinderModMaker doer, BinderModeUnmaker undoer) {
		String modelSerialization = YamlHelper.serializeModel(modelManagement);
		doer.doMod(binder);
		String updatedModelSerialization = YamlHelper.serializeModel(modelManagement);

		List<SourceCodeModification> sourceCodeModifications = JavaSourceChangesHandler.createModifications(modelSerialization, updatedModelSerialization);

		Range range = new Range(new Position(1, 0), new Position(1, 100));
		if (binder != modelManagement) {
			Range findPositionOf = findPositionOf(binder);
			range = new Range(new Position(findPositionOf.begin.line, 0), new Position(findPositionOf.begin.line, 100));
		}
		SourceChange sourceChange = new SourceChange(configURI, range, message);
		sourceChange.insertions = sourceCodeModifications;
		sourceChanges.add(sourceChange);

		undoer.undoMod(binder);
	}

	private Range findPositionOf(Binder<?, ?> bidirectionalBinder) {
		Range range = new Range(new Position(0, 0), new Position(0, 1));
		Binder<?, ?> parent = bidirectionalBinder.getParent();

		if (parent != null) {
			List<Binder<?, ?>> lastBinderChain = parent.getChain();
			DefaultBinder<Object, Object> aBinder = new DefaultBinder<>();
			List<Binder<?, ?>> binders = new ArrayList<>();
			binders.addAll(parent.getChain());
			int indexOf = binders.indexOf(bidirectionalBinder);
			binders.add(indexOf+1, aBinder);
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

	protected void runPath(boolean doLoop, Binder<?, ?> aModelManagement, TraverseListener traverseListener, boolean listenChanges) {
		do {
			SourceChangesListener lastSourceChangesListener = sourceChangesListener;

			if (!listenChanges)
				sourceChangesListener = new DummySourceChangesListener();

			List<Binder<?, ?>> binderChain = aModelManagement.getChain();

			traverse(aModelManagement, new ArrayList<>(Arrays.asList("")), binderChain, 0, new ArrayList<String>(), traverseListener);

			sourceChangesListener = lastSourceChangesListener;
		} while (doLoop);
	}

	protected Type[] getBinderTypes(Binder<?, ?> bidirectionalBinder) {
		return ((sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl) bidirectionalBinder.getClass().getGenericInterfaces()[0]).getActualTypeArguments();
	}

	private void traverse(Binder<?, ?> aModelManagement, List lastValue, List<Binder<?, ?>> binderChain, int i, List ids, TraverseListener traverserListener) {
		if (i < binderChain.size()) {
			Binder bidirectionalBinder = binderChain.get(i);
			lastValue = pickResults(aModelManagement, lastValue, ids);

			if (lastValue.isEmpty()) {
				List binderChain2 = bidirectionalBinder.getChain();
				if (!binderChain2.isEmpty()) {
					Binder<?, ?> object2 = (Binder<?, ?>) binderChain2.get(0);
					List lastValue1 = new ArrayList<>(Arrays.asList(""));

					traverse(object2, lastValue1, binderChain2, 0, bidirectionalBinder.getFilters(), traverserListener);
				}
			} else
				for (Object object : lastValue) {
					bidirectionalBinder.setSourceChangesListener(sourceChangesListener);
					List result = bidirectionalBinder.pull(object);
					List binderChain2 = bidirectionalBinder.getChain();
					if (!binderChain2.isEmpty()) {
						Binder<?, ?> object2 = (Binder<?, ?>) binderChain2.get(0);
						traverse(object2, result, binderChain2, 0, bidirectionalBinder.getFilters(), traverserListener);
					}
					bidirectionalBinder.setSourceChangesListener(null);
					traverse(aModelManagement, result, binderChain, i + 1, bidirectionalBinder.getFilters(), traverserListener);
					traverserListener.valuesPulledFrom(bidirectionalBinder, result);
				}
		}
	}

	private List pickResults(Binder<?, ?> aModelManagement, List lastValue, List<String> ids) {
		if (lastValue != null) {
			List result = new ArrayList(lastValue);
			List models = new ArrayList();

			for (String id : ids) {
				for (Object object : lastValue) {
					/*
					 * boolean assignableFrom = aModelManagement.model == null ||
					 * object.getClass().isAssignableFrom(Class.forName(aModelManagement.model)); if
					 * (assignableFrom)
					 */ {
						if (object.toString().contains(id)) {
							models.add(object);
							break;
						}
					}
				}
			}

			return models.isEmpty() ? result : models;
		} else
			return Arrays.asList();
	}

	public void setConfigUri(String uri) {
		this.configURI = uri;
	}

	public void bind(boolean doLoop) {
	}

}