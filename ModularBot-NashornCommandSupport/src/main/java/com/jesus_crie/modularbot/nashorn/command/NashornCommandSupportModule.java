package com.jesus_crie.modularbot.nashorn.command;

import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.command.CommandModule;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import com.jesus_crie.modularbot.core.nashorn.JavaScriptModule;
import com.jesus_crie.modularbot.core.nashorn.NashornSupportModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@Deprecated
public class NashornCommandSupportModule extends Module {

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
                final JavaScriptCommand[] commands = (JavaScriptCommand[]) module.getUnderlyingModule().callMember("getCommands");

                if (commands == null)
                    continue;

                for (JavaScriptCommand command : commands) {
                    final JavaScriptCommandWrapper wrapper = new JavaScriptCommandWrapper(command);
                    commandModule.registerCommands(wrapper);
                }

            } catch (ClassCastException e) {
                LOG.error("Failed to load commands from JS module: " + module.getInfo().getName(), e);
            } catch (RuntimeException e) {
                // If cause is NoSuchMethodException that means that the module just doesn't have any commands.
                if (!(e.getCause() instanceof NoSuchMethodException)) {
                    throw e;
                }
            }
        }
    }
}
