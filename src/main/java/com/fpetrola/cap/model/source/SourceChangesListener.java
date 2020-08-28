package com.fpetrola.cap.model.source;

import java.util.List;

public interface SourceChangesListener {
	void sourceChange(String resourceUri, List<SourceChange> changes);

	void fileCreation(String resourceUri, String content);
}
