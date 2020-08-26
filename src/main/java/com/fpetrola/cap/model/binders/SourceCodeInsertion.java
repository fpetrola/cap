package com.fpetrola.cap.model.binders;

import com.github.javaparser.Range;

public class SourceCodeInsertion {

	public String content;
	public Range range;

	public SourceCodeInsertion(String content, Range range) {
		this.content = content;
		this.range = range;
	}
}
