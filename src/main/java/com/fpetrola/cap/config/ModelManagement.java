package com.fpetrola.cap.config;

import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.Binder;

public class ModelManagement {

	public String model;
	public List<String> ids = new ArrayList<>();
	public List<BidirectionalBinder> binderChain = new ArrayList<>();

	public ModelManagement() {
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public List<BidirectionalBinder> getBinderChain() {
		return binderChain;
	}

	public void setBinderChain(List<BidirectionalBinder> binders) {
		this.binderChain = binders;
	}
}
