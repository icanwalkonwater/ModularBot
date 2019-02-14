package com.jesus_crie.modularbot;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.utils.SerializableConsumer;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import com.jesus_crie.modularbot_logger.ConsoleLoggerModule;
import com.jesus_crie.modularbot_message_decorator.MessageDecoratorModule;
import com.jesus_crie.modularbot_message_decorator.decorator.AutoDestroyMessageDecorator;
import com.jesus_crie.modularbot_message_decorator.decorator.disposable.AlertReactionDecorator;
import com.jesus_crie.modularbot_message_decorator.decorator.disposable.ConfirmReactionDecorator;
import com.jesus_crie.modularbot_message_decorator.decorator.permanent.PanelReactionDecorator;
import com.jesus_crie.modularbot_message_decorator.decorator.permanent.PollReactionDecorator;
import com.jesus_crie.modularbot_nashorn_support.JavaScriptModule;
import com.jesus_crie.modularbot_nashorn_support.NashornSupportModule;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.ModularLog;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ModularTestRun extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TestBot");

    public static void main(String[] args) {
        CommandModule cmd = new CommandModule();
        NightConfigWrapperModule config = new NightConfigWrapperModule("./example/config.json");
        //NashornSupportModuleOld js = new NashornSupportModuleOld("./example/scripts/");
        NashornSupportModule jsNew = new NashornSupportModule("./example/scripts/");
        MessageDecoratorModule decorator = new MessageDecoratorModule("./example/decorator_cache.json");

        final ModularBotBuilder botBuilder = new ModularBotBuilder(args[0])
                .registerModules(
                        cmd, config, /*js*/jsNew, decorator,
                        new ModularTestRun()
                )
                .autoLoadBaseModules()
                .useShutdownNow();

        ConsoleLoggerModule.MIN_LEVEL = ModularLog.Level.INFO;

        /// Commands

        //cmd.setCreatorId(182547138729869314L);
        cmd.registerCommands(new StopCommand(), new EvalCommand());

        /// JS

        jsNew.getModuleByName("TestModule2").ifPresent(testModule ->
                LOG.info("Test module: " + testModule.getInfo())
        );

        /// Decorator cache
        //config.useSecondaryConfig("deco", "./example/decorator.json");

        /* #### BUILD #### */
        ModularBot bot = botBuilder.build();

        /// Config

        Optional<Integer> startCount = config.getPrimaryConfig().getOptional("start_count");
        int count = startCount.orElse(0);
        count++;
        config.getPrimaryConfig().set("start_count", count);

        // Alert 10s
        cmd.registerQuickCommand("dA10", e -> {
            Message m = e.getChannel().sendMessage("dA 10").complete();
            AlertReactionDecorator dec = new AlertReactionDecorator(m, 1000 * 10, false);
            dec.setup();
        });

        // Alert 10s delete after
        cmd.registerQuickCommand("dA10d", e -> {
            Message m = e.getChannel().sendMessage("dA 10 d").complete();
            AlertReactionDecorator dec = new AlertReactionDecorator(m, 1000 * 10, true);
            dec.setup();
        });

        // Alert 100s + save
        cmd.registerQuickCommand("dA100", e -> {
            Message m = e.getChannel().sendMessage("dA 100").complete();
            AlertReactionDecorator dec = new AlertReactionDecorator(m, 1000 * 100, false);
            dec.setup();

            config.useSecondaryConfig("deco", "./example/decorator.json");
            FileConfig cfg = config.getSecondaryConfig("deco");
            cfg.set("dec100", dec.serialize());
            cfg.save();
        });

        // Alert 100s loaded.
        cmd.registerQuickCommand("ddA100", e -> {
            FileConfig cfg = config.getSecondaryConfig("deco");
            AlertReactionDecorator dec = AlertReactionDecorator.tryDeserialize(cfg.get("dec100"), bot);
            dec.setup();
        });

        // Auto destroy 10s
        cmd.registerQuickCommand("dAD10", e -> {
            Message m = e.getChannel().sendMessage("dAD 10").complete();
            AutoDestroyMessageDecorator d = new AutoDestroyMessageDecorator(m, 10, TimeUnit.SECONDS, () -> LOG.info("Timeout"));
        });

        // Confirm 10s
        cmd.registerQuickCommand("dC10", e -> {
            Message m = e.getChannel().sendMessage("dC 10").complete();
            ConfirmReactionDecorator dec = new ConfirmReactionDecorator(m, 1000 * 10,
                    (deco, success) -> e.fastReply("Choice: " + success),
                    deco -> LOG.info("Timeout"),
                    false);
            dec.setup();
        });

        // Confirm 10s delete after
        cmd.registerQuickCommand("dC10d", e -> {
            Message m = e.getChannel().sendMessage("dC 10 d").complete();
            ConfirmReactionDecorator dec = new ConfirmReactionDecorator(m, 1000 * 10,
                    (deco, success) -> e.fastReply("Choice: " + success),
                    deco -> LOG.info("Timeout"),
                    true);
            dec.setup();
        });

        // Poll 60s, register
        cmd.registerQuickCommand("dP60", e -> {
            Message m = e.getChannel().sendMessage("dP 60").complete();
            PollReactionDecorator dec = new PollReactionDecorator(m, 1000 * 60, null,
                    (SerializableConsumer<PollReactionDecorator>) deco ->
                            deco.getBinding().getChannel().sendMessage("Time out, votes: " + deco.collectVotesByName()).queue(),
                    "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3");
            dec.setup();
            dec.register(decorator);
        });

        cmd.registerQuickCommand("dPa", e -> {
            Message m = e.getChannel().sendMessage("\uD83D\uDCD6 Print books\n \uD83C\uDF7A Print beer\n <:roucool:347030179589390338> Print roucool").complete();
            PanelReactionDecorator dec = new TestPanelDecorator(m, 0);
            dec.setup();
            dec.register(decorator);
        });

        try {
            bot.login();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public static class TestPanelDecorator extends PanelReactionDecorator {

        public TestPanelDecorator(@Nonnull final Message binding, final long timeout) {
            super(binding, timeout);
        }

        @RegisterPanelAction(position = 0, emote = "\uD83D\uDCD6")
        public void bookPanel(GenericMessageReactionEvent e) {
            e.getChannel().sendMessage("\uD83D\uDCD6 BOOKS EVERYWHERE !").queue();
        }

        @RegisterPanelAction(position = 1, emote = "\uD83C\uDF7A")
        public void beerPanel(GenericMessageReactionEvent e) {
            e.getChannel().sendMessage("\uD83C\uDF7A BEER EVERYWHERE !").queue();
        }

        @RegisterPanelAction(position = 2, emoteId = 347030179589390338L)
        public void roucoolPanel(GenericMessageReactionEvent e) {
            e.getChannel().sendMessage("<:roucool:347030179589390338> ROUCOOL EVERYWHERE !").queue();
        }

        @RegisterPanelAction(position = 0, emote = "\uD83D\uDD11")
        public void removePanel(GenericMessageReactionEvent e) {
            e.getChannel().sendMessage("Okay, let s remove that").queue();
            destroy();
        }
    }

    protected ModularTestRun() {
        super(new ModuleInfo("TestModule", "Jesus-Crie", "", "1.0", 1));
    }

    @Override
    public void onShardsReady(final @Nonnull ModularBot bot) {
        super.onShardsReady(bot);
        LOG.info("Module initialized !");
    }

    @CommandInfo(
            name = "stop",
            shortDescription = "Stops the bot",
            options = {"NAME", "FORCE"})
    public static class StopCommand extends Command {

        public StopCommand() {
            super(AccessLevel.CREATOR);
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

    @CommandInfo(
            name = "eval",
            shortDescription = "Evaluate some js code"
    )
    public static class EvalCommand extends Command {

        public EvalCommand() {
            super(AccessLevel.CREATOR);
        }

        @RegisterPattern(arguments = "STRING")
        public void execute(@Nonnull final CommandEvent event, @Nonnull final String script) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
            engine.put("event", event);
            engine.put("deco", event.getModule().getBot().getModuleManager().getModule(MessageDecoratorModule.class));

            Object res;
            try {
                res = engine.eval(script);
            } catch (ScriptException e) {
                res = e;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Eval");
            builder.setColor(Color.WHITE);
            builder.addField("To evaluate", "```js\n" + script + "```", false);
            builder.addField("Result", "```js\n" + res + "```", false);

            event.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
