package com.jeus_crie.modularbot;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_logger.ConsoleLoggerModule;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
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
                        new CommandModule(),
                        new ModularTestRun()
                )
                .build();

        CommandModule cmd = bot.getModuleManager().getModule(CommandModule.class);

        try {
            bot.login();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        bot.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
                LOG.info("Message received: " + event.getMessage().getContentRaw());
                if (event.getMessage().getContentRaw().equals("stop dude")) {
                    bot.shutdown();
                }
            }
        });
    }

    private final Logger logger = LoggerFactory.getLogger("ModuleTest");

    protected ModularTestRun() {
        super(new ModuleInfo("TestModule", "Jesus-Crie", "", "1.0", 1));
    }

    @Override
    public void onShardsReady(final @Nonnull ModularBot bot) {
        logger.info("Bot ready !");
    }
}
