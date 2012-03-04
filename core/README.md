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

TODO

  [guice]: http://code.google.com/p/google-guice/  "Google Guice"