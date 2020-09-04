package com.fpetrola.cap.model.binders;

import java.util.List;

public interface TraverseListener {

	void valuesPulledFrom(Binder<?, ?> Binder, List result);

}
