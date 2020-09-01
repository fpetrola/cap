package com.fpetrola.cap.model.binders;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.source.SourceChangesListener;

public interface Binder<S, T> extends WorkspaceAwareBinder {

	void setSourceChangesListener(SourceChangesListener sourceChangesListener);

	void setFilters(List<String> ids);

	List<String> getFilters();

	void setChain(List<Binder> binders);

	List<Binder> getChain();

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

	default Type[] getTypes() {
		Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments();

		if (getParent() != null && actualTypeArguments[1].equals(Object.class))
			return getParent().getTypes();
		else
			return actualTypeArguments;
	}

	boolean allowsRootBinder();

	boolean canReceiveFrom(Binder binder);

	boolean isRootBinder();

	Type getOutputType();

	List<T> solve(S s);

	void setTraverserListener(TraverseListener traverseListener);

	SourceChangesListener getSourceChangesListener();

	TraverseListener getTraverseListener();
}
