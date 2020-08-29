package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaClassBinder.addAnnotationToClass;
import static com.fpetrola.cap.model.source.JavaClassBinder.addAnnotationToField;
import static com.fpetrola.cap.model.source.JavaClassBinder.addFieldIfNotExists;
import static com.fpetrola.cap.model.source.JavaClassBinder.createAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class JPAEntityMappingWriter extends DefaultJavaClassBinder<ORMEntityMapping, Object> implements BidirectionalBinder<ORMEntityMapping, Object>, WorkspaceAwareBinder {

	protected List<Function<CompilationUnit, SourceChange>> getModifiers(ORMEntityMapping source, String uri) {
		List<Function<CompilationUnit, SourceChange>> modifiers = new ArrayList<>();
		
		modifiers.add(cu1 -> addAnnotationToClass(cu1, createAnnotation("javax.persistence.Entity", new MemberValuePair("name", new StringLiteralExpr(source.tableName))), "JPA Entity detected", "", uri)[0]);

		for (PropertyMapping propertyMapping : source.propertyMappings) {
			modifiers.add(cu1 -> addAnnotationToField(cu1, createAnnotation("javax.persistence.JoinColumn", new MemberValuePair("name", new StringLiteralExpr(propertyMapping.columnName))), propertyMapping.propertyName, uri));
			modifiers.add(cu1 -> addAnnotationToField(cu1, createAnnotation("javax.persistence.ManyToOne"), propertyMapping.propertyName, uri));
			modifiers.add(cu1 -> addFieldIfNotExists(cu1, "add property to match database column: " + propertyMapping.columnName, propertyMapping.propertyName, propertyMapping.typeName, uri));
		}
		return modifiers;
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass;
	}
}
