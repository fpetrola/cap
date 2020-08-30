package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.source.SourceChangesListener;

public class DefaultBinder<S, T> implements Binder<S, T> {
	protected SourceChangesListener sourceChangesListener;
	protected List<String> filters = new ArrayList<>();
	public String workspacePath;
	public List<Binder<?, ?>> chain = new ArrayList<>();
	private Binder<T, ?> parentBinder;

	public DefaultBinder() {
	}

	public SourceChangesListener getSourceChangesListener() {
		return sourceChangesListener;
	}

	public void setSourceChangesListener(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> ids) {
		this.filters = ids;
	}

	public String getWorkspacePath() {
		return workspacePath;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}

	public void setChain(List<Binder<?, ?>> binders) {
		this.chain = binders;
	}

	public List<Binder<?, ?>> getChain() {
		return chain;
	}

	public void accept(BinderVisitor<?, ?> visitor) {
		visitor.visitChainedBinder(this);
		for (Binder<?, ?> binder : chain) {
			binder.accept(visitor);
		}
	}

	public void addBinder(Binder aBinder) {
		chain.add(aBinder);
		aBinder.setParent(this);
	}

	public void setParent(Binder<T, ?> aParentBinder) {
		this.parentBinder = aParentBinder;
	}

	@Override
	public void removeBinder(Binder aBinder) {
		chain.remove(aBinder);
		aBinder.setParent(null);
	}

	public Binder<T, ?> getParent() {
		return parentBinder;
	}
}