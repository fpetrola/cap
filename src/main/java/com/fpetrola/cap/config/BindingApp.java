package com.fpetrola.cap.config;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.esotericsoftware.yamlbeans.YamlException;
import com.fpetrola.cap.helpers.BindersDiscoveryService;
import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.BinderVisitor;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
import com.fpetrola.cap.model.binders.implementations.DatabaseEntitiesExtractor;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.fpetrola.cap.model.source.SourceCodeModification;
import com.github.javaparser.Position;
import com.github.javaparser.Range;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BindingApp extends BaseBinderProcessor {
	public BindingApp(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public void bind(boolean doLoop) {
		if (configURI != null)
			sourceChanges.clear();

		try {
			modelManagement = YamlHelper.deserializeModelFromURI(configURI, ModelManagement.class);
			setParents(modelManagement);

			proposeNewBinders(modelManagement);
			proposeFilters(modelManagement);
			proposeConfigForWorkspaceAwareBinder(modelManagement);

			runPath(false, modelManagement, (b, v) -> {
			}, true);

			sourceChangesListener.sourceChange(configURI, sourceChanges);

		} catch (Exception e1) {
			proposeCreation();
		}
	}

	private void setParents(Binder<?, ?> binder) {
		for (Binder child : binder.getChain()) {
			child.setParent(binder);
			setParents(child);
		}
	}

	private void proposeNewBinders(Binder<?, ?> aModelManagement) throws YamlException {
		BindersDiscoveryService bindersDiscoveryService = new BindersDiscoveryService();

		modelManagement.accept(new BinderVisitor() {
			@Override
			public void visitChainedBinder(Binder binder) {
				List<Binder> availableBinders = bindersDiscoveryService.findBinders();
				for (Binder availableBinder : availableBinders) {
					Type[] availableBinderTypes2 = getBinderTypes(availableBinder);
					boolean sourceBinderPresent = false;
					if (!binder.getChain().isEmpty()) {
						Binder lastBinder = (Binder) binder.getChain().get(binder.getChain().size() - 1);
						sourceBinderPresent = getBinderTypes(lastBinder)[1].equals(availableBinderTypes2[0]);
					}

					boolean noInputBinder = availableBinderTypes2[0].equals(Object.class) && !availableBinder.pull("").isEmpty();
					boolean parentLinkedBinder = false;

					Binder parent = binder.getParent();
					if (parent != null) {
						Type[] parentBinderTypes = getBinderTypes(parent);
						parentLinkedBinder = availableBinderTypes2[0].equals(parentBinderTypes[1]);
					}
					
					if (noInputBinder || sourceBinderPresent || parentLinkedBinder) {
						List<Binder<?, ?>> chain = binder.getChain();
						addChangeProposalToBinder(binder, "Add binder: " + availableBinder.getClass().getSimpleName(), (aBinder) -> {
							aBinder.setChain(new ArrayList<>(chain));
							aBinder.addBinder(availableBinder);
						}, (bidirectionalBinder) -> bidirectionalBinder.setChain(chain));
					} else {
					}
				}
			}
		});
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

	private void proposeFilters(Binder<?, ?> aModelManagement) throws YamlException {
		modelManagement.accept(new BinderVisitor() {
			public void visitChainedBinder(Binder binder) {
				if (binder instanceof DatabaseEntitiesExtractor) {
					String message = binder.getParametersProposalMessage();
					if (message.isEmpty())
						message = "Add filters for: " + binder.getClass().getSimpleName();

					List<String> lastFilters = binder.getFilters();
					addChangeProposalToBinder(binder, message, (bidirectionalBinder) -> {
						runPath(false, modelManagement, (b, v) -> {
							if (b == binder) {
								b.setFilters((List<String>) v.stream().map(o -> o.toString()).collect(Collectors.toList()));
							}
						}, false);
					}, (bidirectionalBinder) -> binder.setFilters(lastFilters));
				}
			}
		});
	}

	private void proposeConfigForWorkspaceAwareBinder(Binder<?, ?> aModelManagement) throws YamlException {

		modelManagement.accept(new BinderVisitor() {
			public void visitChainedBinder(Binder binder) {
				if (binder instanceof WorkspaceAwareBinder) {
					WorkspaceAwareBinder workspaceAwareBinder = (WorkspaceAwareBinder) binder;

					if (workspaceAwareBinder.findWorkspacePath() == null) {
						File[] file = new File[1];
						file[0] = new File(URI.create(configURI));

						while (file[0] != null) {
							if (file[0].isDirectory()) {
								String message = "use workspace in: " + file[0].getPath();
								String lastWorkspacePath = ((WorkspaceAwareBinder) binder).getWorkspacePath();

								addChangeProposalToBinder(binder, message, (bidirectionalBinder) -> workspaceAwareBinder.setWorkspacePath(file[0].getPath()), (bidirectionalBinder) -> ((WorkspaceAwareBinder) binder).setWorkspacePath(lastWorkspacePath));
							}
							file[0] = file[0].getParentFile();
						}
					}
				}
			}
		});

	}
}
