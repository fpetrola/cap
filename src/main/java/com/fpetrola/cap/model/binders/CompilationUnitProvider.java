package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.helpers.Provider;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class CompilationUnitProvider implements Provider<CompilationUnit> {

	private JavaSourceChangesHandler javaSourceChangesHandler;
	private CompilationUnit compilationUnit;
	private List<CompilationUnit> stack = new ArrayList<CompilationUnit>();
	private CompilationUnit originalCompilationUnit;
	private String uri;

	public CompilationUnitProvider(String uri, JavaSourceChangesHandler javaSourceChangesHandler) {
		this.uri = uri;
		this.javaSourceChangesHandler = javaSourceChangesHandler;
	}

	public CompilationUnit get() {
		if (compilationUnit == null)
			return createNew();
		else
			return compilationUnit;
	}

	public CompilationUnit createNew() {
		if (getOriginalCompilationUnit() == null) {
			setOriginalCompilationUnit(javaSourceChangesHandler.createCompilationUnit(uri));
		}

		String originalSource = LexicalPreservingPrinter.print(getOriginalCompilationUnit());
		compilationUnit = javaSourceChangesHandler.createCompilationUnitFromSource(originalSource);
		getStack().add(compilationUnit);
		return compilationUnit;
	}

	public List<CompilationUnit> getStack() {
		return stack;
	}

	public void setStack(List<CompilationUnit> stack) {
		this.stack = stack;
	}

	public CompilationUnit getOriginalCompilationUnit() {
		return originalCompilationUnit;
	}

	public CompilationUnit setOriginalCompilationUnit(CompilationUnit originalCompilationUnit) {
		this.originalCompilationUnit = originalCompilationUnit;
		return originalCompilationUnit;
	}

}
