package c10n.tools.search;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import c10n.tools.search.test1.Buttons;
import c10n.tools.search.test1.Window;
import c10n.tools.search.test1.labels.Labels;
import c10n.tools.search.test1.labels.Labels1;
import c10n.tools.search.test1.labels.Labels2;

public class C10NInterfaceSearchTest {
	private final C10NInterfaceSearch s = new DefaultC10NInterfaceSearch();

	@Test
	@SuppressWarnings("unchecked")
	public void retrieveAllInterfaceAndSubInterfaceMethodsAsKeys() {
		Set<Class<?>> c10nInterfaces = s.find("c10n.tools.search.test1");
		assertThat(c10nInterfaces, is(set(Window.class,//
				Buttons.class,//
				Labels.class,//
				Labels1.class,//
				Labels2.class)));
	}

	private static <E> Set<E> set(E... args) {
		return new HashSet<E>(Arrays.asList(args));
	}
}
