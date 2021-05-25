package io.github.bingorufus.chatitemdisplay.util;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.bingorufus.chatitemdisplay.ChatItemDisplay;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ChatItemConfig {
    public static final ConfigOption<String> COOLDOWN = new ConfigOption<>("messages.cooldown", String.class);
    public static final ConfigOption<String> MISSING_PERMISSION_GENERIC = new ConfigOption<>("messages.missing-permission", String.class);
    public static final ConfigOption<String> INVALID_ID = new ConfigOption<>("messages.invalid-id", String.class);
    public static final ConfigOption<String> EMPTY_DISPLAY = new ConfigOption<>("messages.player-not-displaying-anything", String.class);
    public static final ConfigOption<String> FEATURE_DISABLED = new ConfigOption<>("messages.feature-disabled", String.class);
    public static final ConfigOption<String> MAP = new ConfigOption<>("messages.map-notification", String.class);
    public static final ConfigOption<String> CONTAINS_BLACKLIST = new ConfigOption<>("messages.contains-blacklist", String.class);
    public static final ConfigOption<String> TOO_LARGE_MESSAGE = new ConfigOption<>("messages.too-large-display", String.class);
    public static final ConfigOption<String> EMPTY_HAND = new ConfigOption<>("messages.empty-hand", String.class);
    public static final ConfigOption<String> CHAT_ITEM_FORMAT = new ConfigOption<>("display-messages.inchat-item-format", String.class);
    public static final ConfigOption<String> CHAT_ITEM_FORMAT_MULTIPLE = new ConfigOption<>("display-messages.inchat-item-format-multiple", String.class);
    public static final ConfigOption<String> CHAT_INVENTORY_FORMAT = new ConfigOption<>("display-messages.inchat-inventory-format", String.class);
    public static final ConfigOption<String> COMMAND_ITEM_FORMAT = new ConfigOption<>("display-messages.item-display-format", String.class);
    public static final ConfigOption<String> COMMAND_ITEM_FORMAT_MULTIPLE = new ConfigOption<>("display-messages.item-display-format-multiple", String.class);
    public static final ConfigOption<String> COMMAND_INVENTORY_FORMAT = new ConfigOption<>("display-messages.inventory-display-format", String.class);
    public static final ConfigOption<Boolean> BUNGEE = new ConfigOption<>("send-to-bungee", Boolean.class);
    public static final ConfigOption<Boolean> DEBUG_MODE = new ConfigOption<>("debug-mode", Boolean.class);
    public static final ConfigOption<Integer> MAX_DISPLAYS = new ConfigOption<>("maximum-displays", Integer.class);
    private static FileConfiguration config;
    private static long cacheTime;
    private static final LoadingCache<ConfigOption<?>, Object> configCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<ConfigOption<?>, Object>() {
                        @Override
                        public Object load(@NotNull ConfigOption<?> configOption) {
                            refreshConfig();
                            if (!getConfig().contains(configOption.path)) {
                                if (configOption.type.isAssignableFrom(List.class)) {
                                    return new ArrayList<>();
                                }
                                throw new FieldAccessException("Cannot find a value at " + configOption.path);
                            }
                            return configOption.getValue();
                        }
                    });
    public static final ConfigOption<List<Material>> BLACKLISTED_ITEMS = new ConfigOption("blacklisted-items", List.class) {
        @Override
        protected List<Material> getValue() {
            ArrayList<Material> out = new ArrayList<>();
            for (String s : getConfig().getStringList(this.path)) {
                Material mat = Material.matchMaterial(s);
                if (mat == null) continue;
                out.add(mat);
            }
            return out;
        }
    };

    public static void reloadMessages() {
        configCache.asMap().values().forEach(System.out::println);
        configCache.invalidateAll();
        refreshConfig();
    }

    public static FileConfiguration getConfig() {
        if (cacheTime == 0 || System.currentTimeMillis() - cacheTime >= 300000) refreshConfig();
        return config;
    }

    public static void refreshConfig() {

        ChatItemDisplay.getInstance().saveDefaultConfig();
        ChatItemDisplay.getInstance().reloadConfig();
        config = ChatItemDisplay.getInstance().getConfig();
        cacheTime = System.currentTimeMillis();
    }

    public static class ConfigOption<T> {

        protected final String path;
        private final Class<T> type;

        public ConfigOption(String path, Class<T> type) {
            this.path = path;
            this.type = type;
        }

        public T getCachedValue() {
            try {
                return (T) configCache.get(this);
            } catch (ExecutionException e) {
                return null;
            }
        }

        protected T getValue() {

            return getConfig().getObject(path, type);
        }

    }

}
