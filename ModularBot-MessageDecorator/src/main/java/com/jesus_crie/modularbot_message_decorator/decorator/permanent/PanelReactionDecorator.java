package com.jesus_crie.modularbot_message_decorator.decorator.permanent;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import com.jesus_crie.modularbot_message_decorator.button.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * A permanent decorator that needs to be extended because it might contains pretty complex methods.
 * The subclass MUST have a constructor with two parameters like below to allow the deserialization.
 * <pre> {@code
 * public MyPanelDecorator(Message binding, long timeout) {
 *     super(binding, timeout);
 *     // Some code
 * }
 * } </pre>
 * <p>
 * Each method in the subclass annotated with {@link RegisterPanelAction RegisterPanelAction} and with only one argument
 * of type {@link GenericMessageReactionEvent GenericMessageReactionEvent} will be registered and will become an emote
 * that will trigger the corresponding method.
 * <p>
 * Each of these methods needs to have a particular method signature, a typical method looks like this,
 * <pre> {@code
 * \@RegisterPanelAction(position = 1, emote = "\u2705")
 * private void somePanelAction(GenericMessageReactionEvent event) {
 *     // Some code
 * }
 * } </pre>
 * <p>
 * Otherwise you can register your method by hand using {@link #registerPanelMethod(MessageReaction.ReactionEmote, Method)}.
 * Note that the registered actions are converted into buttons when {@link #setup()} is called.
 * You can still register your own actions directly as buttons.
 * <p>
 * Note that none of the actions will be serialized, they will be rediscovered when the decorator will be instantiated.
 * Because of that you can safely add or remove actions and the serialized decorators will be backward compatible.
 */
public abstract class PanelReactionDecorator extends PermanentReactionDecorator implements Cacheable {

    private static final Logger LOG = LoggerFactory.getLogger("PanelReactionDecorator");

    private final List<Pair<MessageReaction.ReactionEmote, Method>> panelActions = new LinkedList<>();

    protected PanelReactionDecorator(@Nonnull final Message binding, final long timeout) {
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
                    if (params.length != 1 && !params[0].equals(GenericMessageReactionEvent.class))
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

                    if (annotation.position() == -1) registerPanelMethod(emote, method);
                    else registerPanelMethod(annotation.position(), emote, method);
                }
            }
        }
    }

    @Override
    public void setup() {
        panelActions.forEach(entry ->
                buttons.add(DecoratorButton.fromReactionEmote(entry.getLeft(),
                        event -> {
                            try {
                                final Method method = entry.getRight();
                                if (!method.isAccessible()) method.setAccessible(true);

                                method.invoke(this, event);
                            } catch (IllegalAccessException | InvocationTargetException error) {
                                // Should happen only if the underlying method throw an exception.
                                LOG.error("An error occurred while executing a panel method !", error);
                            }
                        })
                ));


        super.setup();
    }

    /**
     * Register the given pair emote/method.
     * The position will be the current size of the map, so if the position are correctly ordered, it will be append at
     * the end of the map.
     *
     * @param emote  The emote to register.
     * @param method The associated method.
     * @see #registerPanelMethod(int, MessageReaction.ReactionEmote, Method)
     */
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
        panelActions.add(position, Pair.of(emote, method));
    }

    /**
     * Deserialize a panel decorator by instantiating its underlying class.
     * Can throw a bunch of exception if the underlying class doesn't have the required characteristics described in the
     * doc of the class.
     *
     * @param serialized The serialized form of the constructor.
     * @param bot        The current instance of {@link ModularBot ModularBot}.
     * @return The deserialized decorator.
     * @throws IllegalArgumentException If a field is missing or if the underlying class isn't valid.
     * @throws IllegalStateException    If the binding can't be retrieved (usually it's deleted)
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static PanelReactionDecorator tryDeserialize(@Nonnull final Config serialized, @Nonnull final ModularBot bot) {

        final Long chanId = serialized.get(KEY_BINDING_CHANNEL_ID);
        final Long bindingId = serialized.get(KEY_BINDING_ID);
        final long expireTime = serialized.getLongOrElse(KEY_TIMEOUT, 1);
        final String underlying = serialized.get(KEY_CLASS_UNDERLYING);

        if (chanId == null || bindingId == null || underlying == null)
            throw new IllegalArgumentException("One or more fields are missing !");

        final Constructor<? extends PanelReactionDecorator> targetConstructor;
        try {
            final Class<? extends PanelReactionDecorator> underlyingClass = (Class<? extends PanelReactionDecorator>) Class.forName(underlying);
            if (Modifier.isAbstract(underlyingClass.getModifiers()))
                throw new IllegalArgumentException("The underlying class is abstract ! " + underlying);

            targetConstructor = underlyingClass.getConstructor(Message.class, long.class);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException("The underlying class isn't valid !", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The underlying class don't have a valid constructor !");
        }

        // Should not happen.
        if (targetConstructor == null)
            return null;

        // Retrieve the binding.
        final Message binding = Cacheable.getBinding(chanId, bindingId, bot);

        // Check if expired, should never happen but who knows ?
        if (expireTime < 0)
            throw new IllegalStateException("Trying to deserialize a decorator that is marked as expired ! (timeout < 0)");

        final long timeout = expireTime == 0 ? 0 : expireTime - System.currentTimeMillis();

        try {
            if (!targetConstructor.isAccessible()) targetConstructor.setAccessible(true);

            return targetConstructor.newInstance(binding, timeout);

        } catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // The only exception that can be thrown is because of an invalid timeout.
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
        serialized.set(KEY_CLASS, PanelReactionDecorator.class.getName());
        serialized.set(KEY_CLASS_UNDERLYING, getClass().getName());
        serialized.set(KEY_BINDING_CHANNEL_ID, binding.getChannel().getIdLong());
        serialized.set(KEY_BINDING_ID, binding.getIdLong());
        serialized.set(KEY_TIMEOUT, getExpireTime());

        return serialized;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface RegisterPanelAction {

        /**
         * The position of the button on the panel.
         */
        int position() default -1;

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
