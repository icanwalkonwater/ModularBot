package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.utils.Waiter;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import com.jesus_crie.modularbot_logger.ConsoleLoggerModule;
import com.jesus_crie.modularbot_message_decorator.decorator.AutoDestroyMessageDecorator;
import com.jesus_crie.modularbot_message_decorator.decorator.MessageDecorator;
import com.jesus_crie.modularbot_message_decorator.decorator.disposable.AlertMessageDecorator;
import com.jesus_crie.modularbot_message_decorator.decorator.disposable.ConfirmMessageDecorator;
import com.jesus_crie.modularbot_nashorn_command_support.NashornCommandSupportModule;
import com.jesus_crie.modularbot_nashorn_support.NashornSupportModule;
import com.jesus_crie.modularbot_nashorn_support.module.JavaScriptModule;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ModularTestRun extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TestBot");

    public static void main(String[] args) {
        final ModularBot bot = new ModularBotBuilder(args[0])
                .registerModules(
                        new ConsoleLoggerModule(),
                        new CommandModule(),
                        new NightConfigWrapperModule("./example/config.json"),
                        new NashornSupportModule("./example/scripts/"),
                        new NashornCommandSupportModule(),

                        new ModularTestRun()
                )
                .autoLoadBaseModules()
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

        /// Decorator

        cmd.registerQuickCommand("dA10", e -> {
            Message m = e.getTextChannel().sendMessage("dA10").complete();
            AlertMessageDecorator dec = new AlertMessageDecorator(m, 1000 * 10, false);
            dec.setup();
        });

        cmd.registerQuickCommand("dAD10", e -> {
            Message m = e.getChannel().sendMessage("dAD 10").complete();
            AutoDestroyMessageDecorator d = new AutoDestroyMessageDecorator(m, 10, TimeUnit.SECONDS, () -> LOG.info("Timeout"));
        });

        try {
            bot.login();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        // Test waiter
    }

    private final List<MessageDecorator<?>> decorators = new ArrayList<>();

    protected ModularTestRun() {
        super(new ModuleInfo("TestModule", "Jesus-Crie", "", "1.0", 1));
    }

    @Override
    public void onShardsReady(final @Nonnull ModularBot bot) {
        super.onShardsReady(bot);
        LOG.info("Bot ready !");

        /// Decorator
        /*TextChannel channel = bot.getTextChannelById(264001800686796800L);

        Message alert10s = channel.sendMessage("Alert 10s").complete();
        Message alert10sDestroy = channel.sendMessage("Alert 10s destroy").complete();
        Message alertInfinite = channel.sendMessage("Alert infinite").complete();
        Message confirm10s = channel.sendMessage("Confirm 10s").complete();
        Message confirm10sDestroy = channel.sendMessage("Confirm 10s destroy").complete();
        Message confirmInfinite = channel.sendMessage("Confirm infinite").complete();
        Message destroy10s = channel.sendMessage("Destroy 10s").complete();

        AlertMessageDecorator alert10sDecorator = new AlertMessageDecorator(alert10s, 1000 * 10, false);
        AlertMessageDecorator alert10sDestroyDecorator = new AlertMessageDecorator(alert10sDestroy, 1000 * 10, true);
        AlertMessageDecorator alertInfiniteDecorator = new AlertMessageDecorator(alertInfinite, 0, false);

        ConfirmMessageDecorator confirm10sDecorator = new ConfirmMessageDecorator(confirm10s, 1000 * 10,
                b -> LOG.info("Result 10s: " + b),
                () -> LOG.info("Time out 10s"),
                false);
        ConfirmMessageDecorator confirm10sDestroyDecorator = new ConfirmMessageDecorator(confirm10sDestroy, 1000 * 10,
                b -> LOG.info("Result 10sD: " + b),
                () -> LOG.info("Time out 10sD"),
                true);
        ConfirmMessageDecorator confirmInfiniteDecorator = new ConfirmMessageDecorator(confirmInfinite, 0,
                b -> LOG.info("Result I: " + b),
                () -> LOG.info("Will never happen"),
                false);

        AutoDestroyMessageDecorator destroy10sDecorator = new AutoDestroyMessageDecorator(destroy10s, 10, TimeUnit.SECONDS, null);

        Collections.addAll(decorators,
                alert10sDecorator,
                alert10sDestroyDecorator,
                alertInfiniteDecorator,
                confirm10sDecorator,
                confirm10sDestroyDecorator,
                confirmInfiniteDecorator,
                destroy10sDecorator);

        decorators.forEach(MessageDecorator::setup);*/
    }

    @Override
    public void onShutdownShards() {
        decorators.forEach(MessageDecorator::destroy);
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
