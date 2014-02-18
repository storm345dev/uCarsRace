package net.stormdev.urace.hotbar;

import net.stormdev.urace.races.Race;
import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class HotBarListener implements Listener {
	@EventHandler
	void interact(PlayerInteractEvent event){
		Player player = event.getPlayer();
		if(!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			return;
		}
		
		Race race = uCarsRace.plugin.raceMethods.inAGame(player, false);
		
		if(race == null){
			return;
		}
		
		ItemStack inHand = player.getItemInHand();
		if(inHand.getType().equals(Material.WOOD_DOOR)){
			uCarsRace.plugin.raceCommandExecutor.urace(player, new String[]{"leave"}, player); //Leave the race
			return;
		}
		else if(inHand.getType().equals(Material.EGG)){ //Respawn
			player.setHealth(0); //Respawn
			return;
		}
		return;
	}
}
