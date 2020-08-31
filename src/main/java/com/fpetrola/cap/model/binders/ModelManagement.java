package com.fpetrola.cap.model.binders;

public class ModelManagement<S, T> extends DefaultBinder<S, T> implements Binder<S, T>{
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
