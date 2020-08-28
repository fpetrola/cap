package com.fpetrola.cap.model.source;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.Range;

public class SourceChange {

	public String message;
	public Range problemRange;
	public String uri;
	public List<SourceCodeModification> insertions = new ArrayList<SourceCodeModification>();

	public SourceChange(String uri, Range range, String message) {
		this.uri = uri;
		this.problemRange = range;
		this.message = message;
	}

	public void addInsertion(SourceCodeInsertion sourceCodeInsertion) {

		insertions.add(sourceCodeInsertion);
	}
}
