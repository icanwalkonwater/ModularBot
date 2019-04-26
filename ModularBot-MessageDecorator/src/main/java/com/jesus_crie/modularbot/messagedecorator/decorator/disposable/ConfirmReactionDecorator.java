package com.jesus_crie.modularbot.messagedecorator.decorator.disposable;

import com.jesus_crie.modularbot.core.utils.SerializableBiConsumer;
import com.jesus_crie.modularbot.core.utils.SerializableConsumer;
import com.jesus_crie.modularbot.messagedecorator.button.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfirmReactionDecorator extends SafeAutoDestroyDisposableReactionDecorator {

    /**
     * The default "yes" emote. Correspond to the unicode character "✅".
     */
    public static final MessageReaction.ReactionEmote DEFAULT_YES_EMOTE = new MessageReaction.ReactionEmote("\u2705", null, null);

    /**
     * The default "no" emote. Corresponds to the unicode character "❎".
     */
    public static final MessageReaction.ReactionEmote DEFAULT_NO_EMOTE = new MessageReaction.ReactionEmote("\u274E", null, null);

    private final SerializableConsumer<ConfirmReactionDecorator> onTimeout;

    /**
     * Create a confirm decorator with the default emotes and custom actions for each choice.
     *
     * @param binding     The bound message.
     * @param timeout     The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param onYes       The action to perform when the "yes" button is triggered.
     * @param onNo        (Optional) The action to perform when the "no" button is triggered.
     * @param onTimeout   (Optional) The action to perform when the decorator times out.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmReactionDecorator(@Nonnull final Message binding, final long timeout,
                                    @Nonnull final SerializableConsumer<ConfirmReactionDecorator> onYes,
                                    @Nullable final SerializableConsumer<ConfirmReactionDecorator> onNo,
                                    @Nullable final SerializableConsumer<ConfirmReactionDecorator> onTimeout,
                                    final boolean deleteAfter) {
        this(binding, timeout,
                (dec, res) -> {
                    if (res) onYes.accept(dec);
                    else if (onNo != null) onNo.accept(dec);
                },
                onTimeout, deleteAfter);
    }

    /**
     * Create a confirm decorator with the default emotes and a common action for both buttons.
     *
     * @param binding     The bound message.
     * @param timeout     The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param onTrigger   The action to perform when one of the buttons is triggered.
     * @param onTimeout   The action to perform when the decorator times out.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmReactionDecorator(@Nonnull final Message binding, final long timeout,
                                    @Nonnull final SerializableBiConsumer<ConfirmReactionDecorator, Boolean> onTrigger,
                                    @Nullable final SerializableConsumer<ConfirmReactionDecorator> onTimeout,
                                    final boolean deleteAfter) {
        this(binding, timeout, DEFAULT_YES_EMOTE, DEFAULT_NO_EMOTE, onTrigger, onTimeout, deleteAfter);
    }

    /**
     * Create a confirm decorator with custom emotes and a common action for both buttons.
     *
     * @param binding     The bound message.
     * @param timeout     The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param yesEmote    The emote for the "yes" button.
     * @param noEmote     The emote for the "no" button.
     * @param onTrigger   The action to trigger when one of the buttons is triggered.
     * @param onTimeout   The action to perform when the decorator times out.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmReactionDecorator(@Nonnull final Message binding, final long timeout,
                                    @Nonnull final MessageReaction.ReactionEmote yesEmote,
                                    @Nonnull final MessageReaction.ReactionEmote noEmote,
                                    @Nonnull final SerializableBiConsumer<ConfirmReactionDecorator, Boolean> onTrigger,
                                    @Nullable final SerializableConsumer<ConfirmReactionDecorator> onTimeout,
                                    final boolean deleteAfter) {
        super(binding, timeout, deleteAfter);
        this.onTimeout = onTimeout;

        buttons.add(DecoratorButton.fromReactionEmote(yesEmote, e -> onTrigger.accept(this, true)));
        buttons.add(DecoratorButton.fromReactionEmote(noEmote, e -> onTrigger.accept(this, false)));
    }

    /**
     * Create a confirm decorator with custom buttons.
     *
     * @param binding     The bound message.
     * @param timeout     The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param yesButton   The "yes" button and its action.
     * @param noButton    The "no" button and its action.
     * @param onTimeout   The action to perform when the decorator times out.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmReactionDecorator(@Nonnull final Message binding, final long timeout,
                                    @Nonnull final DecoratorButton yesButton,
                                    @Nonnull final DecoratorButton noButton,
                                    @Nullable final SerializableConsumer<ConfirmReactionDecorator> onTimeout,
                                    final boolean deleteAfter) {
        super(binding, timeout, deleteAfter, yesButton, noButton);
        this.onTimeout = onTimeout;
    }

    @Override
    protected void onTimeout() {
        if (onTimeout != null) onTimeout.accept(this);
        super.onTimeout();
    }
}
