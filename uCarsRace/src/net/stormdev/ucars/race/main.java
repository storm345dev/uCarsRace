package net.stormdev.ucars.race;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.stormdev.ucars.utils.Ques;
import net.stormdev.ucars.utils.RaceMethods;
import net.stormdev.ucars.utils.RaceQue;
import net.stormdev.ucars.utils.RaceTrackManager;
import net.stormdev.ucars.utils.TrackCreator;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.useful.ucars.Colors;
import com.useful.ucars.ucars;

public class main extends JavaPlugin {
	public YamlConfiguration lang = new YamlConfiguration();
	public static main plugin;
	public static FileConfiguration config = new YamlConfiguration();
	public static Colors colors; 
	public static CustomLogger logger = null;
	public static ucars ucars = null;
	public static URaceCommandExecutor cmdExecutor = null;
	public static URaceListener listener = null;
	public RaceTrackManager trackManager = null;
	public RaceScheduler gameScheduler = null;
	public static HashMap<String, TrackCreator> trackCreators = new HashMap<String, TrackCreator>();
	public HashMap<String, RaceQue> ques = new HashMap<String, RaceQue>();
	public Ques raceQues = null;
	public static Lang msgs = null;
	public RaceMethods raceMethods = null;
	public void onEnable(){
		plugin = this;
		File langFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "lang.yml");
		if (langFile.exists() == false
				|| langFile.length() < 1) {
			try {
				langFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}
			
		}
		try {
			lang.load(langFile);
		} catch (Exception e1) {
			getLogger().log(Level.WARNING, "Error creating/loading lang file! Regenerating..");
		}
		msgs = new Lang(this);
		if (new File(getDataFolder().getAbsolutePath() + File.separator
				+ "config.yml").exists() == false
				|| new File(getDataFolder().getAbsolutePath() + File.separator
						+ "config.yml").length() < 1) {
			getDataFolder().mkdirs();
			File configFile = new File(getDataFolder().getAbsolutePath()
					+ File.separator + "config.yml");
			try {
				configFile.createNewFile();
			} catch (IOException e) {
			}
			copy(getResource("ucarsRaceConfigHeader.yml"), configFile);
		}
		config = getConfig();
		logger = new CustomLogger(getServer().getConsoleSender(), getLogger());
        try {
        	//Setup the Lang file
        	if(!lang.contains("general.cmd.page")){
        		lang.set("general.cmd.page", "Page [%page%/%total%]:");
        	}
        	if(!lang.contains("general.cmd.playersOnly")){
        		lang.set("general.cmd.playersOnly", "This command is for players only!");
        	}
        	if(!lang.contains("general.cmd.delete.success")){
        		lang.set("general.cmd.delete.success", "Successfully deleted track %name%!");
        	}
        	if(!lang.contains("general.cmd.delete.exists")){
        		lang.set("general.cmd.delete.exists", "That track doesn't exist!");
        	}
        	if(!lang.contains("setup.create.exists")){
        		lang.set("setup.create.exists", "This track already exists! Please do /urace delete %name% before proceeding!");
        	}
        	if(!lang.contains("setup.create.start")){
        		lang.set("setup.create.start", "Wand: %id% (%name%)");
        	}
        	if(!lang.contains("setup.create.lobby")){
        		lang.set("setup.create.lobby", "Stand in the lobby and right click anywhere with the wand");
        	}
        	if(!lang.contains("setup.create.exit")){
        		lang.set("setup.create.exit", "Stand at the track exit and right click anywhere with the wand");
        	}
        	if(!lang.contains("setup.create.grid")){
        		lang.set("setup.create.grid", "Stand where you want a car to start the race and right click anywhere (Without the wand). Repeat for all the starting positions. When done, right click anywhere with the wand");
        	}
        	if(!lang.contains("setup.create.checkpoints")){
        		lang.set("setup.create.checkpoints", "Stand at each checkpoint along the track (Checkpoint 10x10 radius) and right click anywhere (Without the wand). Repeat for all checkpoints. When done, right click anywhere with the wand");
        	}
        	if(!lang.contains("setup.create.notEnoughCheckpoints")){
        		lang.set("setup.create.notEnoughCheckpoints", "You must have at least 3 checkpoints! You only have: %num%");
        	}
        	if(!lang.contains("setup.create.line1")){
        		lang.set("setup.create.line1", "Stand at one end of the start/finish line and right click anywhere with the wand");
        	}
        	if(!lang.contains("setup.create.line2")){
        		lang.set("setup.create.line2", "Stand at the other end of the start/finish line and right click anywhere with the wand");
        	}
        	if(!lang.contains("setup.create.done")){
        		lang.set("setup.create.done", "Successfully created Race Track %name%!");
        	}
        	if(!lang.contains("race.que.existing")){
        		lang.set("race.que.existing", "You are already in a game que! Please leave it before joining this one!");
        	}
        	if(!lang.contains("race.que.full")){
        		lang.set("race.que.full", "Race que full!");
        	}
        	if(!lang.contains("race.que.success")){
        		lang.set("race.que.success", "In Race Que!");
        	}
        	if(!lang.contains("race.que.joined")){
        		lang.set("race.que.joined", " joined the race que!");
        	}
        	if(!lang.contains("race.que.left")){
        		lang.set("race.que.left", " left the race que!");
        	}
        	if(!lang.contains("race.que.preparing")){
        		lang.set("race.que.preparing", "Preparing race...");
        	}
        	if(!lang.contains("race.que.starting")){
        		lang.set("race.que.starting", "Race starting in...");
        	}
        	if(!lang.contains("race.que.go")){
        		lang.set("race.que.go", "Go!");
        	}
        	if(!lang.contains("race.end.won")){
        		lang.set("race.end.won", " won the race!");
        	}
        	if(!lang.contains("race.mid.miss")){
        		lang.set("race.mid.miss", "You missed a section of the track! Please go back and do it!");
        	}
        	if(!lang.contains("race.mid.backwards")){
        		lang.set("race.mid.backwards", "You are going the wrong way!");
        	}
        	if(!lang.contains("race.mid.lap")){
        		lang.set("race.mid.lap", "Lap [%lap%/%total%]");
        	}
        	//Setup the config
        	if (!config.contains("setup.create.wand")) {
				config.set("setup.create.wand", 280);
			}
        	if (!config.contains("general.logger.colour")) {
				config.set("general.logger.colour", true);
			}
        	if (!config.contains("general.raceTickrate")) {
				config.set("general.raceTickrate", 6l);
			}
        	//Setup the colour scheme
        	if (!config.contains("colorScheme.success")) {
				config.set("colorScheme.success", "&a");
			}
			if (!config.contains("colorScheme.error")) {
				config.set("colorScheme.error", "&c");
			}
			if (!config.contains("colorScheme.info")) {
				config.set("colorScheme.info", "&e");
			}
			if (!config.contains("colorScheme.title")) {
				config.set("colorScheme.title", "&9");
			}
			if (!config.contains("colorScheme.tp")) {
				config.set("colorScheme.tp", "&5");
			}
        } catch(Exception e){
        }
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		//Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		logger.info("Config loaded!");
		logger.info("Searching for uCars...");
		Plugin[] plugins = getServer().getPluginManager().getPlugins();
		Boolean installed = false;
		for(Plugin p:plugins){
			if(p.getName().equals("uCars")){
			installed = true;
			ucars = (com.useful.ucars.ucars) p;
			}
		}
		if(!installed){
			logger.info("Unable to find uCars!");
			getServer().getPluginManager().disablePlugin(this);
		}
		ucars.hookPlugin(this);
		logger.info("uCars found and hooked!");
		PluginDescriptionFile pldesc = plugin.getDescription();
		Map<String, Map<String, Object>> commands = pldesc.getCommands();
		Set<String> keys = commands.keySet();
		main.cmdExecutor = new URaceCommandExecutor(this);
		for (String k : keys) {
			try {
				getCommand(k).setExecutor(cmdExecutor);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE,
						"Error registering command " + k.toString());
				e.printStackTrace();
			}
		}
		main.listener = new URaceListener(this);
		getServer().getPluginManager().registerEvents(main.listener,
				this);
		this.trackManager = new RaceTrackManager(this, new File(getDataFolder()+File.separator+"Data"+File.separator+"tracks.uracetracks"));
		this.raceQues = new Ques(this);
		this.raceMethods = new RaceMethods();
		this.gameScheduler = new RaceScheduler();
		logger.info("uCarsRace v"+plugin.getDescription().getVersion()+" has been enabled!");
	}
	
	public void onDisable(){
		if(ucars != null){
			ucars.unHookPlugin(this);
		}
		HashMap<String, Race> games = this.gameScheduler.getGames();
		for(Race r:games.values()){
			this.gameScheduler.stopGame(r.getTrack(), r.getGameId());
		}
		logger.info("uCarsRace has been disabled!");
	}
	
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				// System.out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String colorise(String prefix) {
		 return ChatColor.translateAlternateColorCodes('&', prefix);
	}
}
