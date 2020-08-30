package com.fpetrola.cap.config;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.esotericsoftware.yamlbeans.YamlException;
import com.fpetrola.cap.helpers.BindersDiscoveryService;
import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
import com.fpetrola.cap.model.binders.implementations.DatabaseEntitiesExtractor;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.fpetrola.cap.model.source.SourceCodeModification;
import com.github.javaparser.Position;
import com.github.javaparser.Range;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BindingApp {
	public SourceChangesListener sourceChangesListener;
	private String configURI;
	private List<SourceChange> sourceChanges = new ArrayList<SourceChange>();
	private ModelManagement<Object, Object> modelManagement;

	public BindingApp(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public void bind(boolean doLoop) {
		if (configURI != null)
			sourceChanges.clear();

		try {
			modelManagement = YamlHelper.deserializeModelFromURI(configURI, ModelManagement.class);

			proposeAll(modelManagement, Object.class);
			runPath(false, modelManagement, (b, v) -> {
			}, true);

			sourceChangesListener.sourceChange(configURI, sourceChanges);

		} catch (Exception e1) {
			proposeCreation();
		}
	}

	private void proposeAll(Binder<Object, Object, Object> aModelManagement, Type type) throws YamlException {
		proposeChildModels(aModelManagement);
		proposeNewBinders(aModelManagement, type);
		proposeConfigForWorkspaceAwareBinder(aModelManagement);
		proposeFilters(aModelManagement);
	}

	private void proposeChildModels(Binder<Object, Object, Object> defaultBinder) throws YamlException {
		for (BidirectionalBinder<Object, Object> binder : defaultBinder.getChain()) {
			proposeAll(binder, getBinderTypes(binder)[1]);
		}
	}

	private void proposeNewBinders(Binder<Object, Object, Object> aModelManagement, Type type) throws YamlException {
		BindersDiscoveryService bindersDiscoveryService = new BindersDiscoveryService();
		List<BidirectionalBinder> availableBinders = bindersDiscoveryService.findBinders();

		for (BidirectionalBinder bidirectionalBinder : availableBinders) {
			List<BidirectionalBinder<Object, Object>> binderChain = aModelManagement.getChain();
			Type[] binderTypes = getBinderTypes(bidirectionalBinder);
			if (binderChain.isEmpty()) {
				if (binderTypes[0].equals(type)) {
					if (!type.equals(Object.class) || !bidirectionalBinder.pull("").isEmpty())
						addSourceInsertions(aModelManagement, bidirectionalBinder);
				}
			} else {
				BidirectionalBinder lastBinder = (BidirectionalBinder) binderChain.get(binderChain.size() - 1);
				boolean sourceBinderPresent = getBinderTypes(lastBinder)[1].equals(binderTypes[0]);
				if (!binderTypes[0].equals(Object.class) || !bidirectionalBinder.pull("").isEmpty())
					if (sourceBinderPresent)
						addSourceInsertions(aModelManagement, bidirectionalBinder);
			}
		}
	}

	private Range findPositionOf(Binder<Object, Object, Object> bidirectionalBinder) {
		Binder<Object, Object, Object> foundModelManagement = findParentOf(modelManagement, bidirectionalBinder);

		String modelSerialization = YamlHelper.serializeModel(modelManagement);
		List<BidirectionalBinder<Object, Object>> lastBinderChain = foundModelManagement.getChain();

		List<BidirectionalBinder<Object, Object>> binders = new ArrayList<>(foundModelManagement.getChain());
		foundModelManagement.setChain(binders);
		foundModelManagement.getChain().remove(bidirectionalBinder);
		String modelSerialization2 = YamlHelper.serializeModel(modelManagement);
		List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization2, modelSerialization);
		foundModelManagement.setChain(lastBinderChain);

		Range range = new Range(new Position(0, 0), new Position(0, 1));

		try {
			Position begin = createInsertions.get(0).range.begin;
			Position end = createInsertions.get(0).range.end;
			return new Range(new Position(end.line, 100), new Position(end.line, 100));
//				if (foundModelManagement.getBinderChain().size() == 1)
//					return createInsertions.get(1).range;
//				else
//					return createInsertions.get(0).range;
		} catch (Exception e) {
			return range;
		}
	}

	private Binder<Object, Object, Object> findParentOf(Binder<Object, Object, Object> parent, Binder<Object, Object, Object> bidirectionalBinder2) {
		if (parent.getChain().contains(bidirectionalBinder2))
			return parent;
		else
			for (BidirectionalBinder<Object, Object> child : parent.getChain()) {
				if (findParentOf(child, bidirectionalBinder2) != null)
					return child;
			}

		return null;
	}

	private void proposeCreation() {

		SourceChange sourceChange = new SourceChange(configURI, new Range(new Position(1, 1), new Position(1, 1)), "Initialize Model Management");
		modelManagement = new ModelManagement();
		List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications("\n", YamlHelper.serializeModel(modelManagement));
		if (!createInsertions.isEmpty()) {
			sourceChange.insertions = createInsertions;
			sourceChanges.add(sourceChange);
		}

		sourceChangesListener.sourceChange(configURI, sourceChanges);
	}

	private void runPath(boolean doLoop, Binder<Object, Object, Object> aModelManagement, TraverseListener traverseListener, boolean listenChanges) {
		do {
			SourceChangesListener lastSourceChangesListener = sourceChangesListener;

			if (!listenChanges)
				sourceChangesListener = new DummySourceChangesListener();

			List<BidirectionalBinder<Object, Object>> binderChain = aModelManagement.getChain();

			traverse(aModelManagement, new ArrayList<>(Arrays.asList("")), binderChain, 0, new ArrayList<String>(), traverseListener);

			sourceChangesListener = lastSourceChangesListener;
		} while (doLoop);
	}

	private void addSourceInsertions(Binder<Object, Object, Object> aModelManagement, BidirectionalBinder bidirectionalBinder) throws YamlException {
		addBinder(aModelManagement, bidirectionalBinder, "Binder is available: " + bidirectionalBinder.getClass().getSimpleName());
	}

	private void proposeFilters(Binder<Object, Object, Object> aModelManagement) throws YamlException {
		String modelSerialization = YamlHelper.serializeModel(modelManagement);

		List<BidirectionalBinder<Object, Object>> binderChain = aModelManagement.getChain();
		for (BidirectionalBinder bidirectionalBinder : binderChain) {
			if (bidirectionalBinder instanceof DatabaseEntitiesExtractor) {
				List<String> lastIds = bidirectionalBinder.getFilters();

				String message = bidirectionalBinder.getParametersProposalMessage();
				if (message.isEmpty())
					message = "Add filters for: " + bidirectionalBinder.getClass().getSimpleName();

				Range range = findBinderRange(aModelManagement, bidirectionalBinder);

				runPath(false, modelManagement, (b, v) -> {
					if (b == bidirectionalBinder) {
						b.setFilters((List<String>) v.stream().map(o -> o.toString()).collect(Collectors.toList()));
					}
				}, false);

				String modelSerialization2 = YamlHelper.serializeModel(modelManagement);
				List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization, modelSerialization2);
				if (!createInsertions.isEmpty()) {
					SourceChange sourceChange = createSourceChange(message, range);
					sourceChange.insertions = createInsertions;
					sourceChanges.add(sourceChange);
				}

				bidirectionalBinder.setFilters(lastIds);
			}
		}
	}

	private Range findBinderRange(Binder<Object, Object, Object> aModelManagement, Binder<Object, Object, Object> bidirectionalBinder) {
		Range positionOf = findPositionOf(bidirectionalBinder);
		Position withLine = new Position(positionOf.end.line + 2, 0);
		Range range = new Range(positionOf.begin.withLine(positionOf.begin.line + 1), withLine);
		return range;
	}

	private void proposeConfigForWorkspaceAwareBinder(Binder<Object, Object, Object> aModelManagement) throws YamlException {
		String modelSerialization = YamlHelper.serializeModel(modelManagement);

		for (BidirectionalBinder bidirectionalBinder2 : aModelManagement.getChain()) {

			if (bidirectionalBinder2 instanceof WorkspaceAwareBinder) {
				WorkspaceAwareBinder jpaEntityBinder = (WorkspaceAwareBinder) bidirectionalBinder2;

				if (jpaEntityBinder.getWorkspacePath() == null) {
					File file = new File(URI.create(configURI));

					while (file != null) {
						String message = "use workspace in: " + file.getPath();
						SourceChange sourceChange = createSourceChange(message, findBinderRange(aModelManagement, bidirectionalBinder2));
						String workspacePath = ((WorkspaceAwareBinder) bidirectionalBinder2).getWorkspacePath();
						jpaEntityBinder.setWorkspacePath(file.getPath());

						String modelSerialization2 = YamlHelper.serializeModel(modelManagement);
						List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization, modelSerialization2);
						if (!createInsertions.isEmpty()) {
							sourceChange.insertions = createInsertions;
							sourceChanges.add(sourceChange);
						}

						((WorkspaceAwareBinder) bidirectionalBinder2).setWorkspacePath(workspacePath);

						file = file.getParentFile();
					}
				}
			}
		}
	}

	private void addBinder(Binder<Object, Object, Object> aModelManagement, BidirectionalBinder bidirectionalBinder, String message) throws YamlException {
		String modelSerialization = YamlHelper.serializeModel(modelManagement);

		Range range1 = new Range(new Position(1, 1), new Position(1, 2));
		if (aModelManagement != modelManagement)
			range1 = findBinderRange(modelManagement, aModelManagement);

		SourceChange sourceChange = createSourceChange(message, range1);

		aModelManagement.getChain().add(bidirectionalBinder);

		String modelSerialization2 = YamlHelper.serializeModel(modelManagement);
		List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization, modelSerialization2);
		sourceChange.insertions = createInsertions;
		sourceChanges.add(sourceChange);
		aModelManagement.getChain().remove(bidirectionalBinder);
	}

	private SourceChange createSourceChange(String message, Range range) {
		SourceChange sourceChange = new SourceChange(configURI, range, message);
		return sourceChange;
	}

	private Type[] getBinderTypes(BidirectionalBinder<?, ?> bidirectionalBinder) {
		return ((sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl) bidirectionalBinder.getClass().getGenericInterfaces()[0]).getActualTypeArguments();
	}

	private void traverse(Binder<Object, Object, Object> aModelManagement, List lastValue, List<BidirectionalBinder<Object, Object>> binderChain, int i, List ids, TraverseListener traverserListener) {
		if (i < binderChain.size()) {
			BidirectionalBinder bidirectionalBinder = binderChain.get(i);
			lastValue = pickResults(aModelManagement, lastValue, ids);

			if (lastValue.isEmpty()) {
				List binderChain2 = bidirectionalBinder.getChain();
				if (!binderChain2.isEmpty()) {
					Binder<Object, Object, Object> object2 = (Binder<Object, Object, Object>) binderChain2.get(0);
					List lastValue1 = new ArrayList<>(Arrays.asList(""));

					traverse(object2, lastValue1, binderChain2, 0, bidirectionalBinder.getFilters(), traverserListener);
				}
			} else
				for (Object object : lastValue) {
					bidirectionalBinder.setSourceChangesListener(sourceChangesListener);
					List result = bidirectionalBinder.pull(object);
					List binderChain2 = bidirectionalBinder.getChain();
					if (!binderChain2.isEmpty()) {
						Binder<Object, Object, Object> object2 = (Binder<Object, Object, Object>) binderChain2.get(0);
						traverse(object2, result, binderChain2, 0, bidirectionalBinder.getFilters(), traverserListener);
					}
					bidirectionalBinder.setSourceChangesListener(null);
					traverse(aModelManagement, result, binderChain, i + 1, bidirectionalBinder.getFilters(), traverserListener);
					traverserListener.valuesPulledFrom(bidirectionalBinder, result);
//				System.out.println(lastValue);
//				Thread.sleep(100);
				}
		}
	}

	private List pickResults(Binder<Object, Object, Object> aModelManagement, List lastValue, List<String> ids) {
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
}
