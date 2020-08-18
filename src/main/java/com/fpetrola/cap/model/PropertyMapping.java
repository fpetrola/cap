package com.fpetrola.cap.model;

public class PropertyMapping {
	protected String propertyName;
	protected String columnName;
	protected PropertyMappingType propertyMappingType;
	
	public PropertyMapping(String propertyName, String columnName, PropertyMappingType propertyMappingType) {
		super();
		this.propertyName = propertyName;
		this.columnName = columnName;
		this.propertyMappingType = propertyMappingType;
	}

}
