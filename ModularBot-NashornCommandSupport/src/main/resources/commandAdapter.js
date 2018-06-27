var BaseJsModule = Java.type("com.jesus_crie.modularbot_nashornsupport.module.BaseJsModule");
var ModuleInfo = Java.type("com.jesus_crie.modularbot.module.BaseModule.ModuleInfo");

var CommandAdapterModule = Java.extend(BaseJsModule, {
    info: new ModuleInfo("JS CommandAdapter", "Jesus-Crie", "https://github.com/JesusCrie/ModularBot", "1.0", 1),

    onLoad: function (moduleManager, builder) {
        
    }
});

function getModule() {
    return new CommandAdapterModule();
}