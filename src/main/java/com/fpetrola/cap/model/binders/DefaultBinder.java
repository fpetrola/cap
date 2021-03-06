package com.fpetrola.cap.model.binders;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fpetrola.cap.model.binders.sync.ChangesLinker;
import com.fpetrola.cap.model.source.SourceChangesListener;

public class DefaultBinder<S, T> implements Binder<S, T> {
	protected List<String> filters = new ArrayList<>();
	public List<Binder> chain = new ArrayList<>();
	private Binder<T, ?> parentBinder;

	public DefaultBinder() {
	}

	public SourceChangesListener getSourceChangesListener() {
		return parentBinder.getSourceChangesListener();
	}

	public TraverseListener getTraverseListener() {
		return parentBinder.getTraverseListener();
	}

	public void setTraverserListener(TraverseListener traverseListener) {
		parentBinder.setTraverserListener(traverseListener);
	}

	public void setSourceChangesListener(SourceChangesListener sourceChangesListener) {
		parentBinder.setSourceChangesListener(sourceChangesListener);
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> ids) {
		this.filters = ids;
	}

	public String getWorkspacePath() {
		return parentBinder != null ? parentBinder.getWorkspacePath() : null;
	}

	public void setChain(List<Binder> binders) {
		this.chain = binders;
	}

	public List<Binder> getChain() {
		return chain;
	}

	public void accept(BinderVisitor<?, ?> visitor) {
		visitor.visitChainedBinder(this);
		for (Binder<?, ?> binder : chain) {
			binder.accept(visitor);
		}
	}

	public void addBinder(Binder aBinder) {
		chain.add(aBinder);
		aBinder.setParent(this);
	}

	public void setParent(Binder<T, ?> aParentBinder) {
		this.parentBinder = aParentBinder;
	}

	@Override
	public void removeBinder(Binder aBinder) {
		chain.remove(aBinder);
		aBinder.setParent(null);
	}

	public Binder<T, ?> getParent() {
		return parentBinder;
	}

	public boolean allowsRootBinder() {
		return false;
	}

	public boolean canReceiveFrom(Binder binder) {
		if (isRootBinder())
			return binder.allowsRootBinder();
		else
			return getTypes()[0].equals(binder.getOutputType());
	}

	public boolean isRootBinder() {
		return getTypes()[0].equals(Void.class);
	}

	public Type getOutputType() {
		var outputBinder = this;
		if (!chain.isEmpty())
			outputBinder = (DefaultBinder<S, T>) chain.get(chain.size() - 1);

		return outputBinder.getTypes()[1];
	}

	public List<T> solve(S input) {
		List<T> pull = pull(input);
		pull = pickResults(this, pull, getFilters());
		getTraverseListener().valuesPulledFrom(this, pull);

		var result = new ArrayList<>();
		var lastValue = (List<?>) pull;
		var lastResult = (List<?>) pull;

		for (var chainElement : chain) {
			for (var inputItem : lastValue) {
				var solve = chainElement.solve(inputItem);
				result.addAll(solve);
			}

			lastResult = result;
			if (result.isEmpty() && !pull.isEmpty() && !pull.get(0).getClass().equals(Void.class))
				result = new ArrayList<>(pull);

			lastValue = new ArrayList<>(result);
			result.clear();
		}

		return (List<T>) lastResult;
	}

	private List pickResults(Binder<?, ?> binder, List lastValue, List<String> ids) {
		if (ids.isEmpty())
			return lastValue;
		else
			return (List) lastValue.stream().filter(v -> ids.stream().anyMatch(f -> v.toString().contains(f))).collect(Collectors.toList());
	}

	public ChangesLinker getChangesLinker() {
		return parentBinder.getChangesLinker();
	}
}