package com.fpetrola.cap.languageserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WillSaveTextDocumentParams;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

public class CapTextDocumentService implements TextDocumentService {

	private final CapLanguageServer languageServer;

	public CapTextDocumentService(CapLanguageServer chamrousseLanguageServer) {
		this.languageServer = chamrousseLanguageServer;
	}

	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> null);
	}

	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		return null;
	}

	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> {
			return null;
		});
	}

	private Either<String, MarkedString> getHoverContent(String type) {
		return Either.forLeft(type);
	}

	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		return null;
	}

	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> null);

	}

	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return CompletableFuture.supplyAsync(() -> null);
	}

	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		return null;
	}

	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
		return CompletableFuture.supplyAsync(() -> null);
	}

	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		Map<String, List<CodeAction>> list = languageServer.diagnosticGenerator.getCodeActions();

		List<Either<Command, CodeAction>> result = new ArrayList<>();

		List<CodeAction> value = list.get(params.getTextDocument().getUri());
		if (value != null) {
			List<Either<Command, CodeAction>> collect = value.stream().map((codeAction) -> {
				Diagnostic diagnostic = codeAction.getDiagnostics().get(0);
				Diagnostic obj = params.getContext().getDiagnostics().get(0);
				if (diagnostic.getRange().equals(obj.getRange())) {
					codeAction.setDiagnostics(params.getContext().getDiagnostics());
					Either<Command, CodeAction> forRight = Either.forRight(codeAction);
					return forRight;
				} else
					return null;
			}).filter(e -> e != null).collect(Collectors.toList());
			result.addAll(collect);

			// List<Either<Command, CodeAction>> collect = Stream.of(1, 2, 3).map((i) -> {
//			CodeAction codeAction = new CodeAction("hola" + i);
//			codeAction.setCommand(new Command("command1", "view1"));
//			Either<Command, CodeAction> forRight = Either.forRight(codeAction);
//			return forRight;
//
//		}).collect(Collectors.toList());
		}

		CompletableFuture<List<Either<Command, CodeAction>>> supplyAsync = CompletableFuture.completedFuture(result);

		return supplyAsync;
	}

	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {

		List<CodeLens> collect = Stream.of(1, 2, 3).map((i) -> {
			CodeLens codeLens = new CodeLens();
			return codeLens;

		}).collect(Collectors.toList());

		CompletableFuture<List<? extends CodeLens>> supplyAsync = CompletableFuture.supplyAsync(() -> collect);

		return supplyAsync;
	}

	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return null;
	}

	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return null;
	}

	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return null;
	}

	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		return null;
	}

	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return null;
	}

	public void didOpen(DidOpenTextDocumentParams params) {

		String uri = params.getTextDocument().getUri();

		initBindingApp(uri);

		TextDocumentItem textDocument = params.getTextDocument();

		sendDiagnostics(new VersionedTextDocumentIdentifier(textDocument.getUri(), 0));
	}

	private String initBindingApp(String uri) {
		if (uri.endsWith("cap-config.yml"))
			languageServer.bindingApp.setConfigUri(uri);

		return uri;
	}

	public void sendDiagnostics(VersionedTextDocumentIdentifier versionedTextDocumentIdentifier) {
		List<PublishDiagnosticsParams> publish = languageServer.diagnosticGenerator.getDiagnostics(languageServer.getSourceChangesListener().getRanges(), languageServer.bindingApp);
		for (PublishDiagnosticsParams publishDiagnosticsParams : publish) {
			languageServer.client.publishDiagnostics(publishDiagnosticsParams);
		}
	}

	private void editFile() {
		ApplyWorkspaceEditParams params2 = new ApplyWorkspaceEditParams();
		WorkspaceEdit edit = new WorkspaceEdit();
		TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
		VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier();
		textDocument.setUri("uri:///home/fernando/Documents/2aaa.java");
		textDocument.setVersion(0);
		textDocumentEdit.setTextDocument(textDocument);
		TextEdit textEdit = new TextEdit();
		textEdit.setNewText("hola mundo!!!!");
		Range range = new Range();
		range.setStart(new Position(10, 3));
		range.setEnd(new Position(10, 17));
		textEdit.setRange(range);
		textDocumentEdit.setEdits(Arrays.asList(textEdit));
		edit.setDocumentChanges(Arrays.asList(Either.forLeft(textDocumentEdit)));
		params2.setEdit(edit);
		languageServer.client.applyEdit(params2);
	}

	private void sendMessage() {
		MessageActionItem messageActionItemSi = new MessageActionItem("si");
		MessageActionItem messageActionItemNo = new MessageActionItem("no");
		ShowMessageRequestParams requestParams = new ShowMessageRequestParams(Arrays.asList(messageActionItemSi, messageActionItemNo));
		languageServer.client.showMessageRequest(requestParams);
	}

	public void didChange(DidChangeTextDocumentParams params) {
//		DocumentModel model = new DocumentModel(params.getContentChanges().get(0).getText());
//		this.docs.put(params.getTextDocument().getUri(), model);
		// send notification

//		sendDiagnostics(params.getTextDocument());
//
//		CompletableFuture.runAsync(() -> languageServer.client.publishDiagnostics(new PublishDiagnosticsParams(params.getTextDocument().getUri(), validate(model))));
	}

	public void didClose(DidCloseTextDocumentParams params) {
	}

	@Override
	public void willSave(WillSaveTextDocumentParams params) {
		TextDocumentIdentifier textDocument = params.getTextDocument();
//		sendDiagnostics(new VersionedTextDocumentIdentifier(textDocument.getUri(), 0));
	}

	public void didSave(DidSaveTextDocumentParams params) {

		String uri = params.getTextDocument().getUri();

		initBindingApp(uri);

		TextDocumentIdentifier textDocument = params.getTextDocument();

//		DocumentModel model = new DocumentModel(params.getText());
//		this.docs.put(params.getTextDocument().getUri(), model);

		sendDiagnostics(new VersionedTextDocumentIdentifier(textDocument.getUri(), null));
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		CompletableFuture<Hover> supplyAsync = CompletableFuture.supplyAsync(() -> {
			Hover hover = new Hover();
			MarkupContent contents = new MarkupContent();
			contents.setKind("");
			contents.setValue("");
			hover.setContents(contents);
			hover = null;
			return hover;
		});
		return supplyAsync;
	}
}
