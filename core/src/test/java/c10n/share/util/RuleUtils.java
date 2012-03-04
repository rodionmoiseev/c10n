package c10n.share.util;

import java.util.Locale;

import org.junit.rules.ExternalResource;
import org.junit.rules.MethodRule;

public class RuleUtils {
	public static MethodRule tmpLocale(){
		return new TmpLocale(null);
	}
	
	public static MethodRule tmpLocale(Locale tmpLocale){
		return new TmpLocale(tmpLocale);
	}
	
	private static final class TmpLocale extends ExternalResource{
		private Locale oldLocale = null;
		private final Locale tmpLocale;
		
		TmpLocale(Locale tmpLocale){
			this.tmpLocale = tmpLocale;
		}
		
		@Override
		protected void before() throws Throwable {
			oldLocale = Locale.getDefault();
			if(null != tmpLocale){
				Locale.setDefault(tmpLocale);
			}
		}

		@Override
		protected void after() {
			Locale.setDefault(oldLocale);
		}
	}
}
