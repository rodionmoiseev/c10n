package c10n;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
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
				C10NInvocationHandler.create(conf, c10nInterface));
	}

	public void configure(AbstractC10NConfiguration conf) {
		conf.configure();
		this.conf = conf;
	}

	private static final class C10NInvocationHandler implements
			InvocationHandler {
		private final AbstractC10NConfiguration conf;
		private final Class<?> proxiedClass;
		private final Map<String, String> vals;

		C10NInvocationHandler(AbstractC10NConfiguration conf,
				Class<?> proxiedClass, Map<String, String> vals) {
			this.conf = conf;
			this.proxiedClass = proxiedClass;
			this.vals = vals;
		}

		static C10NInvocationHandler create(AbstractC10NConfiguration conf,
				Class<?> c10nInterface) {
			Map<String, String> vals = new HashMap<String, String>();
			for (Method m : c10nInterface.getMethods()) {
				C10NDef c10n = m.getAnnotation(C10NDef.class);
				if (null != c10n) {
					vals.put(m.getName(), c10n.value());
				}
			}
			return new C10NInvocationHandler(conf, c10nInterface, vals);
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

			ResourceBundle bundle = conf.getBundleForLocale(proxiedClass, locale);
			if (null != bundle) {
				StringBuilder sb = new StringBuilder();
				ReflectionUtils.getDefaultKey(proxiedClass, method, sb);
				String key = sb.toString();
				if (bundle.containsKey(key)) {
					return MessageFormat.format(bundle.getString(key), args);
				}
			}

			String methodName = method.getName();
			String res = vals.get(methodName);
			if (null == res) {
				return untranslatedMessage(methodName, args);
			}
			return MessageFormat.format(res, args);
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
