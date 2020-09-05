package com.fpetrola.cap.model.binders.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.esotericsoftware.yamlbeans.YamlException;
import com.fpetrola.cap.helpers.BindersDiscoveryService;
import com.fpetrola.cap.helpers.YamlHelper;
import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.BinderVisitor;
import com.fpetrola.cap.model.binders.ModelBinder;
import com.fpetrola.cap.model.binders.implementations.DatabaseEntitiesExtractor;
import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
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
		sourceChanges.clear();

		try {
			deserializeModel();
			proposeNewBinders();
			proposeFilters();
			proposeConfigForWorkspaceAwareBinder();

			deserializeModel();
		} catch (Exception e1) {
			proposeCreation();
		}
		bindModel(false, modelBinder, (b, v) -> {
		}, true);

		if (configURI != null)
			sourceChangesListener.sourceChange(configURI, sourceChanges);
	}

	private void deserializeModel() throws FileNotFoundException, YamlException {
		modelBinder = YamlHelper.deserializeModelFromURI(configURI, ModelBinder.class);
		setParents(modelBinder);
	}

	private void setParents(Binder<?, ?> binder) {
		for (Binder child : binder.getChain()) {
			child.setParent(binder);
			setParents(child);
		}
	}

	private void proposeNewBinders() {
		BindersDiscoveryService bindersDiscoveryService = new BindersDiscoveryService();

		modelBinder.accept(new BinderVisitor() {
			public void visitChainedBinder(Binder binder) {
				List<Binder> newBinders = bindersDiscoveryService.findBinders();

				newBinders.stream().forEach(newBinder -> {
					if (newBinder.canReceiveFrom(binder)) {
						List<Binder> chain = binder.getChain();
						addChangeProposalToBinder(binder, "Add binder: " + newBinder.getClass().getSimpleName(), (aBinder) -> {
							aBinder.setChain(new ArrayList<>(chain));
							aBinder.addBinder(newBinder);
						}, (Binder) -> Binder.setChain(chain));
					} else {
					}
				});
			}

		});

	}

	private void proposeCreation() {
		CodeProposal codeProposal = new CodeProposal(configURI, new Range(new Position(1, 1), new Position(1, 1)), "Initialize Model Management");
		modelBinder = new ModelBinder();
		List<SourceCodeModification> createInsertions = JavaSourceChangesHandler.createModifications("\n", YamlHelper.serializeModel(modelBinder));
		if (!createInsertions.isEmpty()) {
			codeProposal.getSourceChange().setInsertions(createInsertions);
			sourceChanges.add(codeProposal);
		}
	}

	private void proposeFilters() {
		modelBinder.accept(new BinderVisitor() {
			public void visitChainedBinder(Binder binder) {
				if (binder instanceof DatabaseEntitiesExtractor) {
					String message = binder.getParametersProposalMessage();
					if (message.isEmpty())
						message = "Add filters for: " + binder.getClass().getSimpleName();

					List<String> lastFilters = binder.getFilters();
					addChangeProposalToBinder(binder, message, (Binder) -> {
						bindModel(false, modelBinder, (b, v) -> {
							if (b == binder)
								b.setFilters((List<String>) v.stream().map(o -> o.toString()).collect(Collectors.toList()));
						}, false);
					}, b -> binder.setFilters(lastFilters));
				}
			}
		});
	}

	private void proposeConfigForWorkspaceAwareBinder() {

		modelBinder.accept(new BinderVisitor() {
			public void visitChainedBinder(Binder binder) {
				if (binder == modelBinder) {

					if (binder.findWorkspacePath() == null) {
						File[] file = new File[1];
						file[0] = new File(URI.create(configURI));

						while (file[0] != null) {
							if (file[0].isDirectory()) {
								String message = "use workspace in: " + file[0].getPath();
								String lastWorkspacePath = binder.getWorkspacePath();

								addChangeProposalToBinder(binder, message, (Binder) -> binder.setWorkspacePath(file[0].getPath()), (Binder) -> binder.setWorkspacePath(lastWorkspacePath));
							}
							file[0] = file[0].getParentFile();
						}
					}
				}
			}
		});

	}
}
