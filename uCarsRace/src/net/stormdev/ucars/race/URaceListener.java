package net.stormdev.ucars.race;

import net.stormdev.ucars.utils.TrackCreator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
}
