package com.fpetrola.cap.config;

import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.DefaultBinder;

public class ModelManagement<S, T> extends DefaultBinder<S, T, Object> implements Binder<S, T, Object>{
	public String model;
	public ModelManagement() {
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
}
