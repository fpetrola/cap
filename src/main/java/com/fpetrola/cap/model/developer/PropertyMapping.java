package com.fpetrola.cap.model.developer;

public class PropertyMapping {
	public String propertyName;
	public String columnName;
	public PropertyMappingType propertyMappingType;
	public String typeName;
	
	public PropertyMapping(String propertyName, String columnName, String typeName, PropertyMappingType propertyMappingType) {
		super();
		this.propertyName = propertyName;
		this.columnName = columnName;
		this.typeName = typeName;
		this.propertyMappingType = propertyMappingType;
	}

}
