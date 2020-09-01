package com.fpetrola.cap.languageserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.CreateFileOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import com.fpetrola.cap.model.binders.BaseBindingProcessor;
import com.fpetrola.cap.model.binders.BindingProcessor;
import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceCodeModification;

public class DiagnosticGenerator {

	protected Map<String, List<CodeAction>> codeActions = new HashMap<>();

	public DiagnosticGenerator() {
	}

	public List<PublishDiagnosticsParams> getDiagnostics(Map<String, List<SourceChange>> changesMap, BaseBindingProcessor bindingApp) {
		List<PublishDiagnosticsParams> diagnostics = new ArrayList<>();
		changesMap.clear();
		codeActions.clear();
	
		bindingApp.bind(false);
	
		for (Entry<String, List<SourceChange>> entry : changesMap.entrySet()) {
			List<Diagnostic> validate = new ArrayList<>();
			List<SourceChange> list = entry.getValue();
			String uri = entry.getKey();
			codeActions.put(uri, new ArrayList<>());
			if (!list.isEmpty()) {
				for (SourceChange sourceChange : list)
					createDiagnosticFromSourceChange(new VersionedTextDocumentIdentifier(uri, null), validate, sourceChange);
	
			}
			diagnostics.add(new PublishDiagnosticsParams(uri, validate));
		}
		return diagnostics;
	}

	public void createFileAtClient(String uri, String content, LanguageClient languageClient) {
		TextEdit textEdit = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), content);
		WorkspaceEdit edit = new WorkspaceEdit();
		edit.setDocumentChanges(Arrays.asList(Either.forRight(new CreateFile(uri, new CreateFileOptions(false, true)))));
		HashMap<String, List<TextEdit>> changes = new HashMap<>();
		changes.put(uri, Arrays.asList(textEdit));
		edit.setChanges(changes);
	
//		languageClient.applyEdit(new ApplyWorkspaceEditParams(edit));
	}

	private CodeAction createCodeAction(VersionedTextDocumentIdentifier versionedTextDocumentIdentifier, SourceChange sourceChange, Diagnostic diagnostic) {
		CodeAction codeAction = new CodeAction("" + sourceChange.message);
		codeAction.setDiagnostics(Arrays.asList(diagnostic));
		codeAction.setEdit(createWorkspaceEditFromSourceChange(sourceChange, versionedTextDocumentIdentifier));
		codeAction.setKind(CodeActionKind.RefactorRewrite);
		return codeAction;
	}

	private Diagnostic createDiagnostic(SourceChange sourceChange) {
		com.github.javaparser.Range range = sourceChange.problemRange;
		Position start = new Position(range.begin.line - 1, range.begin.column - 1);
		Position end = new Position(range.end.line - 1, range.end.column);
		Range range1 = new Range(start, end);
		Diagnostic diagnostic = new Diagnostic(range1, sourceChange.message);
		diagnostic.setSeverity(DiagnosticSeverity.Warning);
		return diagnostic;
	}

	private void createDiagnosticFromSourceChange(VersionedTextDocumentIdentifier versionedTextDocumentIdentifier, List<Diagnostic> validate, SourceChange sourceChange) {
		Diagnostic diagnostic = createDiagnostic(sourceChange);
		validate.add(diagnostic);
		codeActions.get(sourceChange.uri).add(createCodeAction(versionedTextDocumentIdentifier, sourceChange, diagnostic));
	}

	private Range createLspRangeFromRange(com.github.javaparser.Range range2) {
		Position start = new Position(range2.begin.line, range2.begin.column);
		Position end = new Position(range2.end.line, range2.end.column);
		Range range = new Range(start, end);
		return range;
	}

	private WorkspaceEdit createWorkspaceEditFromSourceChange(SourceChange sourceChange, VersionedTextDocumentIdentifier versionedTextDocumentIdentifier) {

		List<TextEdit> textEdits = new ArrayList<TextEdit>();
		List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<Either<TextDocumentEdit, ResourceOperation>>();
		List<SourceCodeModification> sourceCodeModifications = new ArrayList<SourceCodeModification>(sourceChange.insertions);
		for (SourceCodeModification sourceCodeModification : sourceCodeModifications) {
			textEdits.add(new TextEdit(createLspRangeFromRange(sourceCodeModification.range), sourceCodeModification.content));
			documentChanges.add(Either.forLeft(new TextDocumentEdit(versionedTextDocumentIdentifier, textEdits)));
		}
		HashMap<String, List<TextEdit>> changes = new HashMap<>();
		changes.put(sourceChange.uri, textEdits);
		WorkspaceEdit edit = new WorkspaceEdit();
		edit.setDocumentChanges(documentChanges);
		edit.setChanges(changes);
		return edit;
	}

	protected Map<String, List<CodeAction>> getCodeActions() {
		return codeActions;
	}
}
