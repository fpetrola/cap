package com.fpetrola.cap.model;

public class JPAEntity implements DeveloperModel {

	private ORMEntityMapping source;

	@Override
	public String toString() {
		return "JPAEntity [" + source.mappedClass.getSimpleName() + "]";
	}

	public JPAEntity(ORMEntityMapping source) {
		this.source = source;
	}

}
