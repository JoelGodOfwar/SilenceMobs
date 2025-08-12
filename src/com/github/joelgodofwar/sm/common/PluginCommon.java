package com.github.joelgodofwar.sm.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;
import com.github.joelgodofwar.sm.util.Version;
import com.github.joelgodofwar.sm.util.YmlConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;

/**
 * Utility class containing common methods for plugin development.
 */
public class PluginCommon {

    /**
     * The minimum configuration version expected by the plugin.
     */
    public static final Version minConfigVersion = new Version("1.0.1");

    /**
     * Checks and updates the plugin's configuration file if necessary.
     *
     * @param plugin    The plugin instance.
     * @param logger    The PluginLogger instance for logging messages.
     * @param reporter  The DetailedErrorReporter for error reporting.
     * @param config    The YmlConfiguration object to load the new config into.
     * @param oldConfig The YamlConfiguration object to load the old config into.
     */
    public static void checkAndUpdateConfig(JavaPlugin plugin, PluginLogger logger, DetailedErrorReporter reporter,
                                            YmlConfiguration config, YamlConfiguration oldConfig) {
        boolean needConfigUpdate = false;
        File dataFolder = plugin.getDataFolder();
        File configFile = new File(dataFolder, "config.yml");
        File oldConfigFile = new File(dataFolder, "old_config.yml");

        // Ensure data folder exists
        try {
            if (!dataFolder.exists()) {
                logger.log("Data Folder doesn't exist");
                logger.log("Creating Data Folder");
                boolean result = dataFolder.mkdirs();
                if (result) {
                    logger.log("Data Folder Created at " + dataFolder);
                }
            }
            if (!configFile.exists()) {
                logger.log("config.yml not found, creating!");
                plugin.saveResource("config.yml", true);
                needConfigUpdate = true; // New config is created, treat as update
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
            return;
        }

        // Load existing config
        try {
            oldConfig.load(configFile);
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
            return;
        }

        // Determine config version
        String versionString = oldConfig.getString("version", "0.0.1"); // Default to 0.0.1 if version is missing
        boolean isVersion100 = oldConfig.get("console.colorful_console") == null; // Version 1.0.0 lacks console.colorful_console
        Version configVersion;

        // Parse version string safely
        try {
            assert versionString != null;
            configVersion = new Version(versionString);
        } catch (Exception e) {
            logger.warn("Invalid config version '" + versionString + "', treating as outdated (0.0.1)");
            configVersion = new Version("0.0.1"); // Treat invalid version as 0.0.1
            needConfigUpdate = true;
        }

        // Check if update is needed
        if (!needConfigUpdate) {
            needConfigUpdate = isVersion100 || configVersion.compareTo(minConfigVersion) < 0;
        }

        if (needConfigUpdate) {
            try {
                // Backup existing config
                copyFile(configFile.getPath(), oldConfigFile.getPath());
                logger.log("Backed up config.yml to old_config.yml");

                // Load old config again for migration
                oldConfig.load(oldConfigFile);

                // Save new config from resources
                logger.log("Saving new config.yml...");
                plugin.saveResource("config.yml", true);

                // Load new config
                config.load(configFile);

                // Migrate values
                if (isVersion100) {
                    // Version 1.0.0: Migrate specific values, use defaults for new fields
                    logger.log("Migrating config from version 1.0.0...");
                    config.set("auto_update_check", oldConfig.get("auto_update_check", true));
                    config.set("debug", oldConfig.get("debug", false));
                    config.set("lang", oldConfig.get("lang", "en_US"));
                    config.set("console.colorful_console", true); // Default for 1.0.1
                    config.set("console.longpluginname", true);  // Default for 1.0.1
                } else {
                    // Other outdated versions: Copy all values directly
                    logger.log("Updating config from version " + configVersion.getVersion() + "...");
                    for (String key : oldConfig.getKeys(true)) {
                        if (!key.equals("version")) { // Preserve new version
                            config.set(key, oldConfig.get(key));
                        }
                    }
                }

                // Save updated config
                config.save(configFile);
                logger.log("config.yml has been updated to version " + minConfigVersion.getVersion());
            } catch (Exception exception) {
                reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
            }
        } else {
            logger.log("config.yml is up-to-date (version " + configVersion.getVersion() + ")");
            // No update needed, just load the config
            try {
                config.load(configFile);
            } catch (Exception exception) {
                reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
            }
        }
    }

    /**
     * Verifies the Minecraft server version against minimum and maximum supported versions.
     *
     * @param plugin   The plugin instance for logging and reporting.
     * @param server   The server instance to check the version of.
     * @param logger   The PluginLogger instance for logging warnings.
     * @param reporter The DetailedErrorReporter for error reporting.
     * @return The current server version, or the maximum version if parsing fails.
     */
    public static Version verifyMinecraftVersion(JavaPlugin plugin, Server server, PluginLogger logger, DetailedErrorReporter reporter) {
        Version minimum = PluginLibrary.MINIMUM_MINECRAFT_VERSION;
        Version maximum = PluginLibrary.MAXIMUM_MINECRAFT_VERSION;
        try {
            Version current = new Version(server);

            if (current.compareTo(minimum) < 0) {
                logger.warn("Version " + current + " is lower than the minimum " + minimum);
            }
            if (current.compareTo(maximum) > 0) {
                logger.warn(ChatColor.RED + "Version " + current + " has not yet been tested! Proceed with caution." + ChatColor.RESET);
            }

            return current;
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(exception).messageParam(maximum));
            return maximum;
        }
    }

    /**
     * Calculates and formats the time taken for a process to complete.
     *
     * @param startTime The start time in milliseconds.
     * @return A formatted string representing the elapsed time (e.g., "1 min 2 s 300 ms").
     */
    public static String loadTime(long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
        long milliseconds = elapsedTime % 1000;

        if (minutes > 0) {
            return String.format("%d min %d s %d ms.", minutes, seconds, milliseconds);
        } else if (seconds > 0) {
            return String.format("%d s %d ms.", seconds, milliseconds);
        } else {
            return String.format("%d ms.", elapsedTime);
        }
    }

    /**
     * Copies a file from the source path to the destination path with specified options.
     *
     * @param origin      The source file path as a string.
     * @param destination The destination file path as a string.
     * @param options     Optional copy options (e.g., StandardCopyOption.REPLACE_EXISTING).
     * @throws IOException If an I/O error occurs during the copy operation.
     */
    public static void copyFile(String origin, String destination, CopyOption... options) throws IOException {
        Path from = Paths.get(origin);
        Path to = Paths.get(destination);
        File sourceFile = from.toFile();
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + origin);
        }
        if (!sourceFile.canRead()) {
            throw new IOException("Source file is not readable: " + origin);
        }
        Files.copy(from, to, options);
    }

    /**
     * Copies a file with default options (REPLACE_EXISTING, COPY_ATTRIBUTES).
     *
     * @param origin      The source file path as a string.
     * @param destination The destination file path as a string.
     * @throws IOException If an I/O error occurs during the copy operation.
     */
    public static void copyFile(String origin, String destination) throws IOException {
        copyFile(origin, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
}