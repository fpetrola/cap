package com.fpetrola.cap.model;

import java.util.ArrayList;
import java.util.List;

public class ORMMappingExtractor implements BidirectionalBinder<ORMEntityMapping, EntityModel> {

	public List<ORMEntityMapping> pull(EntityModel source) {
		try {
			ArrayList<ORMEntityMapping> arrayList = new ArrayList<ORMEntityMapping>();
			ArrayList<PropertyMapping> propertyMappings = new ArrayList<PropertyMapping>();
			for (Property property : source.properties) {
				propertyMappings.add(
						new PropertyMapping(property.columnName, property.columnName, PropertyMappingType.ManyToOne));
			}
			Class<?> mappedClass;
			mappedClass = Class.forName("com.fpetrola.cap.model." + source.name);

			arrayList.add(new ORMEntityMapping(mappedClass, source.name, propertyMappings));
			return arrayList;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "ORMMappingExtractor";
	}

}
