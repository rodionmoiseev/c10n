package c10n.tools.bundles;

import java.lang.reflect.Method;
import java.util.Map;

import c10n.C10NDef;
import c10n.C10NMessages;
import c10n.share.utils.ReflectionUtils;
import c10n.tools.search.C10NInterfaceSearch;

public class DefaultBundleGenerator implements BundleGenerator {

	private final C10NInterfaceSearch search;
	
	public DefaultBundleGenerator(C10NInterfaceSearch search){
		this.search = search;
	}
	
	@Override
	public void generate(Class<?> clazz, Map<String, String> builder) {
		for (Method method : clazz.getDeclaredMethods()) {
			String val = null;
			C10NDef defVal = method.getAnnotation(C10NDef.class);
			if (null != defVal) {
				val = defVal.value();
			}
			builder.put(ReflectionUtils.getDefaultKey(clazz, method), val);
		}
	}

	@Override
	public void generateForPackage(String packagePrefix,
			Map<String, String> builder) {
		for(Class<?> c10nInterface : search.find(packagePrefix, C10NMessages.class)){
			generate(c10nInterface, builder);
		}
	}
}
