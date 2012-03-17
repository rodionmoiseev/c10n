package c10n;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DelegationTest {
	@Test
	public void generatedClassesCanBeDelegated(){
			Window msg = C10N.get(Window.class);
			assertThat(msg.title(), is("MyApp"));
			assertThat(msg.buttons().ok(), is("OK"));
	}
	
	@Test
	public void delegationCanBeSelfReferencing(){
		Window msg = C10N.get(Window.class);
		assertThat(msg.buttons().parent().buttons().parent().title(), is("MyApp"));
		assertThat(msg.buttons().parent().buttons().parent().buttons().ok(), is("OK"));
		
	}
	
	@C10NMessages
	interface Buttons{
		@C10NDef("OK")
		String ok();
		
		Window parent();
	}
	
	@C10NMessages
	interface Window{
		Buttons buttons();
		
		@C10NDef("MyApp")
		String title();
	}
}
