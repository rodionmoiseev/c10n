package c10n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks a c10n message interface in order
 * to make it detectable by the resource bundle
 * generator.</p>
 * 
 * @author rodion
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface C10NMessages {
}
