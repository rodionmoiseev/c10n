package c10n.tools.search;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

class DefaultC10NInterfaceSearch implements C10NInterfaceSearch {
	private final Set<URL> cp;

	DefaultC10NInterfaceSearch() {
		this(ClasspathHelper.forJavaClassPath());
	}

	DefaultC10NInterfaceSearch(Set<URL> cp) {
		this.cp = cp;
	}

	@Override
	public Set<Class<?>> find(String packagePrefix, Class<? extends Annotation> annotationClass) {
		final Predicate<String> filter = new FilterBuilder.Include(
				FilterBuilder.prefix(packagePrefix));
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(cp)
				.filterInputsBy(filter)
				.setScanners(
						new TypeAnnotationsScanner().filterResultsBy(filter),
						new SubTypesScanner().filterResultsBy(filter)));
		Set<String> types = reflections.getStore().getTypesAnnotatedWith(annotationClass.getName());
		URL[] urls = cp.toArray(new URL[cp.size()]);
		return ImmutableSet.copyOf(Reflections.forNames(types, new URLClassLoader(urls)));
	}
}
