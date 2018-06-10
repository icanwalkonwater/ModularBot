package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2_command.exception.CommandProcessingException;
import net.dv8tion.jda.core.utils.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandProcessor {

    private static final char PREFIX_OPTION = '-';
    private static final char WORD_SEPARATOR = ' ';
    private static final char ESCAPE_CHAR = '\\';
    private static final char SINGLE_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '"';

    // EXPERIMENTAL, very little tested so might not work as expected.

    /**
     * Allow duplicates of options, the argument will be the last parsed.
     * Can save computing power because the options aren't checked each time.
     */
    public static final int FLAG_ALLOW_DUPLICATE_OPTION = 0x01;

    /**
     * It will still parse the arguments as usual but they will not be in the final map.
     */
    public static final int FLAG_IGNORE_OPTIONS_ARGUMENTS = 0x02;

    /**
     * Escape characters are just not treated at all.
     */
    public static final int FLAG_IGNORE_ESCAPE_CHARACTER = 0x04;

    private final int flags;

    public CommandProcessor() {
        this(0);
    }

    public CommandProcessor(int flags) {
        this.flags = flags;
    }

    public Pair<List<String>, Map<String, String>> process(@Nonnull String input) throws CommandProcessingException {
        input = input.trim();

        List<String> arguments = new ArrayList<>();
        Map<String, String> options = new LinkedHashMap<>();
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

    public String processArgument(@Nonnull Cursor cursor) throws CommandProcessingException {
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

    public char processEscapeCharacter(@Nonnull Cursor cursor) throws CommandProcessingException {
        try {
            return cursor.nextToken();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandProcessingException("Can't escape character, no characters left !", cursor.position);
        }
    }

    public String processQuotedString(@Nonnull Cursor cursor) throws CommandProcessingException {
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

    public Map<String, String> processOptions(@Nonnull Cursor cursor) throws CommandProcessingException {

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

                } else {
                    throw new CommandProcessingException("Not the option prefix !", cursor.position);
                }
            }
        }

        return options;
    }

    public Pair<String, String> processLongOption(@Nonnull Cursor cursor) throws CommandProcessingException {
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

    public Map<String, String> processShortOptions(@Nonnull Cursor cursor) throws CommandProcessingException {
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

    public static class Cursor {

        private short position = -1;
        private int size;
        private String payload;

        public Cursor(@Nonnull String payload) {
            this.payload = payload;
            size = payload.length();
        }

        public void reset() {
            reset(null);
        }

        public void reset(@Nullable String newPayload) {
            if (newPayload != null) payload = newPayload;
            position = -1;
            size = payload.length();
        }

        public char nextToken() throws StringIndexOutOfBoundsException {
            position++;
            return payload.charAt(position);
        }

        public boolean hasNext() {
            return position + 1 < size;
        }

        public void setPosition(short position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public String getPayload() {
            return payload;
        }

        public void backward() {
            if (position > -1) position--;
        }
    }

}
