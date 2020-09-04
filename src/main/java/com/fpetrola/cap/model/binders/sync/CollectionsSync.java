package com.fpetrola.cap.model.binders.sync;

import com.fpetrola.cap.model.binders.SourceCodeChanger;

public class CollectionsSync {

	private SourceCodeChanger sourceCodeChanger;

	public CollectionsSync(SourceCodeChanger sourceCodeChanger) {
		this.sourceCodeChanger = sourceCodeChanger;
	}

	public <M1, M2, P1, P2> void matchElementMapping(M1 left, M2 right, ModelHandler<M1, M2, P1, P2> modelHandler) {
		processRightCollection(left, right, modelHandler);
		processLeftCollection(left, right, modelHandler);
	}

	private <P1, M2, M1, P2> void processRightCollection(M1 left, M2 right, ModelHandler<M1, M2, P1, P2> modelHandler) {
		var rightCollection = modelHandler.getRightCollection(right);
		for (var rightElement : rightCollection) {
			var leftCollection = modelHandler.getLeftCollection(left);
			var findFirst = leftCollection.stream().filter(leftElement -> modelHandler.getModelMatcher().match(leftElement, rightElement)).findFirst();
			var leftElement = findFirst.orElseGet(() -> modelHandler.getLeftElementFactory().create(left, rightElement));

			modelHandler.getLeftUpdater().update(leftElement, rightElement);
		}
	}

	private <P1, M2, M1, P2> void processLeftCollection(M1 left, M2 right, ModelHandler<M1, M2, P1, P2> modelHandler) {
		var leftCollection = modelHandler.getLeftCollection(left);
		for (var leftElement : leftCollection) {
			var rightCollection = modelHandler.getRightCollection(right);
			var findFirst = rightCollection.stream().filter(rightElement -> modelHandler.getModelMatcher().match(leftElement, rightElement)).findFirst();
			var rightElement = findFirst.orElseGet(() -> modelHandler.getRightElementFactory().create(right, leftElement));

			sourceCodeChanger.aplyChanges2(sourceCodeChanger.getChangesLinker().getChangerOf(rightElement, leftElement));

			modelHandler.getRightUpdater().update(rightElement, leftElement);
		}
	}
}
