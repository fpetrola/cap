package com.fpetrola.cap.model.binders;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.helpers.Provider;
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

	private ChangesLinker defaultChangesLinker;
	private Provider<CompilationUnit> compilationUnitProvider;
	private List<CodeProposal> codeProposals;
	private SourceChangesListener sourceChangesListener;
	private String uri;
	private CompilationUnit originalCompilationUnit;

	public SourceCodeChanger(String uri, String workspacePath, String className, Provider<CompilationUnit> provider, SourceChangesListener sourceChangesListener, ChangesLinker defaultChangesLinker) {
		this.uri = uri;
		this.sourceChangesListener = sourceChangesListener;
		this.compilationUnitProvider = provider;
		this.defaultChangesLinker = defaultChangesLinker;

		this.originalCompilationUnit = provider.get();
		provider.createNew();
		this.setCodeProposals(new ArrayList<>());
	}

	public void addAnnotationToClass(NormalAnnotationExpr createAnnotation) {
		var name = getAnnotationSimpleName(createAnnotation);
		addChange(JavaparserHelper.addAnnotationToClass(this, createAnnotation, "add '" + name + "' annotation to class", ""));
	}

	public void addAnnotationToField(NormalAnnotationExpr createAnnotation, JavaField javaField) {
		if (!javaField.isNew()) {
			var name = getAnnotationSimpleName(createAnnotation);
			var message = "add annotation " + name + " to property: " + javaField.getName();
			addChange(JavaparserHelper.addAnnotationToField(javaField.sourceCodeChanger, createAnnotation, javaField.getName(), message));
		}
	}

	private void addChange(CodeProposal codeProposal) {
		if (codeProposal != null) {
			CompilationUnit compilationUnit = compilationUnitProvider.get();
			var originalSource = LexicalPreservingPrinter.print(originalCompilationUnit);
			var resultingSource = LexicalPreservingPrinter.print(compilationUnit);

			codeProposal.getSourceChange().setInsertions(JavaSourceChangesHandler.createModifications(originalSource, resultingSource));
			getCodeProposals().add(codeProposal);

			compilationUnitProvider.createNew();
		}
	}

	public void addfield(String name, Class<?> type) {
		addChange(JavaparserHelper.addFieldIfNotExists(compilationUnitProvider.get(), "create property: " + name, name, type.getSimpleName(), uri));
	}

	public void aplyChanges() {
		sourceChangesListener.sourceChange(getUri(), getCodeProposals());
	}

	public void aplyChanges2(CodeProposal codeProposal) {
		if (codeProposal != null && codeProposal.getSourceChange() != null)
			sourceChangesListener.sourceChange(getUri(), Arrays.asList(codeProposal));
	}

	public File createFileFromUri() {
		return new File(URI.create(uri));
	}

	private String getAnnotationSimpleName(NormalAnnotationExpr createAnnotation) {
		var name = createAnnotation.getName().asString();
		return name.substring(name.lastIndexOf(".") + 1);
	}

	public Provider<CompilationUnit> getCompilationUnitProvider() {
		return compilationUnitProvider;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public ChangesLinker getChangesLinker() {
		return defaultChangesLinker;
	}

	public List<CodeProposal> getCodeProposals() {
		return codeProposals;
	}

	public void setCodeProposals(List<CodeProposal> codeProposals) {
		this.codeProposals = codeProposals;
	}

}
