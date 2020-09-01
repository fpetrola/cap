package com.fpetrola.cap.model.binders.implementations;

import java.util.Collection;

public class ParametrizedModelHandler<M1, M2, P1, P2> implements ModelHandler<M1, M2, P1, P2> {

	private ModelMatcher<P1, P2> modelMatcher;
	private ModelSync<P1, P2> modelSync;
	private CollectionGetter<M1, P1> leftCollectionGetter;
	private CollectionGetter<M2, P2> rightCollectionGetter;
	private ElementFactory<P1, P2> leftElementFactory;

	public ParametrizedModelHandler(ModelMatcher<P1, P2> modelMatcher, ModelSync<P1, P2> modelSync, CollectionGetter<M1, P1> leftCollectionGetter, CollectionGetter<M2, P2> rightCollectionGetter, ElementFactory<P1, P2> leftElementFactory) {
		super();
		this.modelMatcher = modelMatcher;
		this.modelSync = modelSync == null ? (a, b) -> {
		} : modelSync;
		this.leftCollectionGetter = leftCollectionGetter;
		this.rightCollectionGetter = rightCollectionGetter;
		this.leftElementFactory = leftElementFactory;
	}

	public ParametrizedModelHandler(ModelMatcher<P1, P2> modelMatcher, CollectionGetter<M1, P1> leftCollectionGetter, CollectionGetter<M2, P2> rightCollectionGetter, ElementFactory<P1, P2> leftElementFactory) {
		this(modelMatcher, (a, b) -> {
		}, leftCollectionGetter, rightCollectionGetter, leftElementFactory);
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

	public ModelSync<P1, P2> getModelSync() {
		return modelSync;
	}

	public ElementFactory<P1, P2> getLeftElementFactory() {
		return leftElementFactory;
	}
}
