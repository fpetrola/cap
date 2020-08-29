package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.source.SourceChangesListener;

public class DefaultBinder implements Binder {
	protected SourceChangesListener sourceChangesListener;
	protected List<String> filters = new ArrayList<>();
	public String workspacePath;

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

}