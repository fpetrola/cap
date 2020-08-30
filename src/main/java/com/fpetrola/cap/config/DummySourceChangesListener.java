package com.fpetrola.cap.config;

import java.util.List;

import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;

final class DummySourceChangesListener implements SourceChangesListener {
	public void fileCreation(String resourceUri, String content) {
	}

	public void sourceChange(String resourceUri, List<SourceChange> changes) {
	}
}