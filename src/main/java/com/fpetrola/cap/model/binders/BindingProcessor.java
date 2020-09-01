package com.fpetrola.cap.model.binders;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.esotericsoftware.yamlbeans.YamlException;
import com.fpetrola.cap.helpers.BindersDiscoveryService;
import com.fpetrola.cap.helpers.YamlHelper;
import com.fpetrola.cap.model.binders.implementations.DatabaseEntitiesExtractor;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.fpetrola.cap.model.source.SourceCodeModification;
import com.github.javaparser.Position;
import com.github.javaparser.Range;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BindingProcessor extends BaseBindingProcessor {
	public BindingProcessor(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public void bind(boolean doLoop) {
		if (configURI != null)
			sourceChanges.clear();

		try {

			deserializeModel();
			proposeNewBinders();
			proposeFilters();
			proposeConfigForWorkspaceAwareBinder();

			bindModel(false, modelManagement, (b, v) -> {
			}, true);

			sourceChangesListener.sourceChange(configURI, sourceChanges);

		} catch (Exception e1) {
			proposeCreation();
		}
	}

	private void deserializeModel() throws FileNotFoundException, YamlException {
		modelManagement = YamlHelper.deserializeModelFromURI(configURI, ModelManagement.class);
		setParents(modelManagement);
	}

	private void setParents(Binder<?, ?> binder) {
		for (Binder child : binder.getChain()) {
			child.setParent(binder);
			setParents(child);
		}
	}

	private void proposeNewBinders() throws YamlException {
		BindersDiscoveryService bindersDiscoveryService = new BindersDiscoveryService();

		modelManagement.accept(new BinderVisitor() {
			@Override
			public void visitChainedBinder(Binder binder) {
				List<Binder> newBinders = bindersDiscoveryService.findBinders();

				newBinders.stream().forEach(newBinder -> {
					if (newBinder.canReceiveFrom(binder)) {
						List<Binder> chain = binder.getChain();
						addChangeProposalToBinder(binder, "Add binder: " + newBinder.getClass().getSimpleName(), (aBinder) -> {
							aBinder.setChain(new ArrayList<>(chain));
							aBinder.addBinder(newBinder);
						}, (bidirectionalBinder) -> bidirectionalBinder.setChain(chain));
					} else {
					}
				});
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

		if (configURI != null)
			sourceChangesListener.sourceChange(configURI, sourceChanges);
	}

	private void proposeFilters() throws YamlException {
		modelManagement.accept(new BinderVisitor() {
			public void visitChainedBinder(Binder binder) {
				if (binder instanceof DatabaseEntitiesExtractor) {
					String message = binder.getParametersProposalMessage();
					if (message.isEmpty())
						message = "Add filters for: " + binder.getClass().getSimpleName();

					List<String> lastFilters = binder.getFilters();
					addChangeProposalToBinder(binder, message, (bidirectionalBinder) -> {
						bindModel(false, modelManagement, (b, v) -> {
							if (b == binder) {
								b.setFilters((List<String>) v.stream().map(o -> o.toString()).collect(Collectors.toList()));
							}
						}, false);
					}, b -> binder.setFilters(lastFilters));
				}
			}
		});
	}

	private void proposeConfigForWorkspaceAwareBinder() throws YamlException {

		modelManagement.accept(new BinderVisitor() {
			public void visitChainedBinder(Binder binder) {
				if (binder == modelManagement) {
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
