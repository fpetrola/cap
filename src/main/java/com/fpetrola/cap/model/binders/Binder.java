package com.fpetrola.cap.model.binders;

import java.util.List;

public interface Binder {

	void setSourceChangesListener(SourceChangesListener sourceChangesListener);

	void setFilters(List<String> ids);

	List<String> getFilters();

}
