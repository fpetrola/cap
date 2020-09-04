package com.fpetrola.cap.helpers;

public interface Provider<T> {

	T get();

	T getNew();

}