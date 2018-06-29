package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import com.jesus_crie.modularbot_logger.ConsoleLoggerModule;
import com.jesus_crie.modularbot_nashorn_command_support.NashornCommandSupportModule;
import com.jesus_crie.modularbot_nashorn_support.NashornSupportModule;
import com.jesus_crie.modularbot_nashorn_support.module.JavaScriptModule;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.Optional;

public class ModularTestRun extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TestBot");

    public static void main(String[] args) {
        final ModularBot bot = new ModularBotBuilder(args[0])
                //.autoLoadBaseModules()
                .registerModules(
                        new ConsoleLoggerModule(),
                        new CommandModule(),
                        new NightConfigWrapperModule("./example/config.json"),
                        new NashornSupportModule("./example/scripts/"),
                        new NashornCommandSupportModule()
                )
                .useShutdownNow()
                .build();

        // ConsoleLoggerModule.MIN_LEVEL = ModularLog.Level.TRACE;

        /// Commands

        CommandModule cmd = bot.getModuleManager().getModule(CommandModule.class);
        //cmd.setCreatorId(182547138729869314L);
        cmd.registerCommands(new StopCommand());

        /// Config

        NightConfigWrapperModule config = bot.getModuleManager().getModule(NightConfigWrapperModule.class);
        Optional<Integer> startCount = config.getPrimaryConfig().getOptional("start_count");
        int count = startCount.orElse(0);
        count++;
        config.getPrimaryConfig().set("start_count", count);

        /// JS

        NashornSupportModule js = bot.getModuleManager().getModule(NashornSupportModule.class);
        JavaScriptModule testModule = js.getModuleByName("test");
        LOG.info("Test module file: " + testModule.getScriptLocation().getName());

        try {
            bot.login();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    protected ModularTestRun() {
        super(new ModuleInfo("TestModule", "Jesus-Crie", "", "1.0", 1));
    }

    @Override
    public void onShardsReady(final @Nonnull ModularBot bot) {
        LOG.info("Bot ready !");
    }

    @CommandInfo(
            name = "stop",
            shortDescription = "Stops the bot",
            options = {"NAME", "FORCE"})
    public static class StopCommand extends Command {

        public StopCommand() {
            super(AccessLevel.CREATOR);
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
