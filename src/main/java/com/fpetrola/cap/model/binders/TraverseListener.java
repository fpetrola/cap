package com.fpetrola.cap.model.binders;

import java.util.List;

public interface TraverseListener {

	void valuesPulledFrom(Binder<?, ?> bidirectionalBinder, List result);

}
