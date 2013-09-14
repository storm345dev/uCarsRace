package net.stormdev.ucars.race;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.stormdev.ucars.utils.RaceFinishEvent;
import net.stormdev.ucars.utils.RaceStartEvent;
import net.stormdev.ucars.utils.RaceTrack;
import net.stormdev.ucars.utils.RaceUpdateEvent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class Race {
	public List<String> players = new ArrayList<String>();
	public List<String> inplayers = new ArrayList<String>();
	private String gameId = "";
	private RaceTrack track = null;
	private String trackName = "";
	private String winner = "Unknown";
	public Boolean running = false;
	private BukkitTask task = null;
	public Map<String, ItemStack[]> oldInventories = new HashMap<String, ItemStack[]>();
	public Race(RaceTrack track, String trackName){
		this.gameId = UUID.randomUUID().toString();
		this.track = track;
		this.trackName = trackName;
	}
	public void setOldInventories(Map<String, ItemStack[]> inventories){
		this.oldInventories = inventories;
		return;
	}
	public Map<String, ItemStack[]> getOldInventories(){
		return this.oldInventories;
	}
    public List<String> getInPlayers(){
    	return this.inplayers;
    }
    public void setInPlayers(List<String> in){
    	this.inplayers = in;
    	return;
    }
    public void playerOut(String name){
    	if(this.inplayers.contains(name)){
    	this.inplayers.remove(name);
    	}
    }
	public Boolean join(String playername){
		if(players.size() < this.track.getMaxPlayers()){
			players.add(playername);
			return true;
		}
		return false;
	}
	public void leave(String playername){
		this.getPlayers().remove(playername);
		this.playerOut(playername);
		Player player = main.plugin.getServer().getPlayer(playername);
		if(player != null){
			player.getInventory().clear();
		}
		if(this.getOldInventories().containsKey(playername)){
			if(player != null){
		player.getInventory().setContents(this.getOldInventories().get(playername));
			}
		this.getOldInventories().remove(playername);
		}
		if(player != null){
			player.setGameMode(GameMode.SURVIVAL);
			player.teleport(player.getWorld().getSpawnLocation());
			player.sendMessage(ChatColor.GOLD+"Successfully quit the race!");
			}
		for(String playerName:this.getPlayers()){
			if(main.plugin.getServer().getPlayer(playerName) != null && main.plugin.getServer().getPlayer(playerName).isOnline()){
				Player p=main.plugin.getServer().getPlayer(playerName);
				p.sendMessage(ChatColor.GOLD+playername+" quit the race!");
			}
		}
		return;
	}
	public Boolean isEmpty(){
		if(this.players.size() < 1){
			return true;
		}
		return false;
	}
	public String getGameId(){
		return this.gameId;
	}
	public String getTrackName(){
		return this.trackName;
	}
	public RaceTrack getTrack(){
		return this.track;
	}
    public List<String> getPlayers(){
    	return this.players;
    }
    public void setWinner(String winner){
    	this.winner = winner;
    	return;
    }
    public String getWinner(){
    	return this.winner;
    }
    public Boolean getRunning(){
    	return this.running;
    }
    public void start(){
    	this.running = true;
    	final Race game = this;
    	this.task = main.plugin.getServer().getScheduler().runTaskTimer(main.plugin, new Runnable(){

			public void run() {
				RaceUpdateEvent event = new RaceUpdateEvent(game);
				main.plugin.getServer().getPluginManager().callEvent(event);
				return;
			}}, 30l, 30l);
    	main.plugin.getServer().getPluginManager().callEvent(new RaceStartEvent(this));
    }
    public void end(){
    	this.running = false;
    	if(task != null){
    		task.cancel();
    	}
    	main.plugin.getServer().getPluginManager().callEvent(new RaceFinishEvent(this));
    }
}
