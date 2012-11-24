/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package c10n;

import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.test.utils.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rodion
 */
public class AnnotationBindingConfigurationErrorTest {
    @Rule
    public TestRule tmpConfig = RuleUtils.tmpC10NConfiguration();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void nullAnnotationsAreNotAccepted() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(JUnitMatchers.containsString("is null"));
        C10N.configure(new C10NConfigBase() {
            @Override
            public void configure() {
                bindAnnotation(null);
            }
        });
    }

    @Test
    public void annotationsWithoutValueMethodThrowExceptionAtConfigurationTime() {
        thrown.expect(C10NConfigException.class);
        thrown.expectMessage(JUnitMatchers.containsString("value()"));
        C10N.configure(new C10NConfigBase() {
            @Override
            public void configure() {
                bindAnnotation(NoValueMethod.class);
            }
        });
    }

    @Test
    public void valueMethodMustReturnStringOrElseExceptionIsThrownAtConfigurationTime() {
        thrown.expect(C10NConfigException.class);
        thrown.expectMessage(JUnitMatchers.containsString("value()"));
        thrown.expectMessage(JUnitMatchers.containsString("String"));
        C10N.configure(new C10NConfigBase() {
            @Override
            public void configure() {
                bindAnnotation(IntValueMethod.class);
            }
        });
    }

    @Test
    public void cannotBindToNullLocale() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(JUnitMatchers.containsString("locale is null"));
        C10N.configure(new C10NConfigBase() {
            @Override
            public void configure() {
                bindAnnotation(Eng.class).toLocale(null);
            }
        });
    }

    @Test
    public void annotationsWithNoDefaultValueSpecifiedThrowException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("@En");
        thrown.expectMessage(Messages.class.getCanonicalName());
        C10N.configure(new DefaultC10NAnnotations());
        C10N.get(Messages.class);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NoValueMethod {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IntValueMethod {
        int value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Eng {
        String value();
    }

    public interface Messages {
        @SuppressWarnings("UnusedDeclaration")
        @En
        String illegal();
    }
}
