package com.fpetrola.cap.languageserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersOptions;
import org.eclipse.lsp4j.WorkspaceServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.fpetrola.cap.config.BindingApp;

public class CapLanguageServer implements LanguageServer {

	private TextDocumentService textService;
	private WorkspaceService workspaceService;
	LanguageClient client;
	private FileWriter fileWriter;
	public BindingApp bindingApp;
	private SourceChangesListenerImpl sourceChangesListener;
	public DiagnosticGenerator diagnosticGenerator = new DiagnosticGenerator();

	public CapLanguageServer() {
		try {
			fileWriter = new FileWriter(new File("/home/fernando/git/LSP4J_Tutorial/server/logs.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textService = new CapTextDocumentService(this);
		workspaceService = new CapWorkspaceService(this);
		log("init");

		sourceChangesListener = new SourceChangesListenerImpl() {

			public void fileCreation(String uri, String content) {
				try {
					File file = new File(URI.create(uri));
					file.getParentFile().mkdirs();
					FileWriter fileWriter = new FileWriter(file);
					fileWriter.write(content);
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				diagnosticGenerator.createFileAtClient(uri, "", client);
			}
		};

		bindingApp = new BindingApp(getSourceChangesListener());

	}

	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		final InitializeResult res = new InitializeResult(new ServerCapabilities());
		res.getCapabilities().setCodeActionProvider(Boolean.TRUE);
		CodeActionOptions codeActionProvider = new CodeActionOptions();
		codeActionProvider.setCodeActionKinds(Arrays.asList(CodeActionKind.Source));
		res.getCapabilities().setCodeActionProvider(codeActionProvider);
		res.getCapabilities().setCompletionProvider(new CompletionOptions());
		res.getCapabilities().setDefinitionProvider(Boolean.TRUE);
		res.getCapabilities().setHoverProvider(Boolean.TRUE);
		res.getCapabilities().setReferencesProvider(Boolean.TRUE);
		res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
		TextDocumentSyncOptions textDocumentSync = new TextDocumentSyncOptions();
		TextDocumentSyncKind te = TextDocumentSyncKind.Full;
		textDocumentSync.setChange(te);
		textDocumentSync.setSave(new SaveOptions(true));
		textDocumentSync.setWillSave(true);
		textDocumentSync.setOpenClose(true);
//		textDocumentSync.setWillSaveWaitUntil(false);
		res.getCapabilities().setTextDocumentSync(textDocumentSync);
		res.getCapabilities().setDocumentSymbolProvider(Boolean.TRUE);
		WorkspaceServerCapabilities workspace = new WorkspaceServerCapabilities();
		WorkspaceFoldersOptions workspaceFolders = new WorkspaceFoldersOptions();
		workspaceFolders.setChangeNotifications(true);
		workspaceFolders.setSupported(true);
		workspace.setWorkspaceFolders(workspaceFolders);
		res.getCapabilities().setWorkspace(workspace);

//		WorkspaceEditCapabilities workspaceEdit= new WorkspaceEditCapabilities();
//		workspaceEdit.setResourceOperations(Arrays.asList(ResourceOperationKind.Create, ResourceOperationKind.Delete, ResourceOperationKind.Rename));
//		params.getCapabilities().getWorkspace().setWorkspaceEdit(workspaceEdit);

		return CompletableFuture.completedFuture(res);
	}

	public void getWorkspaceFolders() {
		CompletableFuture<List<WorkspaceFolder>> workspaceFolders = client.workspaceFolders();

		workspaceFolders.thenRunAsync(new Runnable() {

			public void run() {
				List<WorkspaceFolder> list;
				try {
					list = workspaceFolders.get();
					list.isEmpty();
					log(list.toString());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.supplyAsync(() -> Boolean.TRUE);
	}

	public void exit() {
	}

	public TextDocumentService getTextDocumentService() {
		log("getTextDocumentService");

//		getWorkspaceFolders();
		return this.textService;
	}

	public WorkspaceService getWorkspaceService() {
		return this.workspaceService;
	}

	public void setRemoteProxy(LanguageClient remoteProxy) {
		this.client = remoteProxy;
	}

	public void log(String toString) {
		try {
			fileWriter.write(toString);
			fileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SourceChangesListenerImpl getSourceChangesListener() {
		return sourceChangesListener;
	}

}
