Cosmopolitan -- C10N
====================

*Note:* C10N is still alpha. API may (most likely will) change.

A Java library, focused on making internationalisation more modular, easier
to evolve and maintain, robust-to-change and IDE-friendly without excess of
external tools.

Usage example
-------------

Usage can be vaguely split into two flavours: logger-like and [guice][guice]-based (or your favourite dependency injection framework). The latter being the recommended approach :)

### Declaring message

First, all messages that would typically reside in a message bundle become an interface:

```java
public interface MyApp {
  @En("Hello, C10N-app!")
  String title();

  @En("Rodion")
  String author();
}

public interface Buttons {
  @En("OK")
  String ok();

  @En("Cancel")
  String cancel();
}


public interface ConfirmationDialog extends MyApp, Buttons {
  @En("Don't go there!")
  String warningText();

  @En("Abort")
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

There are few possible options:

1. Annotate interface methods with translated strings. C10N will
   choose the correct string based on the configured locale mapping.
   This method works well if you don't have a requirement for storing
   all messages in resource bundle files. Also all messages will have
   to have the same encoding, which means you are stuck with Unicode.
2. Implement the message interfaces manually. For the brave.
   There aren't many real advantages for this method, so I am not
   sure if I should keep support for this.
3. Bind message interfaces to a message bundle(s).

#### Storing translations in source code

Personally I would prefer this approach as it is completely
refactoring-proof, clean, and does not require any tools.

First you'll need to declare a set of annotations for each of the
locale your software will support. 

Let's create two for English and Russian. Note that the annotation
must declare a `String value()` field.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface En {
  String value();
}
	
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ru {
  String value();
}
```

Then you need to bind each of your annotations to a specific locale
(has to be done before your messages are accessed):

```java
C10N.configure(new AbstractC10NConfiguration(){
  @Override
  public void configure() {
    //Omitting locale binding instructs c10n to fallback to
    //this annotation when other locales did not match.
    bindAnnotation(En.class);
    bindAnnotation(Ru.class).toLocale(new Locale("ru","RU"));
  }
});
```

C10N setup is complete. Now you can start declaring interfaces and 
provide translations for each of the locale using the declared annotations:

```java
public interface Buttons{
  @En("OK")
  @Ru("Да")
  String ok();
  
  @En("Cancel")
  @Ru("Отмена")
  String cancel();
  
  @En("Are you sure you want to {0}?")
  @Ru("Вы уверены вы хотите {0}?")
  String confirmationDialog(String action);
}
```

*Note:* Method parameters are substituted as specified by the `java.text.MessageFormat#format`.

#### Binding messages to a resource bundle

Create the c10n message interface. You may choose to leave the default translations
in the source code.

```java
package org.mycompany.myapp;

@C10NMessages
public interface Buttons{
  @En("OK")
  String ok();
  
  @En("Cancel")
  String cancel();
  
  @En("Are you sure you want to {0}?")
  String confirmationDialog(String action);
}
```

Create a resource bundle with the translations (Russian in our case). 
Initial set of resource bundles can be generated using the supplied
command-line utility or a [Gradle][gradle] task:

```
-- src/resources/org/mycompany/myapp/Resources_ru.properties
org.mycompany.myapp.Buttons.ok=Да
org.mycompany.myapp.Buttons.cancel=Отмена
org.mycompany.myapp.Buttons.confirmationDialog_String=Вы уверены вы хотите {0}?
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

Note that `@En` is optional. You can choose to put messages
for the default language into the resource bundle, keep them in
the source code, or both. Keeping default messages in source
code makes it easier to reference message content (in Eclipse
and IntelliJ IDEA a `Ctrl+click` on the method will take you
to where it's declared. Personally I find it easier than 
searching through the resource bundle files).

#### TODOs
* Docs on resource bundle generation

  [guice]: http://code.google.com/p/google-guice/  "Google Guice"
  [gradle]: http://www.gradle.org/                 "Gradle build tool"