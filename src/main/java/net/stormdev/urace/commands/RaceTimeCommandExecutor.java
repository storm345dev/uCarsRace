package net.stormdev.urace.commands;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;

import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RaceTimeCommandExecutor implements CommandExecutor {
	private uCarsRace plugin;
	public RaceTimeCommandExecutor(uCarsRace plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("uracetimes")) {
			if (args.length < 2) {
				return false;
			}
			String trackName = args[0];
			String amount = args[1];
			@SuppressWarnings("unchecked")
			List<String> names = (List<String>) plugin.trackManager
					.getRaceTrackNames().clone();
			for (String n : names) {
				if (n.equalsIgnoreCase(trackName)) {
					trackName = n;
				}
			}
			double d = 5;
			try {
				d = Double.parseDouble(amount);
			} catch (NumberFormatException e) {
				return false;
			}
			SortedMap<String, Double> topTimes = plugin.raceTimes.getTopTimes(
					d, trackName);
			Map<String, Double> times = plugin.raceTimes.getTimes(trackName);
			String msg = uCarsRace.msgs.get("general.cmd.racetimes");
			msg = msg.replaceAll(Pattern.quote("%n%"), d + "");
			msg = msg.replaceAll(Pattern.quote("%track%"), trackName);
			sender.sendMessage(uCarsRace.colors.getTitle() + msg);
			Object[] keys = topTimes.keySet().toArray();
			int pos = 1;
			for (Object o : keys) {
				if(pos <= d){
					String name = (String) o;
					sender.sendMessage(uCarsRace.colors.getTitle() + pos + ")"
							+ uCarsRace.colors.getInfo() + name + "- " + times.get(name)
							+ "s");
					pos++;
				}
			}
			return true;
		}
		return false;
	}
}
