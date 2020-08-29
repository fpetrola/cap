package com.fpetrola.cap.model.binders.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class JPAEntityMappingWriter {

	private final JavaClassBinder javaClassBinder;
	private ORMEntityMapping ormEntityMapping;
	private SourceChangesListener sourceChangesListener;
	private JavaSourceChangesHandler javaSourceChangesHandler;

	public JPAEntityMappingWriter(ORMEntityMapping ormEntityMapping, String workspacePath, SourceChangesListener sourceChangesListener) {
		javaSourceChangesHandler = new JavaSourceChangesHandler(workspacePath, ormEntityMapping.mappedClass);
		this.sourceChangesListener = sourceChangesListener;
		this.ormEntityMapping = ormEntityMapping;
		javaClassBinder = new JavaClassBinder(javaSourceChangesHandler.getUri());
	}

	public void write() {
		try {
			String uri = javaSourceChangesHandler.getUri();

			if (javaSourceChangesHandler.fileExists()) {
				List<SourceChange> sourceChanges = new ArrayList<>();

				NormalAnnotationExpr classNormalAnnotationExpr = new NormalAnnotationExpr(new Name("javax.persistence.Entity"), new NodeList<>());
				classNormalAnnotationExpr.addPair("name", new StringLiteralExpr(ormEntityMapping.tableName));
				javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> javaClassBinder.addAnnotationToClass(cu1, classNormalAnnotationExpr, "JPA Entity detected", "")[0]);

				for (PropertyMapping propertyMapping : ormEntityMapping.propertyMappings) {
					javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> javaClassBinder.addAnnotationToField(cu1, javaClassBinder.createAnnotation("javax.persistence.JoinColumn", new MemberValuePair("name", new StringLiteralExpr(propertyMapping.columnName))), propertyMapping.propertyName));
					javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> javaClassBinder.addAnnotationToField(cu1, javaClassBinder.createAnnotation("javax.persistence.ManyToOne"), propertyMapping.propertyName));
					javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> javaClassBinder.addFieldIfNotExists(cu1, "add property to match database column: " + propertyMapping.columnName, propertyMapping.propertyName, propertyMapping.typeName));
				}

				addFixAllForNow(uri, sourceChanges);
			} else {
				sourceChangesListener.fileCreation(uri, createNewJavaClassContent());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String createNewJavaClassContent() {
		String className = ormEntityMapping.mappedClass;
		String content = "package " + className.substring(0, className.lastIndexOf(".")) + ";\n\nimport java.util.List;\n" + "\n\n" + "public class " + ormEntityMapping.entityModel.name + " {\n\n}";
		return content;
	}

	private void addFixAllForNow(String uri, List<SourceChange> sourceChanges) {
		Range range = new Range(new Position(1, 1), new Position(1, 1));
		SourceChange fixAllSourceChange = new SourceChange(uri, range, "fix all");
		for (SourceChange sourceChange : sourceChanges) {
			fixAllSourceChange.insertions.addAll(sourceChange.insertions);
		}

		if (!fixAllSourceChange.insertions.isEmpty())
			sourceChanges.add(fixAllSourceChange);

		sourceChangesListener.sourceChange(uri, sourceChanges);
	}
}
