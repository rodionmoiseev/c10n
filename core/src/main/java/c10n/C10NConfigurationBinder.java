package c10n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class C10NConfigurationBinder<T> {
	private final Map<Locale, Class<?>> bindings = new HashMap<Locale, Class<?>>();

	public C10NConfigurationBinder<T> to(Class<? extends T> to, Locale forLocale){
		bindings.put(forLocale, to);
		return this;
	}
	
	public C10NConfigurationBinder<T> fallbackTo(Class<? extends T> lastResort){
		return this;
	}

	Class<?> getBindingForLocale(Locale locale) {
		return bindings.get(locale);
	}
}
