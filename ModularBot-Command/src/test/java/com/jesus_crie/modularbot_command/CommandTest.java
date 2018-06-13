package com.jesus_crie.modularbot_command;

import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.exception.InvalidCommandPatternMethodException;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    @Test
    void testArgumentRegister() {
        InnerCommand command = new InnerCommand();

        assertThat(command.getPatterns().size(), is(10));

        assertThat(command.getName(), equalTo("test"));
        assertThat(command.getAliases().size(), equalTo(2));
        assertThat(command.getDescription(), equalTo("No description."));
        assertThat(command.getDescription(), equalTo(command.getShortDescription()));

        assertThat(command.getOptions().size(), is(2));
        assertThat(command.getOptions().contains(Option.FORCE), is(true));
        assertThat(command.getOptions().contains(Option.NAME), is(true));
        assertThat(command.getOptions().contains(Option.RECURSIVE), is(false));
    }

    @Test
    void testArgumentRegisterFail() {
        assertThrows(InvalidCommandPatternMethodException.class, WrongCommand::new);
    }

    @CommandInfo(name = {"test", "t"}, options = {"FORCE", "NAME"})
    public static class InnerCommand extends Command {

        @RegisterPattern
        protected void noArguments() {
            System.out.println("no argument");
        }

        @RegisterPattern
        protected void oneArgument(CommandEvent event) {
            System.out.println("one argument");
        }

        @RegisterPattern(arguments = {"STRING", "USER"})
        protected void twoArgument(CommandEvent event, List<Object> args) {
            System.out.println("2 arg, list");
        }

        @RegisterPattern
        protected void twoArgument(CommandEvent event, Options options) {
            System.out.println("2 args, options");
        }

        @RegisterPattern(arguments = "STRING")
        protected void twoArgument(CommandEvent event, String arg) {
            System.out.println("2 args, arg");
        }

        @RegisterPattern(arguments = {"STRING", "CHANNEL", "USER", "INTEGER"})
        protected void threeArgument(CommandEvent event, Options options, List<Object> args) {
            System.out.println("3 args, list");
        }

        @RegisterPattern
        protected void threeArgument(CommandEvent event, Options options, URL url) {
            System.out.println("3 args, arg");
        }

        @RegisterPattern(arguments = "WORD")
        protected void threeArgument(CommandEvent event, Options options, String... string) {
            System.out.println("3 args, arg implicit");
        }

        @RegisterPattern
        protected void manyArgument(CommandEvent event, Options options, String string, User target, TextChannel hey, URL yo) {
            System.out.println("many args, implicit");
        }

        @RegisterPattern(arguments = {"STRING", "CHANNEL"})
        protected void manyArgument(CommandEvent event, Options options, String hey, TextChannel yo) {
            System.out.println("many args, explicit");
        }
    }

    public static class WrongCommand extends Command {

        public WrongCommand() {
            super("lulz", AccessLevel.EVERYONE);
        }

        @RegisterPattern
        public void someInvalidMethod() {

        }
    }

}