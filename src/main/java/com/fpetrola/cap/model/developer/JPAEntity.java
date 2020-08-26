package com.fpetrola.cap.model.developer;

public class JPAEntity implements DeveloperModel {

	private ORMEntityMapping source;

	@Override
	public String toString() {
		return "JPAEntity [" + source.mappedClass + "]";
	}

	public JPAEntity(ORMEntityMapping source) {
		this.source = source;
	}

}
