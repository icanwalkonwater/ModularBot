package com.jesus_crie.modularbot.core.utils;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Base64;

public class SerializationUtils {

    /**
     * Serialize a given {@link Serializable Serializable} into a {@link String String}.
     *
     * @param source The object to serialize.
     * @return A string representing the serialized object.
     * @throws IllegalArgumentException If the provided object can't be serialized.
     */
    @Nonnull
    public static String serializableToString(@Nonnull final Serializable source) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (final ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(source);
        } catch (IOException e) {
            throw new IllegalArgumentException("The provided object can't be serialized !", e);
        }

        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /**
     * Decode the given string in base64 and convert it back to an object and returns it.
     *
     * @param serialized The base64 string representing the serialized object.
     * @param <T>        The type of the original object.
     * @return The deserialized object.
     * @throws IllegalArgumentException If the argument isn't valid and thus can't be deserialized.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> T deserializeFromString(@Nonnull final String serialized) {
        byte[] data = Base64.getDecoder().decode(serialized);

        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("The provided string is invalid and can't be deserialized", e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The provided string correspond to another object !", e);
        }
    }
}
