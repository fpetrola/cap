package com.fpetrola.cap.config;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
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
import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.BindWriter;
import com.fpetrola.cap.model.binders.BindersFinder;
import com.fpetrola.cap.model.binders.implementations.JPAEntityBinder;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.fpetrola.cap.model.source.SourceCodeInsertion;
import com.fpetrola.cap.model.source.SourceCodeModification;
import com.github.javaparser.Position;
import com.github.javaparser.Range;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BindingApp {
	public SourceChangesListener sourceChangesListener;
	private String configURI;
	private BindersFinder bindersFinder;
	private List<SourceChange> sourceChanges = new ArrayList<SourceChange>();

	public BindingApp(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public void bind(boolean doLoop) {
		if (configURI != null)
			try {
				sourceChanges.clear();

				InputStream inputStream = new FileInputStream(new File(URI.create(configURI)));
				YamlReader reader = new YamlReader(new InputStreamReader(inputStream));

				try {
					ModelManagement modelManagement = reader.read(ModelManagement.class);
					bindersFinder = new BindersFinder();
					bindersFinder.findBinders();

					proposeIds(modelManagement);
					proposeConfigForJPABinder(modelManagement);

					List<BidirectionalBinder> pullers = bindersFinder.pullers;
					for (BidirectionalBinder bidirectionalBinder : pullers) {
						Type[] actualTypeArguments = getBinderTypes(bidirectionalBinder);
						Type type = actualTypeArguments[0];
						Type type2 = actualTypeArguments[1];
						boolean baseBinder = type.equals(Object.class);
						if (baseBinder) {
							try {
								List pull = bidirectionalBinder.pull("");
								if (pull.isEmpty())
									baseBinder = false;
							} catch (Exception e) {
								baseBinder = false;
							}

							if (baseBinder)
								addSourceInsertions(modelManagement, bidirectionalBinder);
						}

						boolean sourceBinderPresent = modelManagement.binderChain.stream().anyMatch(b -> getBinderTypes(b)[1].equals(type));
						if (sourceBinderPresent)
							addSourceInsertions(modelManagement, bidirectionalBinder);
					}
					sourceChangesListener.newSourceChanges(configURI, sourceChanges);

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

	private void proposeCreation() throws YamlException {

		Position begin = new Position(1, 1);
		Position end = new Position(1, 1);
		SourceChange sourceChange = new SourceChange(configURI, new Range(begin, end), "Initialize Model Management");

		ModelManagement modelManagement = new ModelManagement();
		String modelSerialization = "\n";
		String modelSerialization2 = getModelSerialization(modelManagement);
		List<SourceCodeModification> createInsertions = new BindWriter().createModifications(modelSerialization, modelSerialization2);
		if (!createInsertions.isEmpty()) {
			sourceChange.insertions = createInsertions;
			sourceChanges.add(sourceChange);
		}

		sourceChangesListener.newSourceChanges(configURI, sourceChanges);
	}

	private void runPath(boolean doLoop, ModelManagement modelManagement, TraverseListener traverseListener, boolean listenChanges) {
		try {
			do {
				SourceChangesListener lastSourceChangesListener = sourceChangesListener;
				if (!listenChanges) {
					sourceChangesListener = new SourceChangesListener() {
						public void fileCreation(String mappedClass, String content) {
						}

						public void newSourceChanges(String resource, List<SourceChange> changes) {
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
		String modelSerialization = getModelSerialization(modelManagement);

		for (BidirectionalBinder bidirectionalBinder2 : modelManagement.binderChain) {
			try {
				String message = bidirectionalBinder2.getParametersProposalMessage();
				if (message.isEmpty())
					message = "Add filters for: " + bidirectionalBinder2.getClass().getSimpleName();

				SourceChange sourceChange = createSourceChange(bidirectionalBinder2, message);

				List<String> lastIds = bidirectionalBinder2.getFilters();

				runPath(false, modelManagement, (b, v) -> {
					if (b == bidirectionalBinder2) {
						List<String> collect = (List<String>) v.stream().map(o -> o.toString()).collect(Collectors.toList());
						b.setFilters(collect);
					}
				}, false);

				String modelSerialization2 = getModelSerialization(modelManagement);
				List<SourceCodeModification> createInsertions = new BindWriter().createModifications(modelSerialization, modelSerialization2);
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

	private void proposeConfigForJPABinder(ModelManagement modelManagement) throws YamlException {
		String modelSerialization = getModelSerialization(modelManagement);

		for (BidirectionalBinder bidirectionalBinder2 : modelManagement.binderChain) {

			if (bidirectionalBinder2 instanceof JPAEntityBinder) {
				JPAEntityBinder jpaEntityBinder = (JPAEntityBinder) bidirectionalBinder2;

				if (jpaEntityBinder.getWorkspacePath() == null) {
					File file = new File(URI.create(configURI));

					while (file != null) {
						String message = "use workspace in: " + file.getPath();
						SourceChange sourceChange = createSourceChange(bidirectionalBinder2, message);
						String workspacePath = ((JPAEntityBinder) bidirectionalBinder2).getWorkspacePath();
						jpaEntityBinder.setWorkspacePath(file.getPath());

						String modelSerialization2 = getModelSerialization(modelManagement);
						List<SourceCodeModification> createInsertions = new BindWriter().createModifications(modelSerialization, modelSerialization2);
						if (!createInsertions.isEmpty()) {
							sourceChange.insertions = createInsertions;
							sourceChanges.add(sourceChange);
						}

						((JPAEntityBinder) bidirectionalBinder2).setWorkspacePath(workspacePath);

						file = file.getParentFile();
					}
				}
			}
		}
	}

	private void addBinder(ModelManagement modelManagement, BidirectionalBinder bidirectionalBinder, String message) throws YamlException {
		SourceChange sourceChange = createSourceChange(bidirectionalBinder, message);
		String modelSerialization = getModelSerialization(modelManagement);

		modelManagement.binderChain.add(bidirectionalBinder);

		String modelSerialization2 = getModelSerialization(modelManagement);
		List<SourceCodeModification> createInsertions = new BindWriter().createModifications(modelSerialization, modelSerialization2);
		sourceChange.insertions = createInsertions;
		sourceChanges.add(sourceChange);
		modelManagement.binderChain.remove(bidirectionalBinder);
	}

	private SourceChange createSourceChange(BidirectionalBinder bidirectionalBinder, String message) {
		Position begin = new Position(1, 1);
		Position end = new Position(1, 2);
		SourceChange sourceChange = new SourceChange(configURI, new Range(begin, end), message);
		return sourceChange;
	}

	private Type[] getBinderTypes(BidirectionalBinder<?, ?> bidirectionalBinder) {
		Type[] actualTypeArguments = ((sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl) bidirectionalBinder.getClass().getGenericInterfaces()[0]).getActualTypeArguments();
		return actualTypeArguments;
	}

	private String getModelSerialization(ModelManagement modelManagement) throws YamlException {
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
