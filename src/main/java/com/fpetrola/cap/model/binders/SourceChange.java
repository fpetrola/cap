package com.fpetrola.cap.model.binders;

import java.io.File;

import com.github.javaparser.Range;

public class SourceChange {

	public Range range;
	public String message;
	public String code;
	public Range problemRange;
	public File file;

	public SourceChange(File file, Range range, String message) {
		this.file = file;
		problemRange = range;
		this.range = range;
		this.message = message;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
