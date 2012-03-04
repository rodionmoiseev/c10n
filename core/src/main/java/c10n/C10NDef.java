package c10n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Optionally marks c10n message interface methods
 * with default translation strings.</p> 
 * 
 * <p>Specified translation string will be used as
 * a default value during resource bundle generation.
 * The string can also be optionally used as a
 * fallback in case the resource bundle is not found.</p>
 * 
 * @author rodion
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface C10NDef {
	String value();
}
