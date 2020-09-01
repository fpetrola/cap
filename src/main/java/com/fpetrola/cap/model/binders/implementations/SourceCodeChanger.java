package com.fpetrola.cap.model.binders.implementations;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.source.JavaparserHelper;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

@SuppressWarnings("unused")
public class SourceCodeChanger {

	private CompilationUnitProvider compilationUnitProvider;
	private String uri;
	private SourceChangesListener sourceChangesListener;
	private List<SourceChange> sourceChanges;

	public SourceCodeChanger(CompilationUnitProvider compilationUnit, SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
		this.compilationUnitProvider = compilationUnit;
		this.uri = compilationUnit.getJavaSourceChangesHandler().getUri();
		try {
			compilationUnitProvider.get();
			this.sourceChanges = new ArrayList<SourceChange>();
		} catch (Exception e) {
			String content = JavaparserHelper.createNewJavaClassContent(compilationUnitProvider.getJavaSourceChangesHandler().getClassName());
			sourceChangesListener.fileCreation(uri, content);
			throw new RuntimeException(e);
		}
	}

	public SourceCodeChanger(String findWorkspacePath, String classname, SourceChangesListener sourceChangesListener) {
		this(new CompilationUnitProvider(new JavaSourceChangesHandler(findWorkspacePath, classname)), sourceChangesListener);
	}

	void addAnnotationToClass(NormalAnnotationExpr createAnnotation) {
		var name = getAnnotationSimpleName(createAnnotation);
		addChange(compilationUnitProvider.getNew(), JavaparserHelper.addAnnotationToClass(this, createAnnotation, "add '" + name + "' annotation to class", ""));
	}

	private String getAnnotationSimpleName(NormalAnnotationExpr createAnnotation) {
		var name = createAnnotation.getName().asString();
		return name.substring(name.lastIndexOf(".") + 1);
	}

	void addAnnotationToField(NormalAnnotationExpr createAnnotation, JavaField javaField) {
		if (!javaField.isNew()) {
			var name = getAnnotationSimpleName(createAnnotation);
			var message = "add annotation " + name + " to property: " + javaField.getName();
			addChange(compilationUnitProvider.getNew(), JavaparserHelper.addAnnotationToField(javaField.sourceCodeChanger, createAnnotation, javaField.name, message));
		}
	}

	void addfield(String name, Class<?> type) {
		addChange(compilationUnitProvider.getNew(), JavaparserHelper.addFieldIfNotExists(compilationUnitProvider.get(), "create property: " + name, name, type.getSimpleName(), uri));
	}

	public CompilationUnitProvider getCompilationUnitProvider() {
		return compilationUnitProvider;
	}

	public String getURI(File file) {
		return file.toURI().toString().replace("file:/", "file:///");
	}

	public File createFileFromUri() {
		return new File(URI.create(uri));
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	private void addChange(CompilationUnit compilationUnit, SourceChange sourceChange) {
		if (sourceChange != null) {
			var first = getCompilationUnitProvider().getStack().get(0);
			var originalSource = LexicalPreservingPrinter.print(first);
			var resultingSource = LexicalPreservingPrinter.print(compilationUnit);

			sourceChange.insertions = JavaSourceChangesHandler.createModifications(originalSource, resultingSource);
			sourceChanges.add(sourceChange);
		}
	}

	public void aplyChanges() {
		sourceChangesListener.sourceChange(getUri(), sourceChanges);
	}

}
