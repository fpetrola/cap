package com.fpetrola.cap.model.binders.implementations.helpers;

import com.fpetrola.cap.helpers.Provider;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

final class BasicCompilationUnitProvider implements Provider<CompilationUnit> {
	private final String uri;
	private final JavaSourceChangesHandler javaSourceChangesHandler;
	private CompilationUnit originalCompilationUnit;
	private CompilationUnit currentCompilationUnit;

	BasicCompilationUnitProvider(String uri, JavaSourceChangesHandler javaSourceChangesHandler) {
		this.uri = uri;
		this.javaSourceChangesHandler = javaSourceChangesHandler;
	}

	public CompilationUnit get() {
		return createNew();
	}

	public CompilationUnit createNew() {
		if (originalCompilationUnit == null) {
			originalCompilationUnit = javaSourceChangesHandler.createCompilationUnit(uri);
			currentCompilationUnit = javaSourceChangesHandler.createCompilationUnit(uri);
		} else {
			String currentSource = LexicalPreservingPrinter.print(currentCompilationUnit);
			currentCompilationUnit = javaSourceChangesHandler.createCompilationUnitFromSource(currentSource);
		}
			

		return currentCompilationUnit;
	}
}