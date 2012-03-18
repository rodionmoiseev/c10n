package c10n.tools.codegen;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import c10n.tools.search.C10NInterfacesSearches;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			usage();
		} else {
			String cmd = args[0];
			final File cp = new File(args[1]);
			String packagePrefix = args[2];
			File baseFile = new File(args[3]);
			if (cmd.equals("bundle2java")) {
				@SuppressWarnings("serial")
				CodeGenerator cg = new DefaultCodeGenerator(
						C10NInterfacesSearches
								.reflectionsSearch(new HashSet<URL>() {
									{
										add(cp.toURI().toURL());
									}
								}));
				cg.convertAll(packagePrefix, new File("tmp"), baseFile);
			} else if (cmd.equals("java2bundle")) {
				@SuppressWarnings("serial")
				BundleGenerator bg = new DefaultBundleGenerator(C10NInterfacesSearches
						.reflectionsSearch(new HashSet<URL>() {
							{
								add(cp.toURI().toURL());
							}
						}));
				bg.convertAll("c10n.gen", new File("tmp"), baseFile);
			}
		}
	}

	private static void usage() {
		final String nl = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("c10n refactoring support").append(nl);
		sb.append("Synopsis:").append(nl);
		sb.append("\tc10n\tCMD CP PKG BASEFILE").append(nl);
		sb.append("Options:").append(nl);
		sb.append("\tCMD\t\tOne of bundle2java, java2bundle").append(nl);
		sb.append("\tCP\t\tClasspath to scan").append(nl);
		sb.append("\tPKG\t\tPackage prefix to scan for c10n interfaces")
				.append(nl);
		sb.append("\tBASENAME\tResource bundle base path").append(nl);
		System.out.println(sb.toString());
	}
}
