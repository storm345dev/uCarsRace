package net.stormdev.mariokartAddons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import net.stormdev.ucars.race.Race;
import net.stormdev.ucars.race.main;
import net.stormdev.ucars.utils.ItemStackFromId;
import net.stormdev.ucars.utils.ValueComparator;
import net.stormdev.ucars.utils.shellUpdateEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class MarioKart {
	main plugin = null;
	private HashMap<UUID, BukkitTask> tasks = new HashMap<UUID, BukkitTask>();
	Boolean enabled = true;
	public MarioKart(main plugin){
		this.plugin = plugin;
		enabled = main.config.getBoolean("mariokart.enable");
	}
	@SuppressWarnings("deprecation")
	public KartAction calculate(final Player player, Event event){
		if(!enabled){
			return null;
		}
		if(plugin.raceMethods.inAGame(player.getName()) == null){
			return null;
		}
		KartAction kartAction = new KartAction(false, false, Action.UNKNOWN, new Object[]{});
		Boolean freeze = false;
		Boolean destroy = false;
		Action action = Action.UNKNOWN;
		Object[] args = new Object[]{};
		//Start calculations
		if(event instanceof PlayerInteractEvent){
			PlayerInteractEvent evt = (PlayerInteractEvent) event;
			if(!ucars.listener.inACar(evt.getPlayer())){
				return null;
			}
			final Minecart car = (Minecart) evt.getPlayer().getVehicle();
			if(evt.getAction()==org.bukkit.event.block.Action.RIGHT_CLICK_AIR || evt.getAction()==org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK){
				//If green shell, throw forward
			}
			if(!(evt.getAction()==org.bukkit.event.block.Action.RIGHT_CLICK_AIR || evt.getAction()==org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)){
				return null;
			}
			ItemStack inHand = evt.getPlayer().getItemInHand();
			Player ply = evt.getPlayer();
			if(ItemStackFromId.equals(main.config.getString("mariokart.random"), inHand.getTypeId(), inHand.getDurability())){
				inHand.setAmount(inHand.getAmount()-1);
				evt.getPlayer().getInventory().addItem(this.getRandomPowerup());
				kartAction.action = Action.UNKNOWN; //ignore it
			}
			else if(ItemStackFromId.equals(main.config.getString("mariokart.star"), inHand.getTypeId(), inHand.getDurability())){
				inHand.setAmount(inHand.getAmount()-1);
				car.setMetadata("kart.immune", new StatValue(15000, main.plugin)); //Value = length(millis)
				final String pname = ply.getName();
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

					public void run() {
						Player pl = main.plugin.getServer().getPlayer(pname);
						if(pl!=null){
							car.removeMetadata("kart.immune", main.plugin);
						}
					}}, 300l);
				ucars.listener.carBoost(ply.getName(), 35, 15000, ucars.config.getDouble("general.cars.defSpeed")); //Apply speed boost
			}
			else if(ItemStackFromId.equals(main.config.getString("mariokart.mushroom"), inHand.getTypeId(), inHand.getDurability())){
				inHand.setAmount(inHand.getAmount()-1);
				ucars.listener.carBoost(ply.getName(), 19, 9000, ucars.config.getDouble("general.cars.defSpeed")); //Apply speed boost
			}
			else if(ItemStackFromId.equals(main.config.getString("mariokart.redShell"), inHand.getTypeId(), inHand.getDurability())){
				Race race = plugin.raceMethods.inAGame(player.getName());
				if(race == null){
					return null;
				}
				Map<String, Integer> scores = new HashMap<String, Integer>();
				for(String pname:race.getPlayers()){
					int laps = race.totalLaps - race.lapsLeft.get(pname) +1;
					int checkpoints;
					try {
						checkpoints = race.checkpoints.get(pname);
					} catch (Exception e) {
						checkpoints = 0;
					}
					int score = (laps*race.getMaxCheckpoints()) + checkpoints;
					try {
						if(race.getWinner().equals(pname)){
							score = score+1;
						}
					} catch (Exception e) {
					}
					scores.put(pname, score);
				}
				ValueComparator com = new ValueComparator(scores);
		    	SortedMap<String, Integer> sorted = new TreeMap<String, Integer>(com);
				sorted.putAll(scores);
		    	Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				int ppos = 0;
				for(int i=0;i<pls.length;i++){
					if(pls[i].equals(player.getName())){
						ppos = i;
					}
				}
				int tpos = ppos-1;
				if(tpos < 0){
					tpos = ppos+1;
					if(tpos < 0 || tpos >= pls.length){
					return null;
					}
				}
				final String targetName = (String) pls[tpos];
				inHand.setAmount(inHand.getAmount()-1);
				ItemStack toDrop = ItemStackFromId.get(main.config.getString("mariokart.redShell"));
				final Item shell = player.getLocation().getWorld().dropItem(player.getLocation(), toDrop);
				//DEBUG: final Entity shell = player.getLocation().getWorld().spawnEntity(player.getLocation().add(0, 1.3, 0), EntityType.MINECART_CHEST);
				shell.setPickupDelay(Integer.MAX_VALUE);
				shell.setMetadata("shell.target", new StatValue(targetName, plugin));
				shell.setMetadata("shell.expiry", new StatValue(((Integer)33), plugin));
				BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable(){

					public void run() {
						if(shell.hasMetadata("shell.destroy")){
							shell.remove();
							tasks.get(shell.getUniqueId()).cancel();
							tasks.remove(shell.getUniqueId());
							return;
						}
						List<MetadataValue> metas = shell.getMetadata("shell.expiry");
						int expiry = (Integer) ((StatValue) metas.get(0)).getValue();
						expiry--;
						if(expiry < 0){
							shell.remove();
							tasks.get(shell.getUniqueId()).cancel();
							tasks.remove(shell.getUniqueId());
							return;
						}
						shell.setTicksLived(1);
						shell.setPickupDelay(Integer.MAX_VALUE);
						shell.removeMetadata("shell.expiry", main.plugin);
						shell.setMetadata("shell.expiry", new StatValue(expiry, main.plugin));
						shellUpdateEvent event = new shellUpdateEvent(shell, targetName);
					    main.plugin.getServer().getPluginManager().callEvent(event);
						return;
					}}, 3l, 3l);
				tasks.put(shell.getUniqueId(), task);
			}
			else if(ItemStackFromId.equals(main.config.getString("mariokart.blueShell"), inHand.getTypeId(), inHand.getDurability())){
				Race race = plugin.raceMethods.inAGame(player.getName());
				if(race == null){
					return null;
				}
				Map<String, Integer> scores = new HashMap<String, Integer>();
				for(String pname:race.getPlayers()){
					int laps = race.totalLaps - race.lapsLeft.get(pname) +1;
					int checkpoints;
					try {
						checkpoints = race.checkpoints.get(pname);
					} catch (Exception e) {
						checkpoints = 0;
					}
					int score = (laps*race.getMaxCheckpoints()) + checkpoints;
					try {
						if(race.getWinner().equals(pname)){
							score = score+1;
						}
					} catch (Exception e) {
					}
					scores.put(pname, score);
				}
				ValueComparator com = new ValueComparator(scores);
		    	SortedMap<String, Integer> sorted = new TreeMap<String, Integer>(com);
				sorted.putAll(scores);
		    	Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				final String targetName = (String) pls[0];
				inHand.setAmount(inHand.getAmount()-1);
				ItemStack toDrop = ItemStackFromId.get(main.config.getString("mariokart.blueShell"));
				final Item shell = player.getLocation().getWorld().dropItem(player.getLocation(), toDrop);
				//DEBUG: final Entity shell = player.getLocation().getWorld().spawnEntity(player.getLocation().add(0, 1.3, 0), EntityType.MINECART_CHEST);
				shell.setPickupDelay(Integer.MAX_VALUE);
				shell.setMetadata("shell.target", new StatValue(targetName, plugin));
				shell.setMetadata("shell.expiry", new StatValue(((Integer)66), plugin));
				BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable(){

					public void run() {
						if(shell.hasMetadata("shell.destroy")){
							shell.remove();
							tasks.get(shell.getUniqueId()).cancel();
							tasks.remove(shell.getUniqueId());
							return;
						}
						List<MetadataValue> metas = shell.getMetadata("shell.expiry");
						int expiry = (Integer) ((StatValue) metas.get(0)).getValue();
						expiry--;
						if(expiry < 0){
							shell.remove();
							tasks.get(shell.getUniqueId()).cancel();
							tasks.remove(shell.getUniqueId());
							return;
						}
						shell.setTicksLived(1);
						shell.setPickupDelay(Integer.MAX_VALUE);
						shell.removeMetadata("shell.expiry", main.plugin);
						shell.setMetadata("shell.expiry", new StatValue(expiry, main.plugin));
						shellUpdateEvent event = new shellUpdateEvent(shell, targetName);
					    main.plugin.getServer().getPluginManager().callEvent(event);
						return;
					}}, 3l, 3l);
				tasks.put(shell.getUniqueId(), task);
			}
			else if(ItemStackFromId.equals(main.config.getString("mariokart.bomb"), inHand.getTypeId(), inHand.getDurability())){
				inHand.setAmount(inHand.getAmount()-1);
				final Vector vel = ply.getEyeLocation().getDirection();
				final TNTPrimed tnt = (TNTPrimed) car.getLocation().getWorld().spawnEntity(car.getLocation(), EntityType.PRIMED_TNT);
			    tnt.setFuseTicks(80);
			    tnt.setMetadata("explosion.none", new StatValue(null, plugin));
			    vel.setY(0.2); //Distance to throw it
			    tnt.setVelocity(vel);
			    final MoveableInt count = new MoveableInt(12);
			    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
					public void run() {
						if(count.getInt() > 0){
							count.setInt(count.getInt()-1);
							tnt.setVelocity(vel);
							tnt.setMetadata("explosion.none", new StatValue(null, plugin));
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
							}
						}
						else{
							return;
						}
					}});
			}
			else if(ItemStackFromId.equals(main.config.getString("mariokart.lightning"), inHand.getTypeId(), inHand.getDurability())){
				Race race = plugin.raceMethods.inAGame(player.getName());
				if(race == null){
					return null;
				}
				Map<String, Integer> scores = new HashMap<String, Integer>();
				for(String pname:race.getPlayers()){
					int laps = race.totalLaps - race.lapsLeft.get(pname) +1;
					int checkpoints;
					try {
						checkpoints = race.checkpoints.get(pname);
					} catch (Exception e) {
						checkpoints = 0;
					}
					int score = (laps*race.getMaxCheckpoints()) + checkpoints;
					try {
						if(race.getWinner().equals(pname)){
							score = score+1;
						}
					} catch (Exception e) {
					}
					scores.put(pname, score);
				}
				ValueComparator com = new ValueComparator(scores);
		    	SortedMap<String, Integer> sorted = new TreeMap<String, Integer>(com);
				sorted.putAll(scores);
		    	Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				int ppos = 0;
				for(int i=0;i<pls.length;i++){
					if(pls[i].equals(player.getName())){
						ppos = i;
					}
				}
				for(int i=0;i<pls.length && i<ppos;i++){
					Player pl = plugin.getServer().getPlayer((String) pls[i]);
					pl.getWorld().strikeLightningEffect(pl.getLocation());
					if(pl.getVehicle() != null){
					if(pl.getVehicle() instanceof Minecart){
						main.listener.penalty((Minecart) pl.getVehicle(), 4);
					}
					}
				}
				inHand.setAmount(inHand.getAmount()-1);
			}
			else if(ItemStackFromId.equals(main.config.getString("mariokart.pow"), inHand.getTypeId(), inHand.getDurability())){
				Race race = plugin.raceMethods.inAGame(player.getName());
				if(race == null){
					return null;
				}
				Map<String, Integer> scores = new HashMap<String, Integer>();
				for(String pname:race.getPlayers()){
					int laps = race.totalLaps - race.lapsLeft.get(pname) +1;
					int checkpoints;
					try {
						checkpoints = race.checkpoints.get(pname);
					} catch (Exception e) {
						checkpoints = 0;
					}
					int score = (laps*race.getMaxCheckpoints()) + checkpoints;
					try {
						if(race.getWinner().equals(pname)){
							score = score+1;
						}
					} catch (Exception e) {
					}
					scores.put(pname, score);
				}
				ValueComparator com = new ValueComparator(scores);
		    	SortedMap<String, Integer> sorted = new TreeMap<String, Integer>(com);
				sorted.putAll(scores);
		    	Set<String> keys = sorted.keySet();
				Object[] pls = (Object[]) keys.toArray();
				int ppos = 0;
				for(int i=0;i<pls.length;i++){
					if(pls[i].equals(player.getName())){
						ppos = i;
					}
				}
				for(int i=0;i<pls.length && i<ppos;i++){
					Player pl = plugin.getServer().getPlayer((String) pls[i]);
					if(pl.getVehicle() != null){
					if(pl.getVehicle() instanceof Minecart){
						main.listener.penalty((Minecart) pl.getVehicle(), 2);
					}
					}
				}
				inHand.setAmount(inHand.getAmount()-1);
			}
			evt.getPlayer().setItemInHand(inHand);
			evt.getPlayer().updateInventory(); //Fix 1.6 bug with inventory not updating
		}
		else if(event instanceof ucarUpdateEvent){
			ucarUpdateEvent evt = (ucarUpdateEvent) event;
			Minecart car = (Minecart) evt.getVehicle();
			Block under  = car.getLocation().add(0, -1, 0).getBlock();
			if(under.getType() == Material.COAL_BLOCK || under.getType() == Material.COAL_BLOCK || under.getType() == Material.COAL_BLOCK){
				Sign sign = null;
				Location uu = (Location) under.getRelative(BlockFace.DOWN).getLocation();
				Location first = uu;
				try {
					sign = (Sign) uu.getBlock().getState();
				} catch (Exception e) {
					try {
						uu = uu.getBlock().getRelative(BlockFace.SOUTH).getLocation();
						sign = (Sign) uu.getBlock().getState();
					} catch (Exception e1) {
						try {
							uu = uu.getBlock().getRelative(BlockFace.EAST).getLocation();
							sign = (Sign) uu.getBlock().getState();
						} catch (Exception e2) {
							try {
								uu = uu.getBlock().getRelative(BlockFace.NORTH).getLocation();
								sign = (Sign) uu.getBlock().getState();
							} catch (Exception e3) {
								try {
									uu = uu.getBlock().getRelative(BlockFace.WEST).getLocation();
									sign = (Sign) uu.getBlock().getState();
								} catch (Exception e4) {
									try {
										uu = uu.getBlock().getRelative(BlockFace.SOUTH).getLocation();
										sign = (Sign) uu.getBlock().getState();
									} catch (Exception e5) {
										try {
											uu = first.getBlock().getRelative(BlockFace.NORTH).getLocation();
											sign = (Sign) uu.getBlock().getState();
										} catch (Exception e6) {
											try {
												uu = first.getBlock().getRelative(BlockFace.EAST).getLocation();
												sign = (Sign) uu.getBlock().getState();
											} catch (Exception e7) {
                                                return null;
											}
										}
									}
								}
							}
						}
					}
				}
				final String[] lines = sign.getLines();
				if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[urace]")){
					if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("items")){
						if(ChatColor.stripColor(lines[3]).equalsIgnoreCase("wait")){
							return null;
						}
						ItemStack give = null;
						if(ChatColor.stripColor(lines[2]).equalsIgnoreCase("all")){
							//Give all items
							ItemStack a = this.getRandomPowerup();
							ItemStack b = this.getRandomBoost();
							int randomNumber = plugin.random.nextInt(3);
							if(randomNumber < 1){
								give = b;
							}
							else{
								give = a;
							}
						}
						else if(ChatColor.stripColor(lines[2]).equalsIgnoreCase("mario")){
							//Give mario items
							give = this.getRandomPowerup();
						}
						else {
							//Give normal (ucars) items
							give = this.getRandomBoost();
						}
						Player ply = ((Player)car.getPassenger());
						ply.getInventory().addItem(give);
						ply.updateInventory();
						List<Entity> ents = ply.getNearbyEntities(2, 3, 2);
						for(Entity ent:ents){
							if(ent instanceof EnderCrystal){
								final Location loc = ent.getLocation();
								lines[3] = "wait";
								sign.setLine(3, "wait");
								sign.update(true);
								final Sign si = sign;
								plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

									public void run() {
										si.setLine(3, "ready");
										si.update(true);
										main.listener.spawnItemPickupBox(loc);
										return;
									}}, 200l);
								ent.remove();
							}
						}
						
					}
				}
			}
		}
		//End calculations
		kartAction.action = action;
		kartAction.args = args;
		kartAction.freeze = freeze;
		kartAction.destroy = destroy;
		return kartAction;
	}
	public ItemStack getRandomBoost(){
		int type = 1;
		int min = 0;
		Integer[] amts = new Integer[]{1,1,3,2,2,2,2};
		int max = amts.length;
		int randomNumber = plugin.random.nextInt(max - min) + min;
		type = amts[randomNumber];
		if(type == 1){
			return ItemStackFromId.get(ucars.config.getString("general.cars.lowBoost"));
		}
		else if(type == 2){
			return ItemStackFromId.get(ucars.config.getString("general.cars.medBoost"));
		}
	    return ItemStackFromId.get(ucars.config.getString("general.cars.highBoost"));
	}
	public ItemStack getRandomPowerup(){
		Powerup[] pows = Powerup.values();
		int min = 0;
		int max = pows.length;
		int randomNumber = plugin.random.nextInt(max - min) + min;
		Powerup pow = pows[randomNumber];
		Integer[] amts = new Integer[]{1,1,1,1,1,1,1,3,1};
		min = 0;
		max = amts.length-1;
		if(min <1){
			min = 0;
		}
		if(max < 1){
			max = 0;
		}
		randomNumber = plugin.random.nextInt(max - min) + min;
		return PowerupMaker.getPowerup(pow, amts[randomNumber]);
	}

}
