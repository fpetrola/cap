package com.fpetrola.cap.model;

import java.util.List;

public interface Puller<T> extends Binder {

	List<T> pull();

}
