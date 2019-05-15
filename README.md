# ModularBot
[![Maven Central](https://img.shields.io/maven-central/v/com.jesus-crie/modularbot-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.jesus-crie%22)

> There are probably typos and some language mistakes in this project, if you
see one, notify me.

ModularBot is a framework aimed at anyone that would like to create discord bot. It uses
[JDA](https://github.com/DV8FromTheWorld/JDA) to interface with discord, otherwise it provides
a robust mechanism of modules that enables you to create tiny to large scale projects with many features
and at the same time, provides you a organized and transparent way to manage them and their interactions.

It's modularity allows you to build only the modules you want to use instead of everything which means
with the correct build configuration, you will be able to split your application in small chunks and
leverage the amount of code that will be rebuilt and uploaded to your hosting service.

The full framework comes in small modules that allows you to choose the exact features you want.

Side note: since v2.5.0, a module has been released that enables [GraalVM](https://www.graalvm.org/)
users to write and build modules in every languages provided by GraalVM which means you are able to
write modules in any language with the robustness of JDA.

1. [Getting Started](#getting-started)
2. [Modules](#modules)
    1. [Writing a Module](#writing-a-module)
        1. [Quickstart](#quickstart)
        2. [Lifecycle Hooks](#lifecycle-hooks)
        3. [Injecting another Module](#injecting-another-module)
        4. [Passing Parameters to your Module](#passing-parameters-to-your-module)
        5. [Registering/Requesting your Module](#registeringrequesting-your-module)
    2. [Dependency Injection (DI)](#dependency-injection-di)
        1. [Requiring another Module (`@InjectorTarget`)](#requiring-another-module-injectortarget)
        2. [Late injections and circular dependencies (`@LateInjectorTarget`)](#late-injections-and-circular-dependencies-lateinjectortarget)
        3. [Building modules manually](#building-modules-manually)
    3. [Available Modules](#available-modules)
        2. [Core](#core)
        3. [Console Logger](#console-logger)
        4. [Command](#command)
        5. [Night Config Wrapper](#night-config-wrapper)
        6. [~~JS Nashorn Support~~](#js-nashorn-support)
        7. [~~JS Nashorn Command Support~~](#js-nashorn-command-support)
        8. [Message Decorator](#message-decorator)
        9. [GraalVM Support](#graalvm-support)
        10. [GraalVM Support DiscordJS](#graalvm-support-discordjs)

## Getting Started

The framework is available on Maven Central but you still need to add JCenter to your repositories
to be able to download JDA dependencies.
```groovy
repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation 'com.jesus-crie:modularbot-core:2.5.0_23'
    implementation 'com.jesus-crie:modularbot-logger:2.5.0_23'
    implementation 'com.jesus-crie:modularbot-command:2.5.0_23'
}
```
As every module of the framework has the same version you can define a variable with the version to
update every module at the same time.

Now you can build your first instance of `ModularBot` using the `ModularBotBuilder` class which
mirrors the behaviour of the classic `JDABuilder` but with additional methods.
```java
// Build a new instance of ModularBot with the base modules.
ModularBot bot = new ModularBotBuilder("token")
        .requestBaseModules() // Explicitly request every default modules
        .resolveAndBuild(); // If you use only #build(), the modules will not be instantiated !

// Register a quick command.
CommandModule module = bot.getModuleManager().getModule(CommandModule.class);
module.registerCreatorQuickCommand("stop", e -> bot.shutdown());

bot.login();
```
> Note that this is a great way to start experiencing things but not the clean way to use the framework.

## Modules

Each module of your application is typically a collection of related utilities that depends on the same
modules or provides the same kind of services. Each module is a singleton.

The usage of 'request' for saying that you want to use a module refers to the fact that you are actually
asking the DI to provide you an instance of a given module.

All of the modules in this github repository are available on
[Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.jesus-crie%22).

### Writing a module

To get a first glance of what a module is, it way be usefull the start writing one on your own to better
understand how to manipulate them.

#### Quickstart

Modules are just subclasses of `Module` and basically that's all that you need to create your first module.
```java
public class MyModule extends Module {
    
    public MyModule() {
        // Hello world !
    }
}
```

Event the default constructor is optional here !

Note that just that is not enough to tell the framework to create your module, we will register it in
the last section of the chapter. Note also that you will almost never call this constructor directly,
so don't make complex constructor with lots of overloads and all. Only one will be used.

#### Lifecycle hooks

But what if you want to do something when your bot starts ? Fortunately, `Module` implement `Lifecycle`
which defines some lifecycle hooks that you can use to do things at certain steps of the bootstrapping of
your bot until its shutdown. 

For example you can override the method `Lifecycle#onLoad()` to perform some initialization just after
the module has been constructed and before the bot is created. Other hooks includes
`Lifecycle#onShardsReady()`, triggered when the bot comes online and is ready to go.

The full listing of the callbacks is listed in the interface `Lifecycle`.

#### Injecting another module

You now have a module that can interact with the bot at any step of its lifecycle and thats great by
you may want to split your code in multiple modules and communicate between them, or just use the
default modules available like, for example the `CommandModule`, available in the `modularbot-command`
maven artifact.

There are to things required to inject a module:
- First, you need to create a constructor that has the wanted module has one of its parameters.
- Annotate this constructor with `@InjectorTarget` to tell the DI to use this constructor.

**Important**: You can only have **one** annotated constructor and no more.

For example if you want to write a module that register a command in the `CommandModule`, it will
look like this:
```java
public class MyModule extends Module {
    
    @InjectorTarget
    public MyModule(CommandModule cmdModule) {
        cmdModule.registerCreatorQuickCommand("ping", e -> e.fastReply("Pong !"));
    }
}
```

Note that if your module is not explicitly request nor injected in another module, it will never be
instantiated by the DI.

#### Passing parameters to your module

You may want to make a configurable module and inject these settings in the constructor. The surprise
is: you can ! You can define any other type of parameters in your constructor and pass arguments to
them. Consider this module:
```java
public class WelcomeModule extends Module {
    
    public WelcomeModule(NighConfigWrapperModule cfgModule, String messageFormat) {
        // ...
    }
    
    // ...
}
```
This module needs the config module to work, which will be provided automatically, but also need
a string as its second parameter, which the framework can't guess.

You can define what will be the value passed this parameter in the `ModularBotBuilder` with
`ModularBotBuilder#configureModule()`:
```java
ModularBot bot = new ModularBotBuilder("token")
    .requestModules(WelcomeModule.class)
    .configureModule(WelcomeModule.class, "Welcome %s !")
    .resolveAndBuild();
```

You can also specify default parameters in the module class by creating a static field of type
`ModuleSettingsProvider` and annotating it with `@DefaultInjectionParameters`. These settings will be
used if to call to `#configureModule()` has been made !

The order of your arguments matters, you need to provide them in the correct order but the presence of
modules to be injected doesn't matter at all.

#### Registering/Requesting your module

If your module can't be registered implicitly (aka injected in another module), you need to do it
yourself in the builder like:
```java
ModularBot bot = new ModularBotBuilder("token")
    .requestModules(
            MyAwesomeModule.class,
            AnotherModule.class
    )
    .resolveAndBuild();
```
> Don't forget to resolve the modules using one of `#resolveModules()`, `#resolveModulesSilently()` or
> `#resolveAndBuild()`.

Note that `#requestBaseModules()` can be used to request all of the default/official modules.

### Dependency Injection (DI)

This dependency injector is a bit inspired of the way that angular module injection is used, defining a
constructor with the requested modules, declaring the module and boom you have everything you want.

This section will not talk of the internals of this feature but rather of its usage.

#### Requiring another module (`@InjectorTarget`)

Like you saw in the last section, you can request another module implicitly by *requiring* it. It means
writing a constructor with the annotation and the module in question as a parameter.

You can require as many modules as you want in this constructor, in theory you can even request the
module twice or more as long as there have these requirements:
- They need to extend `Module` of course, otherwise they will be treated like common parameters and not
injections.
- They are real modules, you can't inject a module by only knowing one of its superclasses which isn't
instantiable.

About the annotation (`@InjectorTarget`), there need to be **exactly one** constructor annotated by
module, otherwise an exception will be thrown.
> The only exception to that is the empty constructor, if the **only** constructor of your module is
> a constructor without any parameters or the default constructor, you the annotation is optional.

#### Late injections and circular dependencies (`@LateInjectorTarget`)

If for some reason you have a circular dependency, the DI will throw an exception. A circular dependency
is described by the fact that a module A requires module B that in some way also required module A.
It can be a simple loop, `A -> B -> A -> ...` or a bigger loop like `A -> C -> B -> A -> ...`

If such a loop shows up, the natural thing is to rethink your architecture a bit in my opinion. If for
some reasons you can't, you can still take advantage of the late injection mechanism.

Late injection takes place after *every* requested module has been instantiated. The DI will look for
methods and fields annotated with `@LateInjectorTarget` and extract required modules from the method
signature / the field type. Unlike the main injector target, parameters other than modules are considered
an error and the whole method/field will be ignored.

If the target is valid and **the module has already been built**, it will be assigned to the field /
passed to the method. **The late injection will not build any additional modules !!** A late injector
method can contains multiple injections.

Back to the case of the circular dependency, if you have something like `A -> B -> A`, you can choose
to late inject one of the two. For the example, A will be late injected into B.

You will end up with something like:
```java
public class A extends Module {
    
    // Main target
    @InjectorTarget
    public A(B moduleB) {}
}

public class B extends Module {
    
    // Late inject via a field
    @LateInjectorTarget
    private A moduleA;
    
    // Main target
    @InjectorTarget
    public B() {}
    
    // Late inject via a method
    @LateInjectorTarget
    public void whatever(A moduleA) {}
}
```
> For the sake of the example, both a field and a method late injector target are used. This can work
> but just don't.

Here, module A require the module B in its target which lead to the instantiation of module B. Module B
doesn't require anything from the point of view of the main target so it can be instantiated without
any constraint and module A requirements are fulfilled and A can be instantiated.

After everyone has been instantiated, the late injection come in place and sees the field target, query
the corresponding module (A) and set its value to it. Then it looks for method targets, find the method
and fulfill its dependency like for the field.

If you've been paying attention, you will notice something a little problematic with this solution. If
We request only module B, module A will never be instantiated because it is never referenced in the main
injector target and the late injection will just throw a warning when it will see that it hasn't the
module A ready. So for this to work, you need to request the module A, which *require* module B.

You can also take advantage of this late injection mechanism to make optional dependencies.

#### Building modules manually

The DI allows you to provide already built modules if for some reason you want to provide you own
instance of a module.

This is particularly useful for the logger module which need to be built to start receiving logs.
```java
ModularBot bot = new ModularBotBuilder("token")
    .provideBuiltModules(
            new ConsoleLoggerModule()
    )
    .resolveAndBuild();
```

### Available modules

ModularBot provides a few default modules that covers the primary needs of any discord bot such as config
files, commands, ... 

#### Core
[![Javadocs core](http://www.javadoc.io/badge/com.jesus-crie/modularbot-core.svg?label=javadoc-core)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-core)
> *Artifact: `com.jesus-crie:modularbot-core`.*

This is the core of the framework, its contains the main classes like `ModularBot`, `Module` and the DI.
If you want only the base code without any modules you can use this artifact.

It doesn't have any default logger, like the rest of the framework it uses the slf4j logger which
a custom implementation is provided in the logger module and works out of the box without any
configuration.

#### Console Logger
[![Javadocs logger](http://www.javadoc.io/badge/com.jesus-crie/modularbot-logger.svg?label=javadoc-logger)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-logger)
> *Artifact: `com.jesus-crie:modularbot-logger`.*

Provides an implementation of [SLF4J](https://www.slf4j.org/).

Works out of the box without the need of any configuration. But you can still configure the message
format and the log level via the static variables in the module class.

Loggers are typically constants in the class where they are declared.

A typical setup for a logger looks like:
```java
public class MyClass {
    private static final Logger LOG = LoggerFactory.getLogger("MyClass");
    
    public void whatever() {
        LOG.info("I like trains");
    }
}
```

You can listen to logs by yourself by adding a listener using the static `ModularLogger#addListener()`
method.

#### Command
[![Javadocs command](http://www.javadoc.io/badge/com.jesus-crie/modularbot-command.svg?label=javadoc-command)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-command)
> *Artifact: `com.jesus-crie:modularbot-command`*

This module provide a complete command system to craft commands that looks like

`!command arg1 arg2 "arg 3" --explicit-option arg -i -o "implicit options"`

With this system each command need to have a dedicated class that extends `Command`. You can provide 
basic information about this command using the `@CommandInfo` annotation this class to avoid using a
constructor.
 
Note that only the constructors `Command#Command()` and `Command#Command(AccessLevel)` uses the
annotation.

> Note that if you want to specify an `AccessLevel` you need to use a constructor.

The `AccessLevel` of a command is a set of prerequisites that a user need to satisfy before using a
command. It contains a set of permissions that the user need to satisfy if the command is executed in 
a guild, plus some flags and the ID of an user if you want to authorize only one person. However you 
can still override the method `AccessLevel#check(CommandEvent)` and implement your own checks.

> The way that `AccessLevel`s are made is a bit crappy so expect changes.

Each command have a set of `CommandPattern`s that correspond to a certain manner to type a command.
These patterns can be found automatically when the command class is instantiated* by looking at the
methods in the class annotated with `@RegisterPattern`.

Note that if you want to take full advantage of this system you need to provide the argument
`-parameters` to your compiler to be able to read the names of your method parameters.

With this system a command that have this syntax `!command <@User> add <String>` can be automatically
registered by a method signature like this:
```java
@RegisterPattern
protected void someMethod(CommandEvent event, Options options, User user, Void add, String string) {}
```
or:
```java
@RegisterPattern(arguments = {"USER", "'add'", "STRING"})
protected void someMethod(CommandEvent event, Options options, User user, Void add, String string) {}
```
Note that the strings provided in the annotations (except for the second) are the names of constants
in the [Argument class](./ModularBot-Command/src/main/java/com/jesus_crie/modularbot_command/processing/Argument.java).
You can register your own class that contains such constants annotated with `@RegisterArgument` with
the method `Argument#registerArguments(Class)`.

There is a variety of possibility to make such methods, all of them can be found in this
[Test class](./ModularBot-Command/src/test/java/com/jesus_crie/modularbot_command/CommandTest.java).

> *: It's planned to do this at compile-time but for now it happens basically when the command is
> registered so when the bot is waking up.

Each command can accept a certain set of `Option`s provided in the constructor or in the `@CommandInfo`.
These options are totally optional and to not appear in the `CommandPattern`s. These are added at the 
end of the command implicitly (`-f`) or explicitly (`--force`).

Explicit options need to be prefixed with `--` and the long name of the option whereas implicit ones 
are prefixed by `-` and followed by one or more letters each representing the short name of an option.
If they are followed by a string it will be considered as the argument of the option (or the last in
implicit options).

Once parsed these options are accessible through the `Options` object provided along the arguments to
the patterns. The argument of each option is also present.

Note that like the `Argument`s, all of the `Option`s names are constants in the
[Option class](./ModularBot-Command/src/main/java/com/jesus_crie/modularbot_command/processing/Option.java)
and you can register your own constants with `Option#registerOptions(Class)`.

**Experimental:** In the `CommandModule` you can set flags to the command processor to modify the
behaviour of the algorithm but it's experimental and can lead to unexpected behaviour. This feature 
isn't a priority so if your're a volunteer you can fork this repo and send a pull request.

Finally, you can listen to the success or the failure of a command typed by a user by registering your
own `CommandListener` with `CommandModule#addListener`.

#### Night Config Wrapper
[![Javadocs config](http://www.javadoc.io/badge/com.jesus-crie/modularbot-night-config-wrapper.svg?label=javadoc-night-config-wrapper)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-night-config-wrapper)
> *Artifact: `com.jesus-crie:modularbot-night-config-wrapper`*

This module uses [NightConfig 3.6.0](https://github.com/TheElectronWill/Night-Config) to load, parse
and save config files. You will be forced to have a "primary" config file. You can also register names
groups of secondary config files as well singleton config groups (basically a named config).

Note that the default config file contains information that will be delivered to the `CommandModule`
like the "creator_id" and a list of custom prefixes for guilds. Note that the creator id will only be 
loaded whereas the custom prefixes will be loaded and saved when the module is unloaded.

You can customize the path of the default config by configuring the module but if you want to use a
completely different `FileConfig` you will need to instantiate it yourself and provide it as a built
module.

You can use secondary config files like this:
```java
public class MyModule extends Module {
    @InjectorTarget
    public MyModule(NightConfigWrapperModule cfgModule) {
        FileConfig cacheFile = cfgModule.registerSingletonSecondaryConfig("cache", "./cache.json");
    }
}
```

This module is entirely based on [Night Config](https://github.com/TheElectronWill/Night-Config) and I
really recommend you to read its documentation.

#### JS Nashorn support
[![Javadocs nashorn](http://www.javadoc.io/badge/com.jesus-crie/modularbot-nashorn-support.svg?label=javadoc-nashorn-support)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-nashorn-support)
> *Artifact: `com.jesus-crie:modularbot-nashorn-support`*

> As described in the [JEP 335](http://openjdk.java.net/jeps/335), the Nashorn JavaScript engine has been deprecated in Java 11.
> **Therefore this module is considered deprecated**.

> As a replacement, consider using GraalVM and the associated module.

~~This module allows you to load modules in JavaScript using the Nashorn
Script Engine. It will consider each subdirectory in `./scripts/` (or the
specified base folder) as a module and will try to load the `main.js` of
each one (if it exists) and will wrap any object in the `module` top-level
variable into a module and send lifecycle events to it.~~

~~A module in JavaScript looks like this:~~
```javascript
function TestModule() {
    this.log = LoggerFactory.getLogger("TestModule");
    this.info = new ModuleInfo("TestModule", "Author", "http://example.com", "1.0", 1);
    
    this.onInitialization = function() {
        this.log.info("Module initialized");
    }
}

var module = new TestModule();
```

~~See [this section](#your-custom-module) for more information about the
custom modules.~~

~~For each script, a header is added that imports some essential classes.
You can found this header [here](./ModularBot-NashornSupport/src/main/resources/script_header.js).
It can be overridden if there is a file called `_header.js` in the
scripts folder.~~

#### JS Nashorn Command Support
[![Javadocs nashorn command](http://www.javadoc.io/badge/com.jesus-crie/modularbot-nashorn-command-support.svg?label=javadoc-nashorn-support)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-nashorn-command-support)
> *Artifact: `com.jesus-crie:modularbot-nashorn-command-support`*

> As an extension of the nashorn support module, this module too is considered **deprecated**.

~~An extension to the JS module that provide a way to use the command module
in JavaScript.~~

~~This module let you define a `#getCommands()` that returns an array of
`JavaScriptCommand` that will be wrapped into real command objects and
registered. But because of my poor skills in JavaScript you can't use the
annotation system and you need to register your patterns explicitly like
in the example below. Regardless of that, all of the other features are
available.~~

```javascript
with (baseImports) {
    with (commandImports) {
        
        /* Module declaration here */
        
        function getCommands() {
            // Create a typed array
            var commands = new JavaScriptCommandArray(1);
            commands[0] = testJSCommand;
            return commands;
        }
    
        var testJSCommand = JavaScriptCommand.from({
            aliases: ["testjs"],
            description: "A demo command in JavaScript",
            shortDescription: "A demo command",
            accessLevel: AccessLevel.EVERYONE,
            options: [Option.FORCE],
            
            // Create the patterns by hand
            patterns: [
                new CommandPattern(
                    [
                        Argument.forString("add"),
                        Argument.STRING
                    ], function (event, args, options) {
                        event.fastReply("You wan to add: " + args[0]);
                    }
                ),
                new CommandPattern(function (event, args, options) {
                    if (options.has("force"))
                        event.fastReply("Hi, i'm force");
                    else event.fastReply("Hi");
                })
            ]
        });
    }
}
```

~~Note that this code comes in addition to the module declaration. If a script
doesn't contains a module, its entirely ignored.~~

> ~~You can also extends `JavaScriptCommand` but for some reason Nashorn do
not evaluate the arrays correctly and messes up everything, but feel free
to experiment and send me a pull request.~~

~~For convenience you can add these imports to your custom header:~~
```javascript
var JavaScriptCommandArray = Java.type("com.jesus_crie.modularbot_nashorn_command_support.JavaScriptCommand[]");

var commandImports = new JavaImporter(com.jesus_crie.modularbot_nashorn_command_support, 
    com.jesus_crie.modularbot_command, 
    com.jesus_crie.modularbot_command.processing);
```

#### Message Decorator
[![Javadocs message decorator](http://www.javadoc.io/badge/com.jesus-crie/modularbot-message-decorator.svg?label=javadoc-message-decorator)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-message-decorator)
> *Artifact: `com.jesus-crie:modularbot-message-decorator`*

Decorators are objects that can be bound to a specific message to extend their behaviour by listening
to specific events regarding this message. This module is mainly made to allow a bunch of interactions
using the message's reactions (emotes under the message).

Every decorator extends `MessageDecorator` which stores the bound message and its timeout. When a
decorator is triggered, `MessageDecorator#onTrigger` is called and when it times out, it will call
`MessageDecorator#onTimeout` which will call `MessageDecorator#destroy` in most implementations.

Certain decorators implements `Cacheable` which allows them to be saved in a cache file when the bot
is down and reloaded when the bot wake up. This caching is done automatically when the decorator is
registered.
 
> Note that all of the lambdas that you can provide are serializable and will be serialized and this
> means that if your lambda uses variables that aren't in the lambda's parameters, they will be serialized
> too and can lead to unexpected errors.

From there you can use the `AutoDestroyMessageDecorator` which allows you to delete the bound message
automatically after a certain period of time or when the bot is shutting down.

The other decorators extends `ReactionDecorator` which allows interactions by the intermediate of 
message reactions. These reactions are wrapped in `DecoratorButton`s that also contains an action to
perform when the button is triggered.

They are 2 kind of reaction decorator, permanent ones and dismissible ones.

In the dismissible ones you can find `AlertReactionDecorator` which acts a bit like the
`AutoDestroyMessageDecorator` but you can delete it earlier by clicking a reaction under the message.
`ConfirmReactionDecorator` acts like a yes/no dialog box for the user.

In the permanent decorators you can find the `PollReactionDecorator` which allows you to turn a message
into a poll by providing the allowed "votes" to it, then you can query the votes at any times.

> Querying the votes can be expensive if there are too many emotes. Consider querying them as less often
> as possible.

There is also the `PanelReactionDecorator` which, like the poll, allows you to set a bunch of reactions
under the message. But the panel decorator is made too handle more complex operations for each buttons.
You need to extend this class before using it and create a method per button that you want and annotate
it with `@RegisterPanelAction(...)`. More details can be found in the javadoc of the class.

Note: The example bot demonstrates a bunch of these decorators and their possibilities.

#### GraalVM Support
[![Javadocs graalvm suport](http://www.javadoc.io/badge/com.jesus-crie/modularbot-graalvm-support.svg?label=javadoc-graalvm-support)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-graalvm-support)
> *Artifact: `com.jesus-crie:modularbot-graalvm-support`*

This module need to be built using the [GraalVM JDK](https://graalvm.org) and to be run on GraalVM JRE.

GraalVM is an implementation of the JVM (Java Virtual Machine) that allows you to create polyglot
applications, aka using multiple languages and interact between them flawlessly.

This module allows you to write a module in any language supported by GraalVM and interact with it from
the Java code.

The examples that will be given are assuming a module written in Javascript.

You can declare a module by creating a class and exporting the class object using the [`Polyglot` API](https://www.graalvm.org/docs/reference-manual/polyglot/)
of GraalVM.
```javascript
class MyJSModule {
    constructor() {
        console.log("Hello from javascript !");
    }
}

Polyglot.export("class", MyJSModule);
```

You don't need to explicitly extend anything but ModularBot will act like you are extending `Module`
like any other module and will call the corresponding lifecycle hooks with the same signature as
declared in the `Lifecycle` interface.

In order for your module to work, you need to write a wrapper in Java to interact with it.

The most simple wrapper will look like that:
```java
public class MyJSModule extends GraalModuleWrapper {
    @InjectorTarget
    public MyJSModule() {
        super(new File("my-js-module.js"));
    }
}
```

The `GraalModuleWrapper` class extend the `Module` class and provides you a few methods to handle
interaction with the wrapped module. It already extends every lifecycle hook and propagate them to
the module.

If your module has a public method that is supposed to be exposed to the rest of the application, you
need to declare those methods and convert the arguments from Java to the client language.

This example demonstrates both:
```javascript
class MyJSModule {
    constructor(commandModule) {
        commandModule.registerQuickCommand('ping', e => e.fastReply('Pong !'));
    }
    
    signMessage(str) {
        return str + ' <3';
    }
    
    consumeAction(promise) {
        promise.then(res => console.log(res));
    }
}
```

```java
public class MyJSModule extends GraalModuleWrapper {
    @InjectorTarget
    public MyJSModule(CommandModule cmdModule) {
        super(new File("my-js-module.js"), cmdModule);
    }
    
    public String signMessage(String str) {
        return safeInvoke("signMessage", str).asString();
    }
    
    public void consumeAction(Supplier<String> action) {
        safeInvoke("consumeAction", GUtils.createJSPromise(getContext(), new JSPromiseExecutorProxy() {
            @Override
            public void run() {
                resolve(action.get());
            }
        }));
    }
}
```

Yes, this module enable you to wrap Java's functional interfaces and supply them as Javascript promises !
What a time to be alive.

If you are writing [TypeScript](https://typescriptlang.org), you can use the @types declared in the
subporject [ModularBot-TS-Types](https://github.com/JesusCrie/ModularBot-TS-Types/) which declares a
bunch of types for both the Polyglot API and the JDA classes.

#### GraalVM Support DiscordJS
[![Javadocs graalvm suport discordjs](http://www.javadoc.io/badge/com.jesus-crie/modularbot-graalvm-support.svg?label=javadoc-graalvm-support-discordjs)](http://www.javadoc.io/doc/com.jesus-crie/modularbot-graalvm-support-discordjs)
> *Artifact: `com.jesus-crie:modularbot-graalvm-support-discordjs`*

This module isn't ready yet.

This module will wrap everything in the [Discord.JS](https://discord.js.org) API to allow peoples who
prefer the DJS way to do things in Javascript modules.
