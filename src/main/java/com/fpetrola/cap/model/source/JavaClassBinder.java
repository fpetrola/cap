package com.fpetrola.cap.model.source;

import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class JavaClassBinder {

	public static NormalAnnotationExpr createAnnotation(String identifier, MemberValuePair... memberValuePair) {
		return new NormalAnnotationExpr(new Name(identifier), NodeList.nodeList(memberValuePair));
	}

	public static SourceChange[] addAnnotationToClass(CompilationUnit cu, NormalAnnotationExpr normalAnnotationExpr, final String message, String name, String uri) {
		SourceChange[] sourceChange = new SourceChange[1];
		cu.accept(new ModifierVisitor<Void>() {
			public Visitable visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
				sourceChange[0] = addAnnotationToBodyDeclaration(normalAnnotationExpr, message, classDeclaration, classDeclaration.findAll(SimpleName.class).get(0), uri);
				return super.visit(classDeclaration, arg);
			}
		}, null);
		return sourceChange;
	}

	public static SourceChange addAnnotationToField(CompilationUnit cu, NormalAnnotationExpr normalAnnotationExpr, final String propertyName, String uri) {
		SourceChange[] sourceChange = new SourceChange[1];
		cu.accept(new ModifierVisitor<Void>() {
			public Visitable visit(FieldDeclaration fieldDeclaration, Void arg) {
				SimpleName fieldName = fieldDeclaration.getVariable(0).getName();
				if (fieldName.toString().equals(propertyName)) {
					String message = "add annotation " + normalAnnotationExpr.getNameAsString() + " to property: " + propertyName;
					sourceChange[0] = addAnnotationToBodyDeclaration(normalAnnotationExpr, message, fieldDeclaration, fieldDeclaration, uri);
				}

				return super.visit(fieldDeclaration, arg);
			}
		}, null);
		return sourceChange[0];
	}

	public static SourceChange addFieldIfNotExists(CompilationUnit cu, String message, String propertyName, String typeName, String uri) {
		boolean found = fieldExists(cu, propertyName);

		SourceChange[] sourceChange = new SourceChange[1];
		if (!found) {
			cu.findAll(ClassOrInterfaceDeclaration.class).forEach(coid -> {
				VariableDeclarator variables = new VariableDeclarator();
				variables.setName(propertyName);
				variables.setType(typeName.contains("INT") ? Integer.class : String.class);
				FieldDeclaration fieldDeclaration = new FieldDeclaration().addVariable(variables);
				coid.getMembers().add(0, fieldDeclaration);
				Optional<String> fullyQualifiedName = coid.getFullyQualifiedName();
				Optional<SimpleName> simpleNames = coid.findAll(SimpleName.class).stream().filter(sn -> sn.getParentNode().get() instanceof ClassOrInterfaceDeclaration).findFirst();

				simpleNames.ifPresent(p -> {
					sourceChange[0] = new SourceChange(uri, p.getRange().get(), message);
				});

			});
		}
		return sourceChange[0];
	}

	private static SourceChange addAnnotationToBodyDeclaration(NormalAnnotationExpr normalAnnotationExpr, final String message, BodyDeclaration classDeclaration, Node node, String uri) {
		SourceChange[] sourceChange = new SourceChange[1];

		if (node != null) {
			String annotationClass = normalAnnotationExpr.getNameAsString();

			String simpleName = annotationClass.substring(annotationClass.lastIndexOf(".") + 1);
			NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) classDeclaration.getAnnotationByName(simpleName).orElseGet(() -> null);

			if (annotationExpr == null) {
				annotationExpr = classDeclaration.addAndGetAnnotation(simpleName);
				classDeclaration.findAncestor(CompilationUnit.class).ifPresent(p -> p.addImport(annotationClass));
				annotationExpr.setPairs(normalAnnotationExpr.getPairs());
				sourceChange[0] = new SourceChange(uri, node.getRange().get(), message);
			}
		}
		return sourceChange[0];
	}

	private static boolean fieldExists(CompilationUnit cu, final String propertyName) {
		boolean[] found = new boolean[1];
		cu.accept(new ModifierVisitor<Void>() {
			public Visitable visit(FieldDeclaration fieldDeclaration, Void arg) {
				SimpleName fieldName = fieldDeclaration.getVariable(0).getName();
				if (fieldName.toString().equals(propertyName)) {
					found[0] = true;
				}
				return super.visit(fieldDeclaration, arg);
			}
		}, null);
		return found[0];
	}
	
	public static String createNewJavaClassContent(String className) {
		String simpleName = className.substring(0, className.lastIndexOf("."));
		String content = "package " + simpleName + ";\n\nimport java.util.List;\n" + "\n\n" + "public class " + className.substring(simpleName.length() + 1) + " {\n\n}";
		return content;
	}

}
