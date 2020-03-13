/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.annotations.DefaultC10NAnnotations;
import com.github.rodionmoiseev.c10n.annotations.En;
import com.github.rodionmoiseev.c10n.annotations.Ja;
import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * <p>Created: 7/31/12 11:33 AM
 *
 * @author rodion
 */
@SuppressWarnings("deprecation")//rational: using deprecated api for internal testing purposes
public class C10NTest {
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale();
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parametrisationIsDisabledWhenRawFalseIsPresent() {
        C10N.configure(new DefaultC10NAnnotations());
        Messages msg = C10N.get(Messages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.text("ignored"), is("{} {0} {hello}"));
    }

    @Test
    public void multipleC10NMsgFactoriesCanBeCreatedAndUsedIndividually() {
        C10NMsgFactory enC10nFactory = C10N.createMsgFactory(C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                setLocale(Locale.ENGLISH);
            }
        }));
        assertThat(enC10nFactory.get(Messages.class).text(), is("english"));

        C10NMsgFactory jpC10nFactory = C10N.createMsgFactory(C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                setLocale(Locale.JAPANESE);
            }
        }));
        assertThat(jpC10nFactory.get(Messages.class).text(), is("japanese"));
    }

    @Test
    public void configureTimeLocaleProviderIsIgnoreWhenLocaleIsPassedAtMessageCreationTime() {
        C10NMsgFactory c10nFactory = C10N.createMsgFactory(C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                setLocale(Locale.ENGLISH);
            }
        }));
        assertThat(c10nFactory.get(Messages.class, Locale.JAPANESE).text(), is("japanese"));
        assertThat(c10nFactory.get(Messages.class, Locale.ENGLISH).text(), is("english"));
    }

    /*
     * A somewhat hacky test to make sure invoking C10N.get() on a freshly
     * created C10N class throws the configuration warning.
     * The reason we have to do this is because, C10N class in the current
     * class loader is likely to have been already initialized by other unit tests.
     */
    @Test
    public void cannotGetMessagesWhenConfigureHasNotBeenCalled() throws Throwable {
        thrown.expect(exceptionClassWithName(C10NException.class.getName()));
        thrown.expectMessage("C10N.configure()");
        Class<?> c10nClass = freshClassLoader().loadClass(C10N.class.getName());
        try {
            c10nClass.getMethod("get", new Class<?>[]{Class.class}).invoke(null, Messages.class);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test
    public void cannotGetMessagesUsingDefaultConfig() {
        C10NCoreModule coreModule = new C10NCoreModule();
        ConfiguredC10NModule rootConfiguredModule = coreModule.resolve(coreModule.defaultConfig());
        C10NMsgFactory root = coreModule.defaultC10NMsgFactory(rootConfiguredModule);
        thrown.expect(C10NException.class);
        thrown.expectMessage("C10N.configure()");
        root.get(Messages.class);
    }

    private static Matcher<? extends Throwable> exceptionClassWithName(final String name) {
        return new BaseMatcher<Throwable>() {
            @Override
            public boolean matches(Object item) {
                return item.getClass().getName().equals(name);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Exception with class name '" + name + "'");
            }
        };
    }

    interface Messages {
        @En(value = "{} {0} {hello}", raw = true)
        String text(String ignored);

        @En("english")
        @Ja("japanese")
        String text();
    }

    private static ClassLoader freshClassLoader() {
        String pathSeparator = System
                .getProperty("path.separator");
        String[] classPathEntries = System
                .getProperty("java.class.path")
                .split(pathSeparator);
        URL[] urls = Arrays.stream(classPathEntries).map(C10NTest::toFileURL).toArray(URL[]::new);
        return new URLClassLoader(urls, null);
    }

    private static URL toFileURL(String path) {
        try {
            return Paths.get(path).toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not express " + path + " as URL.", e);
        }
    }
}
