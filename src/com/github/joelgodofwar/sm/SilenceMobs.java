package com.github.joelgodofwar.sm;

import com.github.joelgodofwar.sm.command.CommandHandler;
import com.github.joelgodofwar.sm.common.PluginCommon;
import com.github.joelgodofwar.sm.common.PluginLibrary;
import com.github.joelgodofwar.sm.common.PluginLogger;
import com.github.joelgodofwar.sm.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sm.common.error.Report;
import com.github.joelgodofwar.sm.event.*;
import com.github.joelgodofwar.sm.i18n.Translator;
import com.github.joelgodofwar.sm.util.*;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class SilenceMobs  extends JavaPlugin{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SE), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	//public final static Logger logger = Logger.getLogger("Minecraft");
	public static final long DEV_BUILD_START_TIME = 1754972016L; // Placeholder for Maven replacement
	public final BuildValidator buildValidator = new BuildValidator();
	public static String THIS_NAME;
	public static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 75749; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/SilenceMobs/raw/master/versioncheck/1.20/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String DownloadLink = "https://dev.bukkit.org/projects/silence-mobs/files";
	/** end update checker variables */
	public boolean debug;
	public static String daLang;
	Translator lang2;
	public YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	boolean colorful_console;
	String pluginName = THIS_NAME;
	public Map<String, String> map = new HashMap<>();
	public final NamespacedKey silenceKey = new NamespacedKey(this, "silenced");
	public String jarFileName = this.getFile().getAbsoluteFile().toString();
	public static DetailedErrorReporter reporter;
	public PluginLogger LOGGER;
	ArrayList<Entity> dragons = new ArrayList<>();
	public static final String OWNER_ID = "ownerofthedragon";
	private final NamespacedKey SILENT_DRAGON = new NamespacedKey(this, "silent_dragon");
	@SuppressWarnings({ "deprecation", "unused" })
	private final NamespacedKey SILENT_DRAGON2 = new NamespacedKey("petdragon", OWNER_ID);

	@SuppressWarnings("unused")
	@Override // TODO: onEnable
	public void onEnable(){
		long startTime = System.currentTimeMillis();
		setVariables();

		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + " v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		LOGGER.log("Server Version: " + getServer().getVersion());

		Version checkVersion = PluginCommon.verifyMinecraftVersion(this, getServer(), LOGGER, reporter);

		//** DEV check **/
		checkDev();

		Version current = new Version(this.getServer());
		if (!current.atOrAbove(PluginLibrary.MINIMUM_MINECRAFT_VERSION)){
			LOGGER.warn(ChatColor.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + ChatColor.RESET);
			LOGGER.warn(ChatColor.YELLOW + get("sm.message.minimum_version_required").replace("<version>", PluginLibrary.MINIMUM_MINECRAFT_VERSION.toString()) + ChatColor.RESET);
			LOGGER.warn(ChatColor.YELLOW + THIS_NAME + " v" + THIS_VERSION + " disabling." + ChatColor.RESET);
			LOGGER.warn(ChatColor.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + ChatColor.RESET);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if (!buildValidator.isBuildValid()) {
			getLogger().severe(ChatColor.DARK_RED + "This dev-build has expired or is incompatible with your Minecraft version.");
			getLogger().info(ChatColor.AQUA + "Update to the latest dev-build or full release at https://dev.bukkit.org/projects/silence-mobs/files");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Check and update config
		PluginCommon.checkAndUpdateConfig(this, LOGGER, reporter, config, oldconfig);

		//** Update Checker */
		if(UpdateCheck){
			try {
				LOGGER.log("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					//** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sm.version.message").replace("<MyPlugin>", THIS_NAME) );
					LOGGER.log("* " + get("sm.version.old_vers") + ChatColor.RED + UColdVers );
					LOGGER.log("* " + get("sm.version.new_vers") + ChatColor.GREEN + UCnewVers );
					LOGGER.log("*");
					LOGGER.log("* " + get("sm.version.please_update") );
					LOGGER.log("*");
					LOGGER.log("* " + get("sm.version.download") + ": " + DownloadLink );
					LOGGER.log("* " + get("sm.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					//** Up to date */
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
			//** auto_update_check is false so nag. */
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			LOGGER.log("* " + get("sm.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		//** end update checker */

		// Register event listeners
		getServer().getPluginManager().registerEvents(new PlayerJoinEventHandler(this, reporter, DownloadLink, UCnewVers, UColdVers, UpdateAvailable), this);
		getServer().getPluginManager().registerEvents(new PlayerInteractEntityEventHandler(this, LOGGER, reporter, silenceKey), this);
		getServer().getPluginManager().registerEvents(new EntityDeathEventHandler(this, LOGGER, reporter, silenceKey, SILENT_DRAGON), this);
		getServer().getPluginManager().registerEvents(new ChunkLoadEventHandler(this, reporter, silenceKey), this);
		getServer().getPluginManager().registerEvents(new ChunkUnloadEventHandler(this), this);
		getServer().getPluginManager().registerEvents(new CreatureSpawnEventHandler(this, LOGGER, reporter, dragons), this);

		// Register command handler
		CommandHandler commandHandler = new CommandHandler(this, LOGGER, reporter, silenceKey);
		Objects.requireNonNull(getCommand("SM")).setExecutor(commandHandler);
		Objects.requireNonNull(getCommand("SM")).setTabCompleter(commandHandler);
		Objects.requireNonNull(getCommand("silencemobs")).setExecutor(commandHandler);
		Objects.requireNonNull(getCommand("silencemobs")).setTabCompleter(commandHandler);

		consoleInfo("Enabled - Loading took " + PluginCommon.loadTime(startTime));

		try {
			Metrics metrics  = new Metrics(this, 6695);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", () -> {
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
            }));
			metrics.addCustomChart(new Metrics.SimplePie("auto_update_check", () -> Objects.requireNonNull(getConfig().getString("auto_update_check")).toUpperCase()));
			// add to site
			metrics.addCustomChart(new Metrics.SimplePie("var_debug", () -> Objects.requireNonNull(getConfig().getString("debug")).toUpperCase()));
			metrics.addCustomChart(new Metrics.SimplePie("var_lang", () -> Objects.requireNonNull(getConfig().getString("lang")).toUpperCase()));
		} catch (Exception exception) {
			// Handle the exception or log it
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_METRICS_LOAD_ERROR).error(exception));
		}
	}

	public void reload() throws Exception {
		long startTime = System.currentTimeMillis();
		setVariables();
		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + " v" + THIS_VERSION + ChatColor.RESET + " Reloading...");
		LOGGER.log("Server Version: " + getServer().getVersion());
		checkDev();
		PluginCommon.checkAndUpdateConfig(this, LOGGER, reporter, config, oldconfig);
		loadConfig();
		LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		LOGGER.log("* " + get("sm.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
		LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		consoleInfo("Enabled - Reloading took " + PluginCommon.loadTime(startTime));
	}

	private void setVariables() {
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
		if (!getConfig().getBoolean("console.longpluginname", true)) {
			pluginName = "SM";
		} else {
			pluginName = THIS_NAME;
		}
	}

	private void checkDev() {
		File jarfile = this.getFile().getAbsoluteFile();
		if (jarfile.toString().contains("-DEV")) {
			debug = true;
			LOGGER.debug("Jar file contains -DEV, debug set to true");
		}
	}

	private void loadConfig() {
		try{
			config.load(new File(getDataFolder(), "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
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

	@SuppressWarnings("static-access")
	public String get(String key, String... defaultValue) {
		return lang2.get(key, defaultValue);
	}

	public String getJarFileName() {
		return jarFileName;
	}

	public boolean getDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public static SilenceMobs getInstance() {
		return getPlugin(SilenceMobs.class);
	}
}