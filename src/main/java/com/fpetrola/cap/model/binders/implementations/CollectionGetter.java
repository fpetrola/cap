package com.fpetrola.cap.model.binders.implementations;

import java.util.Collection;

public interface CollectionGetter<M, P> {
	Collection<P> getCollection(M model);
}
