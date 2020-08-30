package com.fpetrola.cap.config;

import com.fpetrola.cap.model.binders.Binder;

import java.util.List;

public interface BinderModMaker {
    List<Binder<?, ?>> doMod(Binder<?, ?> bidirectionalBinder);
}
