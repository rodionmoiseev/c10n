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

package c10n.resources;

import c10n.C10N;
import c10n.C10NConfigBase;
import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.annotations.Ja;
import c10n.share.Constants;
import c10n.share.util.RuleUtils;
import c10n.share.util.UsingTmpDir;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author rodion
 */
public class ExternalResourceTest {
    private static final String NL = System.getProperty("line.separator");
    @Rule
    public UsingTmpDir tmp = RuleUtils.tmpDir("ExtResTest");
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void loadingExternalResource() throws IOException {
        C10N.configure(new DefaultC10NAnnotations());

        File englishText = new File(tmp.dir, "english.txt");
        FileUtils.writeStringToFile(englishText, "hello" + NL + "world!");

        File japaneseText = new File(tmp.dir, "japanese.txt");
        FileUtils.writeStringToFile(japaneseText, "konnichiwa" + NL + "world!");

        ExtMessages msg = C10N.get(ExtMessages.class);

        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.fromTextFile("substitute"), is("hello" + NL + "world!"));
        assertThat(msg.normalText(), is("english"));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.fromTextFile("substitute"), is("konnichiwa" + NL + "world!"));
        assertThat(msg.normalText(), is("japanese"));
    }

    @Test
    public void largeText() throws IOException {
        C10N.configure(new DefaultC10NAnnotations());
        File englishText = new File(tmp.dir, "english.txt");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            sb.append(i);
            sb.append(" hello" + NL + "world! {0}" + NL);
        }
        FileUtils.writeStringToFile(englishText, sb.toString());

        ExtMessages msg = C10N.get(ExtMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.fromTextFile("substitute"), is(MessageFormat.format(sb.toString(), "substitute")));
    }

    @Test
    public void httpResourceTest() throws Exception {
        final String text = "hello" + NL + "http" + NL + "world!";
        final String path = "/english.txt";
        final int port = 50800;
        HttpServer httpServer = serveTextOverHttp(text, path, port);

        try {
            httpServer.start();

            C10N.configure(new DefaultC10NAnnotations());
            HttpMessages msg = C10N.get(HttpMessages.class);
            Locale.setDefault(Locale.ENGLISH);
            assertThat(msg.textOverHttp(), is(text));

        } finally {
            httpServer.stop(0);
        }
    }

    @Test
    public void malformedUrlThrowsRuntimeException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Could not interpret external resource URL: invalid://url");
        C10N.configure(new DefaultC10NAnnotations());
        Locale.setDefault(Locale.ENGLISH);
        C10N.get(MalformedUrl.class);
    }

    @Test
    public void internalResourceTest() {
        C10N.configure(new DefaultC10NAnnotations());
        InternalMessages msg = C10N.get(InternalMessages.class);

        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.fromInJarTextFile("substitute"), is("Internal resource test!" + NL + "english.txt substitute"));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.fromInJarTextFile("substitute"), is("内部リソーステスト!" + NL + "japanese.txt"));
    }

    @Test
    public void internalResourceThrowsExceptionWhenFileIsNotFound() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("c10n/does/not/exist.txt");
        thrown.expectMessage("does not exist");
        C10N.configure(new DefaultC10NAnnotations());
        C10N.get(NonExistingInternalResource.class).nonExistingFile();
    }

    @Test
    public void parameterisationIsDisabledWhenRawFalseIsPresentForInternalResources() {
        C10N.configure(new DefaultC10NAnnotations());
        RawInternalMessages msg = C10N.get(RawInternalMessages.class);

        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.rawResource("ignored"), is("This text can contain {} {0} {USER}." + NL + "without any problems!"));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.rawResource("ignored"), is("{} {0} {ユーザー}など含んでいても" + NL + "問題ない!"));
    }

    @Test
    public void parameterisationIsDisabledWhenRawFalseIsPresentForExternalResources() throws IOException {
        C10N.configure(new DefaultC10NAnnotations());

        File englishText = new File(tmp.dir, "english.txt");
        FileUtils.writeStringToFile(englishText, "{hello} {}" + NL + "{world}!");

        File japaneseText = new File(tmp.dir, "japanese.txt");
        FileUtils.writeStringToFile(japaneseText, "{konnichiwa} {}" + NL + "{world}!");

        RawExtMessages msg = C10N.get(RawExtMessages.class);

        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.fromTextFile("ignored"), is("{hello} {}" + NL + "{world}!"));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.fromTextFile("ignored"), is("{konnichiwa} {}" + NL + "{world}!"));
    }

    @Test
    public void customAnnotationsWithNoIntResOrExtResAndNotValuesSetThrowsAnException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("class does not have any of 'value' or 'extRes' or 'intRes'");

        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bindAnnotation(Custom.class);
            }
        });

        C10N.get(CustomTest.class).customString();
    }

    @Test
    public void customAnnotationWithOnlyIntResDeclared() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bindAnnotation(CustomWithIntResOnly.class);
            }
        });

        assertThat(C10N.get(CustomTest.class).internalResource(),
                is("Internal resource test!" + NL + "english.txt {0}"));
    }

    @Test
    public void customAnnotationWithOnlyExtResDeclared() throws IOException {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bindAnnotation(CustomWithExtResOnly.class);
            }
        });

        File englishText = new File(tmp.dir, "english.txt");
        FileUtils.writeStringToFile(englishText, "hello, external world!");

        assertThat(C10N.get(CustomTest.class).externalResource(), is("hello, external world!"));
    }

    private HttpServer serveTextOverHttp(final String text, String path, int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        HttpHandler handler = new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = text.getBytes();
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
                        response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        };
        httpServer.createContext(path, handler);
        return httpServer;
    }

    interface ExtMessages {
        @En(extRes = "file:///${java.io.tmpdir}/ExtResTest/english.txt")
        @Ja(extRes = "file:///${java.io.tmpdir}/ExtResTest/japanese.txt")
        String fromTextFile(String arg);

        @En("english")
        @Ja("japanese")
        String normalText();
    }

    interface HttpMessages {
        @En(extRes = "http://localhost:50800/english.txt")
        String textOverHttp();
    }

    interface MalformedUrl {
        @SuppressWarnings("UnusedDeclaration")
        @En(extRes = "invalid://url")
        String illegal();
    }

    interface InternalMessages {
        @En(intRes = "c10n/text/english.txt")
        @Ja(intRes = "c10n/text/japanese.txt")
        String fromInJarTextFile(String arg);
    }

    interface NonExistingInternalResource {
        @En(intRes = "c10n/does/not/exist.txt")
        String nonExistingFile();
    }

    interface RawInternalMessages {
        @En(intRes = "c10n/text/raw_english.md", raw = true)
        @Ja(intRes = "c10n/text/raw_japanese.md", raw = true)
        String rawResource(String argIgnored);
    }

    interface RawExtMessages {
        @En(extRes = "file:///${java.io.tmpdir}/ExtResTest/english.txt", raw = true)
        @Ja(extRes = "file:///${java.io.tmpdir}/ExtResTest/japanese.txt", raw = true)
        String fromTextFile(String ignored);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Custom {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomWithIntResOnly {
        String intRes();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomWithExtResOnly {
        String extRes();
    }

    interface CustomTest {
        @Custom(Constants.UNDEF)
        String customString();

        @CustomWithIntResOnly(intRes = "c10n/text/english.txt")
        String internalResource();

        @CustomWithExtResOnly(extRes = "file:///${java.io.tmpdir}/ExtResTest/english.txt")
        String externalResource();
    }
}
