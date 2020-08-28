package com.fpetrola.cap.config;

import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;

public interface TraverseListener {

	void valuesPulledFrom(BidirectionalBinder bidirectionalBinder, List result);

}
