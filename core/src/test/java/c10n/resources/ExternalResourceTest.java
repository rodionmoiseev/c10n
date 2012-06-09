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
import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.annotations.Ja;
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
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class ExternalResourceTest {
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
    FileUtils.writeStringToFile(englishText, "hello\nworld!");

    File japaneseText = new File(tmp.dir, "japanese.txt");
    FileUtils.writeStringToFile(japaneseText, "konnichiwa\nworld!");

    ExtMessages msg = C10N.get(ExtMessages.class);

    Locale.setDefault(Locale.ENGLISH);
    assertThat(msg.fromTextFile(), is("hello\nworld!"));
    assertThat(msg.normalText(), is("english"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.fromTextFile(), is("konnichiwa\nworld!"));
    assertThat(msg.normalText(), is("japanese"));
  }

  @Test
  public void largeText() throws IOException {
    C10N.configure(new DefaultC10NAnnotations());
    File englishText = new File(tmp.dir, "english.txt");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1024; i++) {
      sb.append(i);
      sb.append(" hello\nworld!\n");
    }
    FileUtils.writeStringToFile(englishText, sb.toString());

    ExtMessages msg = C10N.get(ExtMessages.class);
    Locale.setDefault(Locale.ENGLISH);
    assertThat(msg.fromTextFile(), is(sb.toString()));
  }

  @Test
  public void httpResourceTest() throws Exception {
    final String text = "hello\nhttp\nworld!";
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
    assertThat(msg.fromInJarTextFile(), is("Internal resource test!\r\nenglish.txt"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.fromInJarTextFile(), is("内部リソーステスト!\r\njapanese.txt"));
  }

  @Test
  public void internalResourceThrowsExceptionWhenFileIsNotFound() {
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("c10n/does/not/exist.txt");
    thrown.expectMessage("does not exist");
    C10N.configure(new DefaultC10NAnnotations());
    C10N.get(NonExistingInternalResource.class).nonExistingFile();
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
    String fromTextFile();

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
    String fromInJarTextFile();
  }

  interface NonExistingInternalResource {
    @En(intRes = "c10n/does/not/exist.txt")
    String nonExistingFile();
  }
}
