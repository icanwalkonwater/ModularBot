import com.jesus_crie.modularbot2.ModularBot;
import com.jesus_crie.modularbot2.ModularBotBuilder;
import com.jesus_crie.modularbot2.module.BaseModule;
import com.jesus_crie.modularbot2_command.CommandModule;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

public class ModularTestRun extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TestBot");

    public static void main(String[] args) {
        ModularTestRun module = new ModularTestRun();
        final ModularBot bot = new ModularBotBuilder(args[0])
                .useShutdownNow()
                .registerModule(module)
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
        super(new ModuleInfo(ModularTestRun.class, "TestModule", "Jesus-Crie", "", "1.0", 1));
    }

    @Override
    public void onShardsReady(final @Nonnull ModularBot bot) {
        logger.info("Bot ready !");
    }
}
