package com.jesus_crie.modularbot;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import com.jesus_crie.modularbot_nightconfigwrapper.NightConfigWrapperModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class ModularTestRun extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TestBot");

    public static void main(String[] args) {
        final ModularBot bot = new ModularBotBuilder(args[0])
                .autoLoadBaseModules()
                .useShutdownNow()
                .build();

        CommandModule cmd = bot.getModuleManager().getModule(CommandModule.class);
        //cmd.setCreatorId(182547138729869314L);
        cmd.registerCommands(new StopCommand());

        NightConfigWrapperModule config = bot.getModuleManager().getModule(NightConfigWrapperModule.class);
        Optional<Long> startCount = config.getPrimaryConfig().getOptional("start_count");
        long count = startCount.orElse(0L);
        count++;
        config.getPrimaryConfig().set("start_count", count);

        config.loadConfigGroup("testConfigs", new File("./configs/"), true, "^.+\\.json$");
        //config.addSecondaryConfigToGroup("testConfigs", "./configs/user.json");

        List<FileConfig> userCfgs = config.getConfigGroup("testConfigs");
        LOG.info(String.valueOf(userCfgs));
        for (FileConfig cfg : userCfgs) {
            cfg.set("hey", count);
        }

        cmd.addCustomPrefixForGuild(264001800686796800L, "!!");

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
