package com.fpetrola.cap.model.binders;

import java.util.List;

import com.fpetrola.cap.model.source.SourceChangesListener;

public interface Binder {

	void setSourceChangesListener(SourceChangesListener sourceChangesListener);

	void setFilters(List<String> ids);

	List<String> getFilters();

}
