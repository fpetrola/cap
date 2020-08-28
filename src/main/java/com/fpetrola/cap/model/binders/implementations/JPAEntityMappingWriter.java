package com.fpetrola.cap.model.binders.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class JPAEntityMappingWriter {

	private final JavaSourceChangesHandler javaSourceChangesHandler;
	private ORMEntityMapping ormEntityMapping;
	private SourceChangesListener sourceChangesListener;

	public JPAEntityMappingWriter(ORMEntityMapping ormEntityMapping, String workspacePath, SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
		javaSourceChangesHandler = new JavaSourceChangesHandler(workspacePath, ormEntityMapping.mappedClass);
		this.ormEntityMapping = ormEntityMapping;
	}

	public void write() {
		try {
			String uri = javaSourceChangesHandler.getUri();

			if (javaSourceChangesHandler.fileExists()) {
				List<SourceChange> sourceChanges = new ArrayList<>();

				javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> addClassAnnotations(cu1));

				for (PropertyMapping propertyMapping : ormEntityMapping.propertyMappings) {
					javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> addPropertiesAnnotations(cu1, propertyMapping));
				}

				addFixAllForNow(uri, sourceChanges);
			} else {
				sourceChangesListener.fileCreation(uri, createNewJavaClassContent());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private SourceChange addClassAnnotations(CompilationUnit cu) {
		String name = "javax.persistence.Entity";
		NormalAnnotationExpr normalAnnotationExpr = new NormalAnnotationExpr(new Name(name), new NodeList<>());
		normalAnnotationExpr.addPair("name", new StringLiteralExpr(ormEntityMapping.tableName));

		SourceChange[] sourceChange = addAnnotation(cu, normalAnnotationExpr, "JPA Entity detected");

		return sourceChange[0];
	}

	private SourceChange[] addAnnotation(CompilationUnit cu, NormalAnnotationExpr normalAnnotationExpr, final String message) {
		SourceChange[] sourceChange = new SourceChange[1];
		cu.accept(new ModifierVisitor<Void>() {
			public Visitable visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
				List<SimpleName> simpleNames = classDeclaration.findAll(SimpleName.class);

				String annotationClass = normalAnnotationExpr.getNameAsString();

				String simpleName = annotationClass.substring(annotationClass.lastIndexOf(".") + 1);
				NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) classDeclaration.getAnnotationByName(simpleName).orElseGet(() -> null);

				if (annotationExpr == null) {
					annotationExpr = classDeclaration.addAndGetAnnotation(simpleName);
					classDeclaration.findAncestor(CompilationUnit.class).ifPresent(p -> p.addImport(annotationClass));
					annotationExpr.setPairs(normalAnnotationExpr.getPairs());
					sourceChange[0] = new SourceChange(javaSourceChangesHandler.getUri(), simpleNames.get(0).getRange().get(), message);
				}


				return super.visit(classDeclaration, arg);
			}
		}, null);
		return sourceChange;
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
							sourceChange[0] = new SourceChange(javaSourceChangesHandler.getUri(), fieldDeclaration.getRange().get(), "column mapping detected for property: " + propertyMapping.columnName);
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
					sourceChange[0] = new SourceChange(javaSourceChangesHandler.getUri(), p.getRange().get(), "add property to match database column: " + propertyMapping.columnName);
				});

			});
		}
		return sourceChange[0];
	}

}
