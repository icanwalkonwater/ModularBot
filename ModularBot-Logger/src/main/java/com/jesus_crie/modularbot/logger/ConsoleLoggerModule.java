package com.jesus_crie.modularbot.logger;

import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import com.jesus_crie.modularbot.core.utils.Utils;
import org.slf4j.impl.ModularLog;
import org.slf4j.impl.ModularLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

public class ConsoleLoggerModule extends Module implements ModularLogger.OnLogListener {

    private static final ModuleInfo INFO = new ModuleInfo("Console Logger",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm:ss");

    /**
     * Editable format for the output log.
     * 0 - Time HH:mm:ss
     * 1 - Level
     * 2 - Thread
     * 3 - Name
     * 4 - Message
     */
    public static String FORMAT_LOG = "[{0}] [{1}] [{2}] [{3}]: {4}";

    /**
     * Editable format for the message in cause of error.
     * 0 - Error name
     * 1 - Error message
     */
    public static String FORMAT_ERROR = "{0}: {1}";

    /**
     * Minimum log level to allow the logs to be printed.
     */
    public static ModularLog.Level MIN_LEVEL = ModularLog.Level.INFO;

    public ConsoleLoggerModule() {
        super(INFO);
        ModularLogger.addListener(this);
    }

    @Override
    public void onLog(@Nonnull ModularLog log) {
        if (log.level.getLevel() >= MIN_LEVEL.getLevel()) {

            final String message = MessageFormat.format(FORMAT_LOG,
                    FORMAT_TIME.format(log.time),
                    log.level.getPrefix(),
                    log.thread,
                    Utils.fullClassToSimpleClassName(log.from),
                    log.error == null ? log.message : MessageFormat.format(FORMAT_ERROR, log.error.getClass().getName(), log.message));

            if (log.level.getLevel() < ModularLog.Level.WARN.getLevel()) {
                System.out.println(message);
            } else {
                System.err.println(message);
                if (log.error != null && log.level.getLevel() == ModularLog.Level.ERROR.getLevel())
                    log.error.printStackTrace();
            }
        }
    }
}
