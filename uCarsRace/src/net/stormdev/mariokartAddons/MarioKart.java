package net.stormdev.mariokartAddons;

import net.stormdev.ucars.race.main;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.useful.ucars.ItemStackFromId;
import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class MarioKart {
	main plugin = null;
	Boolean enabled = true;
	public MarioKart(main plugin){
		this.plugin = plugin;
		enabled = main.config.getBoolean("mariokart.enable");
	}
	@SuppressWarnings("deprecation")
	public KartAction calculate(Player player, Event event){
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
			evt.getPlayer().setItemInHand(inHand);
			evt.getPlayer().updateInventory(); //Fix 1.6 bug with inventory not updating
		}
		else if(event instanceof ucarUpdateEvent){
			ucarUpdateEvent evt = (ucarUpdateEvent) event;
		}
		//End calculations
		kartAction.action = action;
		kartAction.args = args;
		kartAction.freeze = freeze;
		kartAction.destroy = destroy;
		return kartAction;
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
