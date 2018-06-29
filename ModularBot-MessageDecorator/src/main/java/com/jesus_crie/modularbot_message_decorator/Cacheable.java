package com.jesus_crie.modularbot_message_decorator;

import com.electronwill.nightconfig.core.Config;

import javax.annotation.Nonnull;

public interface Cacheable {

    // Constants //

    /**
     * Constant to define the name of a field containing the name of class.
     */
    String KEY_CLASS = "_class";

    /**
     * Constant to define the name of a field containing the ID if the bound object.
     */
    String KEY_BINDING_ID = "binding_id";

    /**
     * Constant to define the name of a field containing the timeout.
     */
    String KEY_TIMEOUT = "timeout";

    /**
     * Constant to define the name of a field containing a serialized functional interface.
     */
    String KEY_ACTION_FUNCTIONAL = "action_functional";

    /**
     * Constant to define the name of a field containing a JS script.
     */
    String KEY_ACTION_SCRIPT = "action_js";

    /**
     * Used to serialize the objet into a {@link Config Config} usable by the config module.
     *
     * @return A {@link Config Config} containing the information necessary to restore the object.
     */
    @Nonnull
    Config serialize();
}
