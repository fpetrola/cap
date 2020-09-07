package com.fpetrola.cap.model.binders.sync;

import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.SourceChange;

public class DummyChangesLinker implements ChangesLinker {

	public CodeProposal getChangerOf(Object o1, Object o2) {
		return null;
	}

	public void addSourceChangeFor(Object pm, SourceChange sourceChange) {
	}

	public void addCodeProposalFor(Object object, CodeProposal codeProposal) {
	}
}
