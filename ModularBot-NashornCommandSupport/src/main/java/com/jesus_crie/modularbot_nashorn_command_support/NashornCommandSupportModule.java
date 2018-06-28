package com.jesus_crie.modularbot_nashorn_command_support;

import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_nashorn_support.NashornSupportModule;
import com.jesus_crie.modularbot_nashorn_support.module.JavaScriptModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

public class NashornCommandSupportModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("NashornCommandSupportModule");

    private static final ModuleInfo INFO = new ModuleInfo("JS Nashorn Command Support", ModularBotBuildInfo.AUTHOR,
            ModularBotBuildInfo.GITHUB_URL, ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    public NashornCommandSupportModule() {
        super(INFO);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onLoad(@Nonnull ModuleManager moduleManager, @Nonnull ModularBotBuilder builder) {
        final NashornSupportModule nashornModule = moduleManager.getModule(NashornSupportModule.class);
        final CommandModule commandModule = moduleManager.getModule(CommandModule.class);

        for (JavaScriptModule module : nashornModule.getModules()) {
            try {
                final JavaScriptCommand[] commands = (JavaScriptCommand[]) module.getEngine().invokeFunction("getCommands");

                if (commands == null)
                    continue;

                for (JavaScriptCommand command : commands) {
                    final JavaScriptCommandWrapper wrapper = new JavaScriptCommandWrapper(command);
                    commandModule.registerCommands(wrapper);
                }

            } catch (ScriptException | ClassCastException e) {
                LOG.error("Failed to load commands from JS module: " + module.getJsModule().getInfo().getName(), e);
            } catch (NoSuchMethodException ignore) {
                // The module have no command to register.
            }
        }
    }
}
