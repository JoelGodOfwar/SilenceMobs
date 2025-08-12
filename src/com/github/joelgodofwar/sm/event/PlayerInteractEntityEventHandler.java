package com.github.joelgodofwar.sm.event;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import com.github.joelgodofwar.sm.SilenceMobs;
import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.PluginLogger;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;

import java.util.Objects;

public class PlayerInteractEntityEventHandler implements Listener {
    private final SilenceMobs plugin;
    private final PluginLogger logger;
    private final DetailedErrorReporter reporter;
    private final NamespacedKey silenceKey;

    public PlayerInteractEntityEventHandler(SilenceMobs plugin, PluginLogger logger, DetailedErrorReporter reporter, NamespacedKey silenceKey) {
        this.plugin = plugin;
        this.logger = logger;
        this.reporter = reporter;
        this.silenceKey = silenceKey;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEntityEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.hasPermission("silencemobs.use")) {
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
                            LivingEntity entity = (LivingEntity) event.getRightClicked();
                            String entityname = event.getRightClicked().toString().replace(" ", "_").replace("Craft", "");
                            entity.setSilent(true);
                            entity.setCustomName("Silenced");
                            entity.getPersistentDataContainer().set(silenceKey, PersistentDataType.STRING, "Silenced");
                            event.setCancelled(true);
                            switch (hand) {
                                case "Main":
                                    if (player.getGameMode() != GameMode.CREATIVE) {
                                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                                    }
                                    logger.log(player.getDisplayName() + " has silenced a " + entityname);
                                    break;
                                case "Off":
                                    if (player.getGameMode() != GameMode.CREATIVE) {
                                        player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
                                    }
                                    logger.log(player.getDisplayName() + " has silenced a " + entityname);
                                    break;
                            }
                            logger.debug("done");
                            return;
                        } catch (Exception exception) {
                            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_SILENCE_ENTITY_FAILED).error(exception));
                        }
                    }
                }
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_PLAYER_INTERACTENTITY_EVENT).error(exception));
        }
    }
}