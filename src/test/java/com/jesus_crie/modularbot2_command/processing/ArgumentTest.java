package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2_command.CommandModule;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;

class ArgumentTest {

    @Test
    void register() {
        assertThat(Argument.getArgument("BOOLEAN").orElse(null), is(Argument.BOOLEAN));
        assertThat(Argument.getArgument("TEST").orElse(null), is(nullValue()));
        Argument.registerArguments(TestArguments.class);
        assertThat(Argument.getArgument("TEST").orElse(null), is(TestArguments.TEST));
    }

    @Test
    void forString() {
        String in = "$ hey !.";
        Argument a = Argument.forString(in);
        assertThat(a.tryMap(new CommandModule(), in), is(notNullValue()));
        assertThat(a.tryMap(new CommandModule(), "hey"), is(nullValue()));
    }

    public static class TestArguments {

        @Argument.RegisterArgument
        public static Argument<Object> TEST = new Argument<>("", Argument.EMPTY_MAPPER);
        @Argument.RegisterArgument
        public static Argument<Object> HEY = new Argument<>("", Argument.EMPTY_MAPPER);
        @Argument.RegisterArgument
        public static Argument<Object> YO = new Argument<>("", Argument.EMPTY_MAPPER);
    }
}