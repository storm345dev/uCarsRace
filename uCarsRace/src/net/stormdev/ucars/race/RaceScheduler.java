package net.stormdev.ucars.race;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.stormdev.ucars.utils.RaceQue;
import net.stormdev.ucars.utils.RaceTrack;
import net.stormdev.ucars.utils.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.useful.ucarsCommon.StatValue;

/*
 * An adaptation of my Minigamez plugin Arena game scheduler. -Code is messy and 
 * weirdly named as a result
 */
public class RaceScheduler {
	//TODO NOTE: This code is probably highly extraneous in places.
	private HashMap<String, Race> games = new HashMap<String, Race>();
	private main plugin;
	Random random = null;
	public RaceScheduler(){
		this.plugin = main.plugin;
		random = new Random();
	}
	public Boolean joinGame(String playername, RaceTrack track, RaceQue que, String trackName){
		que.validatePlayers();
		if(que.getHowManyPlayers() < que.getPlayerLimit() && plugin.getServer().getPlayer(playername).isOnline()){
			if(plugin.getServer().getPlayer(playername).isOnline()){
				//que.addPlayer(playername);
				List<String> arenaque = que.getPlayers();
				if(!arenaque.contains(playername)){
					que.addPlayer(playername);
					//arenaque.add(playername);
				}
				for(String name:arenaque){
					if(!(plugin.getServer().getPlayer(name).isOnline() && plugin.getServer().getPlayer(name) != null)){
						arenaque.remove(name);
						for(String ppname:arenaque){
							if(plugin.getServer().getPlayer(ppname).isOnline() && plugin.getServer().getPlayer(ppname) != null){
								plugin.getServer().getPlayer(ppname).sendMessage(ChatColor.RED+"["+trackName+":] "+main.colors.getInfo()+playername+main.msgs.get("race.que.left"));
							}
						}
					}
					else{
						plugin.getServer().getPlayer(name).sendMessage(ChatColor.RED+"["+trackName+":] "+main.colors.getInfo()+playername+main.msgs.get("race.que.joined"));
					}
				}
				plugin.raceQues.setQue(trackName, que);
				this.reCalculateQues();
				plugin.getServer().getPlayer(playername).sendMessage(main.colors.getSuccess()+main.msgs.get("race.que.success"));
				plugin.getServer().getPlayer(playername).teleport(track.getLobby(plugin.getServer()));
				return true;
			}
		}
		if(plugin.getServer().getPlayer(playername).isOnline()){
			plugin.getServer().getPlayer(playername).sendMessage(main.colors.getError()+main.msgs.get("race.que.full"));
		}
		return false;
	}
	public void reCalculateQues(){
		Set<String> queNames = plugin.raceQues.getQues();
		for(String aname:queNames){
			RaceQue que = plugin.raceQues.getQue(aname);
			List<String> arenaque = que.getPlayers();
			for(String name:arenaque){
				if(!(plugin.getServer().getPlayer(name).isOnline() && plugin.getServer().getPlayer(name) != null)){
					arenaque.remove(name);
				}
			}
			if(que.getTransitioning() == null){
				que.setTransitioning(false);
			}
			if(!trackInUse(aname) && que.getHowManyPlayers() > 1 && !que.getTransitioning()){
				que.setTransitioning(true);
				plugin.raceQues.setQue(aname, que);
				final String queName = aname;
			    plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

					public void run() {
						String aname = queName;
						RaceQue arena = main.plugin.raceQues.getQue(aname);
						if(arena.getHowManyPlayers() < 2){
							arena.setTransitioning(false);
							plugin.raceQues.setQue(aname, arena);
							return;
						}
						Race game = new Race(arena.getTrack(), arena.getTrack().getTrackName()); //Add new stuff when the system is ready
						List<String> aquep = new ArrayList<String>();
						aquep.addAll(arena.getPlayers());
						for(String name:aquep){
						game.join(name);
						arena.removePlayer(name);
						}
						arena.setTransitioning(false);
						plugin.raceQues.setQue(aname, arena);
						startGame(arena, aname, game);
						return;
					}}, 100l);
				
			}
		}
		return;
	}
	public void startGame(RaceQue que, String trackName, final Race race){
		this.games.put(race.getGameId(), race);
		final List<String> players = race.getPlayers();
		Map<String, ItemStack[]> oldInv = new HashMap<String,ItemStack[]>();
			for(String player:players){
				Player pl = plugin.getServer().getPlayer(player);
				oldInv.put(player,pl.getInventory().getContents());
				pl.getInventory().clear();
				pl.setGameMode(GameMode.SURVIVAL);
				
			}
		final ArrayList<Minecart> cars = new ArrayList<Minecart>();
		race.setOldInventories(oldInv);
		RaceTrack track = race.getTrack();
		ArrayList<SerializableLocation> sgrid = track.getStartGrid();
		HashMap<Integer, Location> grid = new HashMap<Integer, Location>();
		for(int i=0;i<sgrid.size();i++){
			SerializableLocation s = sgrid.get(i);
			grid.put(i, s.getLocation(plugin.getServer()));
		}
		ArrayList<String> assigned = new ArrayList<String>();
		assigned.addAll(players);
		int count = grid.size();
		if(count > assigned.size()){
			count = assigned.size();
		}
		for(int i=0;i<count;i++){
		int min = 0;
		int max = assigned.size();
		if(!(max < 1)){
		int randomNumber = random.nextInt(max - min) + min;
		Player p = plugin.getServer().getPlayer(assigned.get(randomNumber));
		assigned.remove(p);
		Location loc = grid.get(i);
		if(p.getVehicle()!=null){
			p.getVehicle().eject();
		}
		p.teleport(loc.add(0, 2, 0));
		Minecart car = (Minecart) loc.getWorld().spawnEntity(loc.add(0, 0.2, 0), EntityType.MINECART);
		car.setMetadata("car.frozen", new StatValue(null, main.plugin));
		car.setPassenger(p);
		p.setMetadata("car.stayIn", new StatValue(null, plugin));
		cars.add(car);
		}
		}
		final Map<String, Location> locations = new HashMap<String, Location>();
		for(String name:players){
			locations.put(name, plugin.getServer().getPlayer(name).getLocation());
			plugin.getServer().getPlayer(name).sendMessage(main.colors.getInfo()+main.msgs.get("race.que.preparing"));
		}
		List<String> gameIn = new ArrayList<String>();
		gameIn.addAll(race.getPlayers());
		race.setInPlayers(gameIn);
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			public void run() {
				for(String name:players){
					Player p=plugin.getServer().getPlayer(name);
					p.sendMessage(main.colors.getInfo()+main.msgs.get("race.que.starting"));
				}
				for(int i=10;i>0;i--){
				for(String name:players){
				Player p=plugin.getServer().getPlayer(name);
				p.sendMessage(main.colors.getInfo()+""+i);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				}
				for(Minecart car:cars){
					car.removeMetadata("car.frozen", main.plugin);
				}
				for(String name:players){
					Player p=plugin.getServer().getPlayer(name);
					p.sendMessage(main.colors.getInfo()+main.msgs.get("race.que.go"));
					}
				race.start();
				return;
			}});
		
		return;
	}
	public void updateGame(Race game){
		this.games.put(game.getGameId(), game);
		return;
	}
	public void stopGame(RaceTrack track, String gameId) throws Exception{
		if(!trackInUse(track.getTrackName())){
			return;
		}
		removeRace(gameId);
		reCalculateQues();
		return;
	}
	public void leaveQue(String playername, RaceQue arena, String arenaName){
		if(getQue(arena).contains(playername)){
			arena.removePlayer(playername);
		}
		for(String ppname:getQue(arena)){
			if(plugin.getServer().getPlayer(ppname).isOnline() && plugin.getServer().getPlayer(ppname) != null){
				plugin.getServer().getPlayer(ppname).sendMessage(ChatColor.RED+"["+arenaName+":] "+main.colors.getInfo()+playername+main.msgs.get("race.que.left"));
			}
		}
		reCalculateQues();
		return;
	}
	public List<String> getQue(RaceQue que){
		return que.getPlayers();
	}
    public Boolean trackInUse(String arenaName){
    	Set<String> keys = this.games.keySet();
    	for(String key:keys){
    		Race game = this.games.get(key);
    		if(game.getTrackName().equalsIgnoreCase(arenaName)){
    			return true;
    		}
    	}
    	return false;
    }
    public Boolean removeRace(String trackName){
    	Set<String> keys = this.games.keySet();
    	for(String key:keys){
    		Race game = this.games.get(key);
    		if(game.getTrackName().equalsIgnoreCase(trackName)){
    			for(String p:game.getPlayers()){
    				Player pl = plugin.getServer().getPlayer(p);
    				pl.removeMetadata("car.stayIn", plugin);
    			}
    			this.games.remove(game.getGameId());
    		}
    	}
    	return false;
    }
    public HashMap<String, Race> getGames(){
    	return this.games;
    }
}
