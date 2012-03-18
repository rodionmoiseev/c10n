package c10n.tools.codegen;

import java.io.File;
import java.io.IOException;

public interface BundleGenerator {
	void convertAll(String packagePrefix, File outputSrcFolder, File baseFile) throws IOException;
}
