package c10n.tools.search.test1.labels;

import c10n.C10NDef;
import c10n.C10NMessages;

@C10NMessages
public interface Labels1 {
	@C10NDef("label 1")
	String label1();
	
	String oops();
}
