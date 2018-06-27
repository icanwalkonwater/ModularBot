package com.jesus_crie.modularbot_nashorn_command_support;

import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.processing.CommandPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * A class that represent a command that can be extended in JS and wrapped into a {@link JavaScriptCommandWrapper JavaScriptCommandWrapper}
 * to be registered in the command module.
 */
public class JavaScriptCommand {

    public String[] aliases;
    public String description = "No description.";
    public String shortDescription = "No description.";

    public AccessLevel accessLevel = AccessLevel.EVERYONE;
    public Option[] options = new Option[0];

    public CommandPattern[] patterns = new CommandPattern[0];

    /**
     * Create a {@link JavaScriptCommand JavaScriptCommand} with the given JS object.
     * This method must be called from a JS script.
     *
     * @param mirror The JS object provided.
     * @return A new instance of {@link JavaScriptCommand JavaScriptCommand} that holds the information provided.
     */
    public static JavaScriptCommand from(ScriptObjectMirror mirror) {
        final JavaScriptCommand command = new JavaScriptCommand();
        command.aliases = ((ScriptObjectMirror) mirror.getMember("aliases")).to(String[].class);
        command.description = (String) mirror.getMember("description");
        command.shortDescription = (String) mirror.getMember("shortDescription");
        command.accessLevel = (AccessLevel) mirror.getMember("accessLevel");
        command.options = ((ScriptObjectMirror) mirror.getMember("options")).to(Option[].class);
        command.patterns = ((ScriptObjectMirror) mirror.getMember("patterns")).to(CommandPattern[].class);

        return command;
    }
}
