package c10n.tools.search;

import java.util.Set;

import org.reflections.Reflections;

import c10n.C10NMessages;

class DefaultC10NInterfaceSearch implements C10NInterfaceSearch {

	@Override
	public Set<Class<?>> find(String packagePrefix) {
		Reflections reflections = new Reflections(packagePrefix);
		return reflections.getTypesAnnotatedWith(C10NMessages.class);
	}
}
