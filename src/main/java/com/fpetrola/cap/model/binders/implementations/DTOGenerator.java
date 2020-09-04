package com.fpetrola.cap.model.binders.implementations;

import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.SourceCodeChanger;
import com.fpetrola.cap.model.binders.implementations.helpers.BaseJavaClassBinder;
import com.fpetrola.cap.model.binders.implementations.java.JavaClassModel;
import com.fpetrola.cap.model.binders.implementations.java.JavaField;
import com.fpetrola.cap.model.binders.sync.CollectionsSync;
import com.fpetrola.cap.model.binders.sync.ParametrizedModelHandler;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.Property;

public class DTOGenerator extends BaseJavaClassBinder<EntityModel, Void> implements Binder<EntityModel, Void> {

	protected String getClassname(EntityModel source) {
		return "com.fpetrola.cap.usermodel." + source.name.toUpperCase().charAt(0) + source.name.substring(1) + "DTO";
	}

	public void computeChanges(EntityModel ormEntityMapping, JavaClassModel javaClassModel, SourceCodeChanger sourceCodeChanger) {

		var modelHandler = new ParametrizedModelHandler<JavaClassModel, EntityModel, JavaField, Property>(JavaClassModel::getFields, EntityModel::getProperties, //
				(javaField, property) -> property.name.equals(javaField.getName()), //
				(classModel, property) -> classModel.createField(property.name, property.typeName.contains("INT") ? Integer.class : String.class));

		new CollectionsSync(sourceCodeChanger).matchElementMapping(javaClassModel, ormEntityMapping, modelHandler);
	}
}
