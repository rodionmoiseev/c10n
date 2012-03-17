package c10n.tools.codegen;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import c10n.C10NDef;
import c10n.tools.search.C10NInterfacesSearches;

public class DefaultCodeGeneratorTest {
	private final CodeGenerator bc = new DefaultCodeGenerator(C10NInterfacesSearches.reflectionsSearch());
	
	@Test
	public void convertingInterfaceWithDefaultStrategy(){
		StringBuilder sb = new StringBuilder();
		bc.convert(sb, Buttons.class);
		assertThat(sb.toString(), is("public class Buttons_def implements "+
				"c10n.tools.bundles.BundleConverterTest.Buttons{"+
				"public String ok(){ return \"\"; }" +
				"public String cancel(){ return \"Cancel\"; }" +
				"}"));
	}
	
	@Test
	public void subInterfacesIncludeParentMethods(){
		StringBuilder sb = new StringBuilder();
		bc.convert(sb, Window.class);
		assertThat(sb.toString(), is("public class Window_def implements "+
				"c10n.tools.bundles.BundleConverterTest.Window{"+
				"public String title(){ return \"MyApp\"; }" +
				"public String ok(){ return \"\"; }" +
				"public String cancel(){ return \"Cancel\"; }" +
				"}"));
	}
	
	interface Buttons{
		//No default
		String ok();
		
		@C10NDef("Cancel")
		String cancel();
	}
	
	interface Window extends Buttons{
		@C10NDef("MyApp")
		String title();
	}
}
