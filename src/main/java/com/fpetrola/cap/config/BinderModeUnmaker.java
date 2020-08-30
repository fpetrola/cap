package com.fpetrola.cap.config;

import com.fpetrola.cap.model.binders.Binder;

public interface BinderModeUnmaker {
	void undoMod(Binder<?, ?> bidirectionalBinder);
}
