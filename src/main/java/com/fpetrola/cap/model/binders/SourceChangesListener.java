package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceChangesListener {

	public Map<String, List<SourceChange>> ranges = new HashMap<>();

	public void newSourceChanges(String resource, List<SourceChange> changes) {
		ranges.put(resource, new ArrayList<>());
		ranges.get(resource).addAll(changes);
	}

	public void fileCreation(String mappedClass, String content) {
		// TODO Auto-generated method stub
		
	}
}
