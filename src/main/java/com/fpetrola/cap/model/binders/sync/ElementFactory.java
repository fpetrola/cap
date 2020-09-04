package com.fpetrola.cap.model.binders.sync;

public interface ElementFactory<M1, M2, T1, T2> {
	T1 create(M1 leftCollection,  T2 rightElement);
}
