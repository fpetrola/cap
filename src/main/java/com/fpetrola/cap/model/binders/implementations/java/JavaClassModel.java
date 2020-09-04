package com.fpetrola.cap.model.binders.implementations.java;

import java.util.List;

import com.fpetrola.cap.model.binders.SourceCodeChanger;
import com.fpetrola.cap.model.source.JavaparserHelper;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

public class JavaClassModel {
	private SourceCodeChanger sourceCodeChanger;

	public JavaClassModel(SourceCodeChanger sourceCodeChanger) {
		this.sourceCodeChanger = sourceCodeChanger;
	}

	public void addAnnotationIfNotExists(NormalAnnotationExpr createAnnotation) {
		sourceCodeChanger.addAnnotationToClass(createAnnotation);
	}

	public List<JavaField> getFields() {
		return JavaparserHelper.getFields(sourceCodeChanger, sourceCodeChanger.getUri());
	}

	public JavaField createField(String name, Class<?> type) {
		sourceCodeChanger.addfield(name, type);
		return new JavaField(name, type.getSimpleName(), sourceCodeChanger, true);
	}

}
