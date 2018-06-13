package com.jeus_crie.modularbot;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import com.jesus_crie.modularbot_logger.ConsoleLoggerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

public class ModularTestRun extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TestBot");

    public static void main(String[] args) {
        final ModularBot bot = new ModularBotBuilder(args[0])
                .useShutdownNow()
                .registerModules(
                        new ConsoleLoggerModule(),
                        new CommandModule()
                )
                .build();

        CommandModule cmd = bot.getModuleManager().getModule(CommandModule.class);
        cmd.registerCommands(new StopCommand());

        try {
            bot.login();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    private final Logger logger = LoggerFactory.getLogger("ModuleTest");

    protected ModularTestRun() {
        super(new ModuleInfo("TestModule", "Jesus-Crie", "", "1.0", 1));
    }

    @Override
    public void onShardsReady(final @Nonnull ModularBot bot) {
        logger.info("Bot ready !");
    }

    @CommandInfo(
            name = "stop",
            shortDescription = "Stops the bot",
            options = {"NAME", "FORCE"})
    public static class StopCommand extends Command {

        public StopCommand() {
            super();
            LOG.info(String.valueOf(patterns));
        }

        @RegisterPattern
        public void execute(CommandEvent event, Options options) {
            if (options.has(Option.FORCE)) {
                event.fastReply("Force shut down");
            } else if (options.has(Option.NAME)) {
                if (options.get(Option.NAME) != null)
                    event.fastReply("Shut down special for " + options.get(Option.NAME));
                else {
                    event.fastReply("Missing argument for name");
                    return;
                }
            } else {
                event.fastReply("Regular shutdown");
            }

            event.getModule().getBot().shutdown();
        }
    }
}
