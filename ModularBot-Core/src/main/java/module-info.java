module com.jesus.crie.modularbot.core {
    requires transitive JDA;
    requires opus.java.api;
    requires opus.java.natives;

    exports com.jesus_crie.modularbot;
    exports com.jesus_crie.modularbot.exception;
    exports com.jesus_crie.modularbot.module;
    exports com.jesus_crie.modularbot.utils;
}