package net.stormdev.ucars.race;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.stormdev.mariokartAddons.KartAction;
import net.stormdev.mariokartAddons.Powerup;
import net.stormdev.ucars.utils.CheckpointCheck;
import net.stormdev.ucars.utils.RaceEndEvent;
import net.stormdev.ucars.utils.RaceFinishEvent;
import net.stormdev.ucars.utils.RaceQue;
import net.stormdev.ucars.utils.RaceStartEvent;
import net.stormdev.ucars.utils.RaceUpdateEvent;
import net.stormdev.ucars.utils.TrackCreator;
import net.stormdev.ucars.utils.ValueComparator;
import net.stormdev.ucars.utils.shellUpdateEvent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class URaceListener implements Listener {
	main plugin = null;
	public URaceListener(main plugin){
		this.plugin = plugin;
	}
	public void penalty(final Minecart car, long time){
		if(car.hasMetadata("kart.immune")){
			return;
		}
		double power = (time/2);
		car.setMetadata("car.frozen", new StatValue(time, plugin));
		car.setVelocity(new Vector(0,power,0));
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

			public void run() {
				car.removeMetadata("car.frozen", plugin);
			}}, (time*20));
		return;
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
	@EventHandler (priority = EventPriority.MONITOR)
	void powerups(ucarUpdateEvent event){
		Player player = (Player) event.getVehicle().getPassenger();
		if(plugin.raceMethods.inAGame(player.getName())==null){
			return;
		}
	    KartAction action = main.marioKart.calculate(player, event);
	    if(action == null){
	    	return;
	    }
	    if(action.getAction()==net.stormdev.mariokartAddons.Action.UNKNOWN){
	    	return;
	    }
	    //TODO perform action
	    return;
	}
	@EventHandler (priority = EventPriority.LOWEST)
	void trackingShells(shellUpdateEvent event){
		//TODO work out why causing crashes
		//if target is null then green shell
				String targetName = event.getTarget();
				if(targetName != null){
					final Entity shell = event.getShell();
					Location shellLoc = shell.getLocation();
					int sound = 0;
			        if(shell.hasMetadata("shell.sound")){
						sound = (Integer) ((StatValue)shell.getMetadata("shell.sound").get(0)).getValue();
					}
			        if(sound < 1){
			        	shellLoc.getWorld().playSound(shellLoc, Sound.NOTE_PLING, 1.25f, 1.25f);
			        	sound = 8;
			        	shell.removeMetadata("shell.sound", plugin);
			        	shell.setMetadata("shell.sound", new StatValue(sound, plugin));
			        }
			        else{
			        	sound--;
			        	shell.removeMetadata("shell.sound", plugin);
			        	shell.setMetadata("shell.sound", new StatValue(sound, plugin));
			        }
					final Player target = plugin.getServer().getPlayer(targetName);
					Location targetLoc = target.getLocation();
					double x = targetLoc.getX()-shellLoc.getX();
					double z = targetLoc.getZ()-shellLoc.getZ();
					double speed = 1.2;
					Boolean ux = true;
					double px = Math.abs(x);
					double pz = Math.abs(z);
					if(px > pz){
						ux = false;
					}
					Vector vel = new Vector(x, 0, z);
					if(ux){
						//x is smaller
						long mult = (long) (pz/speed);
						vel = vel.divide(new Vector(mult,1,mult));
					}
					else{
						//z is smaller
						long mult = (long) (px/speed);
						vel = vel.divide(new Vector(mult,1,mult));
					}
					shell.setVelocity(vel);
					if(pz < 1 && px < 1){
								String msg = main.msgs.get("mario.hit");
								msg = msg.replaceAll(Pattern.quote("%name%"), "tracking shell");
								target.getLocation().getWorld().playSound(target.getLocation(), Sound.ENDERDRAGON_HIT, 1, 0.8f);
								target.sendMessage(ChatColor.RED+msg);
								penalty(((Minecart)target.getVehicle()), 4);
								shell.setMetadata("shell.destroy", new StatValue(0, plugin));
								return;
					}
				    return;
				}
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
				try {
					if(game.getWinner().equals(pname)){
						score = score+1;
					}
				} catch (Exception e) {
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
				toCheck = new Integer[]{0};
			}
			else{
				toCheck = new Integer[]{(old+1)};
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
	@EventHandler
	void damage(EntityDamageEvent event){
		if(!(event.getEntityType() == EntityType.MINECART)){
		    return;	
		}
		if(!(event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION)){
			return;
		}
		if(!ucars.listener.isACar((Minecart) event.getEntity())){
			return;
		}
		if(plugin.raceMethods.inAGame(((Player) event.getEntity().getPassenger()).getName()) == null && !(event.getEntity().hasMetadata("kart.immune"))){
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
	}
	@EventHandler
	void exploder(EntityExplodeEvent event){
		if(!main.config.getBoolean("mariokart.enable")){
			return;
		}
		if(event.getEntity() == null){
			return;
		}
		if(event.getEntity().hasMetadata("explosion.none")){
			Location loc = event.getEntity().getLocation();
			event.setCancelled(true);
			event.getEntity().remove();
			double radius = 6;
			loc.getWorld().createExplosion(loc, 0);
			Double x = (double) radius;
			Double y = (double) radius;
			Double z = (double) radius;
			List<Entity> near = event.getEntity().getNearbyEntities(x, y, z);
			
			Object[] entarray = near.toArray();
			
				Entity listent;
				
				

				for (Object s  : entarray)
				{
				    listent = (Entity) s;
				    EntityType type = listent.getType();
				    if(type == EntityType.MINECART){
				    	if(ucars.listener.isACar((Minecart) listent)){
				    		((Minecart) listent).setDamage(0);
				    		penalty((Minecart) listent, 4);
				    	}
				    }
				}
				
		}
	}
	@EventHandler
	void signClicker(PlayerInteractEvent event){
		KartAction action = main.marioKart.calculate(event.getPlayer(), event);
		//TODO execute basic on kartAction
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
			return;
		}
		if(!(event.getClickedBlock().getState() instanceof Sign)){
			return;
		}
		Sign sign = (Sign) event.getClickedBlock().getState();
		String[] lines = sign.getLines();
		if(!ChatColor.stripColor(lines[0]).equalsIgnoreCase("[uRace]")){
			return;
		}
		String cmd = ChatColor.stripColor(lines[1]);
		if(cmd.equalsIgnoreCase("list")){
			int page = 1;
			try {
				page = Integer.parseInt(ChatColor.stripColor(lines[2]));
			} catch (NumberFormatException e) {
			}
			main.cmdExecutor.urace(event.getPlayer(), new String[]{"list", ""+page}, event.getPlayer());
		}
		else if(cmd.equalsIgnoreCase("leave") || cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")){
			main.cmdExecutor.urace(event.getPlayer(), new String[]{"leave"}, event.getPlayer());
		}
		else if(cmd.equalsIgnoreCase("join")){
			main.cmdExecutor.urace(event.getPlayer(), new String[]{"join", ChatColor.stripColor(lines[2]).toLowerCase()}, event.getPlayer());
		}
		return;
	}
	@EventHandler
	void signWriter(SignChangeEvent event){
		String[] lines = event.getLines();
		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[uRace]")){
			lines[0] = main.colors.getTitle() + "[uRace]";
			Boolean text = true;
			String cmd = ChatColor.stripColor(lines[1]);
			if(cmd.equalsIgnoreCase("list")){
				lines[1] = main.colors.getInfo()+"List";
				if(!(lines[2].length() < 1)){
					text = false;
				}
				lines[2] = main.colors.getSuccess()+ChatColor.stripColor(lines[2]);
			}
			else if(cmd.equalsIgnoreCase("join")){
				lines[1] = main.colors.getInfo()+"Join";
				lines[2] = main.colors.getSuccess()+ChatColor.stripColor(lines[2]);
				if(lines[2].equalsIgnoreCase("auto")){
				   lines[2] = main.colors.getTp() + "Auto";	
				}
				text = false;
			}
			else if(cmd.equalsIgnoreCase("leave") || cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit")){
				char[] raw = cmd.toCharArray();
				if(raw.length > 1){
				String start = ""+raw[0];
				start = start.toUpperCase();
				String body = "";
				for(int i=1;i<raw.length;i++){
					body = body+raw[i];
				}
				body = body.toLowerCase();
				cmd = start+body;
			    }
				lines[1] = main.colors.getInfo() + cmd;
			}
			else{
				text = false;
			}
			if(text){
				lines[2] = ChatColor.ITALIC + "Right click";
				lines[3] = ChatColor.ITALIC + "to use";
			}
		}
	}
	@EventHandler
	void playerDeathEvent(PlayerDeathEvent event){
		Player player = event.getEntity();
		if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
	    if(!(player.getVehicle() == null)){
		player.getVehicle().eject();
        player.getVehicle().remove();
	    }
	    List<MetadataValue> metas = null;
		if(player.hasMetadata("car.stayIn")){
			metas = player.getMetadata("car.stayIn");
			for(MetadataValue val:metas){
				player.removeMetadata("car.stayIn", val.getOwningPlugin());
			}
		}
	    return;
	}
	@EventHandler
	void playerRespawnEvent(PlayerRespawnEvent event){
		final Player player = event.getPlayer();
		if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
		Race race = plugin.raceMethods.inAGame(player.getName());
		int checkpoint = 0;
		try {
			checkpoint = race.checkpoints.get(player.getName());
		} catch (Exception e) {
		}
		final Location loc = race.getTrack().getCheckpoints().get(checkpoint).getLocation(plugin.getServer()).add(0, 2, 0);
	    plugin.getServer().getScheduler().runTask(plugin, new Runnable(){

			public void run() {
				player.teleport(loc);
			}});
		Minecart cart = (Minecart) loc.getWorld().spawnEntity(loc, EntityType.MINECART);
	    cart.setPassenger(player);
	    player.setMetadata("car.stayIn", new StatValue(null, plugin));
	    return;
	}
	@EventHandler
	void blockBreak(BlockBreakEvent event){
	    Player player = event.getPlayer();
	    if(plugin.raceMethods.inAGame(player.getName()) == null){
			return;
		}
	    event.setCancelled(true);
	    return;
	}
}
