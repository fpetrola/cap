package com.fpetrola.cap.model.binders.implementations;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
import com.fpetrola.cap.model.binders.implementations.helpers.DefaultJavaClassBinder;
import com.fpetrola.cap.model.developer.EntityModel;

public class DTOGenerator extends DefaultJavaClassBinder<EntityModel, Void> implements BidirectionalBinder<EntityModel, Void>, WorkspaceAwareBinder {

	protected String getClassname(EntityModel source) {
		return "com.fpetrola.cap.usermodel." + source.name.toUpperCase().charAt(0) + source.name.substring(1) + "DTO";
	}

	public void computeChanges(EntityModel ormEntityMapping, JavaClassModel javaClassModel) {

		var modelHandler = new ParametrizedModelHandler<>( //
				(javaField, property) -> property.name.equals(javaField.getName()), //
				JavaClassModel::getFields, //
				EntityModel::getProperties, //
				(property) -> javaClassModel.createField(property.name, property.typeName.contains("INT") ? Integer.class : String.class));

		new CollectionsSync().matchElementMapping(javaClassModel, ormEntityMapping, modelHandler);
	}
}
