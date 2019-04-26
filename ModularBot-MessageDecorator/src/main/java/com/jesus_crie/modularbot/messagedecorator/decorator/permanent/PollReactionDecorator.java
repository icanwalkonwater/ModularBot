package com.jesus_crie.modularbot.messagedecorator.decorator.permanent;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.core.ModularBot;
import com.jesus_crie.modularbot.core.utils.SerializableBiConsumer;
import com.jesus_crie.modularbot.core.utils.SerializableConsumer;
import com.jesus_crie.modularbot.core.utils.SerializationUtils;
import com.jesus_crie.modularbot.messagedecorator.Cacheable;
import com.jesus_crie.modularbot.messagedecorator.button.DecoratorButton;
import com.jesus_crie.modularbot.messagedecorator.button.EmoteDecoratorButton;
import com.jesus_crie.modularbot.messagedecorator.button.UnicodeDecoratorButton;
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
 * A persistent decorator that can retrieve the amount of the desired emotes under a specific message, like a poll.
 * It will collect only the emote that are specified in the constructor that are added during the setup.
 */
public class PollReactionDecorator extends PermanentReactionDecorator implements Cacheable {

    protected final SerializableBiConsumer<PollReactionDecorator, GenericMessageReactionEvent> onVote;
    protected final SerializableConsumer<PollReactionDecorator> onTimeout;

    /**
     * Create a poll decorator using {@link net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote ReactionEmote} for the votes.
     *
     * @param binding   The bound message.
     * @param timeout   The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param onVote    (Optional) The action to perform when a vote is added or removed.
     * @param onTimeout (Optional) The action to perform when the decorator times out.
     * @param votes     The emotes that corresponds at the votes that will be counted by the decorator.
     */
    public PollReactionDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nullable final SerializableBiConsumer<PollReactionDecorator, GenericMessageReactionEvent> onVote,
                                 @Nullable final SerializableConsumer<PollReactionDecorator> onTimeout,
                                 @Nonnull final MessageReaction.ReactionEmote... votes) {
        super(binding, timeout,
                Arrays.stream(votes)
                        .map(r -> DecoratorButton.fromReactionEmote(r, null))
                        .toArray(DecoratorButton[]::new));
        this.onVote = onVote;
        this.onTimeout = onTimeout;
    }

    /**
     * Constructor that allows only unicode emotes for votes.
     *
     * @param binding   The bound message.
     * @param timeout   The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param onVote    (Optional) The action to perform when a vote is added or removed.
     * @param onTimeout (Optional) The action to perform when the decorator times out.
     * @param emotes    The unicode emotes that will be used for votes.
     */
    public PollReactionDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nullable final SerializableBiConsumer<PollReactionDecorator, GenericMessageReactionEvent> onVote,
                                 @Nullable final SerializableConsumer<PollReactionDecorator> onTimeout,
                                 @Nonnull final String... emotes) {
        super(binding, timeout,
                Arrays.stream(emotes)
                        .map(r -> new UnicodeDecoratorButton(r, null))
                        .toArray(UnicodeDecoratorButton[]::new));
        this.onVote = onVote;
        this.onTimeout = onTimeout;
    }

    /**
     * Constructor that allows only custom server emotes for votes.
     *
     * @param binding   The bound message.
     * @param timeout   The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param onVote    (Optional) The action to perform when a vote is added or removed.
     * @param onTimeout (Optional) The action to perform when the decorator times out.
     * @param emotes    The custom emotes that will be used for votes.
     */
    public PollReactionDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nullable final SerializableBiConsumer<PollReactionDecorator, GenericMessageReactionEvent> onVote,
                                 @Nullable final SerializableConsumer<PollReactionDecorator> onTimeout,
                                 @Nonnull final Emote... emotes) {
        super(binding, timeout,
                Arrays.stream(emotes)
                        .map(r -> new EmoteDecoratorButton(r, null))
                        .toArray(EmoteDecoratorButton[]::new));
        this.onVote = onVote;
        this.onTimeout = onTimeout;
    }

    @Override
    protected boolean onTrigger(@Nonnull GenericMessageReactionEvent event) {
        if (super.onTrigger(event)) {
            if (onVote != null) onVote.accept(this, event);
            return true;
        }

        return false;
    }

    @Override
    protected void onTimeout() {
        if (onTimeout != null) onTimeout.accept(this);
        super.onTimeout();
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

        return buttons.stream()
                .collect(HashMap::new,
                        (map, btn) ->
                                map.put(btn.getReactionEmote(), binding.getReactions().stream()
                                        .filter(r -> btn.getReactionEmote().equals(r.getReactionEmote()))
                                        .findAny()
                                        .map(r -> r.getCount() - (r.isSelf() ? 1 : 0))
                                        .orElse(0)),
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

        final Map<String, Integer> out = new HashMap<>();
        collectVotes().forEach((r, i) -> out.put(r.getName(), i));

        return out;
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
        final long expireTime = serialized.getLongOrElse(KEY_TIMEOUT, 1); // 1 means its expired.
        final List<String> votes = serialized.get(KEY_POLL_VOTES);

        final SerializableBiConsumer<PollReactionDecorator, GenericMessageReactionEvent> onVote;
        if (serialized.contains(KEY_POLL_VOTE_ACTION))
            onVote = SerializationUtils.deserializeFromString(serialized.get(KEY_POLL_VOTE_ACTION));
        else onVote = null;

        final SerializableConsumer<PollReactionDecorator> onTimeout;
        if (serialized.contains(KEY_TIMEOUT_ACTION))
            onTimeout = SerializationUtils.deserializeFromString(serialized.get(KEY_TIMEOUT_ACTION));
        else onTimeout = null;

        if (chanId == null || bindingId == null)
            throw new IllegalArgumentException("One or more fields are missing !");

        // Retrieve the binding.
        final Message binding = Cacheable.getBinding(chanId, bindingId, bot);

        // Check if expired, should never happen but who knows ?
        if (expireTime < 0)
            throw new IllegalStateException("Trying to deserialize a decorator that is marked as expired ! (timeout < 0)");

        final long timeout = expireTime == 0 ? 0 : expireTime - System.currentTimeMillis();

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
        // Check if alive, should never trigger because it's checked but who knows.
        if (!isAlive)
            throw new IllegalStateException("Can't serialize an expired decorator !");

        final Config serialized = Config.inMemory();
        serialized.set(KEY_CLASS, getClass().getName());
        serialized.set(KEY_BINDING_CHANNEL_ID, binding.getChannel().getIdLong());
        serialized.set(KEY_BINDING_ID, binding.getIdLong());
        serialized.set(KEY_TIMEOUT, getExpireTime());

        if (onVote != null) serialized.set(KEY_POLL_VOTE_ACTION, SerializationUtils.serializableToString(onVote));
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
