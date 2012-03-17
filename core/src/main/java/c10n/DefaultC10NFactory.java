package c10n;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import c10n.share.utils.ReflectionUtils;

class DefaultC10NFactory implements C10NFactory {
	private AbstractC10NConfiguration conf = new DefaultC10NConfiguration();

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> c10nInterface) {
		if (null == c10nInterface) {
			throw new NullPointerException("c10nInterface is null");
		}
		return (T) Proxy.newProxyInstance(C10N.class.getClassLoader(),
				new Class[] { c10nInterface },
				C10NInvocationHandler.create(this, conf, c10nInterface));
	}

	public void configure(AbstractC10NConfiguration conf) {
		conf.configure();
		this.conf = conf;
	}

	private static final class C10NInvocationHandler implements
			InvocationHandler {
		private final C10NFactory c10nFactory;
		private final AbstractC10NConfiguration conf;
		private final Class<?> proxiedClass;
		private final Map<Locale, Map<String, String>> translationsByLocale;

		C10NInvocationHandler(C10NFactory c10nFactory,
				AbstractC10NConfiguration conf, Class<?> proxiedClass,
				Map<Locale, Map<String, String>> translationsByLocale) {
			this.c10nFactory = c10nFactory;
			this.conf = conf;
			this.proxiedClass = proxiedClass;
			this.translationsByLocale = translationsByLocale;
		}

		static C10NInvocationHandler create(C10NFactory c10nFactory,
				AbstractC10NConfiguration conf, Class<?> c10nInterface) {
			Map<Locale, Map<String, String>> translationsByLocale = new HashMap<Locale, Map<String, String>>();

			Map<String, String> vals = new HashMap<String, String>();
			for (Method m : c10nInterface.getMethods()) {
				C10NDef c10n = m.getAnnotation(C10NDef.class);
				if (null != c10n) {
					vals.put(m.toString(), c10n.value());
				}
			}
			translationsByLocale.put(C10N.FALLBACK_LOCALE, vals);

			// Process custom bound annotations
			for (Entry<Class<? extends Annotation>, C10NAnnotationBinder<?>> entry : conf
					.getAnnotationBinders().entrySet()) {
				Class<? extends Annotation> annotationClass = entry.getKey();
				Map<String, String> translations = new HashMap<String, String>();
				for (Method m : c10nInterface.getMethods()) {
					Annotation a = m.getAnnotation(annotationClass);
					if (null != a) {
						try {
							String translation = String.valueOf(annotationClass
									.getMethod("value").invoke(a));
							translations.put(m.toString(), translation);
						} catch (SecurityException e) {
							throw new RuntimeException("Annotation "
									+ annotationClass.getName()
									+ " value() method is not accessible", e);
						} catch (NoSuchMethodException e) {
							throw new RuntimeException("Annotation "
									+ annotationClass.getName()
									+ " must declare value() method");
						} catch (Exception e) {
							throw new RuntimeException(
									"Could not call value() on annotation "
											+ annotationClass.getName(), e);
						}
					}
				}
				translationsByLocale.put(entry.getValue().getLocale(),
						translations);
			}

			return new C10NInvocationHandler(c10nFactory, conf, c10nInterface,
					translationsByLocale);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Locale locale = Locale.getDefault();
			Class<?> binding = conf.getBindingForLocale(proxiedClass, locale);
			if (null != binding) {
				// user specified binding exists
				// simply delegate the call to the binding
				Object instance = binding.newInstance();
				return method.invoke(instance, args);
			}

			Class<?> returnType = method.getReturnType();
			if (returnType.isAssignableFrom(String.class)) {
				// For methods returning String or CharSequence

				ResourceBundle bundle = conf.getBundleForLocale(proxiedClass,
						locale);
				if (null != bundle) {
					StringBuilder sb = new StringBuilder();
					ReflectionUtils.getDefaultKey(proxiedClass, method, sb);
					String key = sb.toString();
					if (bundle.containsKey(key)) {
						return MessageFormat
								.format(bundle.getString(key), args);
					}
				}

				String methodName = method.toString();
				String res = null;
				Map<String, String> trs = getTranslations(locale);
				if (trs != null) {
					res = trs.get(methodName);
				}
				if (null == res) {
					return untranslatedMessage(methodName, args);
				}
				return MessageFormat.format(res, args);
			} else if (returnType.isInterface()) {
				if (null != returnType.getAnnotation(C10NMessages.class)) {
					return c10nFactory.get(returnType);
				}
			}
			// don't know how to handle this return type
			return null;
		}

		private Map<String, String> getTranslations(Locale locale) {
			Map<String, String> trs = translationsByLocale.get(locale);
			if (null == trs) {
				return translationsByLocale.get(C10N.FALLBACK_LOCALE);
			}
			return trs;
		}

		private String untranslatedMessage(String methodName, Object[] args) {
			StringBuilder sb = new StringBuilder();
			sb.append(proxiedClass.getSimpleName()).append('.');
			sb.append(methodName);
			if (args != null && args.length > 0) {
				sb.append('(');
				for (int i = 0; i < args.length; i++) {
					sb.append(String.valueOf(args[i]));
					if (i + 1 < args.length) {
						sb.append(", ");
					}
				}
				sb.append(')');
			}
			return sb.toString();
		}

	}
}
