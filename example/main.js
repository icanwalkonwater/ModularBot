class TestModule {
    constructor(commandModule) {
        commandModule.registerQuickCommand("yolo", e => e.fastReply("YOLO !"));
    }

    onLoad() {
        console.log("Loaded !")
    }

    acceptPromise(p) {
        p.then(function (value) {
            console.log(value)
        }).catch(function (reason) {
            console.error(reason)
        })
    }

    listenRestAction(a) {
        a.then(function (value) {
            console.log(value)
        })
    }
}

Polyglot.export("class", TestModule);
