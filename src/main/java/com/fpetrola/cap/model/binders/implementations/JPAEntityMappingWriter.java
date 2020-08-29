package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaClassBinder.addAnnotationToClass;
import static com.fpetrola.cap.model.source.JavaClassBinder.addAnnotationToField;
import static com.fpetrola.cap.model.source.JavaClassBinder.addFieldIfNotExists;
import static com.fpetrola.cap.model.source.JavaClassBinder.createAnnotation;
import static com.fpetrola.cap.model.source.JavaClassBinder.createNewJavaClassContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class JPAEntityMappingWriter extends DefaultBinder implements BidirectionalBinder<ORMEntityMapping, Object>, WorkspaceAwareBinder {

	public List<Object> pull(ORMEntityMapping source) {
		if (workspacePath != null && sourceChangesListener != null) {

			JavaSourceChangesHandler javaSourceChangesHandler = new JavaSourceChangesHandler(workspacePath, source.mappedClass);
			String uri = javaSourceChangesHandler.getUri();

			List<Function<CompilationUnit, SourceChange>> modifiers = new ArrayList<>();
			if (javaSourceChangesHandler.fileExists()) {
				List<SourceChange> sourceChanges = new ArrayList<>();

				modifiers.add(cu1 -> addAnnotationToClass(cu1, createAnnotation("javax.persistence.Entity", new MemberValuePair("name", new StringLiteralExpr(source.tableName))), "JPA Entity detected", "", uri)[0]);

				for (PropertyMapping propertyMapping : source.propertyMappings) {
					modifiers.add(cu1 -> addAnnotationToField(cu1, createAnnotation("javax.persistence.JoinColumn", new MemberValuePair("name", new StringLiteralExpr(propertyMapping.columnName))), propertyMapping.propertyName, uri));
					modifiers.add(cu1 -> addAnnotationToField(cu1, createAnnotation("javax.persistence.ManyToOne"), propertyMapping.propertyName, uri));
					modifiers.add(cu1 -> addFieldIfNotExists(cu1, "add property to match database column: " + propertyMapping.columnName, propertyMapping.propertyName, propertyMapping.typeName, uri));
				}

				javaSourceChangesHandler.addInsertionsFor(sourceChanges, modifiers);
				javaSourceChangesHandler.addFixAllForNow(uri, sourceChanges);
				sourceChangesListener.sourceChange(uri, sourceChanges);
			} else {
				sourceChangesListener.fileCreation(uri, createNewJavaClassContent(source.mappedClass));
			}
		}
		return Arrays.asList();
	}
}
