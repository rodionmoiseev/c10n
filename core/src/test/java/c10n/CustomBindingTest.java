package c10n;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import c10n.share.util.RuleUtils;

public class CustomBindingTest {
	@Rule
	public static MethodRule tmpLocale = RuleUtils.tmpLocale();
	
	@Test
	public void localeBinding(){
		C10N.configure(new MyC10NConfiguration());
		Locale.setDefault(Locale.ENGLISH);
		Labels msg = C10N.get(Labels.class);
		assertThat(msg.label(), is(equalTo("English")));
		
		Locale.setDefault(Locale.JAPANESE);
		assertThat(msg.label(), is(equalTo("Japanese")));
	}
}

class MyC10NConfiguration extends AbstractC10NConfiguration{
	@Override
	public void configure() {
		bind(Labels.class)
			.to(LabelsEng.class, Locale.ENGLISH)
			.to(LabelsJapanese.class, Locale.JAPANESE);
	}
}

@C10NMessages
interface Labels{
	String label();
}

class LabelsEng implements Labels{
	@Override
	public String label() {
		return "English";
	}
}

class LabelsJapanese implements Labels{
	@Override
	public String label() {
		return "Japanese";
	}
}
