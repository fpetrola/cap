package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.source.SourceChangesListener;

public interface Binder<S, T> {

	void setSourceChangesListener(SourceChangesListener sourceChangesListener);

	void setFilters(List<String> ids);

	List<String> getFilters();

	void setChain(List<Binder<?, ?>> binders);

	List<Binder<?, ?>> getChain();

	void accept(BinderVisitor<?, ?> visitor);

	void addBinder(Binder aBinder);

	void removeBinder(Binder availableBinder);

	void setParent(Binder<T, ?> aParentBinder);

	default String getParametersProposalMessage() {
		return "";
	}

	default List<T> pull(S source) {
		return new ArrayList<>();
	}

	Binder<T, ?> getParent();
}
