var BaseJavaScriptModule = Java.type("com.jesus_crie.modularbot_nashorn_support.module.BaseJavaScriptModule");
var ModuleInfo = Java.type("com.jesus_crie.modularbot.module.BaseModule.ModuleInfo");

var baseImports = new JavaImporter(java.lang, java.util, java.io, org.slf4j, com.jesus_crie.modularbot);