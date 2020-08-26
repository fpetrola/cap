package com.fpetrola.cap.model.binders;

import java.io.File;
import java.io.FileNotFoundException;
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

public class BindWriter {

    protected SourceChangesListener sourceChangesListener;
	protected String uri;
	protected String workspacePath;
	protected JavaParser javaParser;

    public BindWriter() {
        super();
    }

    public List<SourceCodeInsertion> createInsertions(String print1, String print2) {
        List<SourceCodeInsertion> insertions = new ArrayList<>();
        try {
            Patch<String> patch = DiffUtils.diff(lines(print1), lines(print2));

            List<Delta<String>> deltas = patch.getDeltas();
            for (Delta<String> delta : deltas) {
                Patch<String> patch2 = new Patch<String>();
                patch2.addDelta(delta);

                List<String> patch3 = DiffUtils.patch(lines(print1), patch2);
                Patch<String> patch4 = DiffUtils.diff(lines(print1), patch3);

                Delta<String> delta2 = patch4.getDeltas().get(0);

                Chunk<String> revised = delta2.getRevised();
                String collect = revised.getLines().stream().collect(Collectors.joining("\n")) + "\n";
                int column = 0;

                int position = delta2.getOriginal().getPosition();
                int position2 = delta2.getRevised().getPosition();
                if (position == position2 && !delta2.getOriginal().getLines().isEmpty()) {
                    int length = delta2.getOriginal().getLines().get(0).length();
                    collect = collect.substring(length);
                    column = length;
                }
                Position begin = new Position(revised.getPosition(), column);
                final Range range2 = new Range(begin, begin);
                SourceCodeInsertion sourceCodeInsertion = new SourceCodeInsertion(collect, range2);
                insertions.add(sourceCodeInsertion);
            }
        } catch (PatchFailedException e) {
            throw new RuntimeException(e);
        }

        return insertions;
    }

    List<String> lines(String print1) {
        String[] split = print1.split("\\r?\\n");
        return Arrays.asList(split);
    }

	protected File initWithClassName(String className) {
		Path mavenModuleRoot = Path.of(workspacePath);
		SourceRoot sourceRoot = new SourceRoot(mavenModuleRoot.resolve("src/main/java"));
		ParserConfiguration parserConfiguration = sourceRoot.getParserConfiguration();
		parserConfiguration.setLexicalPreservationEnabled(true);
		SourceRoot createSourceRoot = sourceRoot;
		createSourceRoot.setPrinter(LexicalPreservingPrinter::print);
		
		String completeFilename = mavenModuleRoot + "/src/main/java/" + className.replace(".", "/") + ".java";
		File file = new File(completeFilename);
		uri = getURI(file);
		return file;
	}

	public void addInsertionsFor(List<SourceChange> sourceChanges, Function<CompilationUnit, SourceChange> function, File file) throws FileNotFoundException {
		CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
		LexicalPreservingPrinter.setup(compilationUnit);
		String originalSource = LexicalPreservingPrinter.print(compilationUnit);
		
		SourceChange sourceChange = function.apply(compilationUnit);
		if (sourceChange != null) {
			List<SourceCodeInsertion> insertions = createInsertions(originalSource, LexicalPreservingPrinter.print(compilationUnit));
			sourceChange.insertions = insertions;
			sourceChanges.add(sourceChange);
		}
	}

	protected JavaParser createJavaParser() {
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

}