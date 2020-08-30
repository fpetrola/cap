package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.source.SourceChangesListener;

public class DefaultBinder<S, T, T2> implements Binder<S, T, T2> {
	protected SourceChangesListener sourceChangesListener;
	protected List<String> filters = new ArrayList<>();
	public String workspacePath;
	public List<BidirectionalBinder<T, T2>> chain = new ArrayList<>();

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

	public void setChain(List<BidirectionalBinder<T, T2>> binders) {
		this.chain = binders;
	}

	public List<BidirectionalBinder<T, T2>> getChain() {
		return chain;
	}
}