package com.github.joelgodofwar.sm;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
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
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.sm.i18n.Translator;
import com.github.joelgodofwar.sm.util.Ansi;
import com.github.joelgodofwar.sm.util.Metrics;
import com.github.joelgodofwar.sm.util.Utils;
import com.github.joelgodofwar.sm.util.VersionChecker;
import com.github.joelgodofwar.sm.util.YmlConfiguration;

public class SilenceMobs  extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	public final static Logger logger = Logger.getLogger("Minecraft");
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
	public static boolean debug;
	public static String daLang;
	File langFile;
    FileConfiguration lang;
    Translator lang2;
    YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
    boolean colorful_console;
	String pluginName = THIS_NAME;
    public Map<String, String> map = new HashMap<String, String>();
    private Set<String> triggeredPlayers = new HashSet<>();
    
    @SuppressWarnings("unused")
	@Override // TODO: onEnable
	public void onEnable(){
		long startTime = System.currentTimeMillis();
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
		
		
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " Loading...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
		
		/** Lang file check */
		/**if(debug){logDebug("datafolder=" + getDataFolder());}
		langFile = new File(getDataFolder() + "" + File.separatorChar + "lang" + File.separatorChar, daLang + ".yml");//\
		if(debug){logDebug("langFilePath=" + langFile.getPath());}
		if(!langFile.exists()){                                  // checks if the yaml does not exist
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "es_MX.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
			log("lang file not found! copied cs_CZ.yml, de_DE.yml, en_US.yml, es_MX.yml, fr_FR.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to " + getDataFolder() + "" + File.separatorChar + "lang");
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		String checklangversion = lang.getString("langversion");
		if(checklangversion != null&&checklangversion.contains("1.0.8")){
			//Up to date do nothing
		}else{
				saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
				saveResource("lang" + File.separatorChar + "de_DE.yml", true);
				saveResource("lang" + File.separatorChar + "en_US.yml", true);
				saveResource("lang" + File.separatorChar + "es_MX.yml", true);
				saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
				saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
				saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
				saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
				log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, es_MX.yml, fr_FR.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to "
						+ getDataFolder() + "" + File.separatorChar + "lang");
				try {
					lang.load(langFile);
				} catch (IOException | InvalidConfigurationException e1) {
					e1.printStackTrace();
				}
		}//*/

		/** Lang file check */
		
		String[] serverversion;
		serverversion = getVersion().split("\\.");
		if(debug){logDebug("getVersion = " + getVersion());}
		if(debug){logDebug("serverversion = " + serverversion.length);}
		for (int i = 0; i < serverversion.length; i++)
			if(debug){logDebug(serverversion[i] + " i=" + i);}
		if (!(Integer.parseInt(serverversion[1]) >= 13)){
			
		//if(!getVersion().contains("1.9")&&!getVersion().contains("1.10")&&!getVersion().contains("1.11")){
			logger.info("" + Ansi.RED + Ansi.BOLD + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.RESET);
			logger.info("" + Ansi.RED + Ansi.BOLD + "WARNING! " + Ansi.YELLOW + get("sm.message.server_not_version") + Ansi.RESET); //, "Server is NOT version 1.13.*+"
			logger.info("" + Ansi.RED + Ansi.BOLD + "WARNING! " + Ansi.YELLOW + THIS_NAME + " v" + THIS_VERSION + " disabling." + Ansi.RESET);
			logger.info("" + Ansi.RED + Ansi.BOLD + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.RESET);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				log("Data Folder doesn't exist");
				log("Creating Data Folder");
				getDataFolder().mkdirs();
				log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			log("" + file);
			if(!file.exists()){
				log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/** end config check */
		/** Check if config.yml is up to date.*/
		boolean needConfigUpdate = false;
		String oldConfig = new File(getDataFolder(), "config.yml").getPath().toString();
		try {
			oldconfig.load(new File(getDataFolder() + "" + File.separatorChar + "config.yml"));
		} catch (Exception e2) {
			logWarn("Could not load config.yml");
			e2.printStackTrace();
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
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e2) {
				logWarn("Could not load config.yml");
				e2.printStackTrace();
			}
			saveResource("config.yml", true);
			try {
				config.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				logWarn("Could not load config.yml");
				e1.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "old_config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				e1.printStackTrace();
			}
			config.set("auto_update_check", oldconfig.get("auto_update_check", true));
			config.set("debug", oldconfig.get("debug", false));
			config.set("lang", oldconfig.get("lang", "en_US"));
			config.set("colorful_console", oldconfig.get("colorful_console", true));
			try {
				config.save(new File(getDataFolder(), "config.yml"));
			} catch (IOException e) {
				logWarn("Could not save old settings to config.yml");
				e.printStackTrace();
			}
			log("config.yml has been updated");
		}else{
			//log("" + "not found");
		}
		/** End Config update check */
		
		//newVerMsg = Ansi.YELLOW + THIS_NAME + Ansi.MAGENTA + " v{oVer}" + Ansi.RESET + " " + get("newvers") + Ansi.GREEN + " v{nVer}" + Ansi.RESET;
		/** Update Checker */
		if(UpdateCheck){
			try {
				Bukkit.getConsoleSender().sendMessage("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();
					
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					log("* " + get("sm.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					log("* " + get("sm.version.old_vers") + ChatColor.RED + UColdVers );
					log("* " + get("sm.version.new_vers") + ChatColor.GREEN + UCnewVers );
					log("*");
					log("* " + get("sm.version.please_update") );
					log("*");
					log("* " + get("sm.version.download") + ": " + DownloadLink + "/history");
					log("* " + get("sm.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					/** Up to date */
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					log("* " + get("sm.version.curvers"));
					log("* " + get("sm.version.donate") + ": https://ko-fi.com/joelgodofwar");
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				/** Error */
				log(get("sm.version.update.error"));
				e.printStackTrace();
			}
		}else {
			/** auto_update_check is false so nag. */
			log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			log("* " + get("sm.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		/** end update checker */
		
		getServer().getPluginManager().registerEvents(this, this);
		
		consoleInfo("Enabled - Loading took " + LoadTime(startTime));
		
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
    }
    
    @Override // TODO: onDisable
	public void onDisable(){
		consoleInfo("Disabled");
	}
    
    public void consoleInfo(String state) {// TODO:
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " is " + state);
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
    }
	
    public  void log(String dalog){// TODO:
    	logger.info(Ansi.YELLOW + "" + pluginName + " " + THIS_VERSION + Ansi.RESET + " " + dalog + Ansi.RESET);
    }
    public  void log(Level lvl, String dalog){// TODO:
    	logger.log(lvl, dalog);
    }
	public  void logDebug(String dalog){
		log("" + Ansi.RED + Ansi.BOLD + "[DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log(" " + Ansi.RED + Ansi.BOLD + "[WARN] " + Ansi.RESET + dalog  + Ansi.RESET);
	}
	
	public static final String OWNER_ID = "ownerofthedragon";
	private final NamespacedKey SILENT_DRAGON = new NamespacedKey(this, "silent_dragon");
	@SuppressWarnings({ "deprecation", "unused" })
	private final NamespacedKey SILENT_DRAGON2 = new NamespacedKey("petdragon", OWNER_ID);
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
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
	      if(args[0].equalsIgnoreCase("dragon")){
	    	  if(sender instanceof Player){
	    		  Player player = (Player) sender;
	    		  //World world = player.getWorld();
	    		  //List<Entity> entities = world.getEntities();
	    		 List<Entity> entities = player.getNearbyEntities(16, 16, 16);
	    		 
	    		  for(Entity e : entities) { // world.getEntities()
	    			  if(e instanceof EnderDragon){
	    				  log("name=" + e.getCustomName());
	    				  Material material = player.getInventory().getItemInMainHand().getType();
	    				  Material material2 = player.getInventory().getItemInOffHand().getType();
	    				  String name = null;
	    				  String hand = null;
	    				  if(material.equals(Material.NAME_TAG)){
	    					  name = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
	    					  hand = "Main";
	    					  if(debug){logDebug(player.getDisplayName() + " Main hand name=" + name);};
	    				  }
	    				  if(material2.equals(Material.NAME_TAG)){
	    					  name = player.getInventory().getItemInOffHand().getItemMeta().getDisplayName();
	    					  hand = "Off";
	    					  if(debug){logDebug(player.getDisplayName() + " Off hand name=" + name);};
	    				  }
	    				  if(name != null){
	    						if(debug){logDebug("name!=null");};
	    						if(name.equalsIgnoreCase("silence me")||name.equalsIgnoreCase("silenceme")){
	    							try{
	    								if(debug){logDebug("name=" + name);};
	    								String entityname = e.getCustomName();
	    								e.setSilent(true);
	    			    		    	e.setCustomName("Silenced");
	    			    		    	e.getScoreboardTags().add("Silenced");
	    								switch (hand){
	    								
	    								case "Main":
	    									player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
	    									log("" + player.getDisplayName() +  " has silenced a " + entityname);
	    									break;
	    								case "Off":
	    									player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
	    									log("" + player.getDisplayName() +  " has silenced a " + entityname);
	    									break;
	    								}
	    								
	    								//entity.setCustomNameVisible(true);
	    								//}
	    								if(debug){logDebug("done");};
	    								return true;
	    							} catch (Exception ignore) {
	    								ignore.printStackTrace();
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
	    return false;
	}
	
	@Override 
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { // TODO: Tab Complete
		if (command.getName().equalsIgnoreCase("sm")) {
			List<String> autoCompletes = new ArrayList<>(); //create a new string list for tab completion
			if (args.length == 1) { // reload, toggledebug, playerheads, customtrader, headfix
				autoCompletes.add("reload");
				autoCompletes.add("toggledebug");
				autoCompletes.add("dragon");
				return autoCompletes; // then return the list
			}
		}
		return null;
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event){ //TODO: onPlayerJoinEvent
	    Player player = event.getPlayer();	
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
	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd");
	    LocalDate localDate = LocalDate.now();
	    String daDay = dtf.format(localDate);

	    if (daDay.equals("04/16")) {
	        String playerId = player.getUniqueId().toString();
	        if (!triggeredPlayers.contains(playerId)) {
	            if (isPluginRequired(THIS_NAME)) {
	                player.sendTitle("Happy Birthday Mom", "I miss you - 4/16/1954-12/23/2022", 10, 70, 20);
	            }
	            triggeredPlayers.add(playerId);
	        }
	    }
	    if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
	    	player.sendMessage(THIS_NAME + " " + THIS_VERSION + " Hello father!");
	    	//p.sendMessage("seed=" + p.getWorld().getSeed());
	    }
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEntityEvent event){// TODO:
		//if(debug){logDebug("PlayerInteractEntityEvent called");};
		if(!(event.getPlayer() instanceof Player))
			return;
		try{
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
					if(debug){logDebug(player.getDisplayName() + " Main hand name=" + name);};
				}
				if(material2.equals(Material.NAME_TAG)){
					name = player.getInventory().getItemInOffHand().getItemMeta().getDisplayName();
					hand = "Off";
					if(debug){logDebug(player.getDisplayName() + " Off hand name=" + name);};
				}
				//LivingEntity mob = (LivingEntity) event.getRightClicked();
				if(name != null){
					if(debug){logDebug("name!=null");};
					if(name.equalsIgnoreCase("silence me")||name.equalsIgnoreCase("silenceme")){
						try{
							if(debug){logDebug("name=" + name);};
							/**String v = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
							if(debug){logDebug("v=" + v);};*/
							LivingEntity entity = (LivingEntity) event.getRightClicked();
							/**if(debug){logDebug("entity vared");};
							Object craftentity = Class.forName("org.bukkit.craftbukkit." + v + ".entity.CraftEntity").cast(entity);
							if(debug){logDebug("entity cast");};
							//Object name = craftentity.getClass().getMethod("getCustomName");
							//if(name.toString().equalsIgnoreCase("silence me")||name.toString().equalsIgnoreCase("silenceme")){
							craftentity.getClass().getMethod("getHandle").invoke("setSilent", true);
							craftentity.getClass().getMethod("getHandle").invoke("setCustomName", "Silenced");*/
							String entityname = event.getRightClicked().toString().replace(" ", "_").replace("Craft", "");
							entity.setSilent(true);
							entity.setCustomName("Silenced");
							event.setCancelled(true);
							switch (hand){
							
							case "Main":
								player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
								log("" + player.getDisplayName() +  " has silenced a " + entityname);
								break;
							case "Off":
								player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
								log("" + player.getDisplayName() +  " has silenced a " + entityname);
								break;
							}
							
							//entity.setCustomNameVisible(true);
							//}
							if(debug){logDebug("done");}
							return;
						} catch (Exception ignore) {
							ignore.printStackTrace();
						}
					}
				}
			}
			
		}catch (Exception e){
			//e.printStackTrace();
		}

	}
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event){// TODO: EnityDeathEvent
		LivingEntity entity = event.getEntity();
		//World world = event.getEntity().getWorld();
		if(entity instanceof EnderDragon){
			EnderDragon ed = (EnderDragon) entity;
			ed.getPersistentDataContainer().set(SILENT_DRAGON, PersistentDataType.STRING, Boolean.toString(ed.isSilent()));
			if(debug){logDebug("EDE died");}
		}
	}
	
	ArrayList<Entity> dragons = new ArrayList<Entity>();
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event){
		Entity[] entities = event.getChunk().getEntities();
		for (int i = 0; i < entities.length; i++) {
			if(entities[i] instanceof EnderDragon){
				if(entities[i].isSilent()){
					dragons.add(entities[i]);
				}
			}
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event){
		LivingEntity entity = event.getEntity();
		SpawnReason reason = event.getSpawnReason();
		if(reason.equals(SpawnReason.INFECTION)||reason.equals(SpawnReason.CURED)){
			String name = entity.getCustomName();
			if(name != null){
				if(name.equalsIgnoreCase("silenced")){
					entity.setSilent(true);
					if(debug){logDebug("" + entity.getType().toString() + " has been ReSilenced");};
				}
			}
		}
		if(entity instanceof EnderDragon && reason.equals(SpawnReason.CUSTOM)){
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
				if(X == X2 && Y == Y2 && Z == Z2){
					dragons.remove(i);
					entity.setSilent(dragon.isSilent());
					if(debug){logDebug("" + entity.getType().toString() + " has been ReSilenced");};
				}
				i++;
			}
		}
		/**if(entity instanceof EnderDragon && reason.equals(SpawnReason.CUSTOM)){
			EnderDragon ed = (EnderDragon) entity;
			//ed.setSilent(Boolean.parseBoolean(ed.getPersistentDataContainer().get(SILENT_DRAGON, PersistentDataType.STRING)));
			Location loc = ed.getLocation();
			Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
			ed.setSilent( Boolean.parseBoolean( map.get( chunk ) ) );
			ed.setCustomName("Silenced");
			log("CS hm size=" + map.size());
			log("CS maploc=" + map.get( chunk ));
			log("CS parse=" + Boolean.parseBoolean( map.get( chunk ) ));
			log("CS edsilent=" + ed.isSilent());
			log("CS loc=" + loc2.toString());
			log("CS ed spawned");
			log("CS eID=" + ed.getEntityId());
			UUID uuid = UUID.fromString(ed.getPersistentDataContainer().get(SILENT_DRAGON2, PersistentDataType.STRING));
		}*/
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
	
	public boolean isPluginRequired(String pluginName) {
	    String[] requiredPlugins = {"SinglePlayerSleep", "MoreMobHeads", "NoEndermanGrief", "ShulkerRespawner", "DragonDropElytra", "RotationalWrench", "SilenceMobs", "VillagerWorkstationHighlights"};
	    for (String requiredPlugin : requiredPlugins) {
	        if (getServer().getPluginManager().getPlugin(requiredPlugin) != null && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
	            if (requiredPlugin.equals(pluginName)) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	    }
	    return true;
	}
	
}
