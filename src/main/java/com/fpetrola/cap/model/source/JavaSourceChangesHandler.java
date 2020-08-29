package com.fpetrola.cap.model.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.utils.SourceRoot;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class JavaSourceChangesHandler {

	private static final String javaSourceFolderBase = "src/main/java";
	private String uri;
	private String workspacePath;
	private JavaParser javaParser;

	public JavaSourceChangesHandler(String workspacePath, String className) {
		this.workspacePath = workspacePath;
		this.javaParser = createJavaParser();
		initWithClassName(className);
	}

	public void addFixAllForNow(String uri, List<SourceChange> sourceChanges) {
		Range range = new Range(new Position(1, 1), new Position(1, 1));
		SourceChange fixAllSourceChange = new SourceChange(uri, range, "fix all");
		for (SourceChange sourceChange : sourceChanges) {
			fixAllSourceChange.insertions.addAll(sourceChange.insertions);
		}

		if (!fixAllSourceChange.insertions.isEmpty())
			sourceChanges.add(fixAllSourceChange);
	}

	public static List<SourceCodeModification> createModifications(String originalSource, String modifiedSource) {
		List<SourceCodeModification> insertions = new ArrayList<>();
		try {
			Patch<String> patch = DiffUtils.diff(getlines(originalSource), getlines(modifiedSource));

			List<Delta<String>> deltas = patch.getDeltas();
			for (Delta<String> delta : deltas) {
				Patch<String> patch2 = new Patch<String>();
				patch2.addDelta(delta);

				List<String> patch3 = DiffUtils.patch(getlines(originalSource), patch2);
				Patch<String> patch4 = DiffUtils.diff(getlines(originalSource), patch3);

				Delta<String> delta2 = patch4.getDeltas().get(0);

				Chunk<String> revised = delta2.getRevised();
				String collect = revised.getLines().stream().collect(Collectors.joining("\n")) + "\n";
				int column = 0;

				int position = delta2.getOriginal().getPosition();
				int position2 = delta2.getRevised().getPosition();
				if (position == position2 && !delta2.getOriginal().getLines().isEmpty()) {
					int length = delta2.getOriginal().getLines().get(0).length();
					Position begin = new Position(revised.getPosition(), 0);
					Position end = new Position(revised.getPosition() + 1, 0);
					final Range range2 = new Range(begin, end);
					String content = revised.getLines().get(0) + "\n";
					insertions.add(new SourceCodeReplace(content, range2));

					Position begin2 = new Position(revised.getPosition() + 1, 0);
					final Range range3 = new Range(begin2, begin2);
					insertions.add(new SourceCodeInsertion(collect.substring(content.length()), range3));
				} else {
					Position begin = new Position(revised.getPosition(), column);
					final Range range2 = new Range(begin, begin);
					insertions.add((SourceCodeModification) new SourceCodeInsertion(collect, range2));
				}
			}
		} catch (PatchFailedException e) {
			throw new RuntimeException(e);
		}

		return insertions;
	}

	static List<String> getlines(String sourceCode) {
		String[] split = sourceCode.split("\\r?\\n");
		return Arrays.asList(split);
	}

	public File initWithClassName(String className) {
		Path mavenModuleRoot = Path.of(getWorkspacePath());
		SourceRoot sourceRoot = new SourceRoot(mavenModuleRoot.resolve(javaSourceFolderBase));
		ParserConfiguration parserConfiguration = sourceRoot.getParserConfiguration();
		parserConfiguration.setLexicalPreservationEnabled(true);
		SourceRoot createSourceRoot = sourceRoot;
		createSourceRoot.setPrinter(LexicalPreservingPrinter::print);

		String completeFilename = mavenModuleRoot + "/" + javaSourceFolderBase + "/" + className.replace(".", "/") + ".java";
		File file = new File(completeFilename);
		uri = getURI(file);
		return file;
	}

	public void addInsertionsFor(List<SourceChange> sourceChanges, List<Function<CompilationUnit, SourceChange>> funcs) {
		try {
			for (Function<CompilationUnit, SourceChange> function : funcs) {
				CompilationUnit compilationUnit = javaParser.parse(createFileFromUri()).getResult().get();
				LexicalPreservingPrinter.setup(compilationUnit);
				String originalSource = LexicalPreservingPrinter.print(compilationUnit);

				SourceChange sourceChange = function.apply(compilationUnit);
				if (sourceChange != null) {
					List<SourceCodeModification> insertions = createModifications(originalSource, LexicalPreservingPrinter.print(compilationUnit));
					sourceChange.insertions = insertions;
					sourceChanges.add(sourceChange);
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private File createFileFromUri() {
		return new File(URI.create(uri));
	}

	private JavaParser createJavaParser() {
		JavaParser javaParser = new JavaParser();
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.setAttributeComments(true);
		parserConfiguration.setIgnoreAnnotationsWhenAttributingComments(false);
		parserConfiguration.setDoNotAssignCommentsPrecedingEmptyLines(false);
		parserConfiguration.setLexicalPreservationEnabled(true);
		return javaParser;
	}

	private String getURI(File file) {
		return file.toURI().toString().replace("file:/", "file:///");
	}

	public String getUri() {
		return uri;
	}

	public String getWorkspacePath() {
		return workspacePath;
	}

	public boolean fileExists() {
		return createFileFromUri().exists();
	}

}