package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaparserHelper.createAnnotation;

import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.SourceCodeChanger;
import com.fpetrola.cap.model.binders.implementations.helpers.BaseJavaClassBinder;
import com.fpetrola.cap.model.binders.implementations.java.JavaClassModel;
import com.fpetrola.cap.model.binders.sync.CollectionsSync;
import com.fpetrola.cap.model.binders.sync.ParametrizedModelHandler;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.developer.PropertyMappingType;

public class JPAEntityMappingWriter extends BaseJavaClassBinder<ORMEntityMapping, Void> implements Binder<ORMEntityMapping, Void> {

	public void computeChanges(ORMEntityMapping ormEntityMapping, JavaClassModel javaClassModel, SourceCodeChanger sourceCodeChanger) {
		javaClassModel.addAnnotationIfNotExists(createAnnotation("javax.persistence.Entity"));
		javaClassModel.addAnnotationIfNotExists(createAnnotation("javax.persistence.Table", createPair("name", ormEntityMapping.tableName)));

		var modelHandler = new ParametrizedModelHandler<>(JavaClassModel::getFields, ORMEntityMapping::getPropertyMappings, //
				(field, propertyMapping) -> propertyMapping.getPropertyName().equals(field.getName()), //
				(field, propertyMapping) -> field.addAnnotationIfNotExists(createAnnotation("javax.persistence.Column", createPair("name", propertyMapping.getColumnName()))), //
				(propertyMapping, field) -> propertyMapping.setColumnName(field.getName()), //
				(classModel, propertyMapping) -> classModel.createField(propertyMapping.getPropertyName(), propertyMapping.getTypeName().contains("INT") ? Integer.class : String.class), //
				(entityMapping, field) -> entityMapping.createPropertyMapping(new PropertyMapping(field.getName(), field.getName(), "INT", PropertyMappingType.ManyToOne))); //

		new CollectionsSync(sourceCodeChanger).matchElementMapping(javaClassModel, ormEntityMapping, modelHandler);
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass;
	}
}
