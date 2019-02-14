module com.jesus.crie.modularbot.command {
    requires transitive com.jesus.crie.modularbot.core;

    exports com.jesus_crie.modularbot_command;
    exports com.jesus_crie.modularbot_command.annotations;
    exports com.jesus_crie.modularbot_command.exception;
    exports com.jesus_crie.modularbot_command.listener;
    exports com.jesus_crie.modularbot_command.processing;
}