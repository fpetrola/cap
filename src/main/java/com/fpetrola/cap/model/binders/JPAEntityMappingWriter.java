package com.fpetrola.cap.model.binders;

import java.nio.file.Paths;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

public class JPAEntityMappingWriter {

	private ORMEntityMapping ormEntityMapping;

	public JPAEntityMappingWriter(ORMEntityMapping ormEntityMapping) {
		this.ormEntityMapping = ormEntityMapping;
	}

	boolean modified;

	public void write() {
		SourceRoot sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(JPAEntityMappingWriter.class).resolve("src/main/java"));
		ParserConfiguration parserConfiguration = sourceRoot.getParserConfiguration();
		parserConfiguration.setLexicalPreservationEnabled(true);
		CompilationUnit cu = sourceRoot.parse(ormEntityMapping.mappedClass.getPackage().getName(), ormEntityMapping.mappedClass.getSimpleName() + ".java");

		LexicalPreservingPrinter.setup(cu);
		cu.accept(new ModifierVisitor<Void>() {

			@Override
			public Visitable visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
				if (!classDeclaration.isAnnotationPresent(Entity.class)) {
					classDeclaration.addAnnotation(Entity.class);
					modified = true;
				}

				if (ormEntityMapping.tableName != null) {
					NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) classDeclaration.getAnnotationByClass(Table.class).orElseGet(() -> null);
					if (annotationExpr == null) {
						annotationExpr = classDeclaration.addAndGetAnnotation(Table.class);
						annotationExpr.addPair("name", new StringLiteralExpr(ormEntityMapping.tableName));
						modified = true;
					}
				}

				return super.visit(classDeclaration, arg);
			}

			@Override
			public Visitable visit(FieldDeclaration fieldDeclaration, Void arg) {

				for (PropertyMapping propertyMapping : ormEntityMapping.propertyMappings) {

					SimpleName name = fieldDeclaration.getVariable(0).getName();

					if (name.toString().equals(propertyMapping.propertyName)) {

						if (propertyMapping.propertyMappingType != null) {
							if (!fieldDeclaration.isAnnotationPresent(ManyToOne.class)) {
								fieldDeclaration.addAnnotation(ManyToOne.class);
								modified = true;
							}
						}

						if (propertyMapping.columnName != null) {
							if (!fieldDeclaration.isAnnotationPresent(JoinColumn.class)) {
								NormalAnnotationExpr annotation = fieldDeclaration.addAndGetAnnotation(JoinColumn.class);
								annotation.addPair("name", new StringLiteralExpr(propertyMapping.columnName));
							}
						}
					}
				}
				return super.visit(fieldDeclaration, arg);
			}
		}, null);

		if (modified) {
			sourceRoot.setPrinter(LexicalPreservingPrinter::print);
			sourceRoot.saveAll(CodeGenerationUtils.mavenModuleRoot(JPAEntityMappingWriter.class).resolve(Paths.get("src/main/java")));
		}
	}

}
