package net.stormdev.urace.queues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.urace.lesslag.DynamicLagReducer;
import net.stormdev.urace.players.PlayerQuitException;
import net.stormdev.urace.players.User;
import net.stormdev.urace.races.Race;
import net.stormdev.urace.races.RaceType;
import net.stormdev.urace.tracks.RaceTrack;
import net.stormdev.urace.uCarsRace.uCarsRace;
import net.stormdev.urace.utils.SerializableLocation;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucarsCommon.StatValue;

public class RaceScheduler {
	private HashMap<UUID, Race> races = new HashMap<UUID, Race>();
	private int raceLimit = 5;
	private boolean lockdown = false;
	private boolean fairCars = true;

	public RaceScheduler(int raceLimit) {
		this.raceLimit = raceLimit;
		fairCars = uCarsRace.config.getBoolean("general.ensureEqualCarSpeed");
	}

	public void joinAutoQueue(Player player, RaceType type) {
		if(lockdown){
			player.sendMessage(uCarsRace.colors.getError()+uCarsRace.msgs.get("error.memoryLockdown"));
			return;
		}
		Map<UUID, RaceQueue> queues = uCarsRace.plugin.raceQueues
				.getOpenQueues(type); // Joinable queues for that racemode
		RaceQueue toJoin = null;
		Boolean added = false;
		List<RaceTrack> tracks = uCarsRace.plugin.trackManager.getRaceTracks();
		List<RaceTrack> openTracks = new ArrayList<RaceTrack>();
		List<RaceTrack> openNoQueueTracks = new ArrayList<RaceTrack>();
		List<RaceTrack> NoQueueTracks = new ArrayList<RaceTrack>();
		for (RaceTrack t : tracks) {
			if (!isTrackInUse(t, type)) {
				openTracks.add(t);
				if (!uCarsRace.plugin.raceQueues.queuesFor(t, type)) {
					openNoQueueTracks.add(t);
				}
			}
			if (!uCarsRace.plugin.raceQueues.queuesFor(t, type)) {
				NoQueueTracks.add(t);
			}
		}
		if (queues.size() > 0) {
			int targetPlayers = uCarsRace.config
					.getInt("general.race.targetPlayers");
			Map<UUID, RaceQueue> recommendedQueues = new HashMap<UUID, RaceQueue>();
			for (UUID id : new ArrayList<UUID>(queues.keySet())) {
				RaceQueue queue = queues.get(id);
				if (queue.playerCount() < targetPlayers) {
					recommendedQueues.put(id, queue);
				}
			}
			if (recommendedQueues.size() > 0) {
				UUID random = (UUID) recommendedQueues.keySet().toArray()[uCarsRace.plugin.random
						.nextInt(recommendedQueues.size())];
				toJoin = recommendedQueues.get(random);
			} else {
				if(uCarsRace.plugin.random.nextBoolean() && openNoQueueTracks.size() > 0){
					//Chance that will join a new track in a new queue
					RaceTrack t = openNoQueueTracks.get(uCarsRace.plugin.random.nextInt(
							openNoQueueTracks.size()));
					toJoin = new RaceQueue(t, type, player);
				}
				else{
					// Join from 'queues'
					UUID random = (UUID) queues.keySet().toArray()[uCarsRace.plugin.random
					                       						.nextInt(queues.size())];
					                       				toJoin = queues.get(random);
				}
			}
		} else {
			// Create a random queue
			RaceTrack track = null;
			if (openNoQueueTracks.size() > 0) {
				track = openNoQueueTracks.get(uCarsRace.plugin.random
						.nextInt(openNoQueueTracks.size()));
			} else {
				if (tracks.size() < 1) {
					// No tracks exist
					// No tracks created
					player.sendMessage(uCarsRace.colors.getError()
							+ uCarsRace.msgs.get("general.cmd.delete.exists"));
					return;
				}
				//All queues and tracks full...
				player.sendMessage(uCarsRace.colors.getError()
						+ uCarsRace.msgs.get("general.cmd.overflow"));
				track = tracks.get(uCarsRace.plugin.random.nextInt(tracks.size()));
				//Joining a new queue for that track (Low priority)
			}
			if (track == null) {
				//Track doesn't exist
				player.sendMessage(uCarsRace.colors.getError()
						+ uCarsRace.msgs.get("general.cmd.delete.exists"));
				return;
			}
			toJoin = new RaceQueue(track, type, player);
			added = true;
		}
		// Join that queue
		if (!added) {
			toJoin.addPlayer(player);
		}
		toJoin.broadcast(uCarsRace.colors.getTitle() + "[uCarsRace:] "
				+ uCarsRace.colors.getInfo() + player.getName()
				+ uCarsRace.msgs.get("race.que.joined") + " ["
				+ toJoin.playerCount() + "/" + toJoin.playerLimit() + "]");
		executeLobbyJoin(player, toJoin);
		recalculateQueues();
		return;
	}

	public void joinQueue(Player player, RaceTrack track, RaceType type) {
		if(lockdown){
			player.sendMessage(uCarsRace.colors.getError()+uCarsRace.msgs.get("error.memoryLockdown"));
			return;
		}
		Boolean added = false;
		Map<UUID, RaceQueue> queues = uCarsRace.plugin.raceQueues.getQueues(track.getTrackName(), type); // Get the oldest queue of that type for that track
		RaceQueue queue = null;
		if (queues.size() < 1) {
			queue = new RaceQueue(track, type, player);
			added = true;
		} else {
			for(UUID id:queues.keySet()){
				RaceQueue q = queues.get(id);
				if(q.playerCount() < q.playerLimit()){
					queue = q;
				}
			}
			if(queue == null){ //No queues of that type available, so create and schedule a new one
				queue = new RaceQueue(track, type, player);
				player.sendMessage(uCarsRace.colors.getInfo()
						+ uCarsRace.msgs.get("general.cmd.overflow"));
				added = true;
			}
		}
		if(!added){
			queue.addPlayer(player);
			added = true;
		}
		queue.broadcast(uCarsRace.colors.getTitle() + "[uCarsRace:] "
				+ uCarsRace.colors.getInfo() + player.getName()
				+ uCarsRace.msgs.get("race.que.joined") + " [" + queue.playerCount()
				+ "/" + queue.playerLimit() + "]");
		executeLobbyJoin(player, queue);
		recalculateQueues();
		return;
	}

	public void executeLobbyJoin(Player player, RaceQueue queue) {
		Location l = queue.getTrack().getLobby(uCarsRace.plugin.getServer());
		Chunk chunk = l.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
		}
		player.teleport(l);
		return;
	}

	public synchronized void leaveQueue(Player player, RaceQueue queue) {
		queue.removePlayer(player);
		return;
	}

	public void recalculateQueues() {
		uCarsRace.plugin.signManager.updateSigns();
		
		if(lockdown){
			//No more races allowed
			if(getRacesRunning() < 1){
				//Need to recall recalculateQueues else all will freeze
				uCarsRace.plugin.getServer().getScheduler().runTaskLater(uCarsRace.plugin, new Runnable(){

					@Override
					public void run() {
						recalculateQueues();
						return;
					}}, 600l);
			}
			return;
		}
		if (getRacesRunning() >= raceLimit) {
			uCarsRace.logger.info("[INFO] Max races running");
			return; // Cannot start any more races for now...
		}
		Map<UUID, RaceQueue> queues = uCarsRace.plugin.raceQueues.getAllQueues();
		if(queues.size() < 1){
			return;
		}
		ArrayList<RaceTrack> queuedTracks = new ArrayList<RaceTrack>();
		for (UUID id : new ArrayList<UUID>(queues.keySet())) {
			final RaceQueue queue = queues.get(id);
			if (queue.getRaceMode() == RaceType.TIME_TRIAL
					&& !isTrackInUse(queue.getTrack(), RaceType.TIME_TRIAL)
					&& !queuedTracks.contains(queue.getTrack()) // Are there
																// other
																// racemodes
																// waiting for
																// the track
																// ahead of it?
					&& getRacesRunning() < raceLimit && !queue.isStarting()) {
				//Time trial races
				double predicted = 110; //Predicted Memory needed
				if(DynamicLagReducer.getResourceScore(predicted) < 30){
					uCarsRace.logger.info("Delayed re-queueing due to lack of server resources!");
					if(getRacesRunning() < 1){
						uCarsRace.plugin.getServer().getScheduler().runTaskLater(uCarsRace.plugin, new Runnable(){
						@Override
						public void run() {
							//Make sure queues don't lock
							recalculateQueues();
							return;
						}}, 600l);
					}
					return; //Cancel - Not enough memory
				}
				//Memory should be available
				queue.setStarting(true);
				List<Player> q = new ArrayList<Player>(queue.getPlayers());
				for (Player p : q) {
					if (p != null && p.isOnline()
							&& getRacesRunning() < raceLimit) {
						Race race = new Race(queue.getTrack(),
								queue.getTrackName(), RaceType.TIME_TRIAL);
						race.join(p);
						if (race.getUsers().size() > 0) {
							startRace(race.getTrackName(), race);
						}
						queue.removePlayer(p);
					}
				}
				if (queue.playerCount() < 1) {
					q.clear();
					uCarsRace.plugin.raceQueues.removeQueue(queue);
				}
			} else if (queue.playerCount() >= uCarsRace.config
					.getInt("race.que.minPlayers")
					&& !isTrackInUse(queue.getTrack(), queue.getRaceMode())
					&& getRacesRunning() < raceLimit
					&& !queuedTracks.contains(queue.getTrack()) // Check it's
																// not reserved
					&& queue.getRaceMode() != RaceType.TIME_TRIAL
					&& !queue.isStarting()) {
				int c = queue.playerCount();
				double predicted = c*60+50; //Predicted Memory needed
				if(DynamicLagReducer.getResourceScore(predicted) < 30){
					uCarsRace.logger.info("Delayed re-queueing due to lack of server resources!");
					if(getRacesRunning() < 1){
						uCarsRace.plugin.getServer().getScheduler().runTaskLater(uCarsRace.plugin, new Runnable(){
						@Override
						public void run() {
							//Make sure queues don't lock
							recalculateQueues();
							return;
						}}, 600l);
					}
					return; //Cancel - Not enough memory
				}
				queuedTracks.add(queue.getTrack());
				// Queue can be initiated
				queue.setStarting(true);
				// Wait grace time
				double graceS = uCarsRace.config
						.getDouble("general.raceGracePeriod");
				long grace = (long) (graceS * 20);
				String msg = uCarsRace.msgs.get("race.que.players");
				msg = msg.replaceAll(Pattern.quote("%time%"), "" + graceS);
				queue.broadcast(uCarsRace.colors.getInfo() + msg);
				uCarsRace.plugin.getServer().getScheduler()
						.runTaskLater(uCarsRace.plugin, new Runnable() {

							@Override
							public void run() {
								if (queue.playerCount() < uCarsRace.config
										.getInt("race.que.minPlayers")) {
									queue.setStarting(false);
									return;
								}
								Race race = new Race(queue.getTrack(), queue
										.getTrackName(), queue.getRaceMode());
								List<Player> q = new ArrayList<Player>(queue
										.getPlayers());
								for (Player p : q) {
									if (p != null && p.isOnline()) {
										race.join(p);
									}
								}
								q.clear();
								if (race.getUsers().size() >= uCarsRace.config
										.getInt("race.que.minPlayers")) {
									queue.clear();
									uCarsRace.plugin.raceQueues.removeQueue(queue);
									startRace(race.getTrackName(), race);
								} else {
									queue.setStarting(false);
								}
								return;
							}
						}, grace);
			} else {
				// Race unable to be started (Unavailable etc...)
				if (queue.getRaceMode() != RaceType.TIME_TRIAL) {
					queuedTracks.add(queue.getTrack());
				}
			}
			if (getRacesRunning() >= raceLimit) {
				uCarsRace.logger.info("[INFO] Max races running");
				return; // No more races can be run for now
			}
		}
	}

	private synchronized void putRace(Race race){
		this.races.put(race.getGameId(), race);
	}
	
	public void startRace(String trackName, final Race race) {
		putRace(race);
		final List<User> users = race.getUsers();
		for (User user : users) {
			Player player = null;
			try {
				player = user.getPlayer();
			} catch (PlayerQuitException e) {
				race.leave(user, true);
				// User has left
			}
			user.setOldInventory(player.getInventory().getContents().clone());
			if (player != null) {
				player.getInventory().clear();
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
		final ArrayList<Minecart> cars = new ArrayList<Minecart>();
		RaceTrack track = race.getTrack();
		ArrayList<SerializableLocation> sgrid = track.getStartGrid();
		HashMap<Integer, Location> grid = new HashMap<Integer, Location>();
		for (int i = 0; i < sgrid.size(); i++) {
			SerializableLocation s = sgrid.get(i);
			grid.put(i, s.getLocation(uCarsRace.plugin.getServer()).clone());
		}
		int count = grid.size();
		if (count > users.size()) { // If more grid slots than players, only
			// use the right number of grid slots
			count = users.size();
		}
		if (users.size() > count) {
			count = users.size(); // Should theoretically never happen but
			// sometimes does?
		}
		for (int i = 0; i < count; i++) {
			int max = users.size();
			if (max>0) {
				Player p = null;
				int randomNumber = uCarsRace.plugin.random.nextInt(max);
				User user = users.get(randomNumber);
				try {
					p = users.get(randomNumber).getPlayer();
				} catch (PlayerQuitException e) {
					// Player has left
				}
				users.remove(user);
				Location loc = grid.get(i);
				if (race.getType() == RaceType.TIME_TRIAL) {
					loc = grid.get(uCarsRace.plugin.random.nextInt(grid.size()));
				}
				if (p != null) {
					if (p.getVehicle() != null) {
						p.getVehicle().eject();
					}
					Chunk c = loc.getChunk();
					if (c.isLoaded()) {
						c.load(true);
					}
					p.teleport(loc.add(0, 2, 0));
					Minecart car = (Minecart) loc.getWorld().spawnEntity(
							loc.add(0, 0.2, 0), EntityType.MINECART);
					car.setMetadata("car.frozen", new StatValue(null,
							uCarsRace.plugin));
					car.setMetadata("kart.racing", new StatValue(null,
							uCarsRace.plugin));
					car.setPassenger(p);
					p.setMetadata("car.stayIn",
							new StatValue(null, uCarsRace.plugin));
					cars.add(car);
					if(fairCars){
					    uCarsAPI.getAPI().setUseRaceControls(car.getUniqueId(), uCarsRace.plugin); //Use the race+ control scheme
					}
				}
			}
		}
		if (users.size() > 0) {
			User user = users.get(0);
			try {
				Player p = user.getPlayer();
				p.sendMessage(uCarsRace.colors.getError()
						+ uCarsRace.msgs.get("race.que.full"));
			} catch (PlayerQuitException e) {
				// Player has left anyway
			}
			race.leave(user, true);
		}

		for (User user : users) {
			Player player;
			try {
				player = user.getPlayer();
				user.setLocation(player.getLocation().clone());
				player.sendMessage(uCarsRace.colors.getInfo()
						+ uCarsRace.msgs.get("race.que.preparing"));
			} catch (PlayerQuitException e) {
				// Player has left
			}
		}
		final List<User> users2 = race.getUsers();
		for (User user2 : users2) {
			user2.setInRace(true);
		}
		uCarsRace.plugin.getServer().getScheduler()
				.runTaskAsynchronously(uCarsRace.plugin, new Runnable() {
					@Override
					public void run() {
						for (User user : users2) {
							try {
								user.getPlayer()
										.sendMessage(
												uCarsRace.colors.getInfo()
														+ uCarsRace.msgs
																.get("race.que.starting"));
							} catch (PlayerQuitException e) {
								// User has left
							}
						}
						for (int i = 10; i > 0; i--) {
							for (User user : users2) {
								try {
									Player p = user.getPlayer();
									p.sendMessage(uCarsRace.colors.getInfo() + ""
											+ i);
								} catch (PlayerQuitException e) {
									// Player has left
								}
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						}
						for (Minecart car : cars) {
							car.removeMetadata("car.frozen", uCarsRace.plugin);
						}
						for (User user : users2) {
							try {
								user.getPlayer().sendMessage(
										uCarsRace.colors.getInfo()
												+ uCarsRace.msgs.get("race.que.go"));
							} catch (PlayerQuitException e) {
								// Player has left
							}
						}
						race.start();
						return;
					}
				});

		return;
	}

	public synchronized void stopRace(Race race) {
		race.end();
		race.clear();
		this.races.put(race.getGameId(), race);
		removeRace(race);
		recalculateQueues();
	}

	public synchronized void removeRace(Race race) {
		race.clear();
		this.races.remove(race.getGameId());
		return;
	}

	@Deprecated
	public synchronized void updateRace(Race race) {
		if(race == null || race.getGameId() == null){
			return;
		}
		if(this.races == null){
			this.races = new HashMap<UUID, Race>();
		}
		if (this.races.containsKey(race.getGameId())) {
			this.races.put(race.getGameId(), race);
		}
	}
	
	public synchronized void lockdown(){
		//Running out of system memory!
		this.lockdown = true;
		uCarsRace.logger.info("[WANRING] Memory resources low, uCarsRace has locked down all queues "
				+ "and may start to terminate races if condition persists!");
		return;
	}
	
	public boolean isLockedDown(){
		return this.lockdown;
	}
	
	public synchronized void unlockDown(){
		//System regained necessary memory
		this.lockdown = false;
		uCarsRace.logger.info("[INFO] System memory stable once more, uCarsRace has unlocked all queues!");
		return;
	}

	public HashMap<UUID, Race> getRaces() {
		return new HashMap<UUID, Race>(races);
	}

	public synchronized int getRacesRunning() {
		return races.size();
	}

	public synchronized Boolean isTrackInUse(RaceTrack track, RaceType type) {
		for (UUID id : races.keySet()) {
			Race r = races.get(id);
			if (r.getTrackName().equals(track.getTrackName())) {
				if (type == RaceType.TIME_TRIAL
						&& r.getType() == RaceType.TIME_TRIAL) {
					return false;
				}
				return true;
			}
		}
		return false;
	}

	public synchronized Race inAGame(Player player, Boolean update) {
		Map<UUID, Race> races = uCarsRace.plugin.raceScheduler.getRaces();
		for (UUID id : new ArrayList<UUID>(races.keySet())) {
			Race r = races.get(id);
			if (update) {
				r.updateUser(player);
			}
			List<User> users = r.getUsersIn(); // Exclude those that have
												// finished the race
			for (User u : users) {
				if (u.getPlayerName().equals(player.getName())) {
					return r;
				}
			}
		}
		return null;
	}
	
	public synchronized void endAll(){
		for (UUID id : races.keySet()) {
			races.get(id).end(); // End the race
		}
	}
}
