package com.fpetrola.cap.model.binders;

public class DefaultBinder {
	SourceChangesListener sourceChangesListener;

	public DefaultBinder() {
	}

	public SourceChangesListener getSourceChangesListener() {
		return sourceChangesListener;
	}

	public void setSourceChangesListener(SourceChangesListener sourceChangesListener) {
		this.sourceChangesListener = sourceChangesListener;
	}

}