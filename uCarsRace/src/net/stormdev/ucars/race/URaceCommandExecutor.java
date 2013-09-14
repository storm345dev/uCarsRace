package net.stormdev.ucars.race;

import java.util.regex.Pattern;

import net.stormdev.ucars.utils.RaceTrack;
import net.stormdev.ucars.utils.TrackCreator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class URaceCommandExecutor implements CommandExecutor {
	main plugin = null;
	public URaceCommandExecutor(main plugin){
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}
		if(cmd.getName().equalsIgnoreCase("urace")){
			if(args.length < 1){
				return false;
			}
			String command = args[0];
			if(command.equalsIgnoreCase("create")){
				// /urace create [TrackName]
				if(player == null){
					sender.sendMessage(main.colors.getError()+main.msgs.get("general.cmd.playersOnly"));
					return true;
				}
				if(args.length < 2){
					return false;
				}
				String trackname = args[1];
				if(plugin.trackManager.raceTrackExists(trackname)){
					String msg = main.msgs.get("setup.create.exists");
					msg = msg.replaceAll(Pattern.quote("%name%"), trackname);
					sender.sendMessage(main.colors.getError()+msg);
					return true;
				}
				int id = main.config.getInt("setup.create.wand");
				ItemStack named = new ItemStack(id);
				String start = main.msgs.get("setup.create.start");
				start = start.replaceAll(Pattern.quote("%id%"), ""+id);
				start = start.replaceAll(Pattern.quote("%name%"), named.getType().name().toLowerCase());
			    sender.sendMessage(main.colors.getInfo()+start);
			    RaceTrack track = new RaceTrack(trackname, 2, 2);
			    new TrackCreator(player, track); //Create the track
			}
			else if(command.equalsIgnoreCase("delete")){
				if(args.length < 2){
					return false;
				}
				String trackname = args[1];
				if(!plugin.trackManager.raceTrackExists(trackname)){
					sender.sendMessage(main.colors.getError()+main.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				plugin.trackManager.deleteRaceTrack(trackname);
				String msg = main.msgs.get("general.cmd.delete.success");
				msg = msg.replaceAll("%name%", trackname);
				return true;
			}
			//TODO
			return true;
		}
		
		return false;
	}

}
