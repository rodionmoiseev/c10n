package c10n.tools.codegen;

import java.io.File;
import java.io.IOException;

public interface CodeGenerator {
	void convert(StringBuilder sb, Class<?> class1);

	void convertAll(String packagePrefix, File outputSrcFolder, File baseFile) throws IOException;
}
