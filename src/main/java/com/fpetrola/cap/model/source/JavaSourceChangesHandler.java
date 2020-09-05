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

import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;
import difflib.PatchFailedException;

public class JavaSourceChangesHandler {

	public static final String javaSourceFolderBase = "src/main/java";
	private String workspacePath;
	private JavaParser javaParser;

	public JavaSourceChangesHandler(String workspacePath) {
		this.workspacePath = workspacePath;
		this.javaParser = createJavaParser();
		initWithClassName();
	}

	public String addFixAllForNow(List<CodeProposal> sourceChanges, List<Function<CompilationUnit, SourceChange>> funcs, String uri, String content) {
		try {
			CompilationUnit compilationUnit;
			if (content == null)
				compilationUnit = javaParser.parse(createFileFromUri(uri)).getResult().get();
			else
				compilationUnit = javaParser.parse(content).getResult().get();

			LexicalPreservingPrinter.setup(compilationUnit);
			String originalSource = LexicalPreservingPrinter.print(compilationUnit);

			boolean solved = true;
			for (Function<CompilationUnit, SourceChange> function : funcs) {
				SourceChange apply = function.apply(compilationUnit);
				solved &= apply == null;
			}

			String originalSource2 = LexicalPreservingPrinter.print(compilationUnit);
			CompilationUnit compilationUnit2 = javaParser.parse(originalSource2).getResult().get();
			LexicalPreservingPrinter.setup(compilationUnit2);

			for (Function<CompilationUnit, SourceChange> function : funcs) {
				SourceChange apply = function.apply(compilationUnit2);
				solved &= apply == null;
			}

			Range range = new Range(new Position(1, 1), new Position(1, 1));
			CodeProposal codeProposal = new CodeProposal(uri, range, "fix all");

			String print = LexicalPreservingPrinter.print(compilationUnit2);
			if (!solved && codeProposal != null) {
				List<SourceCodeModification> insertions = createModifications(originalSource, print);
				codeProposal.getSourceChange().setInsertions(insertions);
				;
				sourceChanges.add(codeProposal);
			}

			return print;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<SourceCodeModification> createModificationsForUri(String originalSource, String modifiedSource, String uri2) {
		List<SourceCodeModification> insertions = new ArrayList<>();
		try {
			Patch<String> patch = DiffUtils.diff(getlines(originalSource), getlines(modifiedSource));
			List<Delta<String>> deltas = patch.getDeltas();
			for (Delta<String> delta : deltas) {
				Patch<String> patch2 = new Patch<String>();
				patch2.addDelta(delta);
				List<Delta<String>> delta2 = DiffUtils.diff(getlines(originalSource), DiffUtils.patch(getlines(originalSource), patch2)).getDeltas();
				convertDeltasToSourceChange(insertions, delta2, uri2);
			}
		} catch (PatchFailedException e) {
			throw new RuntimeException(e);
		}

		return insertions;
	}

	public static List<SourceCodeModification> createModifications(String originalSource, String modifiedSource) {
		return createModificationsForUri(originalSource, modifiedSource, null);
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private static void convertDeltasToSourceChange(List<SourceCodeModification> insertions, List<Delta<String>> deltas, String uri2) {

		for (Delta<String> delta : deltas) {

			Chunk<String> revised = delta.getRevised();
			String collect = revised.getLines().stream().collect(Collectors.joining("\n")) + "\n";

			if (delta instanceof DeleteDelta) {
				DeleteDelta<String> deleteDelta = (DeleteDelta<String>) delta;
				Position begin = new Position(revised.getPosition(), 0);
				Position end = new Position(revised.getPosition() + revised.size() + 1, 0);
				final Range range2 = new Range(begin, end);
				insertions.add((SourceCodeModification) new SourceCodeReplace("", range2, uri2));

			} else if (delta instanceof InsertDelta) {
				Position begin = new Position(revised.getPosition(), 0);
				final Range range2 = new Range(begin, begin);
				insertions.add((SourceCodeModification) new SourceCodeInsertion(collect, range2, uri2));

			} else if (delta instanceof ChangeDelta) {

				ChangeDelta changeDelta = (ChangeDelta) delta;
				int position = delta.getOriginal().getPosition();
				int position2 = delta.getRevised().getPosition();
				if (position == position2 && !delta.getOriginal().getLines().isEmpty()) {
					int length = delta.getOriginal().getLines().get(0).length();
					Position begin = new Position(revised.getPosition(), 0);
					Position end = new Position(revised.getPosition() + 1, 0);
					final Range range2 = new Range(begin, end);
					String content = revised.getLines().get(0) + "\n";
					insertions.add(new SourceCodeReplace(content, range2, uri2));

					Position begin2 = new Position(revised.getPosition() + 1, 0);
					final Range range3 = new Range(begin2, begin2);
					insertions.add(new SourceCodeInsertion(collect.substring(content.length()), range3, uri2));
				}
			}
		}
	}

	static List<String> getlines(String sourceCode) {
		String[] split = sourceCode.split("\\r?\\n");
		return Arrays.asList(split);
	}

	public void initWithClassName() {
		Path workspacePath = new File(getWorkspacePath()).toPath();
		SourceRoot sourceRoot = new SourceRoot(workspacePath.resolve(javaSourceFolderBase));
		ParserConfiguration parserConfiguration = sourceRoot.getParserConfiguration();
		parserConfiguration.setLexicalPreservationEnabled(true);
		SourceRoot createSourceRoot = sourceRoot;
		createSourceRoot.setPrinter(LexicalPreservingPrinter::print);
	}

	public void addInsertionsFor(List<CodeProposal> sourceChanges, List<Function<CompilationUnit, CodeProposal>> funcs, String uri) {
		for (Function<CompilationUnit, CodeProposal> function : funcs) {
			CompilationUnit compilationUnit = createCompilationUnit(uri);
			String originalSource = LexicalPreservingPrinter.print(compilationUnit);

			CodeProposal sourceChange = function.apply(compilationUnit);
			if (sourceChange != null) {
				List<SourceCodeModification> insertions = createModifications(originalSource, LexicalPreservingPrinter.print(compilationUnit));
				sourceChange.getSourceChange().setInsertions(insertions);
				;
				sourceChanges.add(sourceChange);
			}
		}
	}

	public CompilationUnit createCompilationUnit(String uri) {
		try {
			CompilationUnit compilationUnit = javaParser.parse(createFileFromUri(uri)).getResult().get();
			LexicalPreservingPrinter.setup(compilationUnit);
			return compilationUnit;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public CompilationUnit createCompilationUnitFromSource(String source) {
		CompilationUnit compilationUnit = javaParser.parse(source).getResult().get();
		LexicalPreservingPrinter.setup(compilationUnit);
		return compilationUnit;
	}

	private File createFileFromUri(String uri) {
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

	public String getWorkspacePath() {
		return workspacePath;
	}
//
//	public boolean fileExists() {
//		return createFileFromUri().exists();
//	}

}