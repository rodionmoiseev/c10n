package c10n;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import c10n.share.util.RuleUtils;

public class ResourceBundleBindingTest {
	@Rule
	public MethodRule tmpLocale = RuleUtils.tmpLocale(Locale.ENGLISH);
	
	private final C10NFactory f = new DefaultC10NFactory();

	@Test
	public void rootBundleBinding() {
		f.configure(new AbstractC10NConfiguration() {
			@Override
			public void configure() {
				bindBundle("c10n.testBundles.TestBundle");
			}
		});
		Labels labels = f.get(Labels.class);
		assertThat(labels.greeting(), is("Hello, World!"));
		assertThat(labels.argGreeting("C10N"), is("Hello, C10N!"));
	}
	
	@Test
	public void bundlesExplicitlyBoundToOtherClassesDoNotMatch(){
		f.configure(new AbstractC10NConfiguration() {
			@Override
			public void configure() {
				bindBundle("c10n.testBundles.TestBundle")
					.to(Buttons.class);
			}
		});
		
		Labels labels = f.get(Labels.class);
		assertThat(labels.greeting(), is("Labels.greeting"));
		
		Buttons buttons = f.get(Buttons.class);
		assertThat(buttons.ok(), is("OK!"));
	}
	
	@Test
	public void multiLanguageBundleBinding(){
		f.configure(new AbstractC10NConfiguration() {
			@Override
			public void configure() {
				bindBundle("c10n.testBundles.TestBundle");
			}
		});
		Labels labels = f.get(Labels.class);
		Buttons buttons = f.get(Buttons.class);
		
		Locale.setDefault(Locale.JAPANESE);
		assertThat(labels.greeting(), is("Ç±ÇÒÇ…ÇøÇÕê¢äE!"));
		assertThat(labels.argGreeting("C10N"), is("Ç±ÇÒÇ…ÇøÇÕC10N!"));
		assertThat(buttons.ok(), is("ÇÕÇ¢"));
		
		Locale.setDefault(Locale.ENGLISH);
		assertThat(labels.greeting(), is("Hello, World!"));
		assertThat(labels.argGreeting("C10N"), is("Hello, C10N!"));
		assertThat(buttons.ok(), is("OK!"));
	}

	interface Labels {

		String greeting();

		String argGreeting(String who);
	}
	
	interface Buttons{
		String ok();
	}
}