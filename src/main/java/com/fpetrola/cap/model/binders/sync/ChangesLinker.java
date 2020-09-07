package com.fpetrola.cap.model.binders.sync;

import com.fpetrola.cap.model.source.CodeProposal;
import com.fpetrola.cap.model.source.SourceChange;

public interface ChangesLinker {

	CodeProposal getChangerOf(Object o1, Object o2);

	void addSourceChangeFor(Object pm, SourceChange sourceChange);

	void addCodeProposalFor(Object object, CodeProposal codeProposal);

}