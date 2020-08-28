package com.fpetrola.cap.model.binders.implementations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fpetrola.cap.model.binders.BindWriter;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class JPAEntityMappingWriter {

	private final BindWriter bindWriter = new BindWriter();
	private ORMEntityMapping ormEntityMapping;

	public JPAEntityMappingWriter(ORMEntityMapping ormEntityMapping, String workspacePath, SourceChangesListener sourceChangesListener) {
		this.ormEntityMapping = ormEntityMapping;
		bindWriter.workspacePath = workspacePath;
		bindWriter.sourceChangesListener = sourceChangesListener;
	}

	public void write() {
		try {
			File file = bindWriter.initWithClassName(ormEntityMapping.mappedClass);
			if (file.exists()) {
				bindWriter.javaParser = bindWriter.createJavaParser();

				List<SourceChange> sourceChanges = new ArrayList<>();

				bindWriter.addInsertionsFor(sourceChanges, cu1 -> addClassAnnotations(cu1), file);

				for (PropertyMapping propertyMapping : ormEntityMapping.propertyMappings) {
					bindWriter.addInsertionsFor(sourceChanges, cu1 -> addPropertiesAnnotations(cu1, propertyMapping), file);
				}

				Range range = new Range(new Position(1, 1), new Position(1, 1));
				SourceChange fixAllSourceChange = new SourceChange(bindWriter.uri, range, "fix all");
				for (SourceChange sourceChange : sourceChanges) {
					fixAllSourceChange.insertions.addAll(sourceChange.insertions);
				}

				if (!fixAllSourceChange.insertions.isEmpty())
					sourceChanges.add(fixAllSourceChange);

				bindWriter.sourceChangesListener.newSourceChanges(bindWriter.uri, sourceChanges);
			} else {
				String className = ormEntityMapping.mappedClass;
				String content = "package " + className.substring(0, className.lastIndexOf(".")) + ";\n\nimport java.util.List;\n" + "\n\n" + "public class " + ormEntityMapping.entityModel.name + " {\n\n}";
				bindWriter.sourceChangesListener.fileCreation(bindWriter.uri, content);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SourceChange addClassAnnotations(CompilationUnit cu) {
		SourceChange[] sourceChange = new SourceChange[1];
		cu.accept(new ModifierVisitor<Void>() {

			@Override
			public Visitable visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
				if (!classDeclaration.isAnnotationPresent(Entity.class)) {
					classDeclaration.addAnnotation(Entity.class);

					List<SimpleName> simpleNames = classDeclaration.findAll(SimpleName.class);

					sourceChange[0] = new SourceChange(bindWriter.uri, simpleNames.get(0).getRange().get(), "JPA Entity detected");
				}

				if (ormEntityMapping.tableName != null) {
					NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) classDeclaration.getAnnotationByClass(Table.class).orElseGet(() -> null);
					if (annotationExpr == null) {
						annotationExpr = classDeclaration.addAndGetAnnotation(Table.class);
						annotationExpr.addPair("name", new StringLiteralExpr(ormEntityMapping.tableName));
					}
				}

				return super.visit(classDeclaration, arg);
			}
		}, null);

		return sourceChange[0];
	}

	private SourceChange addPropertiesAnnotations(CompilationUnit cu, PropertyMapping propertyMapping) {
		SourceChange[] sourceChange = new SourceChange[1];
		boolean[] found = new boolean[1];

		cu.accept(new ModifierVisitor<Void>() {

			@Override
			public Visitable visit(FieldDeclaration fieldDeclaration, Void arg) {

				SimpleName name = fieldDeclaration.getVariable(0).getName();

				if (name.toString().equals(propertyMapping.propertyName)) {
					found[0] = true;

					if (propertyMapping.propertyMappingType != null) {
						if (!fieldDeclaration.isAnnotationPresent(ManyToOne.class)) {
							fieldDeclaration.addAnnotation(ManyToOne.class);
							sourceChange[0] = new SourceChange(bindWriter.uri, fieldDeclaration.getRange().get(), "column mapping detected for property: " + propertyMapping.columnName);
						}
					}

					if (propertyMapping.columnName != null) {
						if (!fieldDeclaration.isAnnotationPresent(JoinColumn.class)) {
							NormalAnnotationExpr annotation = fieldDeclaration.addAndGetAnnotation(JoinColumn.class);
							annotation.addPair("name", new StringLiteralExpr(propertyMapping.columnName));
						}
					}
				}
				return super.visit(fieldDeclaration, arg);
			}
		}, null);

		if (!found[0]) {
			cu.findAll(ClassOrInterfaceDeclaration.class).forEach(coid -> {
				VariableDeclarator variables = new VariableDeclarator();
				variables.setName(propertyMapping.propertyName);
				variables.setType(propertyMapping.typeName.contains("INT") ? Integer.class : String.class);
				FieldDeclaration fieldDeclaration = new FieldDeclaration().addVariable(variables);
				coid.getMembers().add(0, fieldDeclaration);
				Optional<String> fullyQualifiedName = coid.getFullyQualifiedName();
				Optional<SimpleName> simpleNames = coid.findAll(SimpleName.class).stream().filter(sn -> sn.getParentNode().get() instanceof ClassOrInterfaceDeclaration).findFirst();

				simpleNames.ifPresent(p -> {
					sourceChange[0] = new SourceChange(bindWriter.uri, p.getRange().get(), "add property to match database column: " + propertyMapping.columnName);
				});

			});
		}
		return sourceChange[0];
	}

}
