package com.fpetrola.cap.model.binders.sync;

import java.util.HashMap;
import java.util.Map;

import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.SourceChange;

public class DefaultChangesLinker implements ChangesLinker {

	private Map<Object, SourceChange> sourceChanges = new HashMap<Object, SourceChange>();
	private Map<Object, CodeProposal> codeProposals = new HashMap<Object, CodeProposal>();

	public CodeProposal getChangerOf(Object o1, Object o2) {
		SourceChange sourceChange = sourceChanges.get(o1);
		CodeProposal codeProposal = null;
		if (sourceChange != null) {
			codeProposal = codeProposals.get(o2);
			codeProposal.setSourceChange(sourceChange);
			codeProposal.setMessage(sourceChange.getMessage());
		}
		return codeProposal;
	}

	public void addSourceChangeFor(Object pm, SourceChange sourceChange) {
		sourceChanges.put(pm, sourceChange);
	}

	public void addCodeProposalFor(Object object, CodeProposal codeProposal) {
		codeProposals.put(object, codeProposal);
	}
}
