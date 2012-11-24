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

import c10n.test.utils.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CustomImplementationBindingTest {
    @Rule
    public static TestRule tmpLocale = RuleUtils.tmpLocale();
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Test
    public void localeBinding() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bind(Labels.class)
                        .to(LabelsEng.class, Locale.ENGLISH)
                        .to(LabelsJapanese.class, Locale.JAPANESE);
            }
        });
        Locale.setDefault(Locale.ENGLISH);
        Labels msg = C10N.get(Labels.class);
        assertThat(msg.label(), is(equalTo("English")));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.label(), is(equalTo("Japanese")));
    }

    @Test
    public void metricSystem() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bind(Units.class).to(ImperialUnits.class, Locale.UK);
            }
        });

        Units msg = C10N.get(Units.class);

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.distance(0.91f), is("0.91 meters"));
        Locale.setDefault(Locale.UK);
        assertThat(msg.distance(0.91f), is("1.0 yards"));
    }

    @Test
    public void metricSystemFallBackToImplementation() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bind(Units.class)
                        .to(ImperialUnits.class, Locale.UK)
                        .to(MetricUnits.class);
            }
        });

        Units msg = C10N.get(Units.class);
        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.distance2(0.91f), is("0.91 meters"));
        Locale.setDefault(Locale.UK);
        assertThat(msg.distance2(0.91f), is("1.0 yards"));
    }

    @Test
    public void customImplementationBindingsInChildConfigs() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new C10NConfigBase() {
                    @Override
                    protected void configure() {
                        bind(Units.class).to(ImperialUnits.class, Locale.UK);
                    }
                });
                bind(Units.class).to(MetricUnits.class);
            }
        });

        Units msg = C10N.get(Units.class);
        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.distance2(0.91f), is("0.91 meters"));
        Locale.setDefault(Locale.UK);
        assertThat(msg.distance2(0.91f), is("1.0 yards"));
    }

    @C10NMessages
    interface Labels {
        String label();
    }

    static class LabelsEng implements Labels {
        @Override
        public String label() {
            return "English";
        }
    }

    static class LabelsJapanese implements Labels {
        @Override
        public String label() {
            return "Japanese";
        }
    }

    interface Units {
        @C10NDef("{0} meters")
        String distance(float amount);

        String distance2(float amount);
    }

    static class ImperialUnits implements Units {
        @Override
        public String distance(float amount) {
            return Float.toString(amount / 0.91f) + " yards";
        }

        @Override
        public String distance2(float amount) {
            return distance(amount);
        }
    }

    static class MetricUnits implements Units {
        @Override
        public String distance(float amount) {
            return Float.toString(amount) + " meters";
        }

        @Override
        public String distance2(float amount) {
            return distance(amount);
        }
    }
}