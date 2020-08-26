package com.fpetrola.cap.model.binders;

import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.developer.JPAEntity;
import com.fpetrola.cap.model.developer.ORMEntityMapping;

public class JPAEntityBinder extends DefaultBinder implements BidirectionalBinder<ORMEntityMapping, Object> {

	public String workspacePath;

	@Override
	public String toString() {
		return "JPABinder []";
	}

	@Override
	public List<Object> pull(ORMEntityMapping source) {
		final JPAEntityMappingWriter jpaEntityMappingWriter = new JPAEntityMappingWriter(source, workspacePath, getSourceChangesListener());
		jpaEntityMappingWriter.write();
		return Arrays.asList(new JPAEntity(source));
	}

}
