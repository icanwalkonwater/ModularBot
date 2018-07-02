package com.jesus_crie.modularbot_message_decorator;

import com.electronwill.nightconfig.core.Config;

import javax.annotation.Nonnull;

public interface Cacheable {

    // Constants //

    String KEY_CLASS = "_class";
    String KEY_BINDING_ID = "binding_id";
    String KEY_TIMEOUT = "timeout";
    String KEY_ACTION_FUNCTIONAL = "action_functional";
    String KEY_ACTION_SCRIPT = "action_js";
    String KEY_BUTTONS = "buttons";
    String KEY_DELETE_AFTER = "delete_after";
    String KEY_EMOTE = "emote";
    String KEY_BUTTON_YES = "button_yes";
    String KEY_BUTTON_NO = "button_no";
    String KEY_TIMEOUT_ACTION = "timeout_action";
    String KEY_BINDING_CHANNEL_ID = "binding_channel_id";

    /**
     * Used to serialize the objet into a {@link Config Config} usable by the config module.
     *
     * @return A {@link Config Config} containing the information necessary to restore the object.
     */
    @Nonnull
    Config serialize();
}
