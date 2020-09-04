package com.fpetrola.cap.model.binders.implementations;

import static com.fpetrola.cap.model.source.JavaparserHelper.addMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fpetrola.cap.model.binders.Binder;
import com.fpetrola.cap.model.binders.SourceCodeChanger;
import com.fpetrola.cap.model.binders.implementations.helpers.BaseJavaClassBinder;
import com.fpetrola.cap.model.binders.implementations.java.JavaClassModel;
import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.fpetrola.cap.model.source.CodeProposal;
import com.github.javaparser.ast.CompilationUnit;

public class DtoMapperGenerator extends BaseJavaClassBinder<ORMEntityMapping, Void> implements Binder<ORMEntityMapping, Void> {

	protected List<Function<CompilationUnit, CodeProposal>> getModifiers(ORMEntityMapping source, String uri) {
		List<Function<CompilationUnit, CodeProposal>> modifiers = new ArrayList<>();
		String className = source.mappedClass.substring(source.mappedClass.lastIndexOf(".") + 1);

		modifiers.add(c -> {
			String convertBody = "{\n\tclassnameDTO dto = new classnameDTO();\n";
			convertBody = convertBody.replaceAll("classname", className);

			for (PropertyMapping p : source.getPropertyMappings()) {
				convertBody += "\tdto.setpropertyName (model.getpropertyName());\n".replaceAll("propertyName", p.getPropertyName().toUpperCase().charAt(0) + p.getPropertyName().substring(1));
			}

			convertBody += "}\n";
			return addMethod(c, "convertTo" + className + "DTO", uri, "add convert method", convertBody);
		});
		return modifiers;
	}

	protected String getClassname(ORMEntityMapping source) {
		return source.mappedClass + "Mapper";
	}

	@Override
	public void computeChanges(ORMEntityMapping ormEntityMapping, JavaClassModel javaClassModel, SourceCodeChanger sourceCodeChanger) {
		// TODO Auto-generated method stub
		
	}
}
