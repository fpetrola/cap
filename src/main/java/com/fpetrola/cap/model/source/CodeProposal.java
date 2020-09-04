package com.fpetrola.cap.model.source;

import com.github.javaparser.Range;

public class CodeProposal {

	private String message;
	private Range problemRange;
	private SourceChange sourceChange;
	private String uri;

	public CodeProposal(String uri, Range range, String message) {
		this.uri = uri;
		this.problemRange = range;
		this.message = message;
		this.sourceChange = new SourceChange(uri);
	}

	public String getMessage() {
		return message;
	}

	public Range getProblemRange() {
		return problemRange;
	}

	public SourceChange getSourceChange() {
		return sourceChange;
	}

	public String getUri() {
		return uri;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setProblemRange(Range problemRange) {
		this.problemRange = problemRange;
	}

	public void setSourceChange(SourceChange sourceChange) {
		this.sourceChange = sourceChange;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
