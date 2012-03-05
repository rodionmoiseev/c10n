package c10n.tools.search.test1;

import c10n.C10NDef;
import c10n.C10NMessages;
import c10n.tools.search.test1.labels.Labels;

@C10NMessages
public interface Window extends Buttons, Labels {
	@C10NDef("Test01")
	String title();
	
	@C10NDef("rodion")
	String author();
}
