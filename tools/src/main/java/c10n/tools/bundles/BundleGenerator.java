package c10n.tools.bundles;

import java.util.Map;

public interface BundleGenerator {
	void generate(Class<?> clazz, Map<String, String> builder);

	void generateForPackage(String packagePrefix, Map<String, String> builder);
}
