package c10n.tools.search;

import java.net.URL;
import java.util.Set;

public final class C10NInterfacesSearches {
	public static C10NInterfaceSearch reflectionsSearch(Set<URL> urls){
		return new DefaultC10NInterfaceSearch(urls);
	}
	
	public static C10NInterfaceSearch reflectionsSearch(){
		return new DefaultC10NInterfaceSearch();
	}
}
