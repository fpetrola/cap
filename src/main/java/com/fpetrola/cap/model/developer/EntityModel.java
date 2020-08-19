package com.fpetrola.cap.model.developer;

import java.util.List;

public class EntityModel implements DeveloperModel {

	public String name;
	public List<Property> properties;

	public EntityModel(String name, List<Property> properties) {
		this.name = name;
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "Entity[name=" + name + "]";
	}

}
