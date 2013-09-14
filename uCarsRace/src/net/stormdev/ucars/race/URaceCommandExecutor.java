package net.stormdev.ucars.race;

import java.util.ArrayList;
import java.util.regex.Pattern;

import net.stormdev.ucars.utils.RaceQue;
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
		if(cmd.getName().equalsIgnoreCase("raceAdmin")){
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
				if(args.length < 3){
					return false;
				}
				String trackname = args[1];
				int laps = 3;
				try {
					laps = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				if(laps < 1){
					laps = 1;
				}
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
			    RaceTrack track = new RaceTrack(trackname, 2, 2, laps);
			    new TrackCreator(player, track); //Create the track
			    return true;
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
				sender.sendMessage(main.colors.getSuccess()+msg);
				return true;
			}
			else if(command.equalsIgnoreCase("list")){
				int page = 1;
				if(args.length > 1){
					try {
						page = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						page = 1;
					}
				}
				@SuppressWarnings("unchecked")
				ArrayList<RaceTrack> tracks = (ArrayList<RaceTrack>) plugin.trackManager.getRaceTracks().clone();
				ArrayList<String> names = new ArrayList<String>();
				for(RaceTrack track:tracks){
					names.add(track.getTrackName());
				}
				double total = names.size() / 6;
				int totalpages = (int) Math.ceil(total);
				int pos = (page-1) * 6;
				if(page > totalpages){
					page = totalpages;
				}
				if(pos > names.size()){
					pos = names.size() - 5;
				}
				if(pos < 0){
					pos = 0;
				}
				if(page < 0){
					page = 0;
				}
				String msg = main.msgs.get("general.cmd.page");
				msg = msg.replaceAll(Pattern.quote("%page%"), ""+(page+1));
				msg = msg.replaceAll(Pattern.quote("%total%"), ""+(totalpages+1));
				sender.sendMessage(main.colors.getTitle()+msg);
				for(int i=pos;i<(i+6)&&i<names.size();i++){
					String Trackname = names.get(i);
					char[] chars = Trackname.toCharArray();
					if(chars.length >= 1){
						String s = ""+chars[0];
						s = s.toUpperCase();
						Trackname = s + Trackname.substring(1);
					}
					sender.sendMessage(main.colors.getInfo()+Trackname);
				}
				return true;
			}
			//TODO
			return false;
		}
		else if(cmd.getName().equalsIgnoreCase("urace")){
			if(args.length < 1){
				return false;
			}
			String command = args[0];
			if(command.equalsIgnoreCase("list")){
				int page = 1;
				if(args.length > 1){
					try {
						page = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						page = 1;
					}
				}
				@SuppressWarnings("unchecked")
				ArrayList<RaceTrack> tracks = (ArrayList<RaceTrack>) plugin.trackManager.getRaceTracks().clone();
				ArrayList<String> names = new ArrayList<String>();
				for(RaceTrack track:tracks){
					names.add(track.getTrackName());
				}
				double total = names.size() / 6;
				int totalpages = (int) Math.ceil(total);
				int pos = (page-1) * 6;
				if(page > totalpages){
					page = totalpages;
				}
				if(pos > names.size()){
					pos = names.size() - 5;
				}
				if(pos < 0){
					pos = 0;
				}
				if(page < 0){
					page = 0;
				}
				String msg = main.msgs.get("general.cmd.page");
				msg = msg.replaceAll(Pattern.quote("%page%"), ""+(page+1));
				msg = msg.replaceAll(Pattern.quote("%total%"), ""+(totalpages+1));
				sender.sendMessage(main.colors.getTitle()+msg);
				for(int i=pos;i<(i+6)&&i<names.size();i++){
					String Trackname = names.get(i);
					char[] chars = Trackname.toCharArray();
					if(chars.length >= 1){
						String s = ""+chars[0];
						s = s.toUpperCase();
						Trackname = s + Trackname.substring(1);
					}
					sender.sendMessage(main.colors.getInfo()+Trackname);
				}
				return true;
			}
			else if(command.equalsIgnoreCase("join")){
				if(player == null){
					sender.sendMessage(main.colors.getError()+main.msgs.get("general.cmd.playersOnly"));
					return true;
				}
				if(args.length < 2){
					return false;
				}
				String trackName = args[1];
				RaceTrack track = plugin.trackManager.getRaceTrack(trackName);
				if(track == null){
			    sender.sendMessage(main.colors.getError()+main.msgs.get("general.cmd.delete.exists"));
				return true;	
				}
				RaceQue que = new RaceQue(track);
				trackName = track.getTrackName();
				if(main.plugin.ques.containsKey(trackName)){
					que = main.plugin.ques.get(trackName);
				}
				if(main.plugin.raceMethods.inAGame(player.getName())!=null || main.plugin.raceMethods.inGameQue(player.getName())!=null){
					sender.sendMessage(main.colors.getError()+main.msgs.get("race.que.existing"));
					return true;
				}
				main.plugin.gameScheduler.joinGame(player.getName(), track, que, trackName);
				return true;
			}
			return false;
		}
		
		return false;
	}

}
