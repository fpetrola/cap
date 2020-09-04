package com.fpetrola.cap.model.binders.sync;

import java.util.Collection;

public class ParametrizedModelHandler<M1, M2, P1, P2> implements ModelHandler<M1, M2, P1, P2> {

	private ModelMatcher<P1, P2> modelMatcher;
	private ModelUpdater<P1, P2> leftUpdater;
	private CollectionGetter<M1, P1> leftCollectionGetter;
	private CollectionGetter<M2, P2> rightCollectionGetter;
	private ModelUpdater<P2, P1> rightUpdater;
	private ElementFactory<M1, M2, P1, P2> leftElementFactory;
	private ElementFactory<M2, M1, P2, P1> rightElementFactory;

	public ParametrizedModelHandler(CollectionGetter<M1, P1> leftCollectionGetter, CollectionGetter<M2, P2> rightCollectionGetter, ModelMatcher<P1, P2> modelMatcher, ModelUpdater<P1, P2> leftUpdater, ElementFactory<M1, M2, P1, P2> leftElementFactory) {
		this(leftCollectionGetter, rightCollectionGetter, modelMatcher, leftUpdater, (x, y) -> {
		}, leftElementFactory, (x, y) -> {
			return null;
		});
	}

	public ParametrizedModelHandler(CollectionGetter<M1, P1> leftCollectionGetter, CollectionGetter<M2, P2> rightCollectionGetter, ModelMatcher<P1, P2> modelMatcher, ElementFactory<M1, M2, P1, P2> leftElementFactory) {
		this(leftCollectionGetter, rightCollectionGetter, modelMatcher, (a, b) -> {
		}, leftElementFactory);
	}

	public ParametrizedModelHandler(CollectionGetter<M1, P1> leftCollectionGetter, CollectionGetter<M2, P2> rightCollectionGetter, ModelMatcher<P1, P2> modelMatcher, ModelUpdater<P1, P2> leftUpdater, ModelUpdater<P2, P1> rightUpdater, ElementFactory<M1, M2, P1, P2> leftElementFactory, ElementFactory<M2, M1, P2, P1> rightElementFactory) {
		this.modelMatcher = modelMatcher;
		this.leftUpdater = leftUpdater;
		this.leftCollectionGetter = leftCollectionGetter;
		this.rightCollectionGetter = rightCollectionGetter;
		this.setRightUpdater(rightUpdater);
		this.leftElementFactory = leftElementFactory;
		this.rightElementFactory = rightElementFactory;

	}

	public Collection<P2> getRightCollection(M2 model) {
		return rightCollectionGetter.getCollection(model);
	}

	public Collection<P1> getLeftCollection(M1 model) {
		return leftCollectionGetter.getCollection(model);
	}

	public ModelMatcher<P1, P2> getModelMatcher() {
		return modelMatcher;
	}

	public ModelUpdater<P1, P2> getLeftUpdater() {
		return leftUpdater;
	}

	public ElementFactory<M1, M2, P1, P2> getLeftElementFactory() {
		return leftElementFactory;
	}

	public ModelUpdater<P2, P1> getRightUpdater() {
		return rightUpdater;
	}

	public void setRightUpdater(ModelUpdater<P2, P1> rightUpdater) {
		this.rightUpdater = rightUpdater;
	}

	public ElementFactory<M2, M1, P2, P1> getRightElementFactory() {
		return rightElementFactory;
	}
}
