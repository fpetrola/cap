package com.fpetrola.cap.model.binders.sync;

import java.util.Collection;

public interface ModelHandler<M1, M2, P1, P2> {

	Collection<P2> getRightCollection(M2 model);
	Collection<P1> getLeftCollection(M1 model);
	ModelMatcher<P1, P2> getModelMatcher();
	ModelUpdater<P1, P2> getLeftUpdater();
	ModelUpdater<P2, P1> getRightUpdater();
	ElementFactory<M1, M2, P1, P2> getLeftElementFactory();
	ElementFactory<M2, M1, P2, P1> getRightElementFactory();
}
