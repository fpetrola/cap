package com.fpetrola.cap.model.binders.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.Property;
import com.fpetrola.cap.model.source.JavaClassBinder;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;

public class JavaEntityGenerator extends DefaultBinder implements BidirectionalBinder<EntityModel, Object>, WorkspaceAwareBinder {

	public JavaEntityGenerator() {
	}

	public List<Object> pull(EntityModel source) {
		if (workspacePath != null && sourceChangesListener != null) {
			String className = "com.fpetrola.cap.usermodel." + source.name;

			JavaSourceChangesHandler javaSourceChangesHandler = new JavaSourceChangesHandler(getWorkspacePath(), className);
			String uri = javaSourceChangesHandler.getUri();
			JavaClassBinder javaClassBinder = new JavaClassBinder(uri);

			try {
				if (javaSourceChangesHandler.fileExists()) {
					List<SourceChange> sourceChanges = new ArrayList<>();

					for (Property property : source.properties) {

						String message = "add property: " + property.name;
						javaSourceChangesHandler.addInsertionsFor(sourceChanges, cu1 -> javaClassBinder.addFieldIfNotExists(cu1, message, property.name, property.typeName));
					}

					javaSourceChangesHandler.addFixAllForNow(uri, sourceChanges);
					sourceChangesListener.sourceChange(uri, sourceChanges);
				} else {
					sourceChangesListener.fileCreation(uri, javaClassBinder.createNewJavaClassContent(className));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return new ArrayList<>();
	}
}
