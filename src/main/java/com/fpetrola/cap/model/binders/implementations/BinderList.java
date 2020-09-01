package com.fpetrola.cap.model.binders.implementations;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.DefaultBinder;

public class BinderList extends DefaultBinder<Object, Object> implements BidirectionalBinder<Object, Object> {

	public String getParametersProposalMessage() {
		return "binders list";
	}

	public List<Object> pull(Object source) {
		return new ArrayList<Object>(Arrays.asList(source));
	}

	public Type[] getTypes() {
		if (getParent() == null)
			return new Type[] { Object.class, Object.class };
		else
			return getParent().getTypes();
	}

	public boolean allowsRootBinder() {
		return getParent() != null ? getParent().allowsRootBinder() : false;
	}

	public boolean canReceiveFrom(Binder binder) {
		return getParent() != null ? getParent().canReceiveFrom(binder) : true;
	}
}