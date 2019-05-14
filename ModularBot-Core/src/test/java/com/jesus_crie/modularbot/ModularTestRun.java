package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.command.AccessLevel;
import com.jesus_crie.modularbot.command.Command;
import com.jesus_crie.modularbot.command.CommandEvent;
import com.jesus_crie.modularbot.command.CommandModule;
import com.jesus_crie.modularbot.command.annotations.CommandInfo;
import com.jesus_crie.modularbot.command.annotations.RegisterPattern;
import com.jesus_crie.modularbot.command.processing.Option;
import com.jesus_crie.modularbot.command.processing.Options;
import com.jesus_crie.modularbot.core.ModularBot;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.core.dependencyinjection.InjectorTarget;
import com.jesus_crie.modularbot.core.dependencyinjection.LateInjectorTarget;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import com.jesus_crie.modularbot.core.utils.SerializableConsumer;
import com.jesus_crie.modularbot.graalvm.GUtils;
import com.jesus_crie.modularbot.graalvm.GraalModuleWrapper;
import com.jesus_crie.modularbot.graalvm.js.JSPromiseExecutorProxy;
import com.jesus_crie.modularbot.graalvm.js.JSRestActionWrapper;
import com.jesus_crie.modularbot.logger.ConsoleLoggerModule;
import com.jesus_crie.modularbot.messagedecorator.MessageDecoratorModule;
import com.jesus_crie.modularbot.messagedecorator.decorator.AutoDestroyMessageDecorator;
import com.jesus_crie.modularbot.messagedecorator.decorator.disposable.AlertReactionDecorator;
import com.jesus_crie.modularbot.messagedecorator.decorator.disposable.ConfirmReactionDecorator;
import com.jesus_crie.modularbot.messagedecorator.decorator.permanent.PanelReactionDecorator;
import com.jesus_crie.modularbot.messagedecorator.decorator.permanent.PollReactionDecorator;
import com.jesus_crie.modularbot.nightconfig.NightConfigWrapperModule;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.ModularLog;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ModularTestRun extends Module {

    private static final Logger LOG = LoggerFactory.getLogger("TestBot");

    @InjectorTarget
    public ModularTestRun(final SubModule1 module1, final SubModule2 module2) {
        super(new ModuleInfo("TestModule", "Jesus-Crie", "", "1.0", 1));
        LOG.info("Main: Sub module 1 injection: " + (module1 == null ? "failed" : "successful"));
        LOG.info("Main: Sub module 2 injection: " + (module2 == null ? "failed" : "successful"));
    }

    public static void main(String[] args) {
        final ModularBotBuilder botBuilder = new ModularBotBuilder(args[0])
                .provideBuiltModules(
                        new ConsoleLoggerModule()
                )
                .requestBaseModules()
                .requestModules(
                        ModularTestRun.class,
                        TestJSModule.class
                )
                .configureModule(NightConfigWrapperModule.class, "./example/config.json")
                .configureModule(MessageDecoratorModule.class, "./example/decorator_cache.json")
                .useShutdownNow();

        ConsoleLoggerModule.MIN_LEVEL = ModularLog.Level.INFO;

        Logger l = LoggerFactory.getLogger("hey");
        l.trace("HELLO");

        /// Decorator cache
        //config.useSecondaryConfig("deco", "./example/decorator.json");

        /* #### BUILD #### */
        ModularBot bot = botBuilder.resolveAndBuild();

        /// Commands
        CommandModule cmd = bot.getModuleManager().getModule(CommandModule.class);

        //cmd.setCreatorId(182547138729869314L);
        cmd.registerCommands(new StopCommand(), new EvalCommand());
        //cmd.registerCreatorQuickCommand("stop", e -> bot.shutdown());

        // Test JS
        TestJSModule js = bot.getModuleManager().getModule(TestJSModule.class);

        cmd.registerQuickCommand("testpromise", commandEvent ->
                js.acceptPromise(() -> 42)
        );

        cmd.registerQuickCommand("testpromisefail", commandEvent ->
                js.acceptPromise(() -> {
                    throw new RuntimeException();
                })
        );

        cmd.registerQuickCommand("testra", commandEvent ->
                js.listenRestAction(commandEvent.getChannel().sendMessage("YOLO !"))
        );


        /// Config
        NightConfigWrapperModule config = bot.getModuleManager().getModule(NightConfigWrapperModule.class);

        config.registerSingletonSecondaryConfig()

        Optional<Integer> startCount = config.getPrimaryConfig().getOptional("start_count");
        int count = startCount.orElse(0);
        count++;
        config.getPrimaryConfig().set("start_count", count);

        /// Decorators
        MessageDecoratorModule decorator = bot.getModuleManager().getModule(MessageDecoratorModule.class);

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

            decorator.registerDecorator(dec);
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

        // TODO remove
        //System.exit(0);

        try {
            bot.login();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShardsReady(final @Nonnull ModularBot bot) {
        super.onShardsReady(bot);
        LOG.info("Module initialized !");
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

    public static class SubModule1 extends Module {

        @LateInjectorTarget
        private ModularTestRun module;

        @Override
        public void onLoad(@Nonnull ModuleManager moduleManager, @Nonnull ModularBotBuilder builder) {
            LOG.info("Sub module 1: Load !");
            LOG.info("Sub module 1: Late module injection: " + (module == null ? "failed" : "successful"));
        }
    }

    public static class SubModule2 extends Module {

        @InjectorTarget
        public SubModule2(final SubModule1 module1) {
            super();
            LOG.info("Sub2: Sub module 1 injection: " + (module1 == null ? "failed" : "successful"));
        }

        @Override
        public void onLoad(@Nonnull ModuleManager moduleManager, @Nonnull ModularBotBuilder builder) {
            LOG.info("Sub module 2: Load !");
        }
    }

    public static class TestJSModule extends GraalModuleWrapper {

        @InjectorTarget
        public TestJSModule(@Nonnull final CommandModule cmdModule) {
            super(new File("./example/main.js"), cmdModule);
        }

        public void acceptPromise(Supplier<Integer> action) {
            safeInvoke("acceptPromise", GUtils.createJSPromise(getContext(), new JSPromiseExecutorProxy() {
                @Override
                public void run() {
                    resolve(action.get());
                }
            }));
        }

        public void listenRestAction(RestAction<Message> action) {
            safeInvoke("listenRestAction", GUtils.createJSPromise(getContext(), new JSRestActionWrapper<>(action)));
        }
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
