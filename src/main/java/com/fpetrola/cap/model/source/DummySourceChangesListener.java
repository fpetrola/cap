package com.fpetrola.cap.model.source;

import java.util.List;

public class DummySourceChangesListener implements SourceChangesListener {
	public void fileCreation(String resourceUri, String content) {
	}

	public void sourceChange(String resourceUri, List<CodeProposal> changes) {
	}

	public String getFileContent(String uri) {
		return "";
	}

	public boolean fileExists(String uri) {
		return false;
	}
}