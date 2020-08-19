package com.fpetrola.cap.model.binders;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fpetrola.cap.model.developer.ORMEntityMapping;
import com.fpetrola.cap.model.developer.PropertyMapping;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.utils.SourceRoot;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class JPAEntityMappingWriter {

	private ORMEntityMapping ormEntityMapping;
	private String workspacePath;

	public JPAEntityMappingWriter(ORMEntityMapping ormEntityMapping, String workspacePath) {
		this.ormEntityMapping = ormEntityMapping;
		this.workspacePath = workspacePath;
	}

	boolean modified;
	public static Map<String, List<SourceChange>> ranges = new HashMap<>();
	private File file;

	public void write() {
		try {
			String className = ormEntityMapping.mappedClass.getSimpleName();

			// Path mavenModuleRoot =
			// CodeGenerationUtils.mavenModuleRoot(JPAEntityMappingWriter.class);
			Path mavenModuleRoot = Path.of(workspacePath);
			SourceRoot sourceRoot = createSourceRoot(mavenModuleRoot);
			String name = ormEntityMapping.mappedClass.getPackage().getName();
			String filename = className + ".java";
			String completeFilename = mavenModuleRoot + "/src/main/java/" + name.replace(".", "/") + "/" + filename;
			file = new File(completeFilename);
			CompilationUnit cu = sourceRoot.parse(name, filename);

			JavaParser javaParser = new JavaParser();
			ParserConfiguration parserConfiguration = new ParserConfiguration();
			parserConfiguration.setAttributeComments(true);
			parserConfiguration.setIgnoreAnnotationsWhenAttributingComments(false);
			parserConfiguration.setDoNotAssignCommentsPrecedingEmptyLines(false);
			parserConfiguration.setLexicalPreservationEnabled(true);

			// I used the same setup to read the file in from harddrive worked fine
			CompilationUnit cu3 = javaParser.parse(new File(completeFilename)).getResult().get();
			CompilationUnit cu4 = javaParser.parse(new File(completeFilename)).getResult().get();

			LexicalPreservingPrinter.setup(cu3);
			String print1 = LexicalPreservingPrinter.print(cu3);

			Map<String, List<SourceChange>> ranges1 = addAnnotations(className, cu3);
			processModified(sourceRoot, cu3, print1, className, ranges1);

			sourceRoot = createSourceRoot(mavenModuleRoot);
//		CompilationUnit cu2 = sourceRoot.parse(ormEntityMapping.mappedClass.getPackage().getName(), filename);
			LexicalPreservingPrinter.setup(cu4);
			Map<String, List<SourceChange>> ranges2 = addAnnotations2(className, cu4);
			processModified(sourceRoot, cu4, print1, className, ranges2);

			ranges.put(className, new ArrayList<>());
			ranges.get(className).addAll(ranges1.get(className));
			ranges.get(className).addAll(ranges2.get(className));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SourceRoot createSourceRoot(Path mavenModuleRoot) {
		SourceRoot sourceRoot = new SourceRoot(mavenModuleRoot.resolve("src/main/java"));
		ParserConfiguration parserConfiguration = sourceRoot.getParserConfiguration();
		parserConfiguration.setLexicalPreservationEnabled(true);
		return sourceRoot;
	}

	private void setObserver() {
		try {
			Field field = LexicalPreservingPrinter.class.getDeclaredField("observer");
			field.setAccessible(true);
			field.set(null, null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Map<String, List<SourceChange>> processModified(SourceRoot sourceRoot, CompilationUnit cu, String print1, String className, Map<String, List<SourceChange>> ranges) {
		String print2 = LexicalPreservingPrinter.print(cu);
		if (modified) {
			sourceRoot.setPrinter(LexicalPreservingPrinter::print);

			for (Entry<String, List<SourceChange>> entry : ranges.entrySet()) {
				for (SourceChange sourceChange : entry.getValue()) {
					Patch<String> patch = DiffUtils.diff(lines(print1), lines(print2));
					List<Delta<String>> deltas = patch.getDeltas();
					for (Delta<String> delta : deltas) {
						Chunk<String> revised = deltas.get(0).getRevised();
						String collect = revised.getLines().stream().collect(Collectors.joining("\n")) + "\n";
						Position begin = new Position(revised.getPosition(), 0);
						sourceChange.range = new Range(begin, begin);
						sourceChange.setCode(collect);
					}
				}
			}
//			sourceRoot.saveAll(mavenModuleRoot.resolve(Paths.get("src/main/java")));
		}

		return ranges;
	}

	private Map<String, List<SourceChange>> addAnnotations(String className, CompilationUnit cu) {
		Map<String, List<SourceChange>> ranges = createRanges(className);
		cu.accept(new ModifierVisitor<Void>() {

			@Override
			public Visitable visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
				if (!classDeclaration.isAnnotationPresent(Entity.class)) {
					classDeclaration.addAnnotation(Entity.class);

					List<SimpleName> simpleNames = classDeclaration.findAll(SimpleName.class);

					modified = true;
					addRangeFor(className, simpleNames.get(0).getRange().get(), "JPA Entity detected", ranges);
				}

				if (ormEntityMapping.tableName != null) {
					NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) classDeclaration.getAnnotationByClass(Table.class).orElseGet(() -> null);
					if (annotationExpr == null) {
						annotationExpr = classDeclaration.addAndGetAnnotation(Table.class);
						annotationExpr.addPair("name", new StringLiteralExpr(ormEntityMapping.tableName));
						modified = true;
					}
				}

				return super.visit(classDeclaration, arg);
			}
		}, null);

		return ranges;
	}

	private Map<String, List<SourceChange>> createRanges(String className) {
		Map<String, List<SourceChange>> ranges = new HashMap<>();
		ranges.put(className, new ArrayList<>());
		return ranges;
	}

	private List<String> lines(String print1) {
		String[] split = print1.split("\\r?\\n");
		return Arrays.asList(split);
	}

	private Map<String, List<SourceChange>> addAnnotations2(String className, CompilationUnit cu) {
		Map<String, List<SourceChange>> ranges = createRanges(className);
		cu.accept(new ModifierVisitor<Void>() {

			@Override
			public Visitable visit(FieldDeclaration fieldDeclaration, Void arg) {

				for (PropertyMapping propertyMapping : ormEntityMapping.propertyMappings) {

					SimpleName name = fieldDeclaration.getVariable(0).getName();

					if (name.toString().equals(propertyMapping.propertyName)) {

						if (propertyMapping.propertyMappingType != null) {
							if (!fieldDeclaration.isAnnotationPresent(ManyToOne.class)) {
								fieldDeclaration.addAnnotation(ManyToOne.class);
								modified = true;
								addRangeFor(className, fieldDeclaration.getRange().get(), "column mapping detected", ranges);
							}
						}

						if (propertyMapping.columnName != null) {
							if (!fieldDeclaration.isAnnotationPresent(JoinColumn.class)) {
								NormalAnnotationExpr annotation = fieldDeclaration.addAndGetAnnotation(JoinColumn.class);
								annotation.addPair("name", new StringLiteralExpr(propertyMapping.columnName));
							}
						}
					}
				}
				return super.visit(fieldDeclaration, arg);
			}
		}, null);

		return ranges;
	}

	protected void addRangeFor(String className, Range range, String message, Map<String, List<SourceChange>> ranges) {
		if (!ranges.containsKey(className))
			ranges.put(className, new ArrayList<>());
		ranges.get(className).add(new SourceChange(file, range, message));
	}

}
