package com.jesus_crie.modularbot_command.processing;

import com.jesus_crie.modularbot_command.exception.CommandProcessingException;
import net.dv8tion.jda.core.utils.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.jesus_crie.modularbot_command.CommandModule.*;

public class CommandProcessor {

    private static final char PREFIX_OPTION = '-';
    private static final char WORD_SEPARATOR = ' ';
    private static final char ESCAPE_CHAR = '\\';
    private static final char SINGLE_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '"';

    private final int flags;

    public CommandProcessor() {
        this(0);
    }

    public CommandProcessor(int flags) {
        this.flags = flags;
    }

    /**
     * Process the entire input string and return a list of all the arguments and a map with each option and their
     * corresponding arguments.
     *
     * @param input The input string to process.
     * @return A list containing the arguments and a map containing the options associated with their arguments.
     * @throws CommandProcessingException When there is a syntax error in the input.
     */
    @Nonnull
    public Pair<List<String>, Map<String, String>> process(@Nonnull String input) throws CommandProcessingException {
        List<String> arguments = new ArrayList<>();
        Map<String, String> options = new LinkedHashMap<>();

        if (input.length() == 0)
            return Pair.of(arguments, options);

        input = input.trim();
        Cursor cursor = new Cursor(input);

        char n;
        while (cursor.hasNext()) {
            n = cursor.nextToken();
            if (n == PREFIX_OPTION) {
                cursor.backward();
                options = processOptions(cursor);
            } else if (n != WORD_SEPARATOR) {
                cursor.backward();
                arguments.add(processArgument(cursor));
            }
        }

        return Pair.of(arguments, options);
    }

    /**
     * Parse a single argument from the current position of the cursor until a separator is reached.
     * Can escape characters and quoted strings.
     *
     * Ex: The underlined strings are those who will be parsed and returned by this method.
     * <pre>
     * {@code
     * !command an argument "you see ?"
     *          ^^ ^^^^^^^^  ^^^^^^^^^
     * }
     * </pre>
     *
     * @param cursor The cursor which is set just before the starting point of the argument.
     * @return A string representing the parsed argument.
     * @throws CommandProcessingException If a syntax error is detected.
     */
    public String processArgument(@Nonnull final Cursor cursor) throws CommandProcessingException {
        StringBuilder buffer = new StringBuilder();
        char n;
        while (cursor.hasNext() && (n = cursor.nextToken()) != WORD_SEPARATOR) {
            if (n == ESCAPE_CHAR && (flags & FLAG_IGNORE_ESCAPE_CHARACTER) == 0) {
                // Escape
                buffer.append(processEscapeCharacter(cursor));
            } else if (n == SINGLE_QUOTE || n == DOUBLE_QUOTE) {
                // Quote
                cursor.backward();
                buffer.append(processQuotedString(cursor));
            } else {
                // Common character
                buffer.append(n);
            }
        }

        return buffer.toString();
    }

    /**
     * Just a commodity method that will return the next character regardless of its value or throw an exception if not
     * characters are left in the current cursor.
     *
     * Ex: The underlined characters are those who will be parsed and returned by this method.
     * <pre>
     * {@code
     * !command "hi i\'m here \!"
     *                ^        ^
     * }
     * </pre>
     *
     * @param cursor The current cursor starting just before the character to escape.
     * @return A {@code char} representing the next character.
     * @throws CommandProcessingException If no characters are left to be escaped.
     */
    public char processEscapeCharacter(@Nonnull final Cursor cursor) throws CommandProcessingException {
        try {
            return cursor.nextToken();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandProcessingException("Can't escape character, no characters left !", cursor.position);
        }
    }

    /**
     * Parse an argument that is quoted so it can contains spaces.
     * If the quotes are double quotes, the single quotes inside will not be treated as a quoted string, you don't need
     * to escape them.
     *
     * Ex: The underlined strings are those who will be parsed and returned by this method.
     * <pre>
     * {@code
     * !command hey "how are you ?" 'me' fine "i'm jeff"
     *               ^^^^^^^^^^^^^   ^^        ^^^^^^^^^
     * }
     * </pre>
     *
     * @param cursor The current cursor starting just before the quote.
     * @return A string containing the quoted string
     * @throws CommandProcessingException If the quote isn't closed or if an underlying method throws this exception.
     */
    public String processQuotedString(@Nonnull final Cursor cursor) throws CommandProcessingException {
        final StringBuilder quote = new StringBuilder();

        final short startPos = cursor.position;

        // Parse the type of quote
        boolean singleQuote = cursor.nextToken() == SINGLE_QUOTE;
        char n;
        // Append until there is a corresponding quote that isn't
        while (cursor.hasNext()) {
            n = cursor.nextToken();

            if (n == ESCAPE_CHAR && (flags & FLAG_IGNORE_ESCAPE_CHARACTER) == 0) {
                // Escape next character
                quote.append(processEscapeCharacter(cursor));

            } else if (n == (singleQuote ? SINGLE_QUOTE : DOUBLE_QUOTE)) {
                // Close quote and return
                return quote.toString();

            } else {
                // Append the character
                quote.append(n);
            }
        }

        // If we reach this, the quote was never closed
        throw new CommandProcessingException("Quote not closed !", startPos);
    }

    /**
     * Process the complete set of options that is provided at the end of the command.
     * When this method is called the rest of the cursor will be consumed and no other arguments are parsed.
     *
     * @param cursor The current cursor starting just before the start of the options.
     * @return A map containing the options string and there associated argument.
     * @throws CommandProcessingException If a syntax error is thrown.
     */
    public Map<String, String> processOptions(@Nonnull final Cursor cursor) throws CommandProcessingException {

        final Map<String, String> options = new LinkedHashMap<>();

        boolean prevWasPrefix = false;

        char n;
        while (cursor.hasNext()) {
            n = cursor.nextToken();

            if (prevWasPrefix) {
                // If the last character is a prefix

                if (n == PREFIX_OPTION) {
                    // Another prefix mark a long option
                    final short startPos = cursor.position;
                    final Pair<String, String> option = processLongOption(cursor);

                    if ((flags & FLAG_ALLOW_DUPLICATE_OPTION) == 0) {
                        if (options.containsKey(option.getLeft()))
                            throw new CommandProcessingException("Duplicate long option !", startPos);
                    }
                    options.put(option.getLeft(), option.getRight());

                    // Check for invalid characters in the option string
                } else if ((n == ESCAPE_CHAR && (flags & FLAG_IGNORE_ESCAPE_CHARACTER) == 0)
                        || n == WORD_SEPARATOR
                        || n == SINGLE_QUOTE
                        || n == DOUBLE_QUOTE) {
                    // Invalid character for an option
                    throw new CommandProcessingException("Invalid character in option !", cursor.position);
                } else {
                    // Common character, so backward and pass the cursor
                    cursor.backward();
                    final short startPos = cursor.position;
                    final Map<String, String> option = processShortOptions(cursor);

                    if ((flags & FLAG_ALLOW_DUPLICATE_OPTION) == 0) {
                        for (Map.Entry<String, String> entry : option.entrySet()) {
                            if (options.containsKey(entry.getKey()) && !option.get(entry.getKey()).equals(""))
                                throw new CommandProcessingException("Duplicate short option with argument !", startPos);
                        }
                    }
                    options.putAll(option);
                }

                prevWasPrefix = false;
            } else {
                // If the last wasn't a prefix, this one can only be a prefix or an error
                if (n == PREFIX_OPTION) {
                    // Prefix mode
                    prevWasPrefix = true;

                } else throw new CommandProcessingException("Not the option prefix !", cursor.position);
            }
        }

        return options;
    }

    /**
     * Parse an explicit option starting with two times the option prefix (--) and its argument if found.
     *
     * Ex: The underlined pairs of strings are those who will be parsed and returned by this method.
     * <pre>
     * {@code
     * !command hey -a nope --force --name "a name"
     *                        ^^^^^   ^^^^--^^^^^^
     * }
     * </pre>
     *
     * @param cursor The current cursor starting just before the name of the option.
     * @return A pair of two strings, the name of the option and its argument if present.
     * @throws CommandProcessingException If a syntax error is detected.
     */
    public Pair<String, String> processLongOption(@Nonnull final Cursor cursor) throws CommandProcessingException {
        final StringBuilder buffer = new StringBuilder();
        String name;
        String argument = "";

        char n;

        // Read the name of the long option (so until a space)
        while (cursor.hasNext() && (n = cursor.nextToken()) != WORD_SEPARATOR) {
            if (n == PREFIX_OPTION
                    || (n == ESCAPE_CHAR && (flags & FLAG_IGNORE_ESCAPE_CHARACTER) == 0)
                    || n == SINGLE_QUOTE
                    || n == DOUBLE_QUOTE)
                throw new CommandProcessingException("Illegal character in long option name !", cursor.position);
            buffer.append(n);
        }

        // Nothing (or a word separator) was found after the cursor
        if (buffer.length() == 0)
            throw new CommandProcessingException("Anonymous long option !", cursor.position);

        name = buffer.toString();

        // Check if there is an argument to this option
        if (cursor.hasNext()) {
            if (cursor.nextToken() != PREFIX_OPTION) {
                cursor.backward();
                if ((flags & FLAG_IGNORE_OPTIONS_ARGUMENTS) == 0)
                    argument = processArgument(cursor);
                else processArgument(cursor);
            } else cursor.backward();
        }


        // Make the pair
        return Pair.of(name, argument);
    }

    /**
     * Parse a short option that is a concatenation of multiple options and the optional argument of the last option.
     *
     * Ex: The underlined pairs of strings are those who will be parsed and returned by this method.
     * <pre>
     * {@code
     * !command hey -abc name -h "my name jeff" -v --name hi
     *               ^^^-^^^^  ^--^^^^^^^^^^^^   ^
     * }
     * </pre>
     *
     * @param cursor The current cursor starting just before the first short option.
     * @return A map containing each short option and its argument (but only the last can actually have an argument).
     * @throws CommandProcessingException If a syntax error is reached.
     */
    public Map<String, String> processShortOptions(@Nonnull final Cursor cursor) throws CommandProcessingException {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();

        char lastOption = 0;
        char n;

        // Collect one-letters arguments until a blank space
        while (cursor.hasNext() && (n = cursor.nextToken()) != WORD_SEPARATOR) {
            if ((n == ESCAPE_CHAR && (flags & FLAG_IGNORE_ESCAPE_CHARACTER) == 0)
                    || n == SINGLE_QUOTE
                    || n == DOUBLE_QUOTE) {
                // Filter illegal characters
                throw new CommandProcessingException("Illegal character in short option !", cursor.position);
            }

            lastOption = n;
            map.put(String.valueOf(n), "");
        }

        // No options were found (or very strange)
        if (lastOption == 0)
            throw new CommandProcessingException("Empty short options !", cursor.position);

        // Check if argument after
        if (cursor.hasNext()) {
            if (cursor.nextToken() != PREFIX_OPTION) {
                cursor.backward();
                if ((flags & FLAG_IGNORE_OPTIONS_ARGUMENTS) == 0) {
                    String arg = processArgument(cursor);
                    map.put(String.valueOf(lastOption), arg);
                } else processArgument(cursor);
            } else cursor.backward();
        }

        return map;
    }

    /**
     * A commodity class that helps us navigating through a string by storing our current position.
     */
    public static class Cursor {

        private short position = -1;
        private int size;
        private String payload;

        /**
         * Initialize a new cursor with the given payload.
         *
         * @param payload The non-null payload to bind the cursor to.
         */
        public Cursor(@Nonnull final String payload) {
            this.payload = payload;
            size = payload.length();
        }

        /**
         * Overload of {@link #reset(String)} with a null parameter.
         */
        public void reset() {
            reset(null);
        }

        /**
         * Reset the current cursor to the start of the new payload if provided or just reset the position if the provided
         * payload is {@code null}.
         *
         * @param newPayload The optional new payload.
         */
        public void reset(@Nullable final String newPayload) {
            if (newPayload != null) payload = newPayload;
            position = -1;
            size = payload.length();
        }

        /**
         * Make the cursor move ahead and get the token were it has moved.
         *
         * @return The next character of the payload.
         * @throws StringIndexOutOfBoundsException If the end of the payload was reached.
         */
        public char nextToken() throws StringIndexOutOfBoundsException {
            position++;
            return payload.charAt(position);
        }

        /**
         * Check if there is another character after the current one.
         * Used has a check before calling {@link #nextToken()}.
         *
         * @return True if we can move forward without raising an exception, otherwise false.
         */
        public boolean hasNext() {
            return position + 1 < size;
        }

        /**
         * Set the position of the cursor.
         * No checks or performed so it can be any position including out of bounds ones.
         *
         * @param position The position to move to.
         */
        public void setPosition(final short position) {
            this.position = position;
        }

        /**
         * Get the current position of the cursor.
         *
         * @return The position of the cursor in the payload.
         */
        public int getPosition() {
            return position;
        }

        /**
         * Get the current payload of the cursor.
         * Can't be null.
         *
         * @return The current payload.
         */
        @Nonnull
        public String getPayload() {
            return payload;
        }

        /**
         * Move the cursor back to the previous position.
         */
        public void backward() {
            if (position > -1) position--;
        }
    }

}
