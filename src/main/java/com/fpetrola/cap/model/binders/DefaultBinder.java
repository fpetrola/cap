package com.fpetrola.cap.model.binders;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fpetrola.cap.model.source.SourceChangesListener;

public class DefaultBinder<S, T> implements Binder<S, T> {
	protected SourceChangesListener sourceChangesListener;
	protected List<String> filters = new ArrayList<>();
	public String workspacePath;
	public List<Binder> chain = new ArrayList<>();
	private Binder<T, ?> parentBinder;
	private TraverseListener traverseListener;

	public DefaultBinder() {
	}

	public SourceChangesListener getSourceChangesListener() {
		return sourceChangesListener;
	}

	public void setSourceChangesListener(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> ids) {
		this.filters = ids;
	}

	public String getWorkspacePath() {
		return workspacePath;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
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

	public String findWorkspacePath() {
		if (workspacePath != null)
			return workspacePath;
		if (parentBinder != null) {
			return parentBinder.findWorkspacePath();
		} else
			return null;
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
		Binder outputBinder = this;
		if (!chain.isEmpty())
			outputBinder = (Binder) chain.get(chain.size() - 1);

		return outputBinder.getTypes()[1];
	}

	public List<T> solve(S input) {
		List<T> pull = pull(input);
		traverseListener.valuesPulledFrom(this, pull);

		List<Object> result = new ArrayList<>();
		List<Object> lastValue = (List<Object>) pull;
		List<Object> lastResult = (List<Object>) pull;

		for (Binder<Object, Object> chainElement : chain) {

			for (Object inputItem : lastValue) {
				List<Object> solve = chainElement.solve(inputItem);
				solve = pickResults(chainElement, solve, chainElement.getFilters());
				result.addAll(solve);
			}

			lastResult = result;
			if (result.isEmpty())
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

	public void setTraverserListener(TraverseListener traverseListener) {
		this.traverseListener = traverseListener;
	}
}