package com.fpetrola.cap.model.binders.implementations.helpers;

import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.binders.implementations.JavaClassModel;
import com.fpetrola.cap.model.binders.implementations.SourceCodeChanger;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public abstract class DefaultJavaClassBinder<S, T> extends DefaultBinder<S, T> {

	public DefaultJavaClassBinder() {
	}

	protected abstract String getClassname(S source);

	public List<T> pull(S source) {
		try {
			var foundWorkspacePath = findWorkspacePath();
			var sourceChangesListener = getSourceChangesListener();

			if (foundWorkspacePath != null && sourceChangesListener != null) {
				var sourceCodeChanger = new SourceCodeChanger(foundWorkspacePath, getClassname(source), sourceChangesListener);
				computeChanges(source, new JavaClassModel(sourceCodeChanger));
				sourceCodeChanger.aplyChanges();
			}
		} catch (Exception e) {
		}
		return Arrays.asList();
	}

	protected MemberValuePair createPair(String key, String value) {
		return new MemberValuePair(key, new StringLiteralExpr(value));
	}

	public abstract void computeChanges(S ormEntityMapping, JavaClassModel javaClassModel);
}