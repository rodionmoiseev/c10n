package c10n.guice;

import java.util.Set;

import org.reflections.Reflections;

import c10n.C10N;
import c10n.C10NMessages;

import com.google.inject.AbstractModule;

public class C10NModule extends AbstractModule {
	private final String[] packagePrefixes;

	public static C10NModule scanAllPackages(){
		return scanPackages("");
	}
	
	public static C10NModule scanPackages(String... packagePrefixes) {
		return new C10NModule(packagePrefixes);
	}

	private C10NModule(String[] packagePrefixes) {
		this.packagePrefixes = packagePrefixes;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		Set<Class<?>> c10nTypes = new Reflections(packagePrefixes)
				.getTypesAnnotatedWith(C10NMessages.class);
		for (Class<?> c10nType : c10nTypes) {
			bind((Class<Object>) c10nType)
					.toInstance(C10N.get(c10nType));
		}
	}
}
