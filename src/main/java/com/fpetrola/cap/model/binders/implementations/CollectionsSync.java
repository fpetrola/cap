package com.fpetrola.cap.model.binders.implementations;

public class CollectionsSync {

	public <M1, M2, P1, P2> void matchElementMapping(M1 left, M2 right, ModelHandler<M1, M2, P1, P2> modelHandler) {
		var rightCollection = modelHandler.getRightCollection(right);
		for (var rightElement : rightCollection) {
			var leftCollection = modelHandler.getLeftCollection(left);
			var findFirst = leftCollection.stream().filter(leftElement -> modelHandler.getModelMatcher().match(leftElement, rightElement)).findFirst();
			var leftElement = findFirst.orElseGet(() -> modelHandler.getLeftElementFactory().create(rightElement));

			modelHandler.getModelSync().updateFromRight(leftElement, rightElement);
		}
	}
}
