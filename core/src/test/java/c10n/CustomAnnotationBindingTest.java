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
		assertThat(msg.books(0), is("There are no books."));
		assertThat(msg.books(3), is("There are 3 books."));
		
		Locale.setDefault(Locale.JAPANESE);
		assertThat(msg.label(), is(equalTo("Japanese")));
		assertThat(msg.label2("hikisuu"), is(equalTo("Japanese hikisuu")));
		assertThat(msg.books(0), is("本がありません。"));
		assertThat(msg.books(3), is("本が3本あります。"));
		
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

	interface Labels{
		@Eng("English")
		@Jp("Japanese")
		@Def("Default")
		String label();
		
		@Eng("English {0}")
		@Jp("Japanese {0}")
		@Def("Default {0}")
		String label2(String arg);
		
		@Eng("There are {0,choice,0#no books|0<{0} books}.")
		@Jp("本が{0,choice,0#ありません|0<{0}本あります}。")
		String books(int n);
	}
}