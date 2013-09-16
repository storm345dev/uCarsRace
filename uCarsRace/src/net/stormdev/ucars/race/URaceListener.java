package net.stormdev.ucars.race;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.stormdev.ucars.utils.CheckpointCheck;
import net.stormdev.ucars.utils.RaceEndEvent;
import net.stormdev.ucars.utils.RaceFinishEvent;
import net.stormdev.ucars.utils.RaceQue;
import net.stormdev.ucars.utils.RaceStartEvent;
import net.stormdev.ucars.utils.RaceUpdateEvent;
import net.stormdev.ucars.utils.TrackCreator;
import net.stormdev.ucars.utils.ValueComparator;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import com.useful.ucars.ucars;

public class URaceListener implements Listener {
	main plugin = null;
	public URaceListener(main plugin){
		this.plugin = plugin;
	}
	@EventHandler
	public void onWandClickEvent(PlayerInteractEvent event){
		if(!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			return;
		}
		Player player = event.getPlayer();
		if(!main.trackCreators.containsKey(player.getName())){
			return;
		}
		TrackCreator creator = main.trackCreators.get(player.getName());
		Boolean wand = false;
		int handid = player.getItemInHand().getTypeId();
		if(handid == main.config.getInt("setup.create.wand")){
			wand = true;
		}
		creator.set(wand);
		return;
	}
	@EventHandler (priority = EventPriority.HIGHEST)
	void RaceEnd(RaceEndEvent event){
		Race game = event.getRace();
		game.running = false;
		if(plugin.gameScheduler.trackInUse(game.getTrackName())){
			plugin.gameScheduler.removeRace(game.getTrackName());
		}
		try {
			plugin.gameScheduler.stopGame(game.getTrack(), game.getGameId());
		} catch (Exception e) {
		}
		plugin.gameScheduler.reCalculateQues();
	}
	
	//Much extranious PORTED code from here on (Races)
	@EventHandler (priority = EventPriority.HIGHEST)
	void RaceEnd(RaceFinishEvent event){
		Race game = event.getRace();
		List<String> players = new ArrayList<String>();
		players.addAll(game.getPlayers());
		List<String> inplayers = game.getInPlayers();
		String in = "";
		for(String inp:inplayers){
			in = in+", "+inp; 
		}
		Map<String,Integer> scores = new HashMap<String,Integer>();
		Boolean finished = false;
		String playername = event.getPlayername();
				Player player = plugin.getServer().getPlayer(playername);
				player.removeMetadata("car.stayIn", plugin);
				player.setCustomName(ChatColor.stripColor(player.getCustomName()));
				player.setCustomNameVisible(false);
				if(player.getVehicle()!=null){
					Vehicle veh = (Vehicle) player.getVehicle();
					veh.eject();
					veh.remove();
				}
				Location loc = game.getTrack().getExit(plugin.getServer());
				if(loc == null){
				player.teleport(player.getLocation().getWorld().getSpawnLocation());
				}
				else{
					player.teleport(loc);
				}
				if(player.isOnline()){
						player.getInventory().clear();
						if(game.getOldInventories().containsKey(player.getName())){
							player.getInventory().setContents(game.getOldInventories().get(player.getName()));
						}
				}
				if(game.finished.contains(playername)){
					finished = true;
				}
				else{
				for(String pname:game.getPlayers()){
				int laps = game.totalLaps - game.lapsLeft.get(pname) +1;
				int checkpoints;
				try {
					checkpoints = game.checkpoints.get(pname);
				} catch (Exception e) {
					checkpoints = 0;
				}
				int score = (laps*game.getMaxCheckpoints()) + checkpoints;
				if(game.getWinner().equals(pname)){
					score = score+1;
				}
				scores.put(pname, score);
				}
				}
				player.getInventory().clear();
				if(game.getOldInventories().containsKey(player.getName())){
				player.getInventory().setContents(game.getOldInventories().get(player.getName()));
				}
		if(!finished){
		ValueComparator com = new ValueComparator(scores);
    	SortedMap<String, Integer> sorted = new TreeMap<String, Integer>(com);
		sorted.putAll(scores);
    	Set<String> keys = sorted.keySet();
		Object[] pls = (Object[]) keys.toArray();
    	for(int i=0;i<pls.length;i++){
			Player p = plugin.getServer().getPlayer((String) pls[i]);
			if(p.getName().equals(event.getPlayername())){
			if(p!=null){
				String msg = main.msgs.get("race.end.position");
				String pos = ""+(i+1);
				if(pos.endsWith("1")){
					pos = pos+"st";
				}
				else if(pos.endsWith("2")){
					pos = pos+"nd";
				}
				else if(pos.endsWith("3")){
					pos = pos+"rd";
				}
				else {
					pos = pos+"th";
				}
				msg = msg.replaceAll("%position%", ""+pos);
				p.sendMessage(main.colors.getSuccess()+msg);
			}
			}
    	}
		}
		else{
			Player p = plugin.getServer().getPlayer((String) event.getPlayername());
			if(p!=null){
			int position = 1;
			ArrayList<String> fs = new ArrayList<String>();
			fs.addAll(game.finished);
			for(int i=0;i<fs.size();i++){
				if(fs.get(i).equals(event.getPlayername())){
					position = i+1;
				}
			}
			String msg = main.msgs.get("race.end.position");
			String pos = ""+position;
			if(pos.endsWith("1")){
				pos = pos+"st";
			}
			else if(pos.endsWith("2")){
				pos = pos+"nd";
			}
			else if(pos.endsWith("3")){
				pos = pos+"rd";
			}
			else {
				pos = pos+"th";
			}
			msg = msg.replaceAll("%position%", ""+pos);
			p.sendMessage(main.colors.getSuccess()+msg);
			}
		}
		game.leave(event.getPlayername(), false);
		plugin.gameScheduler.updateGame(game);
		if(game.getInPlayers().size() < 1){
			game.ended = true;
			game.end();
		}
		return;
	}
	@EventHandler
	void gameQuitting(PlayerQuitEvent event){
		Player player = event.getPlayer();
		Race game = plugin.raceMethods.inAGame(player.getName());
		if(game == null){
			String arenaName = plugin.raceMethods.inGameQue(player.getName());
			if(arenaName == null){
				return;
			}
			RaceQue arena = plugin.raceQues.getQue(arenaName);
			arena.removePlayer(player.getName());
			plugin.raceQues.setQue(arenaName, arena);
			return;
		}
		else{
			game.leave(player.getName(), true);
			return;
	    }
	}
	@EventHandler
	void gameQuitting(PlayerKickEvent event){
		Player player = event.getPlayer();
		Race game = plugin.raceMethods.inAGame(player.getName());
		if(game == null){
			String arenaName = plugin.raceMethods.inGameQue(player.getName());
			if(arenaName == null){
				return;
			}
			RaceQue arena = plugin.raceQues.getQue(arenaName);
			arena.removePlayer(player.getName());
			plugin.raceQues.setQue(arenaName, arena);
			return;
		}
		else{
			game.leave(player.getName(), true);
			return;
	    }
	}
	@EventHandler
	void stayInCar(VehicleExitEvent event){
		if(!(event.getVehicle() instanceof Minecart)){
			return;
		}
		Minecart car = (Minecart) event.getVehicle();
		if(!(event.getExited() instanceof Player)){
			return;
		}
		Player player = (Player) event.getExited();
		if(!(player.hasMetadata("car.stayIn"))){
			return;
		}
		if(!ucars.listener.isACar(car)){
			return;
		}
		event.setCancelled(true);
	}
	@EventHandler (priority = EventPriority.HIGHEST)
	void RaceStart(RaceStartEvent event){
		Race game = event.getRace();
		List<String> players = game.getPlayers();
		for(String pname:players){
			plugin.getServer().getPlayer(pname).setGameMode(GameMode.SURVIVAL);
			plugin.getServer().getPlayer(pname).getInventory().clear();
		}
			for(int i=0;i<game.getPlayers().size();i++){
				String pname = game.getPlayers().get(i);
				plugin.gameScheduler.updateGame(game);
				if(game.lapsLeft.containsKey(pname)){
					game.lapsLeft.put(pname, game.totalLaps);
				}
				if(game.checkpoints.containsKey(pname)){
					game.checkpoints.put(pname, 0);
				}
				String msg = main.msgs.get("race.mid.lap");
				msg = msg.replaceAll(Pattern.quote("%lap%"), ""+1);
				msg = msg.replaceAll(Pattern.quote("%total%"), ""+game.totalLaps);
			    plugin.getServer().getPlayer(pname).sendMessage(main.colors.getInfo()+msg);
			}
		plugin.gameScheduler.reCalculateQues();
		return;
	}
	@EventHandler
	void RaceHandler(RaceUpdateEvent event){
		Race game = event.getRace();
		if(!game.getRunning()){
			try {
				plugin.gameScheduler.stopGame(game.getTrack(), game.getTrackName());
			} catch (Exception e) {
			}
			plugin.gameScheduler.reCalculateQues();
			return;
		}
		ArrayList<String> pls = new ArrayList<String>();
		pls.addAll(game.getInPlayers());
		for(String playername:pls){
			Player player = plugin.getServer().getPlayer(playername);
			if(player == null){
				game.leave(playername, true);
			}
			else{
			Location playerLoc = player.getLocation();
			Boolean checkNewLap = false;
			int old = 0;
			try {
				old = game.checkpoints.get(playername);
			} catch (Exception e) {
				old = 0;
			}
			if(old == game.getMaxCheckpoints()){
				checkNewLap = true;
			}
			Integer[] toCheck = new Integer[]{};
			if(checkNewLap){
				toCheck = new Integer[]{0,(old-1)};
			}
			else{
				toCheck = new Integer[]{(old+1),(old-1)};
			}
			CheckpointCheck check = game.playerAtCheckpoint(toCheck, player, plugin.getServer());
			
			if(check.at){ //At a checkpoint
				int ch = check.checkpoint;
				if(ch >=game.getMaxCheckpoints()){
					checkNewLap = true;
				}
				if(!(ch == old)){
				/* Removed to reduce server load - Requires all checkpoints to be checked
				if(ch-2 > old){
					//They missed a checkpoint
					player.sendMessage(main.colors.getError()+main.msgs.get("race.mid.miss"));
					return;
				}
				*/
				if(!(old==0) && !(old==game.getMaxCheckpoints()) && !(ch==0) &&!(ch==game.getMaxCheckpoints())){
				if(old-2 > ch){
					//They are going the wrong way!
					player.sendMessage(main.colors.getError()+main.msgs.get("race.mid.backwards"));
					return;
				}
				}
				if(!(old>=ch)){
					game.checkpoints.put(playername, check.checkpoint);
				}
			  }
			}
			int lapsLeft = 3;
			try {
				lapsLeft = game.lapsLeft.get(playername);
			} catch (Exception e) {
				game.lapsLeft.put(playername, game.totalLaps);
				lapsLeft = game.totalLaps;
			}
			if(lapsLeft < 1 || checkNewLap){
				if(game.atLine(plugin.getServer(), playerLoc)){
					if(checkNewLap){
						int left = game.lapsLeft.get(playername)-1;
						if(left < 0){
							left = 0;
						}
						game.checkpoints.put(playername, 0);
						game.lapsLeft.put(playername, left);
						lapsLeft = left;
						if(left != 0){
							String msg = main.msgs.get("race.mid.lap");
							int lap = game.totalLaps - lapsLeft+1;
							msg = msg.replaceAll(Pattern.quote("%lap%"), ""+lap);
							msg = msg.replaceAll(Pattern.quote("%total%"), ""+game.totalLaps);
						    player.sendMessage(main.colors.getInfo()+msg);
						}
					}
					if(lapsLeft < 1){
					Boolean won = game.getWinner() == null;
					if(won){
					game.setWinner(playername);
					}
					game.finish(playername);
					if(won){
					ArrayList<String> plz = new ArrayList<String>();
					plz.addAll(game.getPlayers());
					for(String pname:plz){
						if(!(plugin.getServer().getPlayer(pname) == null) && !playername.equals(pname)){
							String msg = main.msgs.get("race.end.soon");
							msg = msg.replaceAll("%name%", playername);
							plugin.getServer().getPlayer(pname).sendMessage(main.colors.getSuccess()+game.getWinner()+main.msgs.get("race.end.won"));
							plugin.getServer().getPlayer(pname).sendMessage(main.colors.getInfo()+msg);
						}
					}
					}
					}
				}
			  }
			}
		}
		plugin.gameScheduler.updateGame(game);
	}
}
