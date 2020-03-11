/*
 * Copyright 2012 Rodion Moiseev (https://github.com/rodionmoiseev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.share.utils.Preconditions;

/**
 * @author rodion
 */
public final class C10NFilters {

    /**
     * <p>Create an Enum-to-methods mapping filter to ease Enum translation using C10N.
     * 
     * <h3>Basic Usage</h3>
     * <p>Consider the following Enum type with 3 values:
     * <pre>{@code
     *   enum Status{
     *     Open, Closed, Pending
     *   }
     * }
     * </pre>
     * 
     * <p>In order to localise each of the values, first create a c10n interface with a method for
     * each of the Status values
     * <pre>{@code
     *   &#64;C10NMessages
     *   public interface StatusMsg {
     *     &#64;En("open")
     *     &#64;Ja("未着手")
     *     String open();
     * 
     *     &#64;En("closed! beer time!")
     *     &#64;Ja("完了")
     *     String closed();
     * 
     *     &#64;En("pending ...")
     *     &#64;Ja("進行中")
     *     String pending();
     *   }
     * }
     * </pre>
     * 
     * Then, in your c10n configuration add an enum-filter binding for the Status type
     * 
     * <pre>{@code
     *   void configure(){
     *     bindFilter(C10NFilters.enumMapping(Status.class, StatusMsg.class), Status.class);
     *   }
     * }
     * </pre>
     * 
     * <p>Now, every time c10n encounters Status as a method argument in a c10n-interface type,
     * the it will be replaced with the appropriate localised version of the Enum value:
     * 
     * <pre>{@code
     *   &#64;C10NMessages
     *   public interface Messages{
     *     &#64;En("Localised status is: {0}")
     *     &#64;Ja("状態は{0}")
     *     String showStatus(Status status);
     *   }
     * }
     * </pre>
     * 
     * <p>Invoking <code>showStatus(Status.Closed)</code> will render
     * as <code>"Localised status is: closed! beer time!"</code>.
     * 
     * <h2>Restricting Filter Application</h2>
     * <p>You can restrict filter application only to method arguments annotated with a given
     * annotation, or one of the given annotations from a list, by using <code>annotatedWith(Class)</code> method
     * when binding. For example:
     * <pre>{@code
     *   void configure(){
     *     bindFilter(new IntFormattingFilter(), int.class)
     *       .annotatedWith(Precise.class);
     *   }
     * }
     * </pre>
     * 
     * <p>The above declaration will make sure int arguments are only passed through the
     * <code>IntFormattingFilter</code> whenever the method argument is marked with the <code>&#64;Precise</code>
     * annotation. Other int arguments will not have the filter applied.
     * 
     * <h2>Method Mapping Rules</h2>
     * <p>Enum values are mapped to c10n-interface methods in the following order:
     * <ol>
     * <li>Method name matches "&lt;Enum class name&gt;_&lt;Enum value name&gt;". e.g <code>status_open()</code></li>
     * <li>Method name matches "&lt;Enum value name&gt;" e.g. <code>closed()</code></li>
     * </ol>
     *
     * <p><i>Note:</i> method mapping is case-insensitive.
     * <p><i>Note:</i> mapped methods cannot take any arguments. Methods with arguments will be excluded from mapping.
     * <p><i>Warning:</i> if mapping for one or more values is not found, a runtime exception will be thrown.
     *
     * @param enumClass           Enum type to create mapping for
     * @param c10nMappedInterface a c10n-interface containing mapped methods
     * @param <E>                 Enum type
     * @return a non-cached provider of enum mapping filter
     */
    public static <E extends Enum<?>> C10NFilterProvider<E> enumMapping(Class<E> enumClass, Class<?> c10nMappedInterface) {
        return new EnumMappingFilterProvider<E>(enumClass, c10nMappedInterface);
    }

    /**
     * <p>Filter provider that always returns the specified instance
     *
     * @param filter filter instance to return from the generated provider(not-null)
     * @param <T>    Filter argument type
     * @return instance of filter provider (never-null)
     */
    public static <T> C10NFilterProvider<T> staticFilterProvider(C10NFilter<T> filter) {
        Preconditions.assertNotNull(filter, "filter");
        return new StaticC10NFilterProvider<T>(filter);
    }

    /**
     * <p>Decorates the specified filter provider with a simple static cache.
     * Only the first call will result in an execution of {@link com.github.rodionmoiseev.c10n.C10NFilterProvider#get()} method.
     * The following calls will always return a cached instance of the first call.
     *
     * @param filterProvider filter provider to decorate with caching (not-null)
     * @param <T>            Filter argument type
     * @return instance of a filter provider decorated with simple static cache (never-null)
     */
    public static <T> C10NFilterProvider<T> cachedFilterProvider(C10NFilterProvider<T> filterProvider) {
        Preconditions.assertNotNull(filterProvider, "filterProvider");
        return new CachedC10NFilterProvider<T>(filterProvider);
    }

    private static final class StaticC10NFilterProvider<T> implements C10NFilterProvider<T> {
        private final C10NFilter<T> filter;

        private StaticC10NFilterProvider(C10NFilter<T> filter) {
            this.filter = filter;
        }

        @Override
        public C10NFilter<T> get() {
            return filter;
        }
    }

    private static final class CachedC10NFilterProvider<T> implements C10NFilterProvider<T> {
        private final C10NFilterProvider<T> base;
        private C10NFilter<T> thunk = null;

        private CachedC10NFilterProvider(C10NFilterProvider<T> base) {
            this.base = base;
        }

        @Override
        public C10NFilter<T> get() {
            if (null == thunk) {
                thunk = base.get();
            }
            return thunk;
        }
    }

    /**
     * @author rodion
     */
    private static final class EnumMappingFilterProvider<E extends Enum<?>> implements C10NFilterProvider<E> {
        private final Class<E> enumClass;
        private final Class<?> c10nMappedInterface;

        EnumMappingFilterProvider(Class<E> enumClass, Class<?> c10nMappedInterface) {
            this.enumClass = enumClass;
            this.c10nMappedInterface = c10nMappedInterface;
        }

        @Override
        public C10NFilter<E> get() {
            return new EnumMappingFilter<E>(enumClass, c10nMappedInterface);
        }
    }
}
