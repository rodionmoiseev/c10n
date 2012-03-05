package c10n.tools.codegen;

import java.lang.reflect.Method;

import c10n.C10NDef;
import c10n.share.utils.ReflectionUtils;

class DefaultCodeGenerator implements CodeGenerator {

	@Override
	public void convert(StringBuilder sb, Class<?> c10nInterface) {
		sb.append("public class ");
		sb.append(c10nInterface.getSimpleName());
		sb.append("_def implements ");
		ReflectionUtils.getFQNString(c10nInterface, sb);
		sb.append('{');
		for(Method method : c10nInterface.getMethods()){
			String defaultValue = "";
			C10NDef def = method.getAnnotation(C10NDef.class);
			if(null != def){
				defaultValue = def.value();
			}
			sb.append("public String ");
			sb.append(method.getName());
			sb.append("(){ return \"");
			
			sb.append(defaultValue);
			sb.append("\"; }");
		}
		sb.append('}');
	}

}
