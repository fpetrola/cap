package com.fpetrola.cap.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.fpetrola.cap.model.binders.BidirectionalBinder;
import com.fpetrola.cap.model.binders.SourceChangesListener;

public class BindingApp {
	private SourceChangesListener sourceChangesListener;

	public BindingApp(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public void main(String[] args) throws YamlException, InterruptedException, ClassNotFoundException {

		bind(true);

	}

	public void bind(boolean doLoop) {
		try {
			InputStream inputStream = BindingApp.class.getClassLoader().getResourceAsStream("cap-config.yml");
			YamlReader reader = new YamlReader(new InputStreamReader(inputStream));
			ModelManagement modelManagement = reader.read(ModelManagement.class);

			do {
				List lastValue = new ArrayList<>(Arrays.asList(""));
				List<BidirectionalBinder> binderChain = modelManagement.getBinderChain();

				traverse(modelManagement, lastValue, binderChain, 0);
			} while (doLoop);
		} catch (YamlException | ClassNotFoundException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void traverse(ModelManagement modelManagement, List lastValue, List<BidirectionalBinder> binderChain, int i) throws ClassNotFoundException, InterruptedException {
		if (i < binderChain.size()) {
			BidirectionalBinder bidirectionalBinder = binderChain.get(i);
			bidirectionalBinder.setSourceChangesListener(sourceChangesListener);
			lastValue = pickResults(modelManagement, lastValue);
			for (Object object : lastValue) {
				List result = bidirectionalBinder.pull(object);
				traverse(modelManagement, result, binderChain, i + 1);
//				System.out.println(lastValue);
				Thread.sleep(100);
			}
		}
	}

	private List pickResults(ModelManagement modelManagement, List lastValue) throws ClassNotFoundException {
		List result = new ArrayList(lastValue);
		List models = new ArrayList();

		for (String id : modelManagement.getIds()) {
			for (Object object : lastValue) {
				boolean assignableFrom = object.getClass().isAssignableFrom(Class.forName(modelManagement.model));
				if (assignableFrom) {
					if (object.toString().contains(id)) {
						models.add(object);
						break;
					}
				}
			}
		}

		return models.isEmpty() ? result : models;
	}
}
