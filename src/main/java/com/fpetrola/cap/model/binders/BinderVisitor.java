package com.fpetrola.cap.model.binders;

public interface BinderVisitor<S, T> {

	void visitChainedBinder(Binder<?, ?> binder);

}
