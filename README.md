Cosmopolitan -- C10N
====================
[![Build Status](https://travis-ci.org/rodionmoiseev/c10n.svg?branch=master)](https://travis-ci.org/rodionmoiseev/c10n)

A Java library, focused on making internationalisation more modular, easier
to evolve and maintain, robust-to-change and IDE-friendly without excess of
external tools. See [the guide][C10NGuide] for more information.

Motivation
----------

Compare the traditional approach using resource bundles to C10N:

### Using resource bundles

First, you need to create the individual bundle files with all translations:

```
 # Messages_en.properties
com.example.gui.window.title = Hello, {0}!
com.example.gui.window.buttons.ok = OK
com.example.gui.window.buttons.cancel = Cancel
```

```
 # Messages_ru.properties
com.example.gui.window.title = Привет, {0}!
com.example.gui.window.buttons.ok = Да
com.example.gui.window.buttons.cancel = Отмена
```

Then reference messages by their keys from the source code:

```java
ResourceBundle msg =  ResourceBundle.getBundle("Messages");
//get the message
String ok = msg.getString("com.example.gui.window.buttons.ok");
//get message with parameters
String title = MessageFormat.format(
  msg.getString("com.example.gui.window.buttons.ok"), "James");
```

Doable, but clumsy, and needs extra maintenance for keys.

### Using C10N

C10N uses a more java-esque approach: each message is declared as a
method on an interface, with all translations stored in annotations.

```java
package com.example.gui;

public interface Window{
  @En("Hello, {0}!")
  @Ru("Привет, {0}!")
  String title(String userName);
  
  @En("OK")
  @Ru("Да")
  String ok();
  
  @En("Cancel")
  @Ru("Отмена")
  String cancel();
}
```

To retrieve messages

```java
Window msg = C10N.get(Window.class);
//get message
String ok = msg.ok();
//get message with parameter
String title = msg.title("James");
```

Not only it is much simpler, you will never make a spelling mistake in your key, or miss a message parameter.

If required, you can opt to store all or some of you translations in resource bundles. For more details see the documentation.

Documentation
-------------

Download the latest 1.3 release from the [download section][C10NDownload]. 

* [C10N Guide][C10NGuide]
* [All Features][C10NFeatures]
* [Download C10N][C10NDownload]
* [Contributing][C10NContributing]
* Scala related documentation
 * [Scala Integration][C10NScalaIntegration]
 * [Play Framework 2.0 Integration Example][C10NPlayIntegration]

  [C10NGuide]: https://github.com/rodionmoiseev/c10n/wiki/Overview "C10N Wiki: Guide"
  [C10NDownload]: https://github.com/rodionmoiseev/c10n/wiki/Download "Download C10N"
  [C10NFeatures]: https://github.com/rodionmoiseev/c10n/wiki/Features "C10N Wiki: Features"
  [C10NContributing]: https://github.com/rodionmoiseev/c10n/wiki/Contributing "C10N Wiki: Contributing"
  [C10NScalaIntegration]: https://github.com/rodionmoiseev/c10n/wiki/Scala-Integration "C10N Wiki: Scala Integration"
  [C10NPlayIntegration]: https://github.com/rodionmoiseev/c10n/wiki/Play-Framework-2.0-Integration "C10N Wiki: Play Framework 2.0 Integration"
  
Dependencies and Compatibility
------------------------------

Major project dependencies are listed below:

| C10N Version | Java        | Gradle | Guice (for guice-plugin users) |
|--------------|-------------|--------|------------------------|
| >= 2.0       | OpenJDK 11  | 6.x    | 4.x                    |
| 1.3          | OracleJDK 8 | 2.x    | 4.x                    |
| 1.2          | OracleJDK 6 | 2.x    | 3.x                    |
| < 1.2        | OracleJDK 6 | 1.x    | 3.x                    |
