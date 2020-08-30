package com.fpetrola.cap.config;

import java.util.List;

import com.fpetrola.cap.model.binders.Binder;

public interface BinderModeUnmaker {
	void undoMod(Binder<?, ?> bidirectionalBinder, List<Binder<?, ?>> chain);
}
