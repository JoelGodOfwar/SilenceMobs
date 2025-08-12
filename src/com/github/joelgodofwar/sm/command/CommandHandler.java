package com.github.joelgodofwar.sm.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import com.github.joelgodofwar.sm.SilenceMobs;
import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.PluginLogger;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final SilenceMobs plugin;
    private final PluginLogger logger;
    private final DetailedErrorReporter reporter;
    private final NamespacedKey silenceKey;
    private final ConfigGuiCommand configGui;

    public CommandHandler(SilenceMobs plugin, PluginLogger logger, DetailedErrorReporter reporter, NamespacedKey silenceKey) {
        this.plugin = plugin;
        this.logger = logger;
        this.reporter = reporter;
        this.silenceKey = silenceKey;
        this.configGui = new ConfigGuiCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equalsIgnoreCase("SM") || command.getName().equalsIgnoreCase("silencemobs")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SilenceMobs" + ChatColor.GREEN + "]===============[]");
                    sender.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + " " + plugin.get("sm.command.perms"));
                    if (sender.isOp() || sender.hasPermission("silencemobs.reload") || !(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.WHITE + " /sm reload - " + plugin.get("sm.command.reload"));
                    }
                    if (sender.isOp() || sender.hasPermission("silencemobs.toggledebug") || !(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.WHITE + " /sm toggledebug - " + plugin.get("sm.command.debuguse"));
                    }
                    sender.sendMessage(ChatColor.WHITE + " /sm dragon - " + plugin.get("sm.command.dragon"));
                    sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SilenceMobs" + ChatColor.GREEN + "]===============[]");
                    return true;
                }

                if (args[0].equalsIgnoreCase("config")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + plugin.get("sm.message.player_only"));
                        return false;
                    }
                    if (sender.hasPermission("silencemobs.config")) {
                        configGui.execute((Player) sender);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                        return false;
                    }
                }

                if (args[0].equalsIgnoreCase("check")) {
                    if (!(sender instanceof Player)) {
                        return false;
                    }
                    Player player = (Player) sender;
                    Location playerLocation = player.getLocation();

                    double radius = 10;
                    int unsilencedCount = 0;

                    for (Entity nearbyEntity : player.getWorld().getNearbyEntities(playerLocation, radius, radius, radius)) {
                        if (nearbyEntity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity) nearbyEntity;
                            String silencedStatus = livingEntity.getPersistentDataContainer().get(silenceKey, PersistentDataType.STRING);

                            if ("Silenced".equals(silencedStatus)) {
                                if (!livingEntity.isSilent()) {
                                    unsilencedCount++;
                                    livingEntity.setSilent(true);
                                }
                            } else if ("Silenced".equalsIgnoreCase(livingEntity.getCustomName())) {
                                livingEntity.getPersistentDataContainer().set(silenceKey, PersistentDataType.STRING, "Silenced");
                                livingEntity.setSilent(true);
                                unsilencedCount++;
                            }
                        }
                    }

                    if (unsilencedCount > 0) {
                        sender.sendMessage("Found and silenced " + unsilencedCount + " unsilenced entities with the 'Silenced' identifier.");
                    } else {
                        sender.sendMessage("No unsilenced entities with the 'Silenced' identifier found.");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("dragon")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        List<Entity> entities = player.getNearbyEntities(16, 16, 16);

                        for (Entity entity : entities) {
                            if (entity instanceof EnderDragon) {
                                logger.log("name=" + entity.getCustomName());
                                Material material = player.getInventory().getItemInMainHand().getType();
                                Material material2 = player.getInventory().getItemInOffHand().getType();
                                String name = null;
                                String hand = null;
                                if (material.equals(Material.NAME_TAG)) {
                                    name = Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getDisplayName();
                                    hand = "Main";
                                    logger.debug(player.getDisplayName() + " Main hand name=" + name);
                                }
                                if (material2.equals(Material.NAME_TAG)) {
                                    name = Objects.requireNonNull(player.getInventory().getItemInOffHand().getItemMeta()).getDisplayName();
                                    hand = "Off";
                                    logger.debug(player.getDisplayName() + " Off hand name=" + name);
                                }
                                if (name != null) {
                                    logger.debug("name!=null");
                                    if (name.equalsIgnoreCase("silence me") || name.equalsIgnoreCase("silenceme")) {
                                        try {
                                            logger.debug("name=" + name);
                                            String entityname = entity.getCustomName();
                                            entity.setSilent(true);
                                            entity.setCustomName("Silenced");
                                            entity.getScoreboardTags().add("Silenced");
                                            switch (hand) {
                                                case "Main":
                                                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                                                    logger.log(player.getDisplayName() + " has silenced a " + entityname);
                                                    break;
                                                case "Off":
                                                    player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
                                                    logger.log(player.getDisplayName() + " has silenced a " + entityname);
                                                    break;
                                            }
                                            logger.debug("done");
                                            return true;
                                        } catch (Exception exception) {
                                            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_COMMAND_DRAGON).error(exception));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.isOp() || sender.hasPermission("silencemobs.reload") || !(sender instanceof Player)) {
                        // Updated reload logic (see below for details)
                        plugin.reload();
                        sender.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.reloaded"));
                        return true;
                    } else if (!sender.hasPermission("silencemobs.reload")) {
                        sender.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.noperm"));
                        return false;
                    }
                }
                if (args[0].equalsIgnoreCase("toggledebug") || args[0].equalsIgnoreCase("td")) {
                    if (sender.isOp() || sender.hasPermission("silencemobs.toggledebug") || !(sender instanceof Player)) {
                        plugin.setDebug(!plugin.getDebug());
                        sender.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.debugtrue").replace("<boolean>", plugin.get("sm.message.boolean." + String.valueOf(plugin.getDebug()).toLowerCase())));
                        return true;
                    } else if (!sender.hasPermission("silencemobs.toggledebug")) {
                        sender.sendMessage(ChatColor.YELLOW + SilenceMobs.THIS_NAME + ChatColor.RED + " " + plugin.get("sm.message.noperm"));
                        return false;
                    }
                }
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_PARSING_COMMAND).error(exception));
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            if (command.getName().equalsIgnoreCase("sm")) {
                List<String> autoCompletes = new ArrayList<>();
                if (args.length == 1) {
                    if (sender.isOp() || sender.hasPermission("silencemobs.reload") || !(sender instanceof Player)) {
                        autoCompletes.add("reload");
                    }
                    if (sender.isOp() || sender.hasPermission("silencemobs.toggledebug") || !(sender instanceof Player)) {
                        autoCompletes.add("toggledebug");
                    }
                    if (sender.isOp() || sender.hasPermission("silencemobs.config") || !(sender instanceof Player)) {
                        autoCompletes.add("config");
                    }
                    autoCompletes.add("dragon");
                    return filterCompletions(autoCompletes, args[0]);
                }
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_TAB_COMPLETE_ERROR).error(exception));
        }
        return null;
    }
    /**
     * Filters tab completion suggestions based on the user's input.
     * <p>
     * Returns a list of completions that start with the provided argument (case-insensitive).
     * If the input is null or empty, returns all completions.
     *
     * @param completions The list of possible completions.
     * @param arg         The user's current input to filter against.
     * @return A filtered list of completions matching the input.
     */
    private List<String> filterCompletions(List<String> completions, String arg) {
        if ((arg == null) || arg.isEmpty()) {
            return completions;
        }
        return completions.stream()
                .filter(c -> c.toLowerCase().startsWith(arg.toLowerCase()))
                .collect(Collectors.toList());
    }
}