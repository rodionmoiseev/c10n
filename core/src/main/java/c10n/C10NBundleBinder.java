package c10n;

import java.util.ArrayList;
import java.util.List;

public class C10NBundleBinder {
	private final List<Class<?>> boundInterfaces = new ArrayList<Class<?>>();
	public void to(Class<?> c10nInterface) {
		boundInterfaces.add(c10nInterface);
	}
	
	List<Class<?>> getBoundInterfaces(){
		return boundInterfaces;
	}
}
