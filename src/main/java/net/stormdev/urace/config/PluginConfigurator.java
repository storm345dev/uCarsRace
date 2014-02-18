package net.stormdev.urace.config;

import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfigurator {
	private static double current = 1.1;
	
	public static void load(FileConfiguration config){
		fill(config);
	}
	private static void fill(FileConfiguration config){
		System.out.println("Reading config...");
		// Setup the config
		if (!config.contains("setup.create.wand")) {
			config.set("setup.create.wand", 280);
		}
		else{
			//Config has been generated before
			double version;
			if(!config.contains("misc.configVersion")){
				version = 1;
			}
			else{
				version = config.getDouble("misc.configVersion");
			}
			if(version < current){
				System.out.println("Converting config...");
				config = ConfigVersionConverter.convert(config, current);
			}
		}
		if(!config.contains("misc.configVersion")){
			config.set("misc.configVersion", current);
		}
		if (!config.contains("general.logger.colour")) {
			config.set("general.logger.colour", true);
		}
		if (!config.contains("general.raceLimit")) {
			config.set("general.raceLimit", 10);
		}
		if (!config.contains("general.raceTickrate")) {
			config.set("general.raceTickrate", 4l);
		}
		if (!config.contains("general.checkpointRadius")) {
			config.set("general.checkpointRadius", 10.0);
		}
		if (!config.contains("general.raceGracePeriod")) {
			config.set("general.raceGracePeriod", 10.0);
		}
		if (!config.contains("general.race.timed.log")) {
			config.set("general.race.timed.log", true);
		}
		if (!config.contains("general.race.maxTimePerCheckpoint")) {
			config.set("general.race.maxTimePerCheckpoint", 60);
		}
		if (!config.contains("general.race.enableTimeLimit")) {
			config.set("general.race.enableTimeLimit", true);
		}
		if (!config.contains("general.race.targetPlayers")) {
			config.set("general.race.targetPlayers", 5);
		}
		if (!config.contains("general.race.rewards.enable")) {
			config.set("general.race.rewards.enable", true);
		}
		if (!config.contains("general.race.rewards.win")) {
			config.set("general.race.rewards.win", 10.0);
		}
		if (!config.contains("general.race.rewards.second")) {
			config.set("general.race.rewards.second", 5.0);
		}
		if (!config.contains("general.race.rewards.third")) {
			config.set("general.race.rewards.third", 2.0);
		}
		if (!config.contains("general.race.rewards.currency")) {
			config.set("general.race.rewards.currency", "Dollars");
		}
		if (!config.contains("general.race.music.enable")) {
			config.set("general.race.music.enable", true);
		}
		if (!config.contains("general.ensureEqualCarSpeed")) {
			config.set("general.ensureEqualCarSpeed", true);
		}
		if (!config.contains("race.que.minPlayers")) {
			config.set("race.que.minPlayers", 2);
		}
		if (!config.contains("general.optimiseAtRuntime")) {
			config.set("general.optimiseAtRuntime", true);
		}
		// Setup the colour scheme
		if (!config.contains("colorScheme.success")) {
			config.set("colorScheme.success", "&a");
		}
		if (!config.contains("colorScheme.error")) {
			config.set("colorScheme.error", "&4");
		}
		if (!config.contains("colorScheme.info")) {
			config.set("colorScheme.info", "&6");
		}
		if (!config.contains("colorScheme.title")) {
			config.set("colorScheme.title", "&9");
		}
		if (!config.contains("colorScheme.tp")) {
			config.set("colorScheme.tp", "&b");
		}
	}
}
