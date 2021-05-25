package io.github.bingorufus.chatitemdisplay.util.logger;

import io.github.bingorufus.chatitemdisplay.util.ChatItemConfig;
import org.bukkit.Bukkit;

public class DebugLogger {
    private DebugLogger() {
        //Prevent the util class from being instantialized
    }

    public static void log(String message) {
        if (ChatItemConfig.DEBUG_MODE.getCachedValue())
            Bukkit.getLogger().info(message);
    }
}
