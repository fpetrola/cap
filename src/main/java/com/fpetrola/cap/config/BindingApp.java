package com.fpetrola.cap.config;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.fpetrola.cap.helpers.BindersDiscoveryService;
import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
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
	private BindersDiscoveryService bindersDiscoveryService = new BindersDiscoveryService();
	private List<SourceChange> sourceChanges = new ArrayList<SourceChange>();

	public BindingApp(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public void bind(boolean doLoop) {
		if (configURI != null)
			try {
				sourceChanges.clear();

				try {
					ModelManagement modelManagement = deserializeModelFromURI(configURI);

					bindersDiscoveryService.findBinders();

					proposeConfigForJPABinder(modelManagement);

					List<BidirectionalBinder> availableBinders = bindersDiscoveryService.findBinders();
					for (BidirectionalBinder bidirectionalBinder : availableBinders) {
						if (modelManagement.binderChain.isEmpty() && getBinderTypes(bidirectionalBinder)[0].equals(Object.class) && !bidirectionalBinder.pull("").isEmpty())
							addSourceInsertions(modelManagement, bidirectionalBinder);

						if (!modelManagement.binderChain.isEmpty()) {
							BidirectionalBinder lastBinder = modelManagement.binderChain.get(modelManagement.binderChain.size() - 1);
							boolean sourceBinderPresent = getBinderTypes(lastBinder)[1].equals(getBinderTypes(bidirectionalBinder)[0]);
							if (sourceBinderPresent)
								addSourceInsertions(modelManagement, bidirectionalBinder);
						}
					}

					proposeIds(modelManagement);
					sourceChangesListener.sourceChange(configURI, sourceChanges);

					runPath(doLoop, modelManagement, (b, v) -> {
					}, true);
				} catch (Exception e1) {
					proposeCreation();
				}
			} catch (IOException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private Range findPositionOf(ModelManagement modelManagement, BidirectionalBinder bidirectionalBinder) {
		try {
			String modelSerialization = serializeModel(modelManagement);
			List<BidirectionalBinder> lastBinderChain = modelManagement.binderChain;

			ArrayList<BidirectionalBinder> binders = new ArrayList<>(modelManagement.binderChain);
			modelManagement.binderChain = binders;
			modelManagement.binderChain.remove(bidirectionalBinder);
			String modelSerialization2 = serializeModel(modelManagement);
			List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization2, modelSerialization);
			modelManagement.binderChain = lastBinderChain;
			return createInsertions.get(0).range;
		} catch (YamlException e) {
			throw new RuntimeException(e);
		}
	}

	private ModelManagement deserializeModelFromURI(String uri) throws FileNotFoundException, YamlException {
		InputStream inputStream = new FileInputStream(new File(URI.create(uri)));
		YamlReader reader = new YamlReader(new InputStreamReader(inputStream));
		ModelManagement modelManagement = reader.read(ModelManagement.class);
		return modelManagement;
	}

	private ModelManagement deserializeModel(String serializedModel) throws FileNotFoundException, YamlException {
		InputStream inputStream = new ByteArrayInputStream(serializedModel.getBytes());
		YamlReader reader = new YamlReader(new InputStreamReader(inputStream));
		ModelManagement modelManagement = reader.read(ModelManagement.class);
		return modelManagement;
	}

	private void proposeCreation() throws YamlException {

		SourceChange sourceChange = new SourceChange(configURI, new Range(new Position(1, 1), new Position(1, 1)), "Initialize Model Management");

		ModelManagement modelManagement = new ModelManagement();
		String modelSerialization = "\n";
		String modelSerialization2 = serializeModel(modelManagement);
		List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization, modelSerialization2);
		if (!createInsertions.isEmpty()) {
			sourceChange.insertions = createInsertions;
			sourceChanges.add(sourceChange);
		}

		sourceChangesListener.sourceChange(configURI, sourceChanges);
	}

	private void runPath(boolean doLoop, ModelManagement modelManagement, TraverseListener traverseListener, boolean listenChanges) {
		try {
			do {
				SourceChangesListener lastSourceChangesListener = sourceChangesListener;
				if (!listenChanges) {
					sourceChangesListener = new SourceChangesListener() {
						public void fileCreation(String resourceUri, String content) {
						}

						public void sourceChange(String resourceUri, List<SourceChange> changes) {
						}
					};
				}

				List lastValue = new ArrayList<>(Arrays.asList(""));
				List<BidirectionalBinder> binderChain = modelManagement.getBinderChain();

				traverse(modelManagement, lastValue, binderChain, 0, new ArrayList<String>(), traverseListener);

				sourceChangesListener = lastSourceChangesListener;
			} while (doLoop);
		} catch (ClassNotFoundException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isAlreadyPresent(ModelManagement modelManagement, BidirectionalBinder bidirectionalBinder) {
		boolean anyMatch = modelManagement.binderChain.stream().anyMatch(b -> b.getClass().isAssignableFrom(bidirectionalBinder.getClass()));
		return anyMatch;
	}

	private void addSourceInsertions(ModelManagement modelManagement, BidirectionalBinder bidirectionalBinder) throws YamlException {
		boolean alreadyPresent = isAlreadyPresent(modelManagement, bidirectionalBinder);
		if (!alreadyPresent) {
			addBinder(modelManagement, bidirectionalBinder, "Binder is available: " + bidirectionalBinder.getClass().getSimpleName());
		}

	}

	private void proposeIds(ModelManagement modelManagement) throws YamlException {
		String modelSerialization = serializeModel(modelManagement);

		for (BidirectionalBinder bidirectionalBinder2 : modelManagement.binderChain) {
			try {
				String message = bidirectionalBinder2.getParametersProposalMessage();
				if (message.isEmpty())
					message = "Add filters for: " + bidirectionalBinder2.getClass().getSimpleName();

				Range range = findBInderRange(modelManagement, bidirectionalBinder2);

				SourceChange sourceChange = createSourceChange(message, range);

				List<String> lastIds = bidirectionalBinder2.getFilters();

				runPath(false, modelManagement, (b, v) -> {
					if (b == bidirectionalBinder2) {
						List<String> collect = (List<String>) v.stream().map(o -> o.toString()).collect(Collectors.toList());
						b.setFilters(collect);
					}
				}, false);

				String modelSerialization2 = serializeModel(modelManagement);
				List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization, modelSerialization2);
				if (!createInsertions.isEmpty()) {
					sourceChange.insertions = createInsertions;
					sourceChanges.add(sourceChange);
				}

				bidirectionalBinder2.setFilters(lastIds);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Range findBInderRange(ModelManagement modelManagement, BidirectionalBinder bidirectionalBinder) {
		Range positionOf = findPositionOf(modelManagement, bidirectionalBinder);
		Position withLine = new Position(positionOf.end.line + 2, 0);
		Range range = new Range(positionOf.begin.withLine(positionOf.begin.line + 1), withLine);
		return range;
	}

	private void proposeConfigForJPABinder(ModelManagement modelManagement) throws YamlException {
		String modelSerialization = serializeModel(modelManagement);

		for (BidirectionalBinder bidirectionalBinder2 : modelManagement.binderChain) {

			if (bidirectionalBinder2 instanceof WorkspaceAwareBinder) {
				WorkspaceAwareBinder jpaEntityBinder = (WorkspaceAwareBinder) bidirectionalBinder2;

				if (jpaEntityBinder.getWorkspacePath() == null) {
					File file = new File(URI.create(configURI));

					while (file != null) {
						String message = "use workspace in: " + file.getPath();
						SourceChange sourceChange = createSourceChange(message, findBInderRange(modelManagement, bidirectionalBinder2));
						String workspacePath = ((WorkspaceAwareBinder) bidirectionalBinder2).getWorkspacePath();
						jpaEntityBinder.setWorkspacePath(file.getPath());

						String modelSerialization2 = serializeModel(modelManagement);
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

	private void addBinder(ModelManagement modelManagement, BidirectionalBinder bidirectionalBinder, String message) throws YamlException {
		SourceChange sourceChange = createSourceChange(message);
		String modelSerialization = serializeModel(modelManagement);

		modelManagement.binderChain.add(bidirectionalBinder);

		String modelSerialization2 = serializeModel(modelManagement);
		List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications(modelSerialization, modelSerialization2);
		sourceChange.insertions = createInsertions;
		sourceChanges.add(sourceChange);
		modelManagement.binderChain.remove(bidirectionalBinder);
	}

	private SourceChange createSourceChange(String message) {
		Position begin = new Position(1, 1);
		Position end = new Position(1, 2);
		Range range = new Range(begin, end);
		SourceChange sourceChange = createSourceChange(message, range);
		return sourceChange;
	}

	private SourceChange createSourceChange(String message, Range range) {
		SourceChange sourceChange = new SourceChange(configURI, range, message);
		return sourceChange;
	}

	private Type[] getBinderTypes(BidirectionalBinder<?, ?> bidirectionalBinder) {
		Type[] actualTypeArguments = ((sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl) bidirectionalBinder.getClass().getGenericInterfaces()[0]).getActualTypeArguments();
		return actualTypeArguments;
	}

	private String serializeModel(ModelManagement modelManagement) throws YamlException {
		Writer writer = new CharArrayWriter();
		YamlWriter yamlWriter = new YamlWriter(writer);
		yamlWriter.write(modelManagement);
		yamlWriter.close();
		return writer.toString();
	}

	private void traverse(ModelManagement modelManagement, List lastValue, List<BidirectionalBinder> binderChain, int i, List ids, TraverseListener traverserListener) throws ClassNotFoundException, InterruptedException {
		if (i < binderChain.size()) {
			BidirectionalBinder bidirectionalBinder = binderChain.get(i);
			lastValue = pickResults(modelManagement, lastValue, ids);
			for (Object object : lastValue) {
				bidirectionalBinder.setSourceChangesListener(sourceChangesListener);
				List result = bidirectionalBinder.pull(object);
				bidirectionalBinder.setSourceChangesListener(null);
				traverse(modelManagement, result, binderChain, i + 1, bidirectionalBinder.getFilters(), traverserListener);
				traverserListener.valuesPulledFrom(bidirectionalBinder, result);
//				System.out.println(lastValue);
//				Thread.sleep(100);
			}
		}
	}

	private List pickResults(ModelManagement modelManagement, List lastValue, List<String> ids) throws ClassNotFoundException {
		List result = new ArrayList(lastValue);
		List models = new ArrayList();

		for (String id : ids) {
			for (Object object : lastValue) {
				boolean assignableFrom = modelManagement.model == null || object.getClass().isAssignableFrom(Class.forName(modelManagement.model));
				if (assignableFrom) {
					if (object.toString().contains(id)) {
						models.add(object);
						break;
					}
				}
			}
		}

		return models.isEmpty() ? result : models;
	}

	public void setConfigUri(String uri) {
		this.configURI = uri;
	}
}
