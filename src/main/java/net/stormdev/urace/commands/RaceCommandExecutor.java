package net.stormdev.urace.commands;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.urace.players.User;
import net.stormdev.urace.queues.RaceQueue;
import net.stormdev.urace.races.Race;
import net.stormdev.urace.races.RaceType;
import net.stormdev.urace.tracks.RaceTrack;
import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaceCommandExecutor implements CommandExecutor {
	private uCarsRace plugin;
	public RaceCommandExecutor(uCarsRace plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("urace")) {
			return urace(sender, args, player);
		}
		return false;
	}
	
	public Boolean urace(CommandSender sender, String[] args, final Player player) {
		if (args.length < 1) {
			return false;
		}
		String command = args[0];
		if (command.equalsIgnoreCase("list")) {
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
		} else if (command.equalsIgnoreCase("join")) {
			if (player == null) {
				sender.sendMessage(uCarsRace.colors.getError()
						+ uCarsRace.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			String trackName = null;
			if (args.length < 2) {
				trackName = "auto";
			}
			trackName = args[1];
			RaceType type = RaceType.RACE;
			if (player.getVehicle() != null) {
				sender.sendMessage(uCarsRace.colors.getError()
						+ "Cannot execute whilst in a vehicle");
				return true;
			}
			if (trackName.equalsIgnoreCase("auto")) {
				if (uCarsRace.plugin.raceMethods.inAGame(player, false) != null
						|| uCarsRace.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("race.que.existing"));
					return true;
				}
				plugin.raceScheduler.joinAutoQueue(player, type);
				return true;
			} else {
				if (uCarsRace.plugin.raceMethods.inAGame(player, false) != null
						|| uCarsRace.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("race.que.existing"));
					return true;
				}
				RaceTrack track = plugin.trackManager.getRaceTrack(trackName);
				if (track == null) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				uCarsRace.plugin.raceScheduler.joinQueue(player, track, type);
				return true;
			}
		} else if (command.equalsIgnoreCase("queues")
				|| command.equalsIgnoreCase("ques")) {
			int page = 1;
			if (args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					page = 1;
				}
			}
			Map<UUID, RaceQueue> queues = plugin.raceQueues.getAllQueues();
			double total = queues.size() / 6;
			int totalpages = (int) Math.ceil(total);
			int pos = (page - 1) * 6;
			if (page > totalpages) {
				page = totalpages;
			}
			if (pos > queues.size()) {
				pos = queues.size() - 5;
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
			ArrayList<UUID> keys = new ArrayList<UUID>(queues.keySet());
			for (int i = pos; i < (i + 6) && i < queues.size(); i++) {
				UUID id = keys.get(i);
				RaceQueue queue = queues.get(id);
				String trackName = queue.getTrackName();
				ChatColor color = ChatColor.GREEN;
				int playerCount = queue.playerCount();
				if (playerCount > (queue.playerLimit() - 1)) {
					color = ChatColor.RED;
				}
				if (playerCount > (queue.playerLimit() - 2)) {
					color = ChatColor.YELLOW;
				}
				if (playerCount < uCarsRace.config.getInt("race.que.minPlayers")) {
					color = ChatColor.YELLOW;
				}
				char[] chars = trackName.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					trackName = color + s + trackName.substring(1)
							+ uCarsRace.colors.getInfo() + " (" + color
							+ queue.playerCount() + uCarsRace.colors.getInfo() + "/"
							+ queue.playerLimit() + ")" + " ["
							+ queue.getRaceMode().name().toLowerCase() + "]";
				}
				sender.sendMessage(uCarsRace.colors.getInfo() + trackName);
			}
			return true;
		} else if (command.equalsIgnoreCase("leave")) {
			if (player == null) {
				sender.sendMessage(uCarsRace.colors.getError()
						+ uCarsRace.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			uCarsRace.plugin.hotBarManager.clearHotBar(player.getName());
			Boolean game = true;
			Race race = uCarsRace.plugin.raceMethods.inAGame(player, false);
			RaceQueue queue = uCarsRace.plugin.raceMethods.inGameQue(player);
			if (race == null) {
				game = false;
			}
			if (queue == null) {
				if (!game) {
					sender.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("general.cmd.leave.fail"));
					return true;
				}
			}
			if (game) {
				User u = race.getUser(player.getName());
				race.leave(u, true);
				u.clear();
			} else {
				final RaceTrack track = queue.getTrack();
				try {
					uCarsRace.plugin.raceScheduler.leaveQueue(player, queue);
				} catch (Exception e) {
					e.printStackTrace();
					// Player not in a queue
					sender.sendMessage(uCarsRace.colors.getError()
							+ "ERROR occured. Please contact a member of staff.");
					return true;
				}
				String msg = uCarsRace.msgs.get("general.cmd.leave.success");
				msg = msg.replaceAll(Pattern.quote("%name%"),
						queue.getTrackName());
				sender.sendMessage(uCarsRace.colors.getSuccess() + msg);
				player.teleport(track.getExit(uCarsRace.plugin.getServer()));
				player.setBedSpawnLocation(
						track.getExit(uCarsRace.plugin.getServer()), true);
			}
			return true;
		}
		return false;
	}
}
