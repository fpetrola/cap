package com.fpetrola.cap.model.binders.implementations;

public interface ModelSync<M1, M2> {
	void updateFromRight(M1 model1, M2 model2);
}
