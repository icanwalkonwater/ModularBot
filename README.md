# ModularBot 2
[![Maven Central](https://img.shields.io/maven-central/v/com.jesus-crie/modularbot-core.svg)](https://search.maven.org/#artifactdetails%7Ccom.jesus-crie%7Cmodularbot-core%7C2.1.0%7Cjar)
[![Javadocs global](https://img.shields.io/badge/javadoc-latest-brightgreen.svg)](http://jesus-crie.com/modularbot/latest)

> This project is at its early stage of development, so any bug reports are
welcome.

> There are probably typos and some language mistakes in this project, if you
see one, notify me.

ModularBot is a kind of little framework for making discord bots with [JDA](https://github.com/DV8FromTheWorld/JDA).

It comes in little modules that can be added to the core and allow you to
customize your installation by not having to compile useless things that you
won't use.

It's a v2 because there's been a long pause since v1 and JDA has changed a lot. 

## Getting Started

You can download each modules with gradle from maven central.
```groovy
repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile 'com.jesus-crie:modularbot-base:2.1.0'
}
```
And now you can register commands and start your bot with:
```java
// Build a new instance of ModularBot with the base modules.
ModularBot bot = new ModularBotBuilder("token")
        .autoLoadBaseModules()
        .build();

// Register a quick command.
CommandModule module = bot.getModuleManager().getModule(CommandModule.class);
module.registerCreatorQuickCommand("stop", e -> bot.shutdown());

bot.login();
```
That's all the code required to make ModularBot work with commands.

## Modules

For simplicity, all of the modules (made by me) uses the same version
name, so if version 2.1.0 is released for a module, **ALL** other modules
will be updated to this version. So you can use a global variable do define
the version of ModularBot that you want to use.

All of the modules made by me are in this github repository and available
on [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.jesus-crie%22).

To enable a module, add the corresponding artifact (+ version) to your
gradle/maven dependencies and enable it in the `ModularBotBuilder` with
the method `ModularBotBuilder#autoLoadBaseModules()` that will look for the
"official" modules and load them automatically.

Note that when the associated instance of `ModularBot` is created, you can't
register modules anymore.

You can query a module from the `ModuleManager` (accessible with
`ModularBot#getModuleManager()`) by providing the main class of the module
(usually easy to find, ends with "Module" and located at the root of the package).
For example:
```java
ModularBot bot = ...;
ModuleManager moduleManager = bot.getModuleManager();

CommandModule module = moduleManager.getModule(CommandModule.class);
```

For custom modules, look [here](#your-custom-module).

### Available modules

> The modules with a * are included in the [Base](#Base) module

#### Base

> *Artifact: `com.jesus-crie:modularbot-base`.*

There is no additional code in this module other than the code provided by
the modules [Core](# core*), [Logger](#console-logger*) and [Command](#command*).

This is basically a shortcut to import these 3 modules in one line.

#### Core*

[![Javadocs core](http://www.javadoc.io/badge/com.jesus-crie/modularbot-core.svg?label=javadoc-core)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-core)

> *Artifact: `com.jesus-crie:modularbot-core`.*

If you want only the base code without any modules you can use this artifact.

Use it if you want to use another command system or another implementation
of SLF4J. It only contains the classes necessary to use JDA and the module manager.

#### Console Logger*

[![Javadocs logger](http://www.javadoc.io/badge/com.jesus-crie/modularbot-logger.svg?label=javadoc-logger)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-logger)

> *Artifact: `com.jesus-crie:modularbot-logger`.*

Provides an implementation of [SLF4J](https://www.slf4j.org/).

You can use a logger like this:
```java
Logger logger = LoggerFactory.getLogger("Some Name");
logger.info("Hi mom");
```

You can customize the output of the logger by modifying the two variables
in `ConsoleLoggerModule`.

The default value for `FORMAT_LOG` makes logs look like this:
```
[16:11:01] [Info] [main] [ModularBot]: Starting shards...
[HH:mm:ss] [Level] [Thread] [Logger name]: Message
```

With this module, each log is an instance of `ModularLog` that provide all of
the necessary information about a specific log. You can listen to them by
registering a listener using `ModularLogger#addListener`.

#### Command*

[![Javadocs command](http://www.javadoc.io/badge/com.jesus-crie/modularbot-command.svg?label=javadoc-command)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-command)

> *Artifact: `com.jesus-crie:modularbot-command`*

This module provide a complete command system. Commands that looks like
`!command arg1 arg2 "arg 3" --explicit-option arg -i -o "implicit options"`

With this system each command need to have a dedicated class that extends
`Command`. You can provide basic information about this command using the
`@CommandInfo` annotation this class to avoid using a constructor.
 
Note that only the constructors `Command#Command()` and
`Command#Command(AccessLevel)` uses the annotation.

> Note that if you want to specify an `AccessLevel` you need to use a constructor.

The `AccessLevel` of a command is a set of prerequisites that a user need to
satisfy before using a command. It contains a set of permissions that the user
need to satisfy if the command is executed in a guild, plus some flags and
the ID of an user if you want to authorize only one person. However you can
still override the method `AccessLevel#check(CommandEvent)` and implement
your own checks.

> The way that `AccessLevel`s are made is a bit crappy so expect changes.

Each command have a set of `CommandPattern`s that correspond to a certain
manner to type a command. These patterns can be found automatically when
the command class is instantiated* by looking at the methods in the class
annotated with `@RegisterPattern`.

Note that if you want to take full advantage of this system you need to provide
the argument `-parameters` to your compiler to be able to read the names of
your method parameters.

With this system a command that have this syntax `!command <@User> add <String>`
can be automatically registered by a method signature like this:
```java
@RegisterPattern
protected void someMethod(CommandEvent event, Options options, User user, Void add, String string) {}
```
or:
```java
@RegisterPattern(arguments = {"USER", "'add'", "STRING"})
protected void someMethod(CommandEvent event, Options options) {}
```
Note that the strings provided in the annotations (except for the second) are
the names of constants in the [Argument class](./ModularBot-Command/src/main/java/com/jesus_crie/modularbot_command/processing/Argument.java).
You can register your own class that contains such constants annotated with
`@RegisterArgument` with the method `Argument#registerArguments(Class)`.

There is a variety of possibility to make such methods, all of them can be
found in this [Test class](./ModularBot-Command/src/test/java/com/jesus_crie/modularbot_command/CommandTest.java).

> *: It's planned to do this at compile-time but for now it happens basically
when the command is registered so when the bot is waking up.

Each command can accept a certain set of `Option`s provided in the constructor
or in the `@CommandInfo`. These options are totally optional and to not
appear in the `CommandPattern`s. These are added at the end of the command
implicitly (`-f`) or explicitly (`--force`).

Explicit options need to be prefixed with `--` and the long name of the option
whereas implicit ones are prefixed by `-` and followed by one or more letters
each representing the short name of an option. If they are followed by a string
it will be considered as the argument of the option (or the last in implicit
options).

Once parsed these options are accessible through the `Options` object provided
along the arguments to the patterns. The argument of each option is also
present.

Note that like the `Argument`s, all of the `Option`s names are constants in
the [Option class](./ModularBot-Command/src/main/java/com/jesus_crie/modularbot_command/processing/Option.java)
and you can register your own constants with `Option#registerOptions(Class)`.

**Experimental:** In the `CommandModule` you can set flags to the command
processor to modify the behaviour of the algorithm but it's experimental
and can lead to unexpected behaviour. This feature isn't a priority so if
your're a volunteer you can fork this repo and send a pull request.

Finally, you can listen to the success or the failure of a command typed by a
user by registering your own `CommandListener` with `CommandModule#addListener`.

#### Night Config Wrapper

[![Javadocs config](http://www.javadoc.io/badge/com.jesus-crie/modularbot-night-config-wrapper.svg?label=javadoc-night-config-wrapper)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-night-config-wrapper)

> *Artifact: `com.jesus-crie:modularbot-night-config-wrapper`*

This module uses [NightConfig 3.1.1](https://github.com/TheElectronWill/Night-Config)
to load, parse and save config files. You will be forces to have a "primary"
config file and you can have multiple secondary config files.

Note that the default config file contains information that will be delivered
to the CommandModule like the "creator_id" and a list of custom prefixes for
guilds. Note that the creator id will only be loaded whereas the custom
prefixes will be loaded and saved when the module is unloaded.

By default, the primary config will be created like this:
```java
FileConfigBuilder builder = FileConfig.builder("./config.json")
        .defaultRessource("/default_json.json")
        .autoReload()
        .concurrent();
FileConfig primaryConfig = builder.build();
```

But the `#build()` method will be called only at the initialisation so you
can still customize the builder in the meantime using `NightConfigWrapperModule#customizePrimaryConfig()`

You can also totally override this config by using the other constructor
which will only create a builder with the given path without any alteration.

There are also "secondary" config files that you can register and can exist
alongside the primary config file. You can also load an entire folder of
config file and it will create a "group" of config files that you can
query back whenever you want but querying an entire group can be costly
depending of how many files you have.

You can use secondary config files like this:
```java
NightConfigWrapperModule module = ...;

// Regular secondary config
module.useSecondaryConfig("levels", "./levels.json");

// Load an entire config group but only the .json files and not the subfolders
module.loadConfigGroup("users", "./users/", false, "^.+\\.json$")
// or simply
module.loadConfigGroup("users", "./users/")
```

Then you can query the secondary configs by their names, the names for a
config group will be `[group name].[filename]`, for exemple `users.michel.json`
for the config file located at `./users/michel.json` and loaded by one of the
`#loadConfigGroup()` methods.

This module is entirely based on [Night Config](https://github.com/TheElectronWill/Night-Config)
and I hardly recommend to read its documentation.

#### Audio

TODO

#### Eval

TODO

#### JS Nashorn support

TODO

### Your custom module

If you want to create your own module, you can by simply extending
`BaseModule`, providing information about your module.
> Don't forget to enable it in with `ModularBotBuiler#registerModule()`.

You can now implement the methods from `Lifecycle` to allow your module to
be notify when something important happens. Every callback method is
documented in the class.

> **Nothing can prevent a malicious module from stealing your token using
reflection. And there are no efficient way to prevent reflection.**

You can register a custom module like that
```java
ModularBotBuilder builder = ...;

builder.registerModules(new ClassThatExtendsBaseModule());
// or
builder.registerModules(ClassThatExtendsBaseModule.class);
```