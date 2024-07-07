package com.github.joelgodofwar.sm;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.PluginLogger;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;
import com.github.joelgodofwar.sm.i18n.Translator;
import com.github.joelgodofwar.sm.util.Metrics;
import com.github.joelgodofwar.sm.util.Utils;
import com.github.joelgodofwar.sm.util.Version;
import com.github.joelgodofwar.sm.util.VersionChecker;
import com.github.joelgodofwar.sm.util.YmlConfiguration;

public class SilenceMobs  extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	//public final static Logger logger = Logger.getLogger("Minecraft");
	static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 75749; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/SilenceMobs/raw/master/versioncheck/1.10/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String DownloadLink = "https://www.spigotmc.org/resources/silence-mobs.75749";
	/** end update checker variables */
	public boolean debug;
	public static String daLang;
	File langFile;
	FileConfiguration lang;
	Translator lang2;
	YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	boolean colorful_console;
	String pluginName = THIS_NAME;
	public Map<String, String> map = new HashMap<String, String>();
	//private Set<String> triggeredPlayers = new HashSet<>();
	private final NamespacedKey silenceKey = new NamespacedKey(this, "silenced");
	public String jarfilename = this.getFile().getAbsoluteFile().toString();
	public static DetailedErrorReporter reporter;
	public PluginLogger LOGGER;

	@SuppressWarnings("unused")
	@Override // TODO: onEnable
	public void onEnable(){
		long startTime = System.currentTimeMillis();
		LOGGER = new PluginLogger(this);
		reporter = new DetailedErrorReporter(this);
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		config = new YmlConfiguration();
		oldconfig = new YamlConfiguration();
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		colorful_console = getConfig().getBoolean("colorful_console", true);
		lang2 = new Translator(daLang, getDataFolder().toString());
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		if(!getConfig().getBoolean("console.longpluginname", true)) {
			pluginName = "SM";
		}else {
			pluginName = THIS_NAME;
		}


		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + " v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		LOGGER.log("Server Version: " + getServer().getVersion().toString());

		Version checkVersion = this.verifyMinecraftVersion();

		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			LOGGER.debug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}

		Version current = new Version(this.getServer());
		if (!current.atOrAbove(new Version("1.12"))){
			//if(!getVersion().contains("1.9")&&!getVersion().contains("1.10")&&!getVersion().contains("1.11")){
			LOGGER.warn(ChatColor.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + ChatColor.RESET);
			LOGGER.warn(ChatColor.YELLOW + get("sm.message.server_not_version") + ChatColor.RESET); //, "Server is NOT version 1.13.*+"
			LOGGER.warn(ChatColor.YELLOW + THIS_NAME + " v" + THIS_VERSION + " disabling." + ChatColor.RESET);
			LOGGER.warn(ChatColor.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + ChatColor.RESET);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				LOGGER.log("Data Folder doesn't exist");
				LOGGER.log("Creating Data Folder");
				getDataFolder().mkdirs();
				LOGGER.log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			LOGGER.log("" + file);
			if(!file.exists()){
				LOGGER.log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
		}
		/** end config check */
		/** Check if config.yml is up to date.*/
		boolean needConfigUpdate = false;
		String oldConfig = new File(getDataFolder(), "config.yml").getPath().toString();
		try {
			oldconfig.load(new File(getDataFolder() + "" + File.separatorChar + "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase("1.0.0")){
				needConfigUpdate = true;
			}
		}
		if(needConfigUpdate){
			try {
				copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml",getDataFolder() + "" + File.separatorChar + "old_config.yml");
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_COPY_FILE).error(exception));
			}
			try {
				oldconfig.load(new File(getDataFolder(), "config.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
			}
			saveResource("config.yml", true);
			try {
				config.load(new File(getDataFolder(), "config.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
			}
			try {
				oldconfig.load(new File(getDataFolder(), "old_config.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
			}
			config.set("auto_update_check", oldconfig.get("auto_update_check", true));
			config.set("debug", oldconfig.get("debug", false));
			config.set("lang", oldconfig.get("lang", "en_US"));
			config.set("colorful_console", oldconfig.get("colorful_console", true));
			try {
				config.save(new File(getDataFolder(), "config.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
			}
			LOGGER.log("config.yml has been updated");
		}
		/** End Config update check */

		/** Update Checker */
		if(UpdateCheck){
			try {
				LOGGER.log("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sm.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					LOGGER.log("* " + get("sm.version.old_vers") + ChatColor.RED + UColdVers );
					LOGGER.log("* " + get("sm.version.new_vers") + ChatColor.GREEN + UCnewVers );
					LOGGER.log("*");
					LOGGER.log("* " + get("sm.version.please_update") );
					LOGGER.log("*");
					LOGGER.log("* " + get("sm.version.download") + ": " + DownloadLink + "/history");
					LOGGER.log("* " + get("sm.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					/** Up to date */
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sm.version.curvers"));
					LOGGER.log("* " + get("sm.version.donate") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
			}
		}else {
			/** auto_update_check is false so nag. */
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			LOGGER.log("* " + get("sm.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		/** end update checker */

		getServer().getPluginManager().registerEvents(this, this);

		consoleInfo("Enabled - Loading took " + LoadTime(startTime));

		try {
			Metrics metrics  = new Metrics(this, 6695);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> valueMap = new HashMap<>();

					if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}
					if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}
					if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}
					if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}
					if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}
					//if(getServer().getPluginManager().getPlugin("SilenceMobs") != null){valueMap.put("SilenceMobs", 1);}
					if(getServer().getPluginManager().getPlugin("SinglePlayerSleep") != null){valueMap.put("SinglePlayerSleep", 1);}
					if(getServer().getPluginManager().getPlugin("VillagerWorkstationHighlights") != null){valueMap.put("VillagerWorkstationHighlights", 1);}
					if(getServer().getPluginManager().getPlugin("RotationalWrench") != null){valueMap.put("RotationalWrench", 1);}
					return valueMap;
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("auto_update_check", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("auto_update_check").toUpperCase();
				}
			}));
			// add to site
			metrics.addCustomChart(new Metrics.SimplePie("var_debug", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("debug").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_lang", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("lang").toUpperCase();
				}
			}));
		} catch (Exception exception) {
			// Handle the exception or log it
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_METRICS_LOAD_ERROR).error(exception));
		}
	}

	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo("Disabled");
	}

	public void consoleInfo(String state) {
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.YELLOW + " v" + THIS_VERSION + ChatColor.RESET + " is " + state  + ChatColor.RESET);
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
	}

	public static final String OWNER_ID = "ownerofthedragon";
	private final NamespacedKey SILENT_DRAGON = new NamespacedKey(this, "silent_dragon");
	@SuppressWarnings({ "deprecation", "unused" })
	private final NamespacedKey SILENT_DRAGON2 = new NamespacedKey("petdragon", OWNER_ID);

	@Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		try {
			if (cmd.getName().equalsIgnoreCase("SM")||cmd.getName().equalsIgnoreCase("silencemobs")){
				if (args.length == 0){
					sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SilenceMobs" + ChatColor.GREEN + "]===============[]");
					sender.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + " " + get("sm.command.perms"));
					if(sender.isOp()||sender.hasPermission("silencemobs.reload")||!(sender instanceof Player)){
						sender.sendMessage(ChatColor.WHITE + " /sm reload - " + get("sm.command.reload"));//subject to server admin approval");
					}
					if(sender.isOp()||sender.hasPermission("silencemobs.toggledebug")||!(sender instanceof Player)){
						sender.sendMessage(ChatColor.WHITE + " /sm toggledebug - " + get("sm.command.debuguse"));//Cancels SinglePlayerSleep");
					}
					sender.sendMessage(ChatColor.WHITE + " /sm dragon - " + get("sm.command.dragon"));
					sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SilenceMobs" + ChatColor.GREEN + "]===============[]");
					return true;
				}
				if(args[0].equalsIgnoreCase("check")){
					if(!(sender instanceof Player)){
						return false;
					}
					Player player = (Player) sender;
					Location playerLocation = player.getLocation();

					// Retrieve nearby entities
					double radius = 10;
					int unsilencedCount = 0;

					for (Entity nearbyEntity : player.getWorld().getNearbyEntities(playerLocation,radius, radius, radius)) {
						if (nearbyEntity instanceof LivingEntity) {
							LivingEntity livingEntity = (LivingEntity) nearbyEntity;
							String silencedStatus = livingEntity.getPersistentDataContainer().get(silenceKey, PersistentDataType.STRING);

							if ("Silenced".equals(silencedStatus)) {
								if (!livingEntity.isSilent()) {
									unsilencedCount++;
									livingEntity.setSilent(true);
								}
							}else if ("Silenced".equalsIgnoreCase(livingEntity.getCustomName())) {
								// If not silenced via persistent data, but named "Silenced"
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
				if(args[0].equalsIgnoreCase("dragon")){
					if(sender instanceof Player){
						Player player = (Player) sender;
						//World world = player.getWorld();
						//List<Entity> entities = world.getEntities();
						List<Entity> entities = player.getNearbyEntities(16, 16, 16);

						for(Entity entity : entities) { // world.getEntities()
							if(entity instanceof EnderDragon){
								LOGGER.log("name=" + entity.getCustomName());
								Material material = player.getInventory().getItemInMainHand().getType();
								Material material2 = player.getInventory().getItemInOffHand().getType();
								String name = null;
								String hand = null;
								if(material.equals(Material.NAME_TAG)){
									name = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
									hand = "Main";
									LOGGER.debug(player.getDisplayName() + " Main hand name=" + name);
								}
								if(material2.equals(Material.NAME_TAG)){
									name = player.getInventory().getItemInOffHand().getItemMeta().getDisplayName();
									hand = "Off";
									LOGGER.debug(player.getDisplayName() + " Off hand name=" + name);
								}
								if(name != null){
									LOGGER.debug("name!=null");
									if(name.equalsIgnoreCase("silence me")||name.equalsIgnoreCase("silenceme")){
										try{
											LOGGER.debug("name=" + name);
											String entityname = entity.getCustomName();
											entity.setSilent(true);
											entity.setCustomName("Silenced");
											entity.getScoreboardTags().add("Silenced");
											switch (hand){

											case "Main":
												player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
												LOGGER.log("" + player.getDisplayName() +  " has silenced a " + entityname);
												break;
											case "Off":
												player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
												LOGGER.log("" + player.getDisplayName() +  " has silenced a " + entityname);
												break;
											}

											//entity.setCustomNameVisible(true);
											//}
											LOGGER.debug("done");
											return true;
										} catch (Exception exception) {
											reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_COMMAND_DRAGON).error(exception));
										}
									}
								}
							}
						}
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("reload")){
					if(sender.isOp()||sender.hasPermission("silencemobs.reload")||!(sender instanceof Player)){
						//ConfigAPI.Reloadconfig(this, p);
						getServer().getPluginManager().disablePlugin(this);
						getServer().getPluginManager().enablePlugin(this);
						reloadConfig();
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sm.message.reloaded"));
						return true;
					}else if(!sender.hasPermission("silencemobs.reload")){
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sm.message.noperm"));
						return false;
					}
				}
				if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
					if(sender.isOp()||sender.hasPermission("silencemobs.toggledebug")||!(sender instanceof Player)){
						debug = !debug;
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sm.message.debugtrue").toString().replace("<boolean>", get("sm.message.boolean." + String.valueOf(debug).toLowerCase()) ));
						return true;
					}else if(!sender.hasPermission("silencemobs.toggledebug")){
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sm.message.noperm"));
						return false;
					}
				}
			}
		}catch(Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_COMMAND).error(exception));
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { // TODO: Tab Complete
		try {
			if (command.getName().equalsIgnoreCase("sm")) {
				List<String> autoCompletes = new ArrayList<>(); //create a new string list for tab completion
				if (args.length == 1) { // reload, toggledebug, playerheads, customtrader, headfix
					autoCompletes.add("reload");
					autoCompletes.add("toggledebug");
					autoCompletes.add("dragon");
					return autoCompletes; // then return the list
				}
			}
		}catch(Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_TAB_COMPLETE_ERROR).error(exception));
		}
		return null;
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event){ //TODO: onPlayerJoinEvent
		Player player = event.getPlayer();
		try {
			/** Notify Ops */
			if(UpdateAvailable&&(player.isOp()||player.hasPermission("silencemobs.showUpdateAvailable"))){
				String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
				links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("sm.version.download"))
						.replace("<Donate>", get("sm.version.donate")).replace("<please_update>", get("sm.version.please_update"))
						.replace("<Donate_msg>", get("sm.version.donate.message")).replace("<Notes>", get("sm.version.notes"))
						.replace("<Notes_msg>", get("sm.version.notes.message"));
				String versions = "" + ChatColor.GRAY + get("sm.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("sm.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
				player.sendMessage("" + ChatColor.GRAY + get("sm.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
				Utils.sendJson(player, links);
				player.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
				//p.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " v" + UColdVers + ChatColor.RESET + " " + get("newvers") + ChatColor.GREEN + " v" + UCnewVers + ChatColor.RESET + "\n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
			}
		}catch(Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
		}
		if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
			player.sendMessage(THIS_NAME + " " + THIS_VERSION + " Hello father!");
			//p.sendMessage("seed=" + p.getWorld().getSeed());
		}
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEntityEvent event){// TODO:
		try {
			if(!(event.getPlayer() instanceof Player)) {
				return;
			}
			//if(debug){logDebug("passed player checker");};
			Player player = event.getPlayer();
			if(player.hasPermission("silencemobs.use")){
				Material material = player.getInventory().getItemInMainHand().getType();
				Material material2 = player.getInventory().getItemInOffHand().getType();
				String name = null;
				String hand = null;
				if(material.equals(Material.NAME_TAG)){
					name = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
					hand = "Main";
					LOGGER.debug(player.getDisplayName() + " Main hand name=" + name);
				}
				if(material2.equals(Material.NAME_TAG)){
					name = player.getInventory().getItemInOffHand().getItemMeta().getDisplayName();
					hand = "Off";
					LOGGER.debug(player.getDisplayName() + " Off hand name=" + name);
				}
				//LivingEntity mob = (LivingEntity) event.getRightClicked();
				if(name != null){
					LOGGER.debug("name!=null");
					if(name.equalsIgnoreCase("silence me")||name.equalsIgnoreCase("silenceme")){
						try{
							LOGGER.debug("name=" + name);
							LivingEntity entity = (LivingEntity) event.getRightClicked();
							String entityname = event.getRightClicked().toString().replace(" ", "_").replace("Craft", "");
							entity.setSilent(true);
							entity.setCustomName("Silenced");
							entity.getPersistentDataContainer().set(silenceKey, PersistentDataType.STRING, "Silenced");
							event.setCancelled(true);
							switch (hand){

							case "Main":
								if(player.getGameMode() != GameMode.CREATIVE) {
									player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
								}
								LOGGER.log("" + player.getDisplayName() +  " has silenced a " + entityname);
								break;
							case "Off":
								if(player.getGameMode() != GameMode.CREATIVE) {
									player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
								}
								LOGGER.log("" + player.getDisplayName() +  " has silenced a " + entityname);
								break;
							}

							//entity.setCustomNameVisible(true);
							//}
							LOGGER.debug("done");
							return;
						} catch (Exception exception) {
							exception.printStackTrace();
							// Unable to Silence Entity.
						}
					}
				}
			}

		}catch (Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PLAYER_INTERACTENTITY_EVENT).error(exception));
		}

	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event){// TODO: EnityDeathEvent
		LivingEntity entity = event.getEntity();
		//World world = event.getEntity().getWorld();
		try {
			if(entity instanceof EnderDragon){
				EnderDragon ed = (EnderDragon) entity;
				ed.getPersistentDataContainer().set(SILENT_DRAGON, PersistentDataType.STRING, Boolean.toString(ed.isSilent()));
				LOGGER.debug("EDE died");
			}
			if (event.getEntity().getPersistentDataContainer().has(silenceKey, PersistentDataType.STRING)) {
				event.getEntity().getPersistentDataContainer().remove(silenceKey);
			}
		}catch (Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_ENTITY_DEATH_EVENT).error(exception));
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event){
		try {
			if( event.getChunk() == null ) {
				return;
			}
			Entity[] entities = event.getChunk().getEntities();
			for (int i = 0; i < entities.length; i++) {
				if(entities[i] instanceof EnderDragon){
					String shouldBeSilent = entities[i].getPersistentDataContainer().get(silenceKey, PersistentDataType.STRING);
					if( shouldBeSilent.equalsIgnoreCase("Silenced") &&  !entities[i].isSilent() ) {
						entities[i].setSilent(true);
					}
				}
			}
		}catch (Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CHUNK_UNLOAD_EVENT).error(exception));
		}
	}

	ArrayList<Entity> dragons = new ArrayList<Entity>();
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event){
		/**try {
			if( event.getChunk() == null ) {
				return;
			}
			Entity[] entities = event.getChunk().getEntities();
			for (int i = 0; i < entities.length; i++) {
				if(entities[i] instanceof EnderDragon){
					if(entities[i].isSilent()){
						dragons.add(entities[i]);
					}
				}
			}
		}catch (Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CHUNK_UNLOAD_EVENT).error(exception));
		}//*/
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event){
		try {
			LivingEntity entity = event.getEntity();
			SpawnReason reason = event.getSpawnReason();
			try {
				if(reason.equals(SpawnReason.INFECTION)||reason.equals(SpawnReason.CURED)){
					String name = entity.getCustomName();
					if(name != null){
						if(name.equalsIgnoreCase("silenced")){
							entity.setSilent(true);
							LOGGER.debug("" + entity.getType().toString() + " has been ReSilenced");
						}
					}
				}
			}catch (Exception exception){
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CSE_VILLAGER).error(exception));
			}
			try {
				if((entity instanceof EnderDragon) && reason.equals(SpawnReason.CUSTOM)){
					Location loc = entity.getLocation();
					int X = loc.getBlockX();
					int Y = loc.getBlockY();
					int Z = loc.getBlockZ();
					int i = 0;
					for(Entity dragon : dragons){
						Location loc2 = dragon.getLocation();
						int X2 = loc2.getBlockX();
						int Y2 = loc2.getBlockY();
						int Z2 = loc2.getBlockZ();
						if((X == X2) && (Y == Y2) && (Z == Z2)){
							dragons.remove(i);
							entity.setSilent(dragon.isSilent());
							LOGGER.debug("" + entity.getType().toString() + " has been ReSilenced");
						}
						i++;
					}
				}
			}catch (Exception exception){
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CSE_DRAGON).error(exception));
			}
		}catch (Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_CREATURE_SPAWN_EVENT).error(exception));
		}
	}


	public static String getVersion() {
		String strVersion = Bukkit.getVersion();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}
	public static void copyFile_Java7(String origin, String destination) throws IOException {
		Path FROM = Paths.get(origin);
		Path TO = Paths.get(destination);
		//overwrite the destination file if it exists, and copy
		// the file attributes, including the rwx permissions
		CopyOption[] options = new CopyOption[]{
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES
		};
		Files.copy(FROM, TO, options);
	}

	public String LoadTime(long startTime) {
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

	@SuppressWarnings("static-access")
	public String get(String key, String... defaultValue) {
		return lang2.get(key, defaultValue);
	}

	/**public boolean isPluginRequired(String pluginName) {
		String[] requiredPlugins = {"SinglePlayerSleep", "MoreMobHeads", "NoEndermanGrief", "ShulkerRespawner", "DragonDropElytra", "RotationalWrench", "SilenceMobs", "VillagerWorkstationHighlights"};
		for (String requiredPlugin : requiredPlugins) {
			if ((getServer().getPluginManager().getPlugin(requiredPlugin) != null) && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
				if (requiredPlugin.equals(pluginName)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}//*/

	// Used to check Minecraft version
	private Version verifyMinecraftVersion() {
		Version minimum = new Version(PluginLibrary.MINIMUM_MINECRAFT_VERSION);
		Version maximum = new Version(PluginLibrary.MAXIMUM_MINECRAFT_VERSION);
		try {
			Version current = new Version(this.getServer());

			// We'll just warn the user for now
			if (current.compareTo(minimum) < 0) {
				LOGGER.warn("Version " + current + " is lower than the minimum " + minimum);
			}
			if (current.compareTo(maximum) > 0) {
				LOGGER.warn(ChatColor.RED + "Version " + current + " has not yet been tested! Proceed with caution." + ChatColor.RESET);
			}

			return current;
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(exception).messageParam(maximum));
			// Unknown version - just assume it is the latest
			return maximum;
		}
	}

	public String getjarfilename() {
		return jarfilename;
	}

	public boolean getDebug() {
		return debug;
	}

	public static SilenceMobs getInstance() {
		return getPlugin(SilenceMobs.class);
	}

}
