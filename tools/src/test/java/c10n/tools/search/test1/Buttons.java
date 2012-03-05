package c10n.tools.search.test1;

import c10n.C10NDef;
import c10n.C10NMessages;

@C10NMessages
public interface Buttons {
	@C10NDef("OK")
	String ok();
	@C10NDef("Cancel")
	String cancel();
}
