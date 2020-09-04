package com.fpetrola.cap.model.binders.sync;

public interface RightUpdater<M1, M2> {
	void updateFromRight(M1 model1, M2 model2);
}
