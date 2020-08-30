package com.fpetrola.cap.config;

import com.fpetrola.cap.model.binders.Binder;

public interface BinderModMaker {
    void doMod(Binder<?, ?> bidirectionalBinder);
}
