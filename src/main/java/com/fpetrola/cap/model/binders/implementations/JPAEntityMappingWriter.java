package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaparserHelper.createAnnotation;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
import com.fpetrola.cap.model.binders.implementations.helpers.DefaultJavaClassBinder;
import com.fpetrola.cap.model.developer.ORMEntityMapping;

public class JPAEntityMappingWriter extends DefaultJavaClassBinder<ORMEntityMapping, Void> implements BidirectionalBinder<ORMEntityMapping, Void>, WorkspaceAwareBinder {

	public void computeChanges(ORMEntityMapping ormEntityMapping, JavaClassModel javaClassModel) {
		javaClassModel.addAnnotationIfNotExists(createAnnotation("javax.persistence.Entity"));
		javaClassModel.addAnnotationIfNotExists(createAnnotation("javax.persistence.Table", createPair("name", ormEntityMapping.tableName)));

		var modelHandler = new ParametrizedModelHandler<>( //
				(javaField, propertyMapping) -> propertyMapping.propertyName.equals(javaField.getName()), //
				(javaField, propertyMapping) -> javaField.addAnnotationIfNotExists(createAnnotation("javax.persistence.Column", createPair("name", propertyMapping.columnName))), //
				JavaClassModel::getFields, //
				ORMEntityMapping::getPropertyMappings, //
				propertyMapping -> javaClassModel.createField(propertyMapping.propertyName, propertyMapping.typeName.contains("INT") ? Integer.class : String.class) //
		);

		new CollectionsSync().matchElementMapping(javaClassModel, ormEntityMapping, modelHandler);
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass;
	}
}
