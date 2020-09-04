package com.fpetrola.cap.model.binders.sync;

public interface ModelUpdater<M1, M2> {
	void update(M1 model1, M2 model2);
}
