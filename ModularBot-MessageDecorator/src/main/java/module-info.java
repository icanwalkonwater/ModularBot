module com.jesus.crie.modularbot.message_decorator {
    requires transitive com.jesus.crie.modularbot.core;

    exports com.jesus_crie.modularbot_message_decorator;
    exports com.jesus_crie.modularbot_message_decorator.button;
    exports com.jesus_crie.modularbot_message_decorator.decorator;
    exports com.jesus_crie.modularbot_message_decorator.decorator.disposable;
    exports com.jesus_crie.modularbot_message_decorator.decorator.permanent;
}