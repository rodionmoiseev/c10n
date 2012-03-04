package c10n;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class FallbackC10NFactoryTest {
	@Test
	public void messageDeclaredInAnnotationIsFallenBackOnto() {
		assertThat(C10N.get(WithFallback.class).message(),
				is("Fallback message"));
	}

	@Test
	public void methodsInSuperInterfacesAreVisible() {
		SubInterfaceWithFallback msg = C10N
				.get(SubInterfaceWithFallback.class);
		assertThat(msg.message(), is("Fallback message"));
		assertThat(msg.msg2(), is("msg2"));
	}

	@Test
	public void methodsWithoutDefaultValuesDefaultToMethodName() {
		NoFallback msg = C10N.get(NoFallback.class);
		assertThat(msg.noDefaultValue(), is("NoFallback.noDefaultValue"));
	}

	@Test
	public void messagesCanTakeArgumentsInMsgFormat() {
		WithArguments msg = C10N.get(WithArguments.class);
		assertThat(msg.greet("World"), is("Hello, World!"));
		assertThat(msg.noDefaultValue("value", "value2"),
				is("WithArguments.noDefaultValue(value, value2)"));
	}
}

interface WithFallback {
	@C10NDef("Fallback message")
	String message();
}

interface SubInterfaceWithFallback extends WithFallback {
	@C10NDef("msg2")
	String msg2();
}

interface NoFallback {
	String noDefaultValue();
}

interface WithArguments {
	@C10NDef("Hello, {0}!")
	String greet(String who);

	String noDefaultValue(Object arg, Object arg2);
}