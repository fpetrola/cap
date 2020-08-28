package com.fpetrola.cap.languageserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fpetrola.cap.model.source.SourceChange;
import com.fpetrola.cap.model.source.SourceChangesListener;

public class SourceChangesListenerImpl implements SourceChangesListener {

	private Map<String, List<SourceChange>> ranges = new HashMap<>();

	@Override
	public void sourceChange(String resourceUri, List<SourceChange> changes) {
		ranges.put(resourceUri, new ArrayList<>());
		ranges.get(resourceUri).addAll(changes);
	}

	@Override
	public void fileCreation(String resourceUri, String content) {
	}

	public Map<String, List<SourceChange>> getRanges() {
		return ranges;
	}
}
