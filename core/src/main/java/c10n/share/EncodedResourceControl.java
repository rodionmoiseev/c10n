package c10n.share;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class EncodedResourceControl extends ResourceBundle.Control {
	private final String charsetName;
	
	public EncodedResourceControl(String charsetName){
		this.charsetName = charsetName;
	}
	
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale,
			String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		if (format.equals("java.properties")) {
			String bundleName = toBundleName(baseName, locale);
			final String resourceName = toResourceName(bundleName, "properties");
			final ClassLoader classLoader = loader;
			final boolean reloadFlag = reload;
			InputStream stream = null;
			try {
				stream = AccessController
						.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
							public InputStream run() throws IOException {
								InputStream is = null;
								if (reloadFlag) {
									URL url = classLoader
											.getResource(resourceName);
									if (url != null) {
										URLConnection connection = url
												.openConnection();
										if (connection != null) {
											// Disable caches to get
											// fresh data for
											// reloading.
											connection.setUseCaches(false);
											is = connection.getInputStream();
										}
									}
								} else {
									is = classLoader
											.getResourceAsStream(resourceName);
								}
								return is;
							}
						});
			} catch (PrivilegedActionException e) {
				throw (IOException) e.getException();
			}
			if (stream != null) {
				try {
					return new PropertyResourceBundle(new InputStreamReader(stream, charsetName));
				} finally {
					stream.close();
				}
			}
		}
		return super.newBundle(baseName, locale, format, loader, reload);
	}
}
