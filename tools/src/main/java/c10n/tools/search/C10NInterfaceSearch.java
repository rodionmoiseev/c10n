package c10n.tools.search;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface C10NInterfaceSearch {
	Set<Class<?>> find(String packagePrefix,
			Class<? extends Annotation> annotationClass);
}
