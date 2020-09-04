package com.fpetrola.cap.model.binders.sync;

import java.util.Collection;

public interface CollectionGetter<M, P> {
	Collection<P> getCollection(M model);
}
