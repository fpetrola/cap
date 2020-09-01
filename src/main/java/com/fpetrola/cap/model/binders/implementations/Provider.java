package com.fpetrola.cap.model.binders.implementations;

public interface Provider<T> {

	T get();

	T getNew();

}