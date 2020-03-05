package com.github.joelgodofwar.sm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.sm.api.Ansi;
import com.github.joelgodofwar.sm.api.Metrics;
import com.github.joelgodofwar.sm.api.UpdateChecker;

public class SilenceMobs  extends JavaPlugin implements Listener{
	public final static Logger logger = Logger.getLogger("Minecraft");
	public static boolean UpdateCheck;
	public static boolean debug;
	public static String daLang;
	String updateURL = "https://github.com/JoelGodOfwar/SilenceMobs/raw/master/versioncheck/1.14/version.txt";
	File langFile;
    FileConfiguration lang;
    boolean UpdateAvailable =  false;
    
    @Override // TODO: onEnable
	public void onEnable(){
	    UpdateCheck = getConfig().getBoolean("auto_update_check");
		
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " Loading...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
		
		if(debug){logDebug("datafolder=" + getDataFolder());}
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
		if(checklangversion != null){
			if(!checklangversion.equalsIgnoreCase("1.0.7")){
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
			}
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
		}
		
		String[] serverversion;
		serverversion = getVersion().split("\\.");
		if(debug){logDebug("getVersion = " + getVersion());}
		if(debug){logDebug("serverversion = " + serverversion.length);}
		for (int i = 0; i < serverversion.length; i++)
			if(debug){logDebug(serverversion[i] + " i=" + i);}
		if (!(Integer.parseInt(serverversion[1]) >= 13)){
			
		//if(!getVersion().contains("1.9")&&!getVersion().contains("1.10")&&!getVersion().contains("1.11")){
			logger.info(Ansi.RED + Ansi.BOLD + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.RESET);
			logger.info(Ansi.RED + Ansi.BOLD + "WARNING! " + Ansi.YELLOW + lang.get("server_not_version") + Ansi.RESET); //, "Server is NOT version 1.13.*+"
			logger.info(Ansi.RED + Ansi.BOLD + "WARNING! " + Ansi.YELLOW + pdfFile.getName() + " v" + pdfFile.getVersion() + " disabling." + Ansi.RESET);
			logger.info(Ansi.RED + Ansi.BOLD + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.RESET);
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
		
		/** Update Checker */
		if(UpdateCheck){
			try {
		        Bukkit.getConsoleSender().sendMessage("Checking for updates...");
		        UpdateChecker updater = new UpdateChecker(this, 75749);
				if(updater.checkForUpdates()) {
		        	UpdateAvailable = true;
		        	Bukkit.getConsoleSender().sendMessage(Ansi.YELLOW + this.getName() + Ansi.MAGENTA + " " + lang.get("newvers") + Ansi.RESET);
		        	Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
		        }else{
		        	UpdateAvailable = false;
		        }
		    }catch(Exception e) {
		        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not process update check");
		    }
		}
		/** end update checker */
		
		getServer().getPluginManager().registerEvents(this, this);
		
		consoleInfo("Enabled");
		
		Metrics metrics  = new Metrics(this);
		// New chart here
		// myPlugins()
		metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
	        @Override
	        public Map<String, Integer> call() throws Exception {
	            Map<String, Integer> valueMap = new HashMap<>();
	            //int varTotal = myPlugins();
	            if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}
	    		if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}
	    		if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}
	    		if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}
	    		if(getServer().getPluginManager().getPlugin("SinglePlayerSleep") != null){valueMap.put("SinglePlayerSleep", 1);}
	    		if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}
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
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " is " + state);
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
    }
	
    public  void log(String dalog){// TODO:
    	logger.info(Ansi.YELLOW + "" + this.getName() + Ansi.RESET + " " + dalog + Ansi.RESET);
    }
	public  void logDebug(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.BOLD + " [DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.BOLD + " [WARN] " + Ansi.RESET + dalog  + Ansi.RESET);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	    if (cmd.getName().equalsIgnoreCase("SM")||cmd.getName().equalsIgnoreCase("silencemobs")){
	      if (args.length == 0){
	    	sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SilenceMobs" + ChatColor.GREEN + "]===============[]");
	    	sender.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + " If you have the permissions commands will appear.");
	    	if(sender.isOp()||sender.hasPermission("silencemobs.reload")||!(sender instanceof Player)){
	    		sender.sendMessage(ChatColor.WHITE + " /sm reload - " + lang.get("reload"));//subject to server admin approval");
	    	}
	    	if(sender.isOp()||sender.hasPermission("silencemobs.toggledebug")||!(sender instanceof Player)){
	    		sender.sendMessage(ChatColor.WHITE + " /sm toggledebug - " + lang.get("srdebuguse"));//Cancels SinglePlayerSleep");
	    	}
	    	sender.sendMessage(ChatColor.WHITE + " ");
	        sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SilenceMobs" + ChatColor.GREEN + "]===============[]");
	        return true;
	      }
	      if(args[0].equalsIgnoreCase("reload")){
			  if(sender.isOp()||sender.hasPermission("silencemobs.reload")||!(sender instanceof Player)){
				  //ConfigAPI.Reloadconfig(this, p);
				  getServer().getPluginManager().disablePlugin(this);
                  getServer().getPluginManager().enablePlugin(this);
                  reloadConfig();
                  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("reloaded"));
                  return true;
			  }else if(!sender.hasPermission("silencemobs.reload")){
				  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("noperm"));
				  return false;
			  }
	      }
	      if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
			  if(sender.isOp()||sender.hasPermission("silencemobs.toggledebug")||!(sender instanceof Player)){
				  debug = !debug;
				  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("debugtrue").toString().replace("boolean", "" + debug));
				  return true;
			  }else if(!sender.hasPermission("silencemobs.toggledebug")){
				  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("noperm"));
				  return false;
			  }
	      }
	    }
	    return false;
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	  {
	    Player p = event.getPlayer();	
	    /** Notify Ops */
	    if(UpdateAvailable&&(p.isOp()||p.hasPermission("silencemobs.showUpdateAvailable"))){
	    	p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers") + " \n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
	    }
	    
	    if(p.getDisplayName().equals("JoelYahwehOfWar")||p.getDisplayName().equals("JoelGodOfWar")){
	    	p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
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
							if(debug){logDebug("done");};
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
	 
	public static String getVersion() {
		String strVersion = Bukkit.getVersion();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}
}
