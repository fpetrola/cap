package com.fpetrola.cap.model.source;

import com.github.javaparser.Range;

public class SourceCodeModification {

	public String content;
	public Range range;
	private String uri;

	public SourceCodeModification(String uri) {
		this.setUri(uri);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}