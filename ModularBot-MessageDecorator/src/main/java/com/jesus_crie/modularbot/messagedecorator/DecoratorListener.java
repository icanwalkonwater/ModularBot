package com.jesus_crie.modularbot.messagedecorator;

public interface DecoratorListener {

    void onReady();

    void onTimeout();

    void onDestroy();
}
