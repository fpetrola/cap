package com.fpetrola.cap.model.developer;

public class Property {

	public String name;
	public String typeName;

	public Property(String columnName, String typeName) {
		this.name = columnName;
		this.typeName = typeName;
	}

}
