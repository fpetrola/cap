package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaClassBinder.addAnnotationToClass;
import static com.fpetrola.cap.model.source.JavaClassBinder.addFieldIfNotExists;
import static com.fpetrola.cap.model.source.JavaClassBinder.addMethod;
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

public class RepositoryGenerator extends DefaultJavaClassBinder<ORMEntityMapping, Void> implements BidirectionalBinder<ORMEntityMapping, Void>, WorkspaceAwareBinder {

	protected List<Function<CompilationUnit, SourceChange>> getModifiers(ORMEntityMapping source, String uri) {
		List<Function<CompilationUnit, SourceChange>> modifiers = new ArrayList<>();

		modifiers.add(c -> addAnnotationToClass(c, createAnnotation("Repository"), "Repository annotation", "", uri)[0]);
		modifiers.add(c -> addFieldIfNotExists(c, "add entity manager", "entityManager", "EntityManager", uri));
		modifiers.add(c -> addMethod(c, "findAll", uri, "add findAll method"));
		modifiers.add(c -> addMethod(c, "save", uri, "add save method"));
		for (PropertyMapping p : source.propertyMappings) {
			modifiers.add(c -> addMethod(c, "findBy" + p.propertyName, uri, "add method findBy" + p.propertyName));
		}
		return modifiers;
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass + "Repository";
	}
}
