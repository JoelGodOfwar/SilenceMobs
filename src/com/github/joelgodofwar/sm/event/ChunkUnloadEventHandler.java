package com.github.joelgodofwar.sm.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.github.joelgodofwar.sm.SilenceMobs;

public class ChunkUnloadEventHandler implements Listener {
    private final SilenceMobs plugin;

    public ChunkUnloadEventHandler(SilenceMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Currently empty, but keeping the structure for future use
    }
}