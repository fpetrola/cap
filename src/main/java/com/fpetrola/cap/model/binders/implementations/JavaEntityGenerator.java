package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaClassBinder.addFieldIfNotExists;
import static com.fpetrola.cap.model.source.JavaClassBinder.createNewJavaClassContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.DefaultBinder;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.Property;
import com.fpetrola.cap.model.source.JavaSourceChangesHandler;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.ast.CompilationUnit;

public class JavaEntityGenerator extends DefaultBinder implements BidirectionalBinder<EntityModel, Object>, WorkspaceAwareBinder {

	public JavaEntityGenerator() {
	}

	public List<Object> pull(EntityModel source) {
		if (workspacePath != null && sourceChangesListener != null) {

			String className = "com.fpetrola.cap.usermodel." + source.name;

			JavaSourceChangesHandler javaSourceChangesHandler = new JavaSourceChangesHandler(getWorkspacePath(), className);
			String uri = javaSourceChangesHandler.getUri();

			List<Function<CompilationUnit, SourceChange>> modifiers = new ArrayList<>();

			if (javaSourceChangesHandler.fileExists()) {
				List<SourceChange> sourceChanges = new ArrayList<>();

				for (Property property : source.properties)
					modifiers.add(cu1 -> addFieldIfNotExists(cu1, "add bean property: " + property.name, property.name, property.typeName, uri));

				javaSourceChangesHandler.addInsertionsFor(sourceChanges, modifiers);
				javaSourceChangesHandler.addFixAllForNow(uri, sourceChanges);
				sourceChangesListener.sourceChange(uri, sourceChanges);
			} else
				sourceChangesListener.fileCreation(uri, createNewJavaClassContent(className));

		}

		return Arrays.asList();
	}
}
