package com.jesus_crie.modularbot.command.processing;

import com.jesus_crie.modularbot.command.CommandModule;
import com.jesus_crie.modularbot.command.annotations.RegisterArgument;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;

class ArgumentTest {

    @Test
    void register() {
        assertThat(Argument.getArgument("BOOLEAN"), Matchers.is(Argument.BOOLEAN));
        assertThat(Argument.getArgument("TEST"), is(nullValue()));
        Argument.registerArguments(TestArguments.class);
        assertThat(Argument.getArgument("TEST"), Matchers.is(TestArguments.TEST));
    }

    @Test
    void forString() {
        String in = "$ hey !.";
        Argument a = Argument.forString(in);
        assertThat(a.tryMap(new CommandModule(), in), is(notNullValue()));
        assertThat(a.tryMap(new CommandModule(), "hey"), is(nullValue()));
    }

    @Test
    void getGeneric() {
        assertThat(Argument.STRING.getArgumentsType(), equalTo(String.class));
    }

    @SuppressWarnings("unchecked")
    public static class TestArguments {

        @RegisterArgument
        public static final Argument TEST = new Argument(String.class, "", Argument.EMPTY_MAPPER);
        @RegisterArgument
        public static Argument HEY = new Argument(String.class, "", Argument.EMPTY_MAPPER);
        @RegisterArgument
        public static Argument<String> YO = new Argument<>(String.class, "", (m, b) -> "");
    }
}