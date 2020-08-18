package com.fpetrola.cap.model;

import java.nio.file.Paths;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

public class JPAEntityMappingWriter {

	private ORMEntityMapping ormEntityMapping;

	public JPAEntityMappingWriter(ORMEntityMapping ormEntityMapping) {
		this.ormEntityMapping = ormEntityMapping;
	}

	public void write() {
		SourceRoot sourceRoot = new SourceRoot(
				CodeGenerationUtils.mavenModuleRoot(JPAEntityMappingWriter.class).resolve("src/main/java"));

		CompilationUnit cu = sourceRoot.parse(ormEntityMapping.mappedClass.getPackage().getName(),
				ormEntityMapping.mappedClass.getSimpleName() + ".java");

		cu.accept(new ModifierVisitor<Void>() {
			@Override
			public Visitable visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
				classDeclaration.addAnnotation(Entity.class);

				if (ormEntityMapping.tableName != null) {
					NormalAnnotationExpr annotationExpr = classDeclaration.addAndGetAnnotation(Table.class);
					annotationExpr.addPair("name", new StringLiteralExpr(ormEntityMapping.tableName));
				}

				return super.visit(classDeclaration, arg);
			}

			@Override
			public Visitable visit(FieldDeclaration fieldDeclaration, Void arg) {

				for (PropertyMapping propertyMapping : ormEntityMapping.propertyMappings) {

					SimpleName name = fieldDeclaration.getVariable(0).getName();

					if (name.toString().equals(propertyMapping.propertyName)) {

						if (propertyMapping.propertyMappingType != null)
							fieldDeclaration.addAnnotation(ManyToOne.class);

						if (propertyMapping.columnName != null) {
							NormalAnnotationExpr annotation = fieldDeclaration.addAndGetAnnotation(JoinColumn.class);
							annotation.addPair("name", new StringLiteralExpr(propertyMapping.columnName));
						}
					}
				}
				return super.visit(fieldDeclaration, arg);
			}
		}, null);

		sourceRoot.saveAll(
				CodeGenerationUtils.mavenModuleRoot(JPAEntityMappingWriter.class).resolve(Paths.get("output")));
	}

}
