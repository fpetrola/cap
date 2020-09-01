package com.fpetrola.cap.model.binders.implementations.helpers;

import static com.fpetrola.cap.model.source.JavaClassBinder.createNewJavaClassContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public abstract class DefaultJavaClassBinder<S, T> extends DefaultBinder<S, T> {

	public DefaultJavaClassBinder() {
	}

	protected abstract List<Function<CompilationUnit, SourceChange>> getModifiers(S source, String uri);

	protected abstract String getClassname(S source);

	public List<T> pull(S source) {
		String findWorkspacePath = findWorkspacePath();
		SourceChangesListener sourceChangesListener = getSourceChangesListener();
		if (findWorkspacePath != null && sourceChangesListener != null) {

			JavaSourceChangesHandler javaSourceChangesHandler = new JavaSourceChangesHandler(findWorkspacePath, getClassname(source));
			String uri = javaSourceChangesHandler.getUri();
			List<SourceChange> sourceChanges = new ArrayList<>();
			List<Function<CompilationUnit, SourceChange>> modifiers = getModifiers(source, uri);

			if (javaSourceChangesHandler.fileExists()) {

				javaSourceChangesHandler.addInsertionsFor(sourceChanges, modifiers);
				javaSourceChangesHandler.addFixAllForNow(sourceChanges, modifiers, uri, null);
				sourceChangesListener.sourceChange(uri, sourceChanges);
			} else {
				String content = createNewJavaClassContent(getClassname(source));
				content= javaSourceChangesHandler.addFixAllForNow(sourceChanges, modifiers, uri, content);
				sourceChangesListener.fileCreation(uri, content);
			}
		}
		return Arrays.asList();
	}

	protected MemberValuePair createPair(String key, String value) {
		return new MemberValuePair(key, new StringLiteralExpr(value));
	}

}