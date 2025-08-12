package com.github.joelgodofwar.sm.event;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import com.github.joelgodofwar.sm.SilenceMobs;
import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.PluginLogger;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;

public class EntityDeathEventHandler implements Listener {
    private final SilenceMobs plugin;
    private final PluginLogger logger;
    private final DetailedErrorReporter reporter;
    private final NamespacedKey silenceKey;
    private final NamespacedKey silentDragonKey;

    public EntityDeathEventHandler(SilenceMobs plugin, PluginLogger logger, DetailedErrorReporter reporter,
                                   NamespacedKey silenceKey, NamespacedKey silentDragonKey) {
        this.plugin = plugin;
        this.logger = logger;
        this.reporter = reporter;
        this.silenceKey = silenceKey;
        this.silentDragonKey = silentDragonKey;
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        try {
            if (entity instanceof EnderDragon) {
                EnderDragon ed = (EnderDragon) entity;
                ed.getPersistentDataContainer().set(silentDragonKey, PersistentDataType.STRING, Boolean.toString(ed.isSilent()));
                logger.debug("EDE died");
            }
            if (event.getEntity().getPersistentDataContainer().has(silenceKey, PersistentDataType.STRING)) {
                event.getEntity().getPersistentDataContainer().remove(silenceKey);
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_ENTITY_DEATH_EVENT).error(exception));
        }
    }
}