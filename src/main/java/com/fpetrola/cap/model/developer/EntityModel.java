package com.fpetrola.cap.model.developer;

import java.util.List;

public class EntityModel implements DeveloperModel {

	public String name;
	public List<Property> properties;
	private EntityModelListener entityModelListener;

	public EntityModel(String name, List<Property> properties) {
		this.name = name;
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "Entity[" + name + "]";
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void createProperty(Property property, PropertyMapping propertyMapping) {
		properties.add(property);
		entityModelListener.propertyCreated(this, property, propertyMapping);
	}

	protected EntityModelListener getEntityModelListener() {
		return entityModelListener;
	}

	public void setEntityModelListener(EntityModelListener entityModelListener) {
		this.entityModelListener = entityModelListener;
	}

}
