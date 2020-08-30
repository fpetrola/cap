package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaClassBinder.addMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.WorkspaceAwareBinder;
import com.fpetrola.cap.model.binders.implementations.helpers.DefaultJavaClassBinder;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.SourceChange;
import com.github.javaparser.ast.CompilationUnit;

public class DtoMapperGenerator extends DefaultJavaClassBinder<ORMEntityMapping, Void> implements BidirectionalBinder<ORMEntityMapping, Void>, WorkspaceAwareBinder {

	protected List<Function<CompilationUnit, SourceChange>> getModifiers(ORMEntityMapping source, String uri) {
		List<Function<CompilationUnit, SourceChange>> modifiers = new ArrayList<>();
		String className = source.mappedClass.substring(source.mappedClass.lastIndexOf(".") + 1);

		modifiers.add(c -> {
			String convertBody = "{\n\tclassnameDTO dto = new classnameDTO();\n";
			convertBody = convertBody.replaceAll("classname", className);

			for (PropertyMapping p : source.propertyMappings) {
				convertBody += "\tdto.setpropertyName (model.getpropertyName());\n".replaceAll("propertyName", p.propertyName.toUpperCase().charAt(0) + p.propertyName.substring(1));
			}

			convertBody += "}\n";
			return addMethod(c, "convertTo" + className + "DTO", uri, "add convert method", convertBody);
		});
		return modifiers;
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass + "Mapper";
	}
}
