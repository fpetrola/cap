package com.fpetrola.cap.model.source;

import java.util.List;

public interface SourceChangesListener {
	void sourceChange(String resourceUri, List<CodeProposal> changes);

	void fileCreation(String resourceUri, String content);

	String getFileContent(String uri);

	boolean fileExists(String uri);
}
