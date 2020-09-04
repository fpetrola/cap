package com.fpetrola.cap.model.binders.sync;

public interface ModelMatcher<M1, M2> {
	boolean match(M1 model1, M2 model2);
}
