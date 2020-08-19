package com.fpetrola.cap.model.developer;

public class PropertyMapping {
	public String propertyName;
	public String columnName;
	public PropertyMappingType propertyMappingType;
	
	public PropertyMapping(String propertyName, String columnName, PropertyMappingType propertyMappingType) {
		super();
		this.propertyName = propertyName;
		this.columnName = columnName;
		this.propertyMappingType = propertyMappingType;
	}

}
