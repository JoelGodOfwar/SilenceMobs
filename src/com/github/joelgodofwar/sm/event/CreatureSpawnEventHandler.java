package com.github.joelgodofwar.sm.event;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.github.joelgodofwar.sm.SilenceMobs;
import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.PluginLogger;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;

public class CreatureSpawnEventHandler implements Listener {
    private final SilenceMobs plugin;
    private final PluginLogger logger;
    private final DetailedErrorReporter reporter;
    private final ArrayList<Entity> dragons;

    public CreatureSpawnEventHandler(SilenceMobs plugin, PluginLogger logger, DetailedErrorReporter reporter, ArrayList<Entity> dragons) {
        this.plugin = plugin;
        this.logger = logger;
        this.reporter = reporter;
        this.dragons = dragons;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        try {
            LivingEntity entity = event.getEntity();
            SpawnReason reason = event.getSpawnReason();
            try {
                if (reason.equals(SpawnReason.INFECTION) || reason.equals(SpawnReason.CURED)) {
                    String name = entity.getCustomName();
                    if (name != null) {
                        if (name.equalsIgnoreCase("silenced")) {
                            entity.setSilent(true);
                            logger.debug(entity.getType().toString() + " has been ReSilenced");
                        }
                    }
                }
            } catch (Exception exception) {
                reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_CSE_VILLAGER).error(exception));
            }
            try {
                if ((entity instanceof EnderDragon) && reason.equals(SpawnReason.CUSTOM)) {
                    Location loc = entity.getLocation();
                    int X = loc.getBlockX();
                    int Y = loc.getBlockY();
                    int Z = loc.getBlockZ();
                    int i = 0;
                    for (Entity dragon : dragons) {
                        Location loc2 = dragon.getLocation();
                        int X2 = loc2.getBlockX();
                        int Y2 = loc2.getBlockY();
                        int Z2 = loc2.getBlockZ();
                        if ((X == X2) && (Y == Y2) && (Z == Z2)) {
                            dragons.remove(i);
                            entity.setSilent(dragon.isSilent());
                            logger.debug(entity.getType().toString() + " has been ReSilenced");
                        }
                        i++;
                    }
                }
            } catch (Exception exception) {
                reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_CSE_DRAGON).error(exception));
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_CREATURE_SPAWN_EVENT).error(exception));
        }
    }
}