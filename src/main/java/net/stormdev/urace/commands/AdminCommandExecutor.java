package net.stormdev.urace.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.urace.races.Race;
import net.stormdev.urace.tracks.RaceTrack;
import net.stormdev.urace.tracks.TrackCreator;
import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminCommandExecutor implements CommandExecutor {
	private uCarsRace plugin;
	public AdminCommandExecutor(uCarsRace plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("RaceAdmin")) {
			if (args.length < 1) {
				return false;
			}
			String command = args[0];
			if (command.equalsIgnoreCase("create")) {
				// /urace create [TrackName]
				if (player == null) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("general.cmd.playersOnly"));
					return true;
				}
				if (args.length < 3) {
					return false;
				}
				String trackname = args[1];
				int laps = 3;
				try {
					laps = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				if (laps < 1) {
					laps = 1;
				}
				if (plugin.trackManager.raceTrackExists(trackname)) {
					String msg = uCarsRace.msgs.get("setup.create.exists");
					msg = msg.replaceAll(Pattern.quote("%name%"), trackname);
					sender.sendMessage(uCarsRace.colors.getError() + msg);
					return true;
				}
				int id = uCarsRace.config.getInt("setup.create.wand");
				@SuppressWarnings("deprecation")
				ItemStack named = new ItemStack(id);
				String start = uCarsRace.msgs.get("setup.create.start");
				start = start.replaceAll(Pattern.quote("%id%"), "" + id);
				start = start.replaceAll(Pattern.quote("%name%"), named
						.getType().name().toLowerCase());
				sender.sendMessage(uCarsRace.colors.getInfo() + start);
				RaceTrack track = new RaceTrack(trackname, 2, 2, laps);
				new TrackCreator(player, track); // Create the track
				return true;
			} else if (command.equalsIgnoreCase("delete")) {
				if (args.length < 2) {
					return false;
				}
				String trackname = args[1];
				if (!plugin.trackManager.raceTrackExists(trackname)) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				plugin.trackManager.deleteRaceTrack(trackname);
				String msg = uCarsRace.msgs.get("general.cmd.delete.success");
				msg = msg.replaceAll("%name%", trackname);
				sender.sendMessage(uCarsRace.colors.getSuccess() + msg);
				return true;
			} else if (command.equalsIgnoreCase("list")) {
				int page = 1;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						page = 1;
					}
				}
				@SuppressWarnings("unchecked")
				ArrayList<RaceTrack> tracks = (ArrayList<RaceTrack>) plugin.trackManager
						.getRaceTracks().clone();
				ArrayList<String> names = new ArrayList<String>();
				for (RaceTrack track : tracks) {
					names.add(track.getTrackName());
				}
				double total = names.size() / 6;
				int totalpages = (int) Math.ceil(total);
				int pos = (page - 1) * 6;
				if (page > totalpages) {
					page = totalpages;
				}
				if (pos > names.size()) {
					pos = names.size() - 5;
				}
				if (pos < 0) {
					pos = 0;
				}
				if (page < 0) {
					page = 0;
				}
				String msg = uCarsRace.msgs.get("general.cmd.page");
				msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
				msg = msg.replaceAll(Pattern.quote("%total%"), ""
						+ (totalpages + 1));
				sender.sendMessage(uCarsRace.colors.getTitle() + msg);
				for (int i = pos; i < (i + 6) && i < names.size(); i++) {
					String Trackname = names.get(i);
					char[] chars = Trackname.toCharArray();
					if (chars.length >= 1) {
						String s = "" + chars[0];
						s = s.toUpperCase();
						Trackname = s + Trackname.substring(1);
					}
					sender.sendMessage(uCarsRace.colors.getInfo() + Trackname);
				}
				return true;
			} else if (command.equalsIgnoreCase("races") || command.equalsIgnoreCase("games")) {
				int page = 1;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						page = 1;
					}
				}
				HashMap<UUID, Race> games = plugin.raceScheduler.getRaces();
				ArrayList<String> names = new ArrayList<String>();
				for (Race game:games.values()) {
					names.add(game.getTrackName()+" ("+game.getType().toString().toLowerCase()+")");
				}
				double total = names.size() / 6;
				int totalpages = (int) Math.ceil(total);
				int pos = (page - 1) * 6;
				if (page > totalpages) {
					page = totalpages;
				}
				if (pos > names.size()) {
					pos = names.size() - 5;
				}
				if (pos < 0) {
					pos = 0;
				}
				if (page < 0) {
					page = 0;
				}
				String msg = uCarsRace.msgs.get("general.cmd.page");
				msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
				msg = msg.replaceAll(Pattern.quote("%total%"), ""
						+ (totalpages + 1));
				sender.sendMessage(uCarsRace.colors.getTitle() + msg);
				for (int i = pos; i < (i + 6) && i < names.size(); i++) {
					String Trackname = names.get(i);
					char[] chars = Trackname.toCharArray();
					if (chars.length >= 1) {
						String s = "" + chars[0];
						s = s.toUpperCase();
						Trackname = s + Trackname.substring(1);
					}
					sender.sendMessage(uCarsRace.colors.getInfo() + Trackname);
				}
				return true;
			} else if(command.equalsIgnoreCase("end")){
				if(args.length < 2){
					return false;
				}
				String trackName = args[1];
				if (!plugin.trackManager.raceTrackExists(trackName)) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				HashMap<UUID, Race> games = plugin.raceScheduler.getRaces();
				Race race = null;
				for(Race r:games.values()){
					if(r.getTrackName().equalsIgnoreCase(trackName)){
						race = r;
					}
				}
				if(race == null){
					sender.sendMessage(uCarsRace.colors.getError() + uCarsRace.msgs.get("general.cmd.noRaces"));
					return true;
				}
				race.broadcast(uCarsRace.colors.getTitle()+uCarsRace.msgs.get("general.cmd.forceEnd"));
				race.end();
				sender.sendMessage(uCarsRace.colors.getSuccess()+uCarsRace.msgs.get("general.cmd.endSuccess"));
				return true;
			} else if(command.equalsIgnoreCase("endall")){
				HashMap<UUID, Race> games = plugin.raceScheduler.getRaces();
				for(Race race:games.values()){
					race.broadcast(uCarsRace.colors.getTitle()+uCarsRace.msgs.get("general.cmd.forceEnd"));
					race.end();
				}
				sender.sendMessage(uCarsRace.colors.getSuccess()+uCarsRace.msgs.get("general.cmd.endSuccess"));
				return true;
			} else if (command.equalsIgnoreCase("setLaps")) {
				if (args.length < 3) {
					return false;
				}
				String trackname = args[1];
				if (!plugin.trackManager.raceTrackExists(trackname)) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				String lapsStr = args[2];
				int laps = 3;
				try {
					laps = Integer.parseInt(lapsStr);
				} catch (NumberFormatException e) {
					return false;
				}
				plugin.trackManager.getRaceTrack(trackname).laps = laps;
				plugin.trackManager.save();
				String msg = uCarsRace.msgs.get("general.cmd.setlaps.success");
				msg = msg.replaceAll(Pattern.quote("%name%"),
						plugin.trackManager.getRaceTrack(trackname)
								.getTrackName());
				sender.sendMessage(uCarsRace.colors.getSuccess() + msg);
				return true;
			}
			return false;
		}
		return false;
	}
}
