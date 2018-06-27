with (baseImports) {
    var LOG = LoggerFactory.getLogger("JS TestModule");

    var TestModule = Java.extend(BaseJavaScriptModule, {
        info: new ModuleInfo("TestModule", "Author", "URL", "1.0", 1),

        onLoad: function(moduleManager, builder) {
            LOG.info("Module loaded !");
        },
        onUnload: function () {
            LOG.info("Module unloaded !");
        }
    });
}

function getModule() {
    return new TestModule();
}
