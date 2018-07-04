package com.jesus_crie.modularbot_message_decorator.decorator.permanent;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.utils.SerializableConsumer;
import com.jesus_crie.modularbot.utils.SerializableRunnable;
import com.jesus_crie.modularbot.utils.SerializationUtils;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import com.jesus_crie.modularbot_message_decorator.button.DecoratorButton;
import com.jesus_crie.modularbot_message_decorator.button.EmoteDecoratorButton;
import com.jesus_crie.modularbot_message_decorator.button.UnicodeDecoratorButton;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A persistent decorator
 */
public class PollReactionDecorator extends PermanentReactionDecorator implements Cacheable {

    protected final SerializableConsumer<GenericMessageReactionEvent> onVote;
    protected final SerializableRunnable onTimeout;

    /**
     *
     * @param binding
     * @param timeout
     * @param onVote
     * @param onTimeout
     * @param votes
     */
    public PollReactionDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nullable final SerializableConsumer<GenericMessageReactionEvent> onVote,
                                 @Nullable final SerializableRunnable onTimeout,
                                 @Nonnull final MessageReaction.ReactionEmote... votes) {
        super(binding, timeout,
                Arrays.stream(votes)
                        .map(r -> DecoratorButton.fromReactionEmote(r, null))
                        .toArray(DecoratorButton[]::new));
        this.onVote = onVote;
        this.onTimeout = onTimeout;
    }

    public PollReactionDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nullable final SerializableConsumer<GenericMessageReactionEvent> onVote,
                                 @Nullable final SerializableRunnable onTimeout,
                                 @Nonnull final String... emotes) {
        super(binding, timeout,
                Arrays.stream(emotes)
                        .map(r -> new UnicodeDecoratorButton(r, null))
                        .toArray(UnicodeDecoratorButton[]::new));
        this.onVote = onVote;
        this.onTimeout = onTimeout;
    }

    public PollReactionDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nullable final SerializableConsumer<GenericMessageReactionEvent> onVote,
                                 @Nullable final SerializableRunnable onTimeout,
                                 @Nonnull final Emote... emotes) {
        super(binding, timeout,
                Arrays.stream(emotes)
                        .map(r -> new EmoteDecoratorButton(r, null))
                        .toArray(EmoteDecoratorButton[]::new));
        this.onVote = onVote;
        this.onTimeout = onTimeout;
    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    protected boolean onTrigger(@Nonnull GenericMessageReactionEvent event) {
        if (super.onTrigger(event)) {
            if (onVote != null) onVote.accept(event);
            return true;
        }

        return false;
    }

    /**
     * Request the emotes under the message and count them, minus one for the base reaction.
     * Calling this method too often can be costly since the message is queried from discord each time.
     *
     * @return A map containing the votes present on the bound message.
     */
    @Nonnull
    public Map<MessageReaction.ReactionEmote, Integer> collectVotes() {
        updateMessage();
        return binding.getReactions().stream()
                .filter(MessageReaction::isSelf)
                .collect(HashMap::new,
                        (map, reaction) -> map.put(reaction.getReactionEmote(), reaction.getCount()),
                        HashMap::putAll);
    }

    /**
     * Same as {@link #collectVotes()} but the key will be the name of the emote (or the unicode character).
     *
     * @return A map containing the votes present on the bound message.
     */
    @Nonnull
    public Map<String, Integer> collectVotesByName() {
        updateMessage();
        return binding.getReactions().stream()
                .filter(MessageReaction::isSelf)
                .collect(HashMap::new,
                        (map, reaction) -> map.put(reaction.getReactionEmote().getName(), reaction.getCount()),
                        HashMap::putAll);
    }

    /**
     * Try to convert a {@link Config Config} into a poll decorator.
     *
     * @param serialized The serialized version of the decorator.
     * @param bot        The current instance of the bot.
     * @return The deserialized {@link PollReactionDecorator PollReactionDecorator} or {@code null} if it wasn't valid anymore.
     * @throws IllegalArgumentException If a required field is missing.
     * @throws IllegalStateException    If the binding can't be retrieved (usually it's deleted)
     */
    @Nullable
    public static PollReactionDecorator tryDeserialize(@Nonnull final Config serialized, @Nonnull final ModularBot bot) {

        final Long chanId = serialized.get(KEY_BINDING_CHANNEL_ID);
        final Long bindingId = serialized.get(KEY_BINDING_ID);
        final Long expireTime = serialized.get(KEY_TIMEOUT);
        final List<String> votes = serialized.get(KEY_POLL_VOTES);
        final SerializableConsumer<GenericMessageReactionEvent> onVote = serialized.get(KEY_POLL_VOTE_ACTION);
        final SerializableRunnable onTimeout = serialized.get(KEY_TIMEOUT_ACTION);

        if (chanId == null || bindingId == null || expireTime == null)
            throw new IllegalArgumentException("One or more fields are missing !");

        // Retrieve the binding.
        final Message binding = Cacheable.getBinding(chanId, bindingId, bot);

        final long timeout = expireTime - System.currentTimeMillis();

        try {

            // Retrieve the votes.
            if (votes == null)
                return new PollReactionDecorator(binding, timeout, onVote, onTimeout, new String[0]);

            return new PollReactionDecorator(binding, timeout, onVote, onTimeout,
                    votes.stream()
                            .map(emoteS -> Cacheable.deserializeReactionEmote(emoteS, bot))
                            .toArray(MessageReaction.ReactionEmote[]::new));
        } catch (IllegalArgumentException e) {
            // Invalid timeout
            return null;
        }
    }

    @Nonnull
    @Override
    public Config serialize() {
        final Config serialized = Config.inMemory();
        serialized.set(KEY_CLASS, getClass().getName());
        serialized.set(KEY_BINDING_CHANNEL_ID, binding.getChannel().getIdLong());
        serialized.set(KEY_BINDING_ID, binding.getChannel().getIdLong());
        serialized.set(KEY_TIMEOUT, getExpireTime());

        if (onTimeout != null) serialized.set(KEY_TIMEOUT_ACTION, SerializationUtils.serializableToString(onTimeout));

        if (buttons.size() != 0) {
            serialized.set(KEY_POLL_VOTES,
                    buttons.stream()
                            .map(DecoratorButton::getEmoteSerialized)
                            .collect(Collectors.toList()));
        }

        return serialized;
    }
}
