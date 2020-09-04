package com.fpetrola.cap.model.binders;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.binders.implementations.java.JavaField;
import com.fpetrola.cap.model.binders.sync.ChangesLinker;
import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.JavaparserHelper;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

@SuppressWarnings("unused")
public class SourceCodeChanger {

	private ChangesLinker changesLinker;
	private CompilationUnitProvider compilationUnitProvider;
	private List<CodeProposal> sourceChanges;
	private SourceChangesListener sourceChangesListener;
	private String uri;

	public SourceCodeChanger(CompilationUnitProvider compilationUnit, SourceChangesListener sourceChangesListener, ChangesLinker changesLinker) {
		this.sourceChangesListener = sourceChangesListener;
		this.compilationUnitProvider = compilationUnit;
		this.changesLinker = changesLinker;
		this.uri = compilationUnit.getJavaSourceChangesHandler().getUri();
		try {
			compilationUnitProvider.getNew();
			this.sourceChanges = new ArrayList<>();
		} catch (Exception e) {
			String content = JavaparserHelper.createNewJavaClassContent(compilationUnitProvider.getJavaSourceChangesHandler().getClassName());
			sourceChangesListener.fileCreation(uri, content);
			throw new RuntimeException(e);
		}
	}

	public SourceCodeChanger(String findWorkspacePath, String classname, SourceChangesListener sourceChangesListener, ChangesLinker changesLinker) {
		this(new CompilationUnitProvider(new JavaSourceChangesHandler(findWorkspacePath, classname)), sourceChangesListener, changesLinker);
	}

	public void addAnnotationToClass(NormalAnnotationExpr createAnnotation) {
		var name = getAnnotationSimpleName(createAnnotation);
		addChange(compilationUnitProvider.getNew(), JavaparserHelper.addAnnotationToClass(this, createAnnotation, "add '" + name + "' annotation to class", ""));
	}

	public void addAnnotationToField(NormalAnnotationExpr createAnnotation, JavaField javaField) {
		if (!javaField.isNew()) {
			var name = getAnnotationSimpleName(createAnnotation);
			var message = "add annotation " + name + " to property: " + javaField.getName();
			addChange(compilationUnitProvider.get(), JavaparserHelper.addAnnotationToField(javaField.sourceCodeChanger, createAnnotation, javaField.getName(), message));
			compilationUnitProvider.getNew();
		}
	}

	private void addChange(CompilationUnit compilationUnit, CodeProposal sourceChange) {
		if (sourceChange != null) {
			var originalSource = LexicalPreservingPrinter.print(getCompilationUnitProvider().getOriginalCompilationUnit());
			var resultingSource = LexicalPreservingPrinter.print(compilationUnit);

			sourceChange.getSourceChange().setInsertions(JavaSourceChangesHandler.createModifications(originalSource, resultingSource));
			sourceChanges.add(sourceChange);
		}
	}

	public void addfield(String name, Class<?> type) {
		addChange(compilationUnitProvider.get(), JavaparserHelper.addFieldIfNotExists(compilationUnitProvider.get(), "create property: " + name, name, type.getSimpleName(), uri));
		compilationUnitProvider.getNew();
	}

	public void aplyChanges() {
		sourceChangesListener.sourceChange(getUri(), sourceChanges);
	}

	public void aplyChanges2(CodeProposal codeProposal) {
		if (codeProposal.getSourceChange() != null)
			sourceChangesListener.sourceChange(getUri(), Arrays.asList(codeProposal));
	}

	public File createFileFromUri() {
		return new File(URI.create(uri));
	}

	private String getAnnotationSimpleName(NormalAnnotationExpr createAnnotation) {
		var name = createAnnotation.getName().asString();
		return name.substring(name.lastIndexOf(".") + 1);
	}

	public CompilationUnitProvider getCompilationUnitProvider() {
		return compilationUnitProvider;
	}

	public String getUri() {
		return uri;
	}

	public String getURI(File file) {
		return file.toURI().toString().replace("file:/", "file:///");
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public ChangesLinker getChangesLinker() {
		return changesLinker;
	}

}
