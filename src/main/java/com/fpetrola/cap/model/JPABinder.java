package com.fpetrola.cap.model;

import java.util.Arrays;
import java.util.List;

public class JPABinder implements BidirectionalBinder<Object, ORMEntityMapping> {

	@Override
	public String toString() {
		return "JPABinder []";
	}

	@Override
	public List<Object> pull(ORMEntityMapping source) {
		new JPAEntityMappingWriter(source).write();
		return Arrays.asList(new JPAEntity(source));
	}

}
