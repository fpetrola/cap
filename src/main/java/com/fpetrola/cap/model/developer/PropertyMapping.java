package com.fpetrola.cap.model.developer;

import java.util.Objects;

public class PropertyMapping {
	public String propertyName;
	public String columnName;
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public PropertyMappingType propertyMappingType;
	public String typeName;
	
	public PropertyMapping(String propertyName, String columnName, String typeName, PropertyMappingType propertyMappingType) {
		super();
		this.propertyName = propertyName;
		this.columnName = columnName;
		this.typeName = typeName;
		this.propertyMappingType = propertyMappingType;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getColumnName() {
		return columnName;
	}

	public PropertyMappingType getPropertyMappingType() {
		return propertyMappingType;
	}

	public String getTypeName() {
		return typeName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(columnName, propertyMappingType, propertyName, typeName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyMapping other = (PropertyMapping) obj;
		return Objects.equals(columnName, other.columnName) && propertyMappingType == other.propertyMappingType && Objects.equals(propertyName, other.propertyName) && Objects.equals(typeName, other.typeName);
	}

}
