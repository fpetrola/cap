package com.fpetrola.cap.model.binders.implementations.java;

import com.fpetrola.cap.model.binders.SourceCodeChanger;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

public class JavaField {

	String name;
	private String type;
	public SourceCodeChanger sourceCodeChanger;
	private boolean isNew;

	public JavaField(String name, String type, SourceCodeChanger sourceCodeChanger, boolean isNew) {
		this.name = name;
		this.type = type;
		this.sourceCodeChanger = sourceCodeChanger;
		this.isNew = isNew;
	}

	public void addAnnotationIfNotExists(NormalAnnotationExpr createAnnotation) {
		sourceCodeChanger.addAnnotationToField(createAnnotation, this);
	}

	public String getName() {
		return name;
	}

	public boolean isNew() {
		return isNew;
	}
}
