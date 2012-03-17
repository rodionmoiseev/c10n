package c10n;

import java.util.Locale;

public final class C10NAnnotationBinder<T> {
	private Locale locale = C10N.FALLBACK_LOCALE;

	public void toLocale(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}
}
