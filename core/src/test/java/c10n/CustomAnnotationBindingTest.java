package c10n;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import c10n.share.util.RuleUtils;

public class CustomAnnotationBindingTest {
	@Rule
	public static MethodRule tmpLocale = RuleUtils.tmpLocale();
	
	@Test
	public void localeBinding(){
		C10N.configure(new AbstractC10NConfiguration() {
			@Override
			public void configure() {
				bindAnnotation(Def.class);
				bindAnnotation(Eng.class).toLocale(Locale.ENGLISH);
				bindAnnotation(Jp.class).toLocale(Locale.JAPANESE);
			}
		});
		Locale.setDefault(Locale.ENGLISH);
		Labels msg = C10N.get(Labels.class);
		assertThat(msg.label(), is(equalTo("English")));
		assertThat(msg.label2("arg"), is(equalTo("English arg")));
		
		Locale.setDefault(Locale.JAPANESE);
		assertThat(msg.label(), is(equalTo("Japanese")));
		assertThat(msg.label2("hikisuu"), is(equalTo("Japanese hikisuu")));
		
		Locale.setDefault(Locale.GERMAN);
		assertThat(msg.label(), is(equalTo("Default")));
		assertThat(msg.label2("def"), is(equalTo("Default def")));
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Eng {
		String value();
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Jp {
		String value();
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Def {
		String value();
	}

	@C10NMessages
	interface Labels{
		@Eng("English")
		@Jp("Japanese")
		@Def("Default")
		String label();
		
		@Eng("English {0}")
		@Jp("Japanese {0}")
		@Def("Default {0}")
		String label2(String arg);
	}
}