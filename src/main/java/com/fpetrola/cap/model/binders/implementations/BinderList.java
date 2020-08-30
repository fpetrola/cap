package com.fpetrola.cap.model.binders.implementations;

import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;

public class BinderList extends DefaultBinder<Object, Object, Object> implements BidirectionalBinder<Object, Object> {

	public String getParametersProposalMessage() {
		return "binders list";
	}

	@Override
	public List<Object> pull(Object source) {
		return Arrays.asList(source);
	}
}