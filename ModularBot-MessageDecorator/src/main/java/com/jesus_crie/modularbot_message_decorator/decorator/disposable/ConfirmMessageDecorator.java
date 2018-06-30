package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ConfirmMessageDecorator extends DisposableMessageDecorator {

    /**
     * The default "yes" emote. Correspond to the unicode character "✅".
     */
    public static final MessageReaction.ReactionEmote DEFAULT_YES_EMOTE = new MessageReaction.ReactionEmote("\u2705", null, null);

    /**
     * The default "no" emote. Corresponds to the unicode character "❎".
     */
    public static final MessageReaction.ReactionEmote DEFAULT_NO_EMOTE = new MessageReaction.ReactionEmote("\u274E", null, null);

    private final Runnable onTimeout;
    private final boolean deleteAfter;

    /**
     * Create a confirm decorator with the default emotes and custom actions for each choice.
     *
     * @param binding      The bound message.
     * @param timeout      The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param onYes        The action to perform when the "yes" button is triggered.
     * @param onNo         (Optional) The action to perform when the "no" button is triggered.
     * @param onTimeout    (Optional) The action to perform when the decorator times out.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmMessageDecorator(@Nonnull final Message binding, final long timeout,
                                   @Nonnull final Runnable onYes,
                                   @Nullable final Runnable onNo,
                                   @Nullable final Runnable onTimeout,
                                   final boolean deleteAfter) {
        this(binding, timeout,
                DecoratorButton.fromReactionEmote(DEFAULT_YES_EMOTE, e -> onYes.run()),
                DecoratorButton.fromReactionEmote(DEFAULT_NO_EMOTE, onNo != null ? e -> onNo.run() : null),
                onTimeout, deleteAfter);
    }

    /**
     * Create a confirm decorator with the default emotes and a common action for both buttons.
     *
     * @param binding      The bound message.
     * @param timeout      The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param onTrigger    The action to trigger when one of the buttons is triggered.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmMessageDecorator(@Nonnull final Message binding, final long timeout,
                                   @Nonnull final Consumer<Boolean> onTrigger, final boolean deleteAfter) {
        this(binding, timeout, DEFAULT_YES_EMOTE, DEFAULT_NO_EMOTE, onTrigger, null, deleteAfter);
    }

    /**
     * Create a confirm decorator with custom emotes and a common action for both buttons.
     *
     * @param binding      The bound message.
     * @param timeout      The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param yesEmote     The emote for the "yes" button.
     * @param noEmote      The emote for the "no" button.
     * @param onTrigger    The action to trigger when one of the buttons is triggered.
     * @param onTimeout    The action to perform when the decorator times out.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmMessageDecorator(@Nonnull final Message binding, final long timeout,
                                   @Nonnull final MessageReaction.ReactionEmote yesEmote,
                                   @Nonnull final MessageReaction.ReactionEmote noEmote,
                                   @Nonnull final Consumer<Boolean> onTrigger,
                                   @Nullable final Runnable onTimeout,
                                   final boolean deleteAfter) {
        super(binding, timeout);
        this.onTimeout = onTimeout;
        this.deleteAfter = deleteAfter;

        buttons.add(DecoratorButton.fromReactionEmote(yesEmote, e -> onTrigger.accept(true)));
        buttons.add(DecoratorButton.fromReactionEmote(noEmote, e -> onTrigger.accept(false)));
    }

    /**
     * Create a confirm decorator with custom buttons.
     *
     * @param binding      The bound message.
     * @param timeout      The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param yesButton    The "yes" button and its action.
     * @param noButton     The "no" button and its action.
     * @param onTimeout    The action to perform when the decorator times out.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public ConfirmMessageDecorator(@Nonnull final Message binding, final long timeout,
                                   @Nonnull final DecoratorButton yesButton,
                                   @Nonnull final DecoratorButton noButton,
                                   @Nullable final Runnable onTimeout,
                                   final boolean deleteAfter) {
        super(binding, timeout, yesButton, noButton);
        this.onTimeout = onTimeout;
        this.deleteAfter = deleteAfter;
    }

    @Override
    protected void onTimeout() {
        if (onTimeout != null) onTimeout.run();
        super.onTimeout();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (deleteAfter) binding.delete().complete();
    }
}
