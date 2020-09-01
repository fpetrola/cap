package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaparserHelper.addAnnotationToClass;
import static com.fpetrola.cap.model.source.JavaparserHelper.addFieldIfNotExists;
import static com.fpetrola.cap.model.source.JavaparserHelper.addMethod;
import static com.fpetrola.cap.model.source.JavaparserHelper.createAnnotation;

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
//		String className = source.mappedClass.substring(source.mappedClass.lastIndexOf(".") + 1);
//
//		String select = "{\n\tTypedQuery<classname> q = entityManager.createQuery(\"SELECT b FROM classname b WHERE b.propertyname = :propertyname\", classname.class);\n\tq.setParameter(\"propertyname\", propertyname);\n\treturn q.getSingleResult();\n}";
////		String select = "{\n" + "    return 1;\n" + "}";
//		
//		modifiers.add(c -> addAnnotationToClass(c, createAnnotation("Repository"), "Repository annotation", "")[0]);
//		modifiers.add(c -> addFieldIfNotExists(c, "add entity manager", "entityManager", "EntityManager", uri));
//		modifiers.add(c -> {
//			String body = "{\n" + "    return entityManager.find(" + className + ".class, id);\n";
//			return addMethod(c, "find" + className + "ById", uri, "add findById method", body + "}");
//		});
//		modifiers.add(c -> addMethod(c, "save", uri, "add save method", "{\n" + "    return 1;\n" + "}"));
//		for (PropertyMapping p : source.propertyMappings) {
//			modifiers.add(c -> {
//				String replaceAll = select.replaceAll("propertyname", p.propertyName).replaceAll("classname", className);
//				return addMethod(c, "findBy" + p.propertyName, uri, "add method findBy" + p.propertyName, replaceAll);
//			});
//		}
		return modifiers;
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass + "Repository";
	}

	@Override
	public void computeChanges(ORMEntityMapping ormEntityMapping, JavaClassModel javaClassModel) {
		// TODO Auto-generated method stub
		
	}
}
