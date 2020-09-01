package com.fpetrola.cap.model.binders.implementations;

import java.util.Collection;

public interface ModelHandler<M1, M2, P1, P2> {

	Collection<P2> getRightCollection(M2 model);
	Collection<P1> getLeftCollection(M1 model);
	ModelMatcher<P1, P2> getModelMatcher();
	ModelSync<P1, P2> getModelSync();
	ElementFactory<P1, P2> getLeftElementFactory();
}
