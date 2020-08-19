package com.fpetrola.cap.model.binders;

import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.developer.JPAEntity;
import com.fpetrola.cap.model.developer.ORMEntityMapping;

public class JPAEntityBinder implements BidirectionalBinder<ORMEntityMapping, Object> {

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
