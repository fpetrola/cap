package com.fpetrola.cap.model.binders.implementations.helpers;

import static com.fpetrola.cap.model.source.JavaClassBinder.createNewJavaClassContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.ast.CompilationUnit;

public abstract class DefaultJavaClassBinder<S, T> extends DefaultBinder {

	public DefaultJavaClassBinder() {
	}

	protected abstract List<Function<CompilationUnit, SourceChange>> getModifiers(S source, String uri);

	protected abstract String getClassname(S source);

	public List<T> pull(S source) {
		if (workspacePath != null && sourceChangesListener != null) {

			JavaSourceChangesHandler javaSourceChangesHandler = new JavaSourceChangesHandler(workspacePath, getClassname(source));
			String uri = javaSourceChangesHandler.getUri();
			List<SourceChange> sourceChanges = new ArrayList<>();

			if (javaSourceChangesHandler.fileExists()) {

				List<Function<CompilationUnit, SourceChange>> modifiers = getModifiers(source, uri);

				javaSourceChangesHandler.addInsertionsFor(sourceChanges, modifiers);
				javaSourceChangesHandler.addFixAllForNow(uri, sourceChanges);
				sourceChangesListener.sourceChange(uri, sourceChanges);
			} else {
				sourceChangesListener.fileCreation(uri, createNewJavaClassContent(getClassname(source)));
			}
		}
		return Arrays.asList();
	}

}