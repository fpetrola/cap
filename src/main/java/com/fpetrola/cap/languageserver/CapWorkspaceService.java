package com.fpetrola.cap.languageserver;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class CapWorkspaceService implements WorkspaceService {

	private CapLanguageServer capLanguageServer;

	public CapWorkspaceService(CapLanguageServer capLanguageServer) {
		this.capLanguageServer = capLanguageServer;
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		capLanguageServer.log("symbol");
		return null;
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		capLanguageServer.log("didChangeConfiguration");
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		capLanguageServer.log("didChangeWatchedFiles");
	}
	
	public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
		WorkspaceService.super.didChangeWorkspaceFolders(params);
	}

}
