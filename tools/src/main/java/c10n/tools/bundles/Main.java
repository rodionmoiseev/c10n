package c10n.tools.bundles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import c10n.tools.search.C10NInterfacesSearches;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			usage();
		} else {
			String packagePrefix = args[0];
			String baseName = args[1];
			List<String> locales = new ArrayList<String>();
			for (int i = 2; i < args.length; i++) {
				locales.add(args[i]);
			}
			generateBundles(packagePrefix, baseName, locales);
		}
	}

	private static void generateBundles(String packagePrefix, String baseName,
			List<String> locales) throws IOException {
		File baseFile = new File(baseName);
		File parent = baseFile.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new IOException("Failed to create directory:"
						+ parent.getAbsolutePath());
			}
		}
		String fileName = baseFile.getName();

		BundleGenerator bg = new DefaultBundleGenerator(
				C10NInterfacesSearches.reflectionsSearch());
		Map<String, String> builder = new HashMap<String, String>();
		bg.generateForPackage(packagePrefix, builder);
		if (!builder.isEmpty()) {
			generateBundle(parent, fileName, builder, "", null);
			for (String locale : locales) {
				generateBundle(parent, fileName, builder, "_" + locale,
						"NOT YET TRANSLATED: ");
			}
		} else {
			System.out
					.println("No messages found for package. Make sure classpath is correctly set: "
							+ packagePrefix);
		}
	}

	private static void generateBundle(File parent, String fileName,
			Map<String, String> builder, String localeSuffix, String valuePrefix)
			throws IOException {
		Properties bundle = new Properties();
		for (Entry<String, String> entry : builder.entrySet()) {
			String value = entry.getValue() == null ? "" : entry.getValue();
			if (null != valuePrefix) {
				value = valuePrefix + value;
			}
			bundle.put(entry.getKey(), value);
		}

		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(parent, fileName
					+ localeSuffix + ".properties"));
			bundle.store(
					new OutputStreamWriter(fout, Charset.forName("UTF-8")),//
					"Generated C10N bundle");
		} finally {
			if (null != fout) {
				fout.close();
			}
		}
	}

	private static void usage() {
		final String nl = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("c10n resource bundle generator").append(nl);
		sb.append("Synopsis:").append(nl);
		sb.append("\tc10n\tPKG BASENAME [locale1[ locale2[ ...]]]").append(nl);
		sb.append("Options:").append(nl);
		sb.append("\tPKG\t\tPackage prefix to scan for C10NMessage interfaces")
				.append(nl);
		sb.append("\tBASENAME\tResource bundle base name").append(nl);
		sb.append("\tlocaleN\t\tAdditional locales to generate stubs for")
				.append(nl);
		System.out.println(sb.toString());
	}
}
