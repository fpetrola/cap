package com.fpetrola.cap.languageserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.fpetrola.cap.config.BindingApp;
import com.fpetrola.cap.model.binders.SourceChange;
import com.fpetrola.cap.model.binders.SourceChangesListener;
import com.fpetrola.cap.model.binders.SourceCodeInsertion;
import com.fpetrola.cap.model.binders.SourceCodeModification;
import com.fpetrola.cap.model.binders.SourceCodeReplace;

public class DiagnosticGenerator {

	static Map<String, List<CodeAction>> codeActions = new HashMap<>();

	public DiagnosticGenerator() {
	}

	static void editFile(CapLanguageServer languageServer, String uri, String content) {
		ApplyWorkspaceEditParams params2 = new ApplyWorkspaceEditParams();
		WorkspaceEdit edit = new WorkspaceEdit();
		TextEdit textEdit = new TextEdit();
		textEdit.setNewText(content);
		Range range = new Range();
		range.setStart(new Position(0, 0));
		range.setEnd(new Position(0, 0));
		textEdit.setRange(range);
		CreateFileOptions options = new CreateFileOptions();
		options.setIgnoreIfExists(false);
		ResourceOperation resourceChange = new CreateFile(uri, options);
		edit.setDocumentChanges(Arrays.asList(Either.forRight(resourceChange)));
		HashMap<String, List<TextEdit>> changes = new HashMap<>();
		changes.put(uri, Arrays.asList(textEdit));
		edit.setChanges(changes);
		params2.setEdit(edit);
		LanguageClient client = languageServer.client;
		client.applyEdit(params2);
	}

	static void send(CapLanguageServer languageServer) {
		codeActions.clear();
		languageServer.bindingApp.sourceChangesListener.ranges.clear();
		languageServer.bindingApp.bind(false);

		Map<String, List<SourceChange>> ranges = languageServer.bindingApp.sourceChangesListener.ranges;

		for (Entry<String, List<SourceChange>> entry : ranges.entrySet()) {
			List<Diagnostic> validate = new ArrayList<>();
			List<SourceChange> list = entry.getValue();
			String uri = entry.getKey();
			codeActions.put(uri, new ArrayList<>());
			if (!list.isEmpty()) {
				for (SourceChange sourceChange : list)
					createDiagnosticFromSourceChange(new VersionedTextDocumentIdentifier(uri, null), validate, sourceChange);

			}
			PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams(uri, validate);
			LanguageClient client = languageServer.client;
			client.publishDiagnostics(diagnostics);
		}
	}

	static void createDiagnosticFromSourceChange(VersionedTextDocumentIdentifier versionedTextDocumentIdentifier, List<Diagnostic> validate, SourceChange sourceChange) {
		com.github.javaparser.Range range = sourceChange.problemRange;
		Position start = new Position(range.begin.line - 1, range.begin.column - 1);
		Position end = new Position(range.end.line - 1, range.end.column);
		Range range1 = new Range(start, end);
		Diagnostic diagnostic = new Diagnostic(range1, sourceChange.message);
		diagnostic.setSeverity(DiagnosticSeverity.Warning);
		validate.add(diagnostic);

		CodeAction codeAction = new CodeAction("fix: " + sourceChange.message);
		codeAction.setDiagnostics(Arrays.asList(diagnostic));
		codeAction.setEdit(editFile1(sourceChange, versionedTextDocumentIdentifier));
		codeAction.setKind(CodeActionKind.Source);
		codeActions.get(sourceChange.uri).add(codeAction);
	}

	private static WorkspaceEdit editFile1(SourceChange sourceChange, VersionedTextDocumentIdentifier versionedTextDocumentIdentifier) {

		WorkspaceEdit edit = new WorkspaceEdit();
		HashMap<String, List<TextEdit>> changes = new HashMap<>();
		List<TextEdit> asList2 = new ArrayList<TextEdit>();
		List<Either<TextDocumentEdit, ResourceOperation>> asList = new ArrayList<Either<TextDocumentEdit, ResourceOperation>>();
		List<SourceCodeModification> sourceCodeModifications = new ArrayList<SourceCodeModification>(sourceChange.insertions);
		for (SourceCodeModification sourceCodeModification : sourceCodeModifications) {
			if (sourceCodeModification instanceof SourceCodeInsertion) {
				TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
				textDocumentEdit.setTextDocument(versionedTextDocumentIdentifier);
				TextEdit textEdit = new TextEdit();
				textEdit.setNewText(sourceCodeModification.content);
				Range range = new Range();
				Position start = new Position(sourceCodeModification.range.begin.line, sourceCodeModification.range.begin.column);
				range.setStart(start);
				range.setEnd(start);
				textEdit.setRange(range);
				asList2.add(textEdit);
				textDocumentEdit.setEdits(asList2);
				asList.add(Either.forLeft(textDocumentEdit));
			} else if (sourceCodeModification instanceof SourceCodeReplace) {
				TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
				textDocumentEdit.setTextDocument(versionedTextDocumentIdentifier);
				TextEdit textEdit = new TextEdit();
				textEdit.setNewText(sourceCodeModification.content);
				Range range = new Range();
				Position start = new Position(sourceCodeModification.range.begin.line, sourceCodeModification.range.begin.column);
				Position end = new Position(sourceCodeModification.range.end.line, sourceCodeModification.range.end.column);
				range.setStart(start);
				range.setEnd(end);
				textEdit.setRange(range);
				asList2.add(textEdit);
				textDocumentEdit.setEdits(asList2);
				asList.add(Either.forLeft(textDocumentEdit));
			}

		}
		changes.put(sourceChange.uri, asList2);
		edit.setDocumentChanges(asList);
		edit.setChanges(changes);
		return edit;
	}

}
