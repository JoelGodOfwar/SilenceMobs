package com.github.joelgodofwar.sm.command;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.joelgodofwar.sm.SilenceMobs;
import com.github.joelgodofwar.sm.common.PluginLogger;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;
import com.github.joelgodofwar.sm.common.error.ReportType;
import com.github.joelgodofwar.sm.util.YmlConfiguration;
import com.github.joelgodofwar.sm.util.gui.Language;
import com.github.joelgodofwar.sm.util.heads.HeadUtils;
import com.github.joelgodofwar.sm.util.gui.InventoryGUI;

public class ConfigGuiCommand {
    private final SilenceMobs plugin;
    private final YmlConfiguration config;
    private final File configFile;
    private static final NamespacedKey MENU_KEY = new NamespacedKey(SilenceMobs.getInstance(), "menu_action");
    private final PluginLogger logger;
    private final DetailedErrorReporter reporter;
    public static final ReportType COMMAND_CONFIG_EXECUTE = new ReportType("Error executing Config Command.");
    public static final ReportType COMMAND_CONFIG_CONFIGMAIN = new ReportType("Error processing configMain.");
    public static final ReportType COMMAND_CONFIG_CONFIGLANGUAGE = new ReportType("Error processing configLanguage.");

    public ConfigGuiCommand(SilenceMobs plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.config = plugin.config;
        this.logger = plugin.LOGGER;
        this.reporter = SilenceMobs.reporter;
    }

    public void execute(Player player) {
        try {
            if (!player.hasPermission("silencemobs.config")) {
                player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.noperm"));
                return;
            }
            configMain(player);
        } catch (Exception exception) {
            reporter.reportDetailed(this, Report.newBuilder(COMMAND_CONFIG_EXECUTE).error(exception));
            player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.error"));
        }
    }

    public void configMain(Player player) {
        try {
            logger.debug(String.format("Opening main config menu for player: %s", player.getName()));
            Map<ItemStack, Runnable> choices = new HashMap<>();
            Map<ItemStack, Integer> slotAssignments = new HashMap<>();

            // Auto Update Check
            ItemStack autoUpdate = new ItemStack(config.getBoolean("auto_update_check") ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
            ItemMeta auMeta = autoUpdate.getItemMeta();
            if (auMeta != null) {
                auMeta.setDisplayName(ChatColor.YELLOW + plugin.get("sm.config.auto_update_check"));
                auMeta.setLore(Arrays.asList(
                        ChatColor.WHITE + plugin.get("sm.config.current") + ": " + (config.getBoolean("auto_update_check") ? ChatColor.GREEN + plugin.get("sm.config.enabled") : ChatColor.RED + plugin.get("sm.config.disabled")),
                        ChatColor.WHITE + plugin.get("sm.config.click_to_toggle"),
                        ChatColor.GRAY + plugin.get("sm.config.auto_update_desc")));
                auMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "auto_update_check");
                autoUpdate.setItemMeta(auMeta);
            }
            choices.put(autoUpdate, () -> {
                boolean current = config.getBoolean("auto_update_check");
                config.set("auto_update_check", !current);
                try {
                    YmlConfiguration.saveConfig(configFile, config);
                    logger.debug(String.format("Toggled auto_update_check to %s for player: %s", !current, player.getName()));
                    player.sendMessage(ChatColor.GREEN + plugin.get("sm.config.auto_update_check") + " " + plugin.get("sm.config.set_to") + " " + (!current ? plugin.get("sm.config.enabled") : plugin.get("sm.config.disabled")));
                } catch (Exception e) {
                    logger.debug(String.format("Failed to save config for auto_update_check: %s", e.getMessage()));
                    player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.config.save_failed"));
                }
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        configMain(player);
                    }
                }.runTaskLater(plugin, 1L);
            });
            slotAssignments.put(autoUpdate, 0);

            // Debug Mode
            ItemStack debugMode = new ItemStack(config.getBoolean("debug") ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
            ItemMeta dmMeta = debugMode.getItemMeta();
            if (dmMeta != null) {
                dmMeta.setDisplayName(ChatColor.YELLOW + plugin.get("sm.config.debug"));
                dmMeta.setLore(Arrays.asList(
                        ChatColor.WHITE + plugin.get("sm.config.current") + ": " + (config.getBoolean("debug") ? ChatColor.GREEN + plugin.get("sm.config.enabled") : ChatColor.RED + plugin.get("sm.config.disabled")),
                        ChatColor.WHITE + plugin.get("sm.config.click_to_toggle"),
                        ChatColor.GRAY + plugin.get("sm.config.debug_desc")));
                dmMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "debug");
                debugMode.setItemMeta(dmMeta);
            }
            choices.put(debugMode, () -> {
                boolean current = config.getBoolean("debug");
                config.set("debug", !current);
                try {
                    YmlConfiguration.saveConfig(configFile, config);
                    logger.debug(String.format("Toggled debug to %s for player: %s", !current, player.getName()));
                    player.sendMessage(ChatColor.GREEN + plugin.get("sm.config.debug") + " " + plugin.get("sm.config.set_to") + " " + (!current ? plugin.get("sm.config.enabled") : plugin.get("sm.config.disabled")));
                } catch (Exception e) {
                    logger.debug(String.format("Failed to save config for debug: %s", e.getMessage()));
                    player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.config.save_failed"));
                }
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        configMain(player);
                    }
                }.runTaskLater(plugin, 1L);
            });
            slotAssignments.put(debugMode, 1);

            // Language
            ItemStack language = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta langMeta = (SkullMeta) language.getItemMeta();
            if (langMeta != null) {
                String langCode = config.getString("lang", "en_US");
                Language lang = Language.getByLangCode(langCode);
                String langName = lang != null ? lang.getLangNameInEnglish() : langCode;
                langMeta.setDisplayName(ChatColor.YELLOW + langName);
                langMeta.setLore(Arrays.asList(
                        ChatColor.WHITE + plugin.get("sm.config.current") + ": " + ChatColor.WHITE + langName,
                        ChatColor.WHITE + plugin.get("sm.config.click_to_select_language"),
                        ChatColor.GRAY + "čeština (cs_CZ), Deutsch (de_DE), English (en_US),",
                        ChatColor.GRAY + "Español (es_ES), Español (es_MX), Français (fr_FR),",
                        ChatColor.GRAY + "Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR),",
                        ChatColor.GRAY + "Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL),",
                        ChatColor.GRAY + "Polski (pl_PL), Português (pt_BR), Русский (ru_RU),",
                        ChatColor.GRAY + "Svenska (sv_SE), Türkçe (tr_TR), 中文(简体) (zh_CN),",
                        ChatColor.GRAY + "中文(繁體) (zh_TW)"));
                if (lang != null) {
                    String texture = lang.getTexture();
                    if (texture != null) {
                        language = HeadUtils.makeHead(ChatColor.YELLOW + langName, texture, lang.getUuid(),
                                new ArrayList<>(Arrays.asList(
                                        ChatColor.WHITE + plugin.get("sm.config.current") + ": " + ChatColor.WHITE + langName,
                                        ChatColor.WHITE + plugin.get("sm.config.click_to_select_language"))),
                                null);
                    }
                }
                langMeta = (SkullMeta) language.getItemMeta();
                assert langMeta != null;
                langMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "language");
                language.setItemMeta(langMeta);
            }
            choices.put(language, () -> {
                logger.debug(String.format("Language clicked by player: %s", player.getName()));
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        configLanguage(player);
                    }
                }.runTaskLater(plugin, 1L);
            });
            slotAssignments.put(language, 2);

            // Colorful Console
            ItemStack colorfulConsole = new ItemStack(config.getBoolean("console.colorful_console") ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
            ItemMeta ccMeta = colorfulConsole.getItemMeta();
            if (ccMeta != null) {
                ccMeta.setDisplayName(ChatColor.YELLOW + plugin.get("sm.config.colorful_console"));
                ccMeta.setLore(Arrays.asList(
                        ChatColor.WHITE + plugin.get("sm.config.current") + ": " + (config.getBoolean("console.colorful_console") ? ChatColor.GREEN + plugin.get("sm.config.enabled") : ChatColor.RED + plugin.get("sm.config.disabled")),
                        ChatColor.WHITE + plugin.get("sm.config.click_to_toggle"),
                        ChatColor.GRAY + plugin.get("sm.config.colorful_console_desc")));
                ccMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "colorful_console");
                colorfulConsole.setItemMeta(ccMeta);
            }
            choices.put(colorfulConsole, () -> {
                boolean current = config.getBoolean("console.colorful_console");
                config.set("console.colorful_console", !current);
                try {
                    YmlConfiguration.saveConfig(configFile, config);
                    logger.debug(String.format("Toggled colorful_console to %s for player: %s", !current, player.getName()));
                    player.sendMessage(ChatColor.GREEN + plugin.get("sm.config.colorful_console") + " " + plugin.get("sm.config.set_to") + " " + (!current ? plugin.get("sm.config.enabled") : plugin.get("sm.config.disabled")));
                } catch (Exception e) {
                    logger.debug(String.format("Failed to save config for colorful_console: %s", e.getMessage()));
                    player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.config.save_failed"));
                }
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        configMain(player);
                    }
                }.runTaskLater(plugin, 1L);
            });
            slotAssignments.put(colorfulConsole, 3);

            // Long Plugin Name
            ItemStack longPluginName = new ItemStack(config.getBoolean("console.longpluginname") ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
            ItemMeta lpnMeta = longPluginName.getItemMeta();
            if (lpnMeta != null) {
                lpnMeta.setDisplayName(ChatColor.YELLOW + plugin.get("sm.config.longpluginname"));
                lpnMeta.setLore(Arrays.asList(
                        ChatColor.WHITE + plugin.get("sm.config.current") + ": " + (config.getBoolean("console.longpluginname") ? ChatColor.GREEN + plugin.get("sm.config.enabled") : ChatColor.RED + plugin.get("sm.config.disabled")),
                        ChatColor.WHITE + plugin.get("sm.config.click_to_toggle"),
                        ChatColor.GRAY + plugin.get("sm.config.longpluginname_desc")));
                lpnMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "longpluginname");
                longPluginName.setItemMeta(lpnMeta);
            }
            choices.put(longPluginName, () -> {
                boolean current = config.getBoolean("console.longpluginname");
                config.set("console.longpluginname", !current);
                try {
                    YmlConfiguration.saveConfig(configFile, config);
                    logger.debug(String.format("Toggled longpluginname to %s for player: %s", !current, player.getName()));
                    player.sendMessage(ChatColor.GREEN + plugin.get("sm.config.longpluginname") + " " + plugin.get("sm.config.set_to") + " " + (!current ? plugin.get("sm.config.enabled") : plugin.get("sm.config.disabled")));
                } catch (Exception e) {
                    logger.debug(String.format("Failed to save config for longpluginname: %s", e.getMessage()));
                    player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.config.save_failed"));
                }
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        configMain(player);
                    }
                }.runTaskLater(plugin, 1L);
            });
            slotAssignments.put(longPluginName, 4);

            // Close Button
            ItemStack closeButton = new ItemStack(Material.BARRIER);
            ItemMeta closeMeta = closeButton.getItemMeta();
            if (closeMeta != null) {
                closeMeta.setDisplayName(ChatColor.RED + plugin.get("sm.config.close"));
                closeMeta.setLore(Arrays.asList(ChatColor.GRAY + plugin.get("sm.config.close_desc")));
                closeMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "close");
                closeButton.setItemMeta(closeMeta);
            }
            choices.put(closeButton, () -> {
                logger.debug(String.format("Close clicked by player: %s", player.getName()));
                player.closeInventory();
            });
            slotAssignments.put(closeButton, 49);

            InventoryGUI gui = new InventoryGUI("SilenceMobs Config", choices);
            gui.setForcePreviousButton(false);
            gui.openWithSlots(player, slotAssignments, null);
            logger.debug(String.format("Main config menu opened for player: %s, items: %d", player.getName(), choices.size()));
        } catch (Exception exception) {
            reporter.reportDetailed(this, Report.newBuilder(COMMAND_CONFIG_CONFIGMAIN).error(exception));
            player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.error"));
        }
    }

    private void configLanguage(Player player) {
        try {
            logger.debug(String.format("Opening language menu for player: %s", player.getName()));
            Map<ItemStack, Runnable> choices = new HashMap<>();
            Map<ItemStack, Integer> slotAssignments = new HashMap<>();

            // Language Options with Skinned Heads
            int slot = 0;
            for (Language lang : Language.values()) {
                ItemStack langItem = HeadUtils.makeHead(ChatColor.YELLOW + lang.getLangNameInEnglish(), lang.getTexture(), lang.getUuid(),
                        new ArrayList<>(Arrays.asList(
                                ChatColor.WHITE + plugin.get("sm.config.code") + ": " + lang.getLangCode(),
                                ChatColor.WHITE + plugin.get("sm.config.click_to_select"))),
                        null);
                SkullMeta lmMeta = (SkullMeta) langItem.getItemMeta();
                if (lmMeta != null) {
                    lmMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "language_" + lang.getLangCode());
                    langItem.setItemMeta(lmMeta);
                }
                final String langCode = lang.getLangCode();
                choices.put(langItem, () -> {
                    config.set("lang", langCode);
                    try {
                        YmlConfiguration.saveConfig(configFile, config);
                        logger.debug(String.format("Language set to %s for player: %s", langCode, player.getName()));
                        player.sendMessage(ChatColor.GREEN + plugin.get("sm.config.language_set_to") + " " + lang.getLangNameInEnglish());
                    } catch (Exception e) {
                        logger.debug(String.format("Failed to save language: %s", e.getMessage()));
                        player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.config.save_failed"));
                    }
                    player.closeInventory();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            configMain(player);
                        }
                    }.runTaskLater(plugin, 1L);
                });
                slotAssignments.put(langItem, slot++);
                if (slot >= 45) {
                    break; // Limit to 45 slots for a single inventory page
                }
            }

            // Previous Menu
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.YELLOW + plugin.get("sm.config.previous_menu"));
                prevMeta.setLore(Arrays.asList(ChatColor.GRAY + plugin.get("sm.config.previous_menu_desc")));
                prevMeta.getPersistentDataContainer().set(MENU_KEY, PersistentDataType.STRING, "previous_menu");
                prevButton.setItemMeta(prevMeta);
            }
            choices.put(prevButton, () -> {
                logger.debug(String.format("Previous Menu clicked by player: %s", player.getName()));
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        configMain(player);
                    }
                }.runTaskLater(plugin, 1L);
            });
            slotAssignments.put(prevButton, 45);

            InventoryGUI gui = new InventoryGUI("Select Language", choices);
            gui.setForcePreviousButton(false);
            gui.openWithSlots(player, slotAssignments, null);
            logger.debug(String.format("Language menu opened for player: %s, items: %d", player.getName(), choices.size()));
        } catch (Exception exception) {
            reporter.reportDetailed(this, Report.newBuilder(COMMAND_CONFIG_CONFIGLANGUAGE).error(exception));
            player.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.error"));
        }
    }
}