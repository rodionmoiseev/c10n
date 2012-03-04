Cosmopolitan -- C10N
====================

A Java library, focused on making internatiolisation more modular, easier
to evolve and maintain, robust-to-change and IDE-friendly without excess of
external tools.

Usage example
-------------

Usage can be vaguely split into two flavours: logger-like and [guice][guice]-based (or your favourite dependency injection framework). The latter being the recommended approach :)

### Declaring message

First, all messages that would typically reside in a message bundle become an interface:

```java
@C10NMessages
public interface MyApp {
  @C10NDef("Hello, C10N-app!")
  String title();

  @C10NDef("Rodion")
  String author();
}

@C10NMessages
public interface Buttons {
  @C10NDef("OK")
  String ok();

  @C10NDef("Cancel")
  String cancel();
}

@C10NMessages
public interface ConfirmationDialog extends MyApp, Buttons {
  @C10NDef("Don't go there!")
  String warningText();

  @C10NDef("Abort")
  String abort();
}
```

Retrieving message content can now be down in two ways. First of all the logger-like approach:

```java
public class MyAppWindow {
  private static final MyApp msg = C10N.get(MyApp.class);
  private final JFrame frame = new JFrame(msg.title());
  private final JLabel authorLabel = new JLabel(msg.author());
  ...
}

public class ConfirmationDialogWindow {
  private static final ConfirmationDialog msg = C10N.get(ConfirmationDialog.class);
  private final JFrame frame = new JFrame(msg.title());
  private final JLabel warningText = new JLabel(msg.warningText());
  private final JButton okButton = new JButton(msg.ok());
  private final JButton cancelButton = new JButton(msg.cancel());
  private final JButton abortButton = new JButton(msg.abort());
  ...
}
```

If you are a [Guice][guice] user messages can be auto-injected:

```java
public class MyAppWindow {
  @Inject
  public MyAppWindow(MyApp msg){
    frame = new JFrame(msg.title());
    authorLabel = new JLabel(msg.author());
    ...
  }
}

public class ConfirmationDialogWindow {
  @Inject
  public ConfirmationDialogWindow(ConfirmationDialog msg){
    frame = new JFrame(msg.title());
    warningText = new JLabel(msg.warningText());
    okButton = new JButton(msg.ok());
    cancelButton = new JButton(msg.cancel());
    abortButton = new JButton(msg.abort());
    ...
  }
}
```

### How does C10N find the messages?

Again, a few options here, depending on your philosophy.

1. Implement the message interfaces manually.
   For a small project this may be a quick and dirty way to get it
   done and out of the way. C10N can be configured to choose the
   correct implementation based on the current locale.
2. Bind message interfaces to a message bundle(s).
3. When requested message cannot be found using one of the above
   the value specified in the `@C10NDef("Default value")` will be
   used.

#### Manual message binding 

This is just for the sake of demostration, and is not recommended 
for medium to big projects. Please use the message bundle approach instead.

Implement the interface

```java
@C10NMessages
public interface Buttons{
  String ok();
  String cancel();
}

class EnglishButtons implements Buttons{
  String ok(){ return "OK"; }
  String cancel(){ return "Cancel"; }
}

class RussianButtons implements Buttons{
  String ok(){ return "Да"; }
  String cancel(){ return "Отмена"; }
}
```

Now bind it (somewhere near the `main`):

```java
C10N.configure(new AbstractC10NConfiguration(){
  @Override
  public void configure() {
    bind(Buttons.class)
      .to(EnglishButtons.class, Locale.ENGLISH)
      .to(RussianButtons.class, Locale.RUSSIAN);
  }
});
```

#### Binding messages to a resource bundle

Create the c10n message interface:

```java
package org.mycompany.myapp;

@C10NMessages
public interface Buttons{
  @C10NDef("OK")
  String ok();
  
  @C10NDef("Cancel")
  String cancel();
}
```

Create a resource bundle with the translations (Russian in our case):

```
-- src/resources/org/mycompany/myapp/Resources_ru.properties
org.mycompany.myapp.Buttons.ok=Да
org.mycompany.myapp.Buttons.cancel=Отмена
```

Tell c10n to look for messages in our bundle:

```java
C10N.configure(new AbstractC10NConfiguration() {
  @Override
  public void configure() {
    bindBundle("org.mycompany.myapp.Resources");
  }
});
```

Note that `@C10NDef` is optional. You can choose to put messages
for the default language into the resource bundle, keep them in
the source code, or both. Keeping default messages in source
code makes it easier to reference message content (in Eclipse
and IntelliJ IDEA a `Ctrl+click` on the method will take you
to where it's declared. Personally I find it easier than 
searching through the resource bundle files).

  [guice]: http://code.google.com/p/google-guice/  "Google Guice"