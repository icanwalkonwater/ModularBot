package com.jesus_crie.modularbot_message_decorator.decorator.permanent;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A permanent decorator that needs to be extended because it might contains pretty complex methods.
 * Each method in the subclass annotated with {@link RegisterPanelAction RegisterPanelAction} and with only one argument
 * of type {@link GenericMessageReactionEvent GenericMessageReactionEvent} will be registered and will become an emote
 * that will trigger the corresponding method.
 *
 * Each of these methods needs to have a particular method signature, a typical method looks like this,
 * <pre> {@code
 * \@RegisterPanelAction(emote = "\u2705")
 * private void somePanelAction(GenericMessageReactionEvent event) {
 *     // Some code
 * }
 * } </pre>
 *
 * Otherwise you can register your method by hand using {@link #registerPanelMethod(MessageReaction.ReactionEmote, Method)}.
 */
public abstract class PanelReactionDecorator extends PermanentReactionDecorator implements Cacheable {

    private Map<Integer, Pair<MessageReaction.ReactionEmote, Method>> panelActions = new HashMap<>();

    public PanelReactionDecorator(@Nonnull final Message binding, final long timeout) {
        super(binding, timeout);

        registerActions();
    }

    /**
     * Used internally to look for and register methods that are eligible.
     */
    private void registerActions() {

        // For each superclass until the Object class
        for (Class<?> current = getClass(); current != null; current = current.getSuperclass()) {

            // Iterate through all declared methods
            for (final Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(RegisterPanelAction.class)) {

                    final Class<?>[] params = method.getParameterTypes();
                    if (params.length != 0 && params[0].equals(GenericMessageReactionEvent.class))
                        continue;

                    final RegisterPanelAction annotation = method.getAnnotation(RegisterPanelAction.class);
                    if (annotation.emote().equals("") && annotation.emoteId() == 0)
                        throw new IllegalArgumentException("You need to provide a way to get an emote to the annotation !");

                    final MessageReaction.ReactionEmote emote;
                    if (annotation.emote().length() > 0)
                        emote = new MessageReaction.ReactionEmote(annotation.emote(), null, null);
                    else
                        emote = new MessageReaction.ReactionEmote(
                                binding.getJDA().asBot().getShardManager().getEmoteById(annotation.emoteId()));

                    registerPanelMethod(emote, method);
                }
            }
        }
    }

    protected void registerPanelMethod(@Nonnull final MessageReaction.ReactionEmote emote, @Nonnull final Method method) {
        registerPanelMethod(panelActions.size(), emote, method);
    }

    /**
     * Register the given pair emote/method at the given position.
     * If another pair is already at this position, it will be moved one position after.
     *
     * @param position The position of the pair.
     * @param emote    The emote that will represent the action.
     * @param method   The action.
     */
    protected void registerPanelMethod(final int position, @Nonnull final MessageReaction.ReactionEmote emote, @Nonnull final Method method) {
        panelActions.compute(position, (pos, current) -> {
            if (current != null) {
                while (panelActions.containsKey(pos))
                    pos++;

                panelActions.put(pos, current);
            }
            return Pair.of(emote, method);
        });
    }

    @Nonnull
    @Override
    public Config serialize() {
        // TODO 23/07/2018 serialization things
        return null;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface RegisterPanelAction {

        /**
         * The unicode representation of the targeted emote.
         * If this is set, {@link #emoteId()} will not even be checked.
         */
        String emote() default "";

        /**
         * The ID of the custom emote.
         */
        long emoteId() default 0;
    }
}
