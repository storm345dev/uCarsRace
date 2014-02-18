package net.stormdev.urace.events;

import net.stormdev.urace.hotbar.HotBarSlot;
import net.stormdev.urace.hotbar.RaceHotBar;
import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

import com.useful.ucars.ucarUpdateEvent;
import com.useful.ucarsCommon.StatValue;

public class HotbarEventsListener implements Listener {
	private uCarsRace plugin;
	
	public HotbarEventsListener(uCarsRace plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void hotBarScrolling(VehicleUpdateEvent event) { //Let people change their hotbar upgrade selection
		Vehicle car = event.getVehicle();
		Entity e = car.getPassenger();
		if(event instanceof ucarUpdateEvent){
			e = ((ucarUpdateEvent) event).getPlayer();
		}
		else{
			while(e!=null && !(e instanceof Player) && e.getPassenger() != null){
				e = e.getPassenger();
			}
			if(!(e instanceof Player)){
				return;
			}
		}
		final Player player = (Player) e;
		if (uCarsRace.plugin.raceMethods.inAGame(player, false) == null) {
			return;
		}
		if (car.hasMetadata("car.braking")
				&& !player.hasMetadata("mariokart.slotChanging")
				&& (player.getInventory().getHeldItemSlot() == 6 || player
						.getInventory().getHeldItemSlot() == 7)) {
			RaceHotBar hotBar = uCarsRace.plugin.hotBarManager.getHotBar(player
					.getName());
			if (player.getInventory().getHeldItemSlot() == 6) {
				hotBar.scroll(HotBarSlot.SCROLLER);
			} else {
				hotBar.scroll(HotBarSlot.UTIL);
			}
			player.setMetadata("mariokart.slotChanging", new StatValue(true,
					uCarsRace.plugin));
			uCarsRace.plugin.getServer().getScheduler()
					.runTaskLater(uCarsRace.plugin, new Runnable() {

						@Override
						public void run() {
							player.removeMetadata("mariokart.slotChanging",
									uCarsRace.plugin);
						}
					}, 15);
			plugin.hotBarManager.updateHotBar(player);
		}
		return;
	}
	
}
