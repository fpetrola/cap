package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaClassBinder.addAnnotationToClass;
import static com.fpetrola.cap.model.source.JavaClassBinder.addAnnotationToField;
import static com.fpetrola.cap.model.source.JavaClassBinder.addFieldIfNotExists;
import static com.fpetrola.cap.model.source.JavaClassBinder.createAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
import com.fpetrola.cap.model.binders.implementations.helpers.DefaultJavaClassBinder;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.ast.CompilationUnit;

public class JPAEntityMappingWriter extends DefaultJavaClassBinder<ORMEntityMapping, Void> implements BidirectionalBinder<ORMEntityMapping, Void>, WorkspaceAwareBinder {

	protected List<Function<CompilationUnit, SourceChange>> getModifiers(ORMEntityMapping source, String uri) {
		
		
		List<Function<CompilationUnit, SourceChange>> modifiers;
		modifiers = new ArrayList<>();
		modifiers.add(c -> addAnnotationToClass(c, createAnnotation("javax.persistence.Entity"), "JPA Entity detected", "", uri)[0]);
		modifiers.add(c -> addAnnotationToClass(c, createAnnotation("javax.persistence.Table", createPair("name", source.tableName)), "JPA Entity table detected", "", uri)[0]);

		for (PropertyMapping p : source.propertyMappings) {
			modifiers.add(c -> addAnnotationToField(c, createAnnotation("javax.persistence.JoinColumn", createPair("name", p.columnName)), p.propertyName, uri));
			modifiers.add(c -> addAnnotationToField(c, createAnnotation("javax.persistence.ManyToOne"), p.propertyName, uri));
			modifiers.add(c -> addFieldIfNotExists(c, "add property to match database column: " + p.columnName, p.propertyName, p.typeName, uri));
		}
		return modifiers;
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass;
	}
}
