package com.fpetrola.cap.config;

import java.util.List;

import com.fpetrola.cap.model.binders.Binder;

public interface TraverseListener {

	void valuesPulledFrom(Binder<?, ?> bidirectionalBinder, List result);

}
