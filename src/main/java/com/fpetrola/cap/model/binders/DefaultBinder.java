package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.List;

public class DefaultBinder implements Binder {
	transient SourceChangesListener sourceChangesListener;
	private List<String> filters = new ArrayList<>();

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

}