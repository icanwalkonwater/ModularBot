with (baseImports) {

    var LOG = LoggerFactory.getLogger("TestJSModule");

    var TestModule = Java.extend(BaseJavaScriptModule, {
        info: new ModuleInfo("TestModule", "Author", "URL", "1.0", 1),
        onLoad: function () {
            LOG.info("TestJSCmd loaded !");
        }
    });

    function getModule() {
        return new TestModule();
    }

    with (commandImports) {

        function getCommands() {
            var commands = new JavaScriptCommandArray(1);
            commands[0] = testJSCommand;
            return commands;
        }

        var testJSCommand = JavaScriptCommand.from({
            aliases: ["testjs"],
            description: "Hey",
            shortDescription: "yo",
            accessLevel: AccessLevel.EVERYONE,
            options: [Option.FORCE],
            patterns: [
                new CommandPattern(
                    [
                        Argument.forString("add"),
                        Argument.STRING
                    ], function (event, args, options) {
                        event.fastReply("You wan to add: " + args[0].toString());
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
