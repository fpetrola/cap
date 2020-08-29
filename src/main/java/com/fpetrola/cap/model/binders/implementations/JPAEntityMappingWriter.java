package com.fpetrola.cap.model.binders.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.JPAEntity;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.JavaClassBinder;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class JPAEntityMappingWriter extends DefaultBinder implements BidirectionalBinder<ORMEntityMapping, Object>, WorkspaceAwareBinder {

	public void write(ORMEntityMapping ormEntityMapping) {
		try {
			JavaSourceChangesHandler javaSourceChangesHandler = new JavaSourceChangesHandler(workspacePath, ormEntityMapping.mappedClass);
			String uri = javaSourceChangesHandler.getUri();
			JavaClassBinder javaClassBinder = new JavaClassBinder(uri);

			if (javaSourceChangesHandler.fileExists()) {
				List<SourceChange> sourceChanges = new ArrayList<>();

				NormalAnnotationExpr classNormalAnnotationExpr = new NormalAnnotationExpr(new Name("javax.persistence.Entity"), new NodeList<>());
				classNormalAnnotationExpr.addPair("name", new StringLiteralExpr(ormEntityMapping.tableName));
				javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> javaClassBinder.addAnnotationToClass(cu1, classNormalAnnotationExpr, "JPA Entity detected", "")[0]);

				for (PropertyMapping propertyMapping : ormEntityMapping.propertyMappings) {
					javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> {
						NormalAnnotationExpr createAnnotation = javaClassBinder.createAnnotation("javax.persistence.JoinColumn", new MemberValuePair("name", new StringLiteralExpr(propertyMapping.columnName)));
						return javaClassBinder.addAnnotationToField(cu1, createAnnotation, propertyMapping.propertyName);
					});
					javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> {
						NormalAnnotationExpr createAnnotation = javaClassBinder.createAnnotation("javax.persistence.ManyToOne");
						return javaClassBinder.addAnnotationToField(cu1, createAnnotation, propertyMapping.propertyName);
					});
					javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> javaClassBinder.addFieldIfNotExists(cu1, "add property to match database column: " + propertyMapping.columnName, propertyMapping.propertyName, propertyMapping.typeName));
				}

				javaSourceChangesHandler.addFixAllForNow(uri, sourceChanges);
				sourceChangesListener.sourceChange(uri, sourceChanges);
			} else {
				sourceChangesListener.fileCreation(uri, javaClassBinder.createNewJavaClassContent(ormEntityMapping.mappedClass));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Object> pull(ORMEntityMapping source) {
		List<Object> result = new ArrayList<Object>();

		try {
			write(source);
			result.add(new JPAEntity(source));
		} catch (Exception e) {
		}
		return result;
	}

	public String toString() {
		return "JPABinder []";
	}
}
