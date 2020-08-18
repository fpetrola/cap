package com.fpetrola.cap.model;

import java.util.Collection;

public class ORMEntityMapping {
	@Override
	public String toString() {
		return "ORMEntityMapping [" + tableName + "]";
	}

	public ORMEntityMapping(Class<?> mappedClass, String tableName, Collection<PropertyMapping> propertyMappings) {
		this.mappedClass = mappedClass;
		this.tableName = tableName;
		this.propertyMappings = propertyMappings;
	}

	public Class<?> mappedClass;
	public String tableName;
	
	public Collection<PropertyMapping> propertyMappings;

}
