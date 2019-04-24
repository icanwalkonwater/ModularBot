package com.jesus_crie.modularbot.command.processing;

import com.jesus_crie.modularbot.command.exception.CommandProcessingException;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandProcessorTest {

    private CommandProcessor.Cursor cursor;
    private CommandProcessor processor;

    @BeforeEach
    void setup() {
        cursor = new CommandProcessor.Cursor("");
        processor = new CommandProcessor();
    }

    @AfterEach
    void reset() {
        cursor.reset("");
    }

    @Test
    void cursor() {
        cursor.reset("test");

        assertThat(cursor.getPosition(), is(-1));
        assertThat(cursor.nextToken(), is('t'));
        assertThat(cursor.hasNext(), is(true));
        assertThat(cursor.nextToken(), is('e'));
        cursor.nextToken();
        cursor.nextToken();
        assertThat(cursor.hasNext(), is(false));
        assertThat(cursor.getPosition(), is(3));
        assertThrows(StringIndexOutOfBoundsException.class, cursor::nextToken);
        assertThat(cursor.getPosition(), is(4));

        cursor.reset();
        assertThat(cursor.getPosition(), is(-1));
        assertThat(cursor.hasNext(), is(true));
        assertThat(cursor.nextToken(), is('t'));

        cursor.nextToken();
        assertThat(cursor.nextToken(), is('s'));
        assertThat(cursor.getPosition(), is(2));
        cursor.backward();
        assertThat(cursor.getPosition(), is(1));
        assertThat(cursor.nextToken(), is('s'));
    }

    private static Stream<Arguments> provideEscape() {
        return Stream.of(
                Arguments.of("\\d", 'd'),
                Arguments.of("\\\\", '\\'),
                Arguments.of("\\-", '-'),
                Arguments.of("\\\"", '\"'),
                Arguments.of("\\'", '\'')
        );
    }

    @ParameterizedTest(name = "[{index}] Parse [ {0} ] to [ {1} ]")
    @MethodSource("provideEscape")
    void processEscapeCharacter(String escape, char parsed) throws CommandProcessingException {
        cursor.reset(escape);
        cursor.nextToken();
        assertThat(processor.processEscapeCharacter(cursor), is(parsed));
    }

    private static Stream<Arguments> provideQuote() {
        return Stream.of(
                Arguments.of("\"Hey\"", "Hey"),
                Arguments.of("'hey'", "hey"),
                Arguments.of("\"Hi, fellow 'humans' \\\"!\"", "Hi, fellow 'humans' \"!")
        );
    }

    private static Stream<String> provideQuoteFail() {
        return Stream.of(
                "'Hi\"",
                "Hi",
                "'Hey, how are you ?\"",
                "'Hi\\'"
        );
    }

    @ParameterizedTest(name = "[{index}] Parse [ {0} ] to [ {1} ]")
    @MethodSource("provideQuote")
    void processQuote(String quote, String parsed) throws CommandProcessingException {
        cursor.reset(quote);
        assertThat(processor.processQuotedString(cursor), equalTo(parsed));
    }

    @ParameterizedTest(name = "[{index}] Failed to parse [ {0} ]")
    @MethodSource("provideQuoteFail")
    void processQuoteFail(String quote) {
        cursor.reset(quote);
        assertThrows(CommandProcessingException.class, () -> processor.processQuotedString(cursor));
    }

    private static Stream<Arguments> provideArgument() {
        return Stream.of(
                Arguments.of("add   me lol", Arrays.asList("add", "me", "lol")),
                Arguments.of("add 'me lol'", Arrays.asList("add", "me lol")),
                Arguments.of("'add' me lol", Arrays.asList("add", "me", "lol")),
                Arguments.of("add \\' me" , Arrays.asList("add", "'", "me"))
        );
    }

    @ParameterizedTest(name = "[{index}] Map [ {0} ] to {1}")
    @MethodSource("provideArgument")
    void processArgument(String input, List<String> arguments) throws CommandProcessingException {
        assertThat(processor.process(input).getLeft(), equalTo(arguments));
    }

    private static Stream<Arguments> provideOption() {
        return Stream.of(
                Arguments.of("-n", Collections.singletonList(
                        Pair.of("n", ""))),
                Arguments.of("-noff", Arrays.asList(
                        Pair.of("n", ""),
                        Pair.of("o", ""),
                        Pair.of("f", ""))),
                Arguments.of("-no Hey", Arrays.asList(
                        Pair.of("n", ""),
                        Pair.of("o", "Hey"))),
                Arguments.of("-no 'Hey dude'", Arrays.asList(
                        Pair.of("n", ""),
                        Pair.of("o", "Hey dude"))),
                Arguments.of("-n -o 'Hi'", Arrays.asList(
                        Pair.of("n", ""),
                        Pair.of("o", "Hi"))),
                Arguments.of("-no --force", Arrays.asList(
                        Pair.of("n", ""),
                        Pair.of("o", ""),
                        Pair.of("force", ""))),
                Arguments.of("-no Hey --force false", Arrays.asList(
                        Pair.of("n", ""),
                        Pair.of("o", "Hey"),
                        Pair.of("force", "false")))
        );
    }

    private static Stream<String> provideOptionFail() {
        return Stream.of(
                "-no Hey dude",
                "-no Hi --force no dude",
                "---hey",
                "--force hi !",
                "--",
                "- hey",
                "-- yo h",
                "-n hey -n ho",
                "-n -n hy",
                "--force --force",
                "--force bite --force",
                "--force --force bite"
        );
    }

    @ParameterizedTest(name = "[{index}] Map [ {0} ] to {1}")
    @MethodSource("provideOption")
    void processOption(String input, List<Pair<String, String>> results) throws CommandProcessingException {
        cursor.reset(input);
        Map<String, String> options = processor.processOptions(cursor);

        assertThat(options.size(), is(results.size()));

        // Check each entries
        for (Pair<String, String> pair : results) {
            assertThat(options, hasKey(pair.getLeft()));
            assertThat(options.get(pair.getLeft()), equalTo(pair.getRight()));
        }
    }

    @ParameterizedTest(name = "[{index}] Fail to map [ {0} ]")
    @MethodSource("provideOptionFail")
    void processOptionFail(String input) {
        cursor.reset(input);
        assertThrows(CommandProcessingException.class, () ->
                System.out.println(processor.processOptions(cursor)));
    }

    private static Stream<Arguments> provideProcess() {
        return Stream.of(
                Arguments.of("add 'me first' yup --force --name 'np mek' -o lol",
                        Arrays.asList("add", "me first", "yup"),
                        new LinkedHashMap<String, String>() {{
                            put("force", "");
                            put("name", "np mek");
                            put("o", "lol");
                        }})
        );
    }

    @ParameterizedTest(name = "[{index}] Map [ {0} ] to args {1} and options {2}")
    @MethodSource("provideProcess")
    void process(String input, List<String> arguments, Map<String, String> options) throws CommandProcessingException {
        Pair<List<String>, Map<String, String>> result = processor.process(input);
        assertThat(result.getLeft(), equalTo(arguments));
        assertThat(result.getRight(), equalTo(options));
    }
}