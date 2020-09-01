package com.fpetrola.cap.model.binders.implementations;

public interface ElementFactory<T, T2> {
	T create(T2 rightElement);
}
