package com.fpetrola.cap.model.binders;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.Range;

public class SourceChange {

	public String message;
	public Range problemRange;
	public String uri;
	public List<SourceCodeInsertion> insertions = new ArrayList<SourceCodeInsertion>();

	public SourceChange(String uri, Range range, String message) {
		this.uri = uri;
		this.problemRange = range;
		this.message = message;
	}

	public void addInsertion(SourceCodeInsertion sourceCodeInsertion) {

		insertions.add(sourceCodeInsertion);
	}
}
