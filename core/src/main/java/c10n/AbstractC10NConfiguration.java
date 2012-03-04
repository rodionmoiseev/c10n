package c10n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import c10n.share.EncodedResourceControl;

public abstract class AbstractC10NConfiguration {
	private final Map<String, C10NBundleBinder> bundleBinders = new HashMap<String, C10NBundleBinder>();
	private final Map<Class<?>, C10NConfigurationBinder<?>> binders = new HashMap<Class<?>, C10NConfigurationBinder<?>>();

	public abstract void configure();

	protected <T> C10NConfigurationBinder<T> bind(Class<T> c10nInterface) {
		C10NConfigurationBinder<T> binder = new C10NConfigurationBinder<T>();
		binders.put(c10nInterface, binder);
		return binder;
	}

	public C10NBundleBinder bindBundle(String baseName) {
		C10NBundleBinder binder = new C10NBundleBinder();
		bundleBinders.put(baseName, binder);
		return binder;
	}

	ResourceBundle getBundleForLocale(Class<?> c10nInterface, Locale locale) {
		for (Entry<String, C10NBundleBinder> entry : bundleBinders.entrySet()) {
			C10NBundleBinder binder = entry.getValue();
			if (binder.getBoundInterfaces().isEmpty()
					|| binder.getBoundInterfaces().contains(c10nInterface)) {
				return ResourceBundle.getBundle(entry.getKey(), locale,
						new EncodedResourceControl("UTF-8"));
			}
		}
		return null;
	}

	Class<?> getBindingForLocale(Class<?> c10nInterface, Locale locale) {
		C10NConfigurationBinder<?> binder = binders.get(c10nInterface);
		if (null != binder) {
			return binder.getBindingForLocale(locale);
		}
		return null;
	}
}
