package com.fpetrola.cap.model.source;

import java.util.ArrayList;
import java.util.List;

public class SourceChange {

	private String uri;
	private List<SourceCodeModification> insertions = new ArrayList<SourceCodeModification>();

	public SourceChange(String uri) {
		this.uri = uri;
	}

	public void addInsertion(SourceCodeInsertion sourceCodeInsertion) {

		insertions.add(sourceCodeInsertion);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public List<SourceCodeModification> getInsertions() {
		return insertions;
	}

	public void setInsertions(List<SourceCodeModification> insertions) {
		this.insertions = insertions;
	}
}
