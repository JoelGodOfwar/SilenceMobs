package com.github.joelgodofwar.sm.event;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import com.github.joelgodofwar.sm.SilenceMobs;
import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;

public class ChunkLoadEventHandler implements Listener {
    private final SilenceMobs plugin;
    private final DetailedErrorReporter reporter;
    private final NamespacedKey silenceKey;

    public ChunkLoadEventHandler(SilenceMobs plugin, DetailedErrorReporter reporter, NamespacedKey silenceKey) {
        this.plugin = plugin;
        this.reporter = reporter;
        this.silenceKey = silenceKey;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        try {
            Entity[] entities = event.getChunk().getEntities();
            for (Entity entity : entities) {
                if (entity instanceof EnderDragon) {
                    String shouldBeSilent = entity.getPersistentDataContainer().get(silenceKey, PersistentDataType.STRING);
                    if ((shouldBeSilent != null) && shouldBeSilent.equalsIgnoreCase("Silenced") && !entity.isSilent()) {
                        entity.setSilent(true);
                    } else {
                        return;
                    }
                }
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.ERROR_CHUNK_UNLOAD_EVENT).error(exception));
        }
    }
}