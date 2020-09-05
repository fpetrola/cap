package com.fpetrola.cap.helpers;

import com.github.javaparser.ast.CompilationUnit;

public interface Provider<T> {

	T get();

	CompilationUnit createNew();

}