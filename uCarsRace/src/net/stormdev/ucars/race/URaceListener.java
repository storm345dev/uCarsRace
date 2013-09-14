package net.stormdev.ucars.race;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.utils.RaceFinishEvent;
import net.stormdev.ucars.utils.RaceQue;
import net.stormdev.ucars.utils.RaceStartEvent;
import net.stormdev.ucars.utils.RaceUpdateEvent;
import net.stormdev.ucars.utils.TrackCreator;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.useful.ucars.ItemStackFromId;

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
	
	//Much extranious PORTED code from here on (Races)
	@EventHandler (priority = EventPriority.HIGHEST)
	void RaceEnd(RaceFinishEvent event){
		Race game = event.getRace();
		if(plugin.gameScheduler.trackInUse(game.getTrackName())){
			plugin.gameScheduler.removeRace(game.getTrackName());
		}
		List<String> players = new ArrayList<String>();
		players.addAll(game.getPlayers());
		List<String> inplayers = game.getInPlayers();
		String in = "";
		for(String inp:inplayers){
			in = in+", "+inp; 
		}
		for(String playername:players){
				Player player = plugin.getServer().getPlayer(playername);
				player.setCustomName(ChatColor.stripColor(player.getCustomName()));
				player.setCustomNameVisible(false);
				Location loc = game.getTrack().getExit(plugin.getServer());
				if(loc == null){
				player.teleport(player.getLocation().getWorld().getSpawnLocation());
				}
				else{
					player.teleport(loc);
				}
					player.getInventory().clear();
					player.setHealth(0);
					List<String> inners = new ArrayList<String>();
					inners.addAll(inplayers);
					for(String tplayername:inners){
						if(tplayername != game.getWinner()){
							inplayers.remove(tplayername);
						}
					}
				if(player.isOnline()){
					if(!inplayers.contains(playername)){
						player.getInventory().clear();
						if(game.getOldInventories().containsKey(player.getName())){
							player.getInventory().setContents(game.getOldInventories().get(player.getName()));
						}
					}
					player.sendMessage(main.colors.getInfo()+game.getWinner()+main.msgs.get("race.end.won"));
				}
				player.getInventory().clear();
				if(game.getOldInventories().containsKey(player.getName())){
				player.getInventory().setContents(game.getOldInventories().get(player.getName()));
				}
		}
		plugin.gameScheduler.reCalculateQues();
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
			game.leave(player.getName());
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
			game.leave(player.getName());
			return;
	    }
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
				Player player = plugin.getServer().getPlayer(pname);
				plugin.gameScheduler.updateGame(game);
				if(game.lapsLeft.containsKey(pname)){
					game.lapsLeft.put(pname, game.totalLaps);
				}
			}
		plugin.gameScheduler.reCalculateQues();
		return;
	}
	@EventHandler
	void RaceHandler(RaceUpdateEvent event){
		Race game = event.getRace();
		if(!game.getRunning()){
			plugin.gameScheduler.stopGame(game.getTrack(), game.getTrackName());
			plugin.gameScheduler.reCalculateQues();
			return;
		}
		
		plugin.gameScheduler.updateGame(game);
	}
}
