package c10n.guice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import c10n.C10NDef;
import c10n.C10NMessages;

import com.google.inject.Guice;

public class GuiceLoaderTest {
	@Test
	public void guiceTest(){
		MyGuiceMessages msg = Guice.createInjector(C10NModule.scanAllPackages())
				.getInstance(MyGuiceMessages.class);
		assertThat(msg.greet(), is("Hello, Guice!"));
	}
}

@C10NMessages
interface MyGuiceMessages{
	@C10NDef("Hello, Guice!")
	String greet();
}
