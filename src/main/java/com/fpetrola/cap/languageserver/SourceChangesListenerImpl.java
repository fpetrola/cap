package com.fpetrola.cap.languageserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.SourceChangesListener;

public class SourceChangesListenerImpl implements SourceChangesListener {

	private Map<String, List<CodeProposal>> ranges = new HashMap<>();

	public void sourceChange(String resourceUri, List<CodeProposal> changes) {
		if (!ranges.containsKey(resourceUri))
			ranges.put(resourceUri, new ArrayList<>());
		ranges.get(resourceUri).addAll(changes);
	}

	public void fileCreation(String resourceUri, String content) {
		try {
			File file = new File(URI.create(resourceUri));
			file.getParentFile().mkdirs();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(content);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, List<CodeProposal>> getRanges() {
		return ranges;
	}

	public String getFileContent(String uri) {
		try {
			Path path = Paths.get(URI.create(uri));
			String content = Files.readString(path);
			return content;
		} catch (Exception e) {
			fileCreation(uri, "");
			return "";
		}
	}
}
