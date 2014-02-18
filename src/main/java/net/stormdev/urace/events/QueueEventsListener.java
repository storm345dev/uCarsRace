package net.stormdev.urace.events;

import net.stormdev.urace.queues.RaceQueue;
import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class QueueEventsListener implements Listener {
	@SuppressWarnings("unused")
	private uCarsRace plugin;
	
	public QueueEventsListener(uCarsRace plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void queueRespawns(PlayerRespawnEvent event) { //Handle respawns while in a queue
		Player player = event.getPlayer();
		RaceQueue r = uCarsRace.plugin.raceMethods.inGameQue(player);
		if (r == null) {
			return;
		}
		event.setRespawnLocation(r.getTrack().getLobby(uCarsRace.plugin.getServer()));
	}
}
