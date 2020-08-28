package com.fpetrola.cap.model.binders;
 
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.Property;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.developer.PropertyMappingType;

public class UppercaseORMMappingGenerator extends DefaultBinder implements BidirectionalBinder<EntityModel, ORMEntityMapping> {
	public String modelPackage = "com.fpetrola.cap.usermodel";

	public UppercaseORMMappingGenerator() {
	}
	public List<ORMEntityMapping> pull(EntityModel source) {
		ArrayList<ORMEntityMapping> arrayList = new ArrayList<ORMEntityMapping>();
		ArrayList<PropertyMapping> propertyMappings = new ArrayList<PropertyMapping>();
		for (Property property : source.properties) {
			propertyMappings.add(new PropertyMapping(property.name, property.name, property.typeName, PropertyMappingType.ManyToOne));
		}
		source.name=source.name.toUpperCase();

		arrayList.add(new ORMEntityMapping(source, modelPackage + "." + source.name.toUpperCase(), source.name.toUpperCase(), propertyMappings));
		return arrayList;
	}

	@Override
	public String toString() {
		return "UppercaseORMMappingGenerator";
	}
}
