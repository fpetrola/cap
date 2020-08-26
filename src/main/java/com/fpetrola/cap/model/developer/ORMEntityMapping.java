package com.fpetrola.cap.model.developer;

import java.util.Collection;

public class ORMEntityMapping {
	public EntityModel entityModel;
	public String mappedClass;
	public Collection<PropertyMapping> propertyMappings;
	public String tableName;

	public ORMEntityMapping(EntityModel entityModel, String mappedClass, String tableName, Collection<PropertyMapping> propertyMappings) {
		this.entityModel = entityModel;
		this.mappedClass = mappedClass;
		this.tableName = tableName;
		this.propertyMappings = propertyMappings;
	}

	@Override
	public String toString() {
		return "ORMEntityMapping [" + tableName + "]";
	}
}
