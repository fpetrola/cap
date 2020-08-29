package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaClassBinder.addFieldIfNotExists;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
import com.fpetrola.cap.model.binders.implementations.helpers.DefaultJavaClassBinder;
import com.fpetrola.cap.model.developer.EntityModel;
import com.fpetrola.cap.model.developer.Property;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.ast.CompilationUnit;

public class DTOGenerator extends DefaultJavaClassBinder<EntityModel, Object> implements BidirectionalBinder<EntityModel, Object>, WorkspaceAwareBinder {

	protected List<Function<CompilationUnit, SourceChange>> getModifiers(EntityModel source, String uri) {
		List<Function<CompilationUnit, SourceChange>> modifiers = new ArrayList<>();

		for (Property property : source.properties)
			modifiers.add(cu1 -> addFieldIfNotExists(cu1, "add bean property: " + property.name, property.name, property.typeName, uri));

		return modifiers;
	}

	protected String getClassname(EntityModel source) {
		return "com.fpetrola.cap.usermodel." + source.name.toUpperCase().charAt(0) + source.name.substring(1) + "DTO";
	}
}
