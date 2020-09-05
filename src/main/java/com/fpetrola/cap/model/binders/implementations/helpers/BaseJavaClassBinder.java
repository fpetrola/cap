package com.fpetrola.cap.model.binders.implementations.helpers;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.helpers.Provider;
import com.fpetrola.cap.model.binders.CompilationUnitProvider;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.binders.SourceCodeChanger;
import com.fpetrola.cap.model.binders.implementations.java.JavaClassModel;
import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceCodeModification;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public abstract class BaseJavaClassBinder<S, T> extends DefaultBinder<S, T> {

	public BaseJavaClassBinder() {
	}

	protected abstract String getClassname(S source);

	public List<T> pull(S source) {
		try {
			var foundWorkspacePath = findWorkspacePath();
			var sourceChangesListener = getSourceChangesListener();

			if (foundWorkspacePath != null && sourceChangesListener != null) {
				var uri = getURI(new File(foundWorkspacePath + "/" + JavaSourceChangesHandler.javaSourceFolderBase + "/" + getClassname(source).replace(".", "/") + ".java"));

				JavaSourceChangesHandler javaSourceChangesHandler = new JavaSourceChangesHandler(foundWorkspacePath);
				var sourceCodeChanger = new SourceCodeChanger(uri, foundWorkspacePath, getClassname(source), new CompilationUnitProvider(uri, javaSourceChangesHandler), sourceChangesListener, getChangesLinker());
				computeChanges(source, new JavaClassModel(sourceCodeChanger), sourceCodeChanger);
				sourceCodeChanger.aplyChanges();

//				Provider<CompilationUnit> provider = new BasicCompilationUnitProvider(uri, javaSourceChangesHandler);
//				var sourceCodeChangerFixall = new SourceCodeChanger(uri, foundWorkspacePath, getClassname(source), provider, sourceChangesListener, getChangesLinker());
//				computeChanges(source, new JavaClassModel(sourceCodeChangerFixall), sourceCodeChangerFixall);

//				List<CodeProposal> codeProposals = sourceCodeChangerFixall.getCodeProposals();

//				CodeProposal fixallCodeProposal = new CodeProposal(uri, new Range(new Position(0, 1), new Position(0, 1)), "fixall");
//				for (CodeProposal codeProposal : codeProposals) {
//					List<SourceCodeModification> insertions = codeProposal.getSourceChange().getInsertions();
//					for (SourceCodeModification sourceCodeModification : insertions) {
//						sourceCodeModification.setUri(uri);
//						fixallCodeProposal.getSourceChange().getInsertions().add(sourceCodeModification);
//					}
//				}
//				sourceCodeChangerFixall.aplyChanges2(fixallCodeProposal);

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return Arrays.asList();
	}

	public String getURI(File file) {
		return file.toURI().toString().replace("file:/", "file:///");
	}

	protected MemberValuePair createPair(String key, String value) {
		return new MemberValuePair(key, new StringLiteralExpr(value));
	}

	public abstract void computeChanges(S ormEntityMapping, JavaClassModel javaClassModel, SourceCodeChanger sourceCodeChanger);
}