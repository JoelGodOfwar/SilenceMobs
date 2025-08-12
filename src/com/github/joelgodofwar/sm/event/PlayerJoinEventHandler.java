package com.github.joelgodofwar.sm.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.joelgodofwar.sm.SilenceMobs;
import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;
import com.github.joelgodofwar.sm.util.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerJoinEventHandler implements Listener {
    private final SilenceMobs plugin;
    private final DetailedErrorReporter reporter;
    private final String downloadLink;
    private final String newVersion;
    private final String oldVersion;
    private final boolean updateAvailable;
    private final Set<UUID> warnedPlayers = new HashSet<>();

    public PlayerJoinEventHandler(SilenceMobs plugin, DetailedErrorReporter reporter, String downloadLink,
                                  String newVersion, String oldVersion, boolean updateAvailable) {
        this.plugin = plugin;
        this.reporter = reporter;
        this.downloadLink = downloadLink;
        this.newVersion = newVersion;
        this.oldVersion = oldVersion;
        this.updateAvailable = updateAvailable;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            if (updateAvailable && (player.isOp() || player.hasPermission("silencemobs.showUpdateAvailable"))) {
                String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
                links = links.replace("<DownloadLink>", downloadLink)
                        .replace("<Download>", plugin.get("sm.version.download"))
                        .replace("<Donate>", plugin.get("sm.version.donate"))
                        .replace("<please_update>", plugin.get("sm.version.please_update"))
                        .replace("<Donate_msg>", plugin.get("sm.version.donate.message"))
                        .replace("<Notes>", plugin.get("sm.version.notes"))
                        .replace("<Notes_msg>", plugin.get("sm.version.notes.message"));
                String versions = ChatColor.GRAY + plugin.get("sm.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + plugin.get("sm.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
                player.sendMessage(ChatColor.GRAY + plugin.get("sm.version.message").replace("<MyPlugin>", ChatColor.GOLD + SilenceMobs.THIS_NAME + ChatColor.GRAY));
                Utils.sendJson(player, links);
                player.sendMessage(versions.replace("{nVers}", newVersion).replace("{oVers}", oldVersion));
            }
            if (player.getDisplayName().equals("JoelYahwehOfWar") || player.getDisplayName().equals("JoelGodOfWar")) {
                player.sendMessage(SilenceMobs.THIS_NAME + " " + SilenceMobs.THIS_VERSION + " Hello father!");
            }
        } catch (Exception exception) {
            reporter.reportDetailed(plugin, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
        }

        long daysRemaining = plugin.buildValidator.getDaysRemaining();
        try {
            if (player.isOp()) {
                UUID playerUUID = player.getUniqueId();
                // Only warn if player hasn't been warned this session
                if (!warnedPlayers.contains(playerUUID)) {
                    if (!plugin.buildValidator.isBuildValid()) {
                        player.sendMessage("§c[" + SilenceMobs.THIS_NAME + "] Dev-build has expired!");
                    } else if (daysRemaining <= 7) {
                        player.sendMessage("§e[" + SilenceMobs.THIS_NAME + "] Dev-build expires in " + daysRemaining + " day(s)!");
                    } else if (daysRemaining == 30 || daysRemaining == 14) {
                        player.sendMessage("§a[" + SilenceMobs.THIS_NAME + "] Dev-build valid, expires in " + daysRemaining + " days");
                    }
                    warnedPlayers.add(playerUUID);
                }
            }
        } catch (Exception e) {
            reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_PLAYER_JOIN_ERROR).error(e));
        }
    }
}