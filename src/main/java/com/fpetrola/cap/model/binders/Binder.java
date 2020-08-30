package com.fpetrola.cap.model.binders;

import java.util.List;

import com.fpetrola.cap.model.source.SourceChangesListener;

public interface Binder<S, T, T2> {

	void setSourceChangesListener(SourceChangesListener sourceChangesListener);

	void setFilters(List<String> ids);

	List<String> getFilters();

	void setChain(List<BidirectionalBinder<T, T2>> binders);

	List<BidirectionalBinder<T, T2>> getChain();

}
