package com.fpetrola.cap.model.source;

import com.github.javaparser.Range;

public class SourceCodeInsertion extends SourceCodeModification {

	public SourceCodeInsertion(String content, Range range, String uri) {
		super(uri);
		this.content = content;
		this.range = range;
	}
}
