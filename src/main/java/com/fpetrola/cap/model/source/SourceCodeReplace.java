package com.fpetrola.cap.model.source;

import com.github.javaparser.Range;

public class SourceCodeReplace extends SourceCodeModification {

	public SourceCodeReplace(String content, Range range, String uri) {
		super(uri);
		this.content = content;
		this.range = range;
	}
}
