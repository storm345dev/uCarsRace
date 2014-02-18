package net.stormdev.urace.races;

import java.util.Map;
import java.util.UUID;

import net.stormdev.urace.queues.RaceQueue;
import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.entity.Player;

public class RaceMethods {
	@SuppressWarnings("unused")
	private uCarsRace plugin = null;

	public RaceMethods() {
		this.plugin = uCarsRace.plugin;
	}

	public synchronized Race inAGame(Player player, Boolean update) {
		return uCarsRace.plugin.raceScheduler.inAGame(player, update);
	}

	public synchronized RaceQueue inGameQue(Player player) {
		Map<UUID, RaceQueue> queues = uCarsRace.plugin.raceQueues.getAllQueues();
		for (UUID id : queues.keySet()) {
			try {
				RaceQueue queue = queues.get(id);
				if (queue.containsPlayer(player)) {
					return queue;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
