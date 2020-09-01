package com.fpetrola.cap.model.binders.implementations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.developer.PropertyMappingType;

public class BasicORMMappingGenerator extends DefaultBinder<EntityModel, ORMEntityMapping> implements BidirectionalBinder<EntityModel, ORMEntityMapping> {

	public List<ORMEntityMapping> pull(EntityModel source) {

		List<PropertyMapping> propertyMappings = source.getProperties().stream()
			.map(property -> new PropertyMapping(property.name, property.name, property.typeName, PropertyMappingType.ManyToOne))
			.collect(Collectors.toList());

		return Arrays.asList(new ORMEntityMapping(source, "com.fpetrola.cap.usermodel" + "." + source.name, source.name, propertyMappings));
	}
}
