package com.fpetrola.cap.model.binders;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.binders.processor.BaseBindingProcessor;
import com.fpetrola.cap.model.binders.sync.ChangesLinker;
import com.fpetrola.cap.model.binders.sync.DefaultChangesLinker;
import com.fpetrola.cap.model.source.SourceChangesListener;

public class ModelBinder<S, T> extends DefaultBinder<Object, Object> implements Binder<Object, Object> {
	public String model;
	protected SourceChangesListener sourceChangesListener;
	private TraverseListener traverseListener;
	private ChangesLinker defaultChangesLinker= new DefaultChangesLinker();
	private String workspacePath;

	public ModelBinder() {
	}
	
	public String getWorkspacePath() {
		return workspacePath;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Type[] getTypes() {
		return new Type[] { Void.class, Void.class };
	}

	public boolean allowsRootBinder() {
		return getChain().isEmpty();
	}

	public boolean isRootBinder() {
		return false;
	}

	public List<Object> pull(Object source) {
		return Arrays.asList(BaseBindingProcessor.createVoid());
	}

	public TraverseListener getTraverseListener() {
		return traverseListener;
	}

	public void setTraverseListener(TraverseListener traverseListener) {
		this.traverseListener = traverseListener;
	}

	public SourceChangesListener getSourceChangesListener() {
		return sourceChangesListener;
	}

	public void setSourceChangesListener(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public ChangesLinker getChangesLinker() {
		return defaultChangesLinker;
	}

	public void setChangesLinker(ChangesLinker defaultChangesLinker) {
		this.defaultChangesLinker = defaultChangesLinker;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}
}
