package com.fpetrola.cap.model.binders.implementations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.JPAEntity;
import com.fpetrola.cap.model.developer.ORMEntityMapping;

public class JPAEntityBinder extends DefaultBinder implements BidirectionalBinder<ORMEntityMapping, Object> {

	public String workspacePath;

	public JPAEntityBinder() {
	}

	@Override
	public String toString() {
		return "JPABinder []";
	}

	@Override
	public List<Object> pull(ORMEntityMapping source) {
		List<Object> result = new ArrayList<Object>();

		try {
			final JPAEntityMappingWriter jpaEntityMappingWriter = new JPAEntityMappingWriter(source, getWorkspacePath(), getSourceChangesListener());
			jpaEntityMappingWriter.write();
			result.add(new JPAEntity(source));
		} catch (Exception e) {
		}
		return result;
	}

	public String getWorkspacePath() {
		return workspacePath;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}

}
