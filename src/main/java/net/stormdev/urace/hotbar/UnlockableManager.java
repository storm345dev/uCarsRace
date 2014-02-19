package net.stormdev.urace.hotbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class UnlockableManager {

	private Map<String, String> data = new HashMap<String, String>();
	private Map<String, Unlockable> unlocks = null; // ShortId:Unlockable
	private File saveFile = null;
	private boolean sql = false;
	private boolean enabled = true;

	public UnlockableManager(File saveFile, Boolean sql) {
		this.saveFile = saveFile;
		this.sql = false;
		this.unlocks = getUnlocks();
		this.enabled = uCarsRace.config.getBoolean("general.upgrades.enable");
		// SQL setup...
		unlocks = getUnlocks();
		load(); // Load the data
	}
	
	public synchronized void unloadSQL(){
		//Null
	}

	public List<Upgrade> getUpgrades(String playerName) {
		if (!data.containsKey(playerName) || !enabled) {
			return new ArrayList<Upgrade>();
		}
		List<Upgrade> upgrades = new ArrayList<Upgrade>();
		String[] unlocks = this.data.get(playerName).split(Pattern.quote(","));
		for (String unlock : unlocks) {
			String[] upgradeData = unlock.split(Pattern.quote(":"));
			if (upgradeData.length > 1) {
				String shortId = upgradeData[0];
				String amount = upgradeData[1];
				if(!this.unlocks.containsKey(shortId)){
					continue;
				}
				int a = 1;
				try {
					a = Integer.parseInt(amount);
				} catch (NumberFormatException e) {
					a = 0;
				}
				if (a > 0) {
					upgrades.add(new Upgrade(this.unlocks.get(shortId), a));
				}
			}
		}
		return upgrades;
	}

	public Boolean useUpgrade(String player, Upgrade upgrade) {
		if(!enabled){
			return false;
		}
		String[] unlocks = this.data.get(player).split(Pattern.quote(","));
		String[] un = unlocks.clone();
		Boolean used = false;
		Boolean remove = false;
		Boolean update = false;
		for (int i = 0; i < un.length; i++) {
			remove = false;
			String unlock = un[i];
			String[] upgradeData = unlock.split(Pattern.quote(":"));
			if (upgradeData.length > 1) {
				String shortId = upgradeData[0];
				String amount = upgradeData[1];
				int a = 1;
				try {
					a = Integer.parseInt(amount);
				} catch (NumberFormatException e) {
					a = 0;
				}
				if (a > 0) {
					if (shortId.equals(upgrade.getUnlockedAble().shortId)) {
						int q = a - upgrade.getQuantity();
						if (q < 1) {
							remove = true;
						} else {
							// Set quantity to q
							unlocks[i] = shortId + ":" + q;
						}
						used = true;
					}
				} else {
					remove = true;
				}
			}
			if (remove) {
				unlocks[i] = " ";
				update = true;
			}
		}
		if (used || update) {
			// Update database
			String s = "";
			for (String u : unlocks) {
				if (u.length() > 1) {
					if (s.length() < 1) {
						s = u;
					} else {
						s = s + "," + u;
					}
				}
			}
			if (s.length() < 2) {
				this.data.remove(player);
			} else {
				this.data.put(player, s);
			}
			save(player); // Save to file/sql
		}
		return used;
	}

	public Boolean addUpgrade(String player, Upgrade upgrade) {
		if(!enabled){
			return false;
		}
		String[] un = new String[] {};
		String[] unlocks = new String[] {};
		if (this.data.containsKey(player)) {
			unlocks = this.data.get(player).split(Pattern.quote(","));
			un = unlocks.clone();
		}
		Boolean added = false;
		for (int i = 0; i < un.length; i++) {
			String unlock = un[i];
			String[] upgradeData = unlock.split(Pattern.quote(":"));
			if (upgradeData.length > 1) {
				String shortId = upgradeData[0];
				String amount = upgradeData[1];
				int a = 1;
				try {
					a = Integer.parseInt(amount);
				} catch (NumberFormatException e) {
					a = 0;
				}
				if (shortId.equals(upgrade.getUnlockedAble().shortId)) {
					int q = a + upgrade.getQuantity();
					if (q < 1) {
						added = true;
					} else {
						if (q <= 64) {
							// Set quantity to q
							unlocks[i] = shortId + ":" + q;
							added = true;
						} else {
							return false; // Not allowed more than 64 of an
											// upgrade
						}
					}
				}
			}
		}
		// Update database
		String s = "";
		for (String u : unlocks) {
			if (s.length() < 1) {
				s = "" + u;
			} else {
				s += "," + u;
			}
		}
		if (!added) {
			if (s.length() < 1) {
				s = upgrade.getUnlockedAble().shortId + ":"
						+ upgrade.getQuantity();
			} else {
				s += "," + upgrade.getUnlockedAble().shortId + ":"
						+ upgrade.getQuantity();
			}
		}
		if (s.length() < 255) {
			this.data.put(player, s);
			save(player); // Save to file/sql
			return true;
		}
		return false; // They have too many upgrades
	}

	public Boolean hasUpgradeById(String player, String shortId) {
		List<Upgrade> ups = getUpgrades(player);
		for (Upgrade u : ups) {
			if (u.getUnlockedAble().shortId.equals(shortId)) {
				return true;
			}
		}
		return false;
	}

	public Boolean hasUpgradeByName(String player, String upgradeName) {
		List<Upgrade> ups = getUpgrades(player);
		for (Upgrade u : ups) {
			if (u.getUnlockedAble().upgradeName.equals(upgradeName)) {
				return true;
			}
		}
		return false;
	}

	public void resetUpgrades(String player) {
		if(!enabled){
			return;
		}
		this.data.remove(player);
		save(player);
		return;
	}

	public Unlockable getUnlockable(String shortId) {
		if (!unlocks.containsKey(shortId)) {
			return null;
		}
		return unlocks.get(shortId);
	}

	public String getShortId(String unlockName) {
		List<String> keys = new ArrayList<String>(unlocks.keySet());
		for (String s : keys) {
			Unlockable u = unlocks.get(s);
			if (u.upgradeName.equals(unlockName)) {
				return s;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		if (!(this.saveFile.length() < 1 || !this.saveFile.exists())) {
			// Load from file
			try {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(this.saveFile));
				Object result = ois.readObject();
				ois.close();
				data = (Map<String, String>) result;
			} catch (Exception e) {
				// File just created
			}
		}
	}

	public void save(final String playerName) {
		uCarsRace.plugin.getServer().getScheduler()
				.runTaskAsynchronously(uCarsRace.plugin, new Runnable() {

					@Override
					public void run() {
						saveFile.getParentFile().mkdirs();
						if (!saveFile.exists() || saveFile.length() < 1) {
							try {
								saveFile.createNewFile();
							} catch (IOException e) {
							}
						}
						try {
							ObjectOutputStream oos = new ObjectOutputStream(
									new FileOutputStream(saveFile));
							oos.writeObject(data);
							oos.flush();
							oos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				});
	}

	public void save() {
		if (!sql) {
			save("");
			return;
		}
		List<String> keys = new ArrayList<String>(data.keySet());
		for (String k : keys) {
			save(k);
		}
		return;
	}
	
	public Map<String, Unlockable> getUnlocks() {
		if (unlocks != null) {
			return unlocks;
		}
		// Begin load them from a YAML file
		Map<String, Unlockable> unlockables = new HashMap<String, Unlockable>();
		File saveFile = new File(uCarsRace.plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "upgrades.yml");
		YamlConfiguration upgrades = new YamlConfiguration();
		saveFile.getParentFile().mkdirs();
		Boolean setDefaults = false;
		try {
			upgrades.load(saveFile);
		} catch (Exception e) {
			setDefaults = true;
		}
		if (!saveFile.exists() || saveFile.length() < 1 || setDefaults) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				return unlockables;
			}
			// Set defaults
			upgrades.set("upgrades.speedBurstI.name", "Speed Burst I (5s)");
			upgrades.set("upgrades.speedBurstI.id", "aa");
			upgrades.set("upgrades.speedBurstI.type", HotBarUpgrade.SPEED_BOOST
					.name().toUpperCase());
			upgrades.set("upgrades.speedBurstI.item", Material.APPLE.name()
					.toUpperCase());
			upgrades.set("upgrades.speedBurstI.length", 5000l);
			upgrades.set("upgrades.speedBurstI.power", 10d);
			upgrades.set("upgrades.speedBurstI.useItem", true);
			upgrades.set("upgrades.speedBurstI.useUpgrade", true);
			upgrades.set("upgrades.speedBurstI.price", 3d);
			upgrades.set("upgrades.speedBurstII.name", "Speed Burst II (10s)");
			upgrades.set("upgrades.speedBurstII.id", "ab");
			upgrades.set("upgrades.speedBurstII.type",
					HotBarUpgrade.SPEED_BOOST.name().toUpperCase());
			upgrades.set("upgrades.speedBurstII.item", Material.CARROT_ITEM
					.name().toUpperCase());
			upgrades.set("upgrades.speedBurstII.length", 10000l);
			upgrades.set("upgrades.speedBurstII.power", 13d);
			upgrades.set("upgrades.speedBurstII.useItem", true);
			upgrades.set("upgrades.speedBurstII.useUpgrade", true);
			upgrades.set("upgrades.speedBurstII.price", 6d);
			upgrades.set("upgrades.immunityI.name", "Immunity I (5s)");
			upgrades.set("upgrades.immunityI.id", "ac");
			upgrades.set("upgrades.immunityI.type", HotBarUpgrade.IMMUNITY
					.name().toUpperCase());
			upgrades.set("upgrades.immunityI.item", Material.IRON_HELMET.name()
					.toUpperCase());
			upgrades.set("upgrades.immunityI.length", 5000l);
			upgrades.set("upgrades.immunityI.useItem", true);
			upgrades.set("upgrades.immunityI.useUpgrade", true);
			upgrades.set("upgrades.immunityI.price", 6d);
			upgrades.set("upgrades.immunityII.name", "Immunity II (10s)");
			upgrades.set("upgrades.immunityII.id", "ad");
			upgrades.set("upgrades.immunityII.type", HotBarUpgrade.IMMUNITY
					.name().toUpperCase());
			upgrades.set("upgrades.immunityII.item", Material.GOLD_HELMET
					.name().toUpperCase());
			upgrades.set("upgrades.immunityII.length", 10000l);
			upgrades.set("upgrades.immunityII.useItem", true);
			upgrades.set("upgrades.immunityII.useUpgrade", true);
			upgrades.set("upgrades.immunityII.price", 12d);
			try {
				upgrades.save(saveFile);
			} catch (IOException e) {
				uCarsRace.logger.info(uCarsRace.colors.getError()
						+ "[WARNING] Failed to create upgrades.yml!");
			}
		}
		// Load them
		ConfigurationSection ups = upgrades.getConfigurationSection("upgrades");
		Set<String> upgradeKeys = ups.getKeys(false);
		for (String key : upgradeKeys) {
			ConfigurationSection sect = ups.getConfigurationSection(key);
			if (!sect.contains("name") || !sect.contains("type")
					|| !sect.contains("id") || !sect.contains("useItem")
					|| !sect.contains("useUpgrade") || !sect.contains("price")
					|| !sect.contains("item")) {
				// Invalid upgrade
				uCarsRace.logger.info(uCarsRace.colors.getError()
						+ "[WARNING] Invalid upgrade: " + key);
				continue;
			}
			String name = sect.getString("name");
			HotBarUpgrade type = null;
			Material item = null;
			try {
				type = HotBarUpgrade.valueOf(sect.getString("type"));
				item = Material.valueOf(sect.getString("item"));
			} catch (Exception e) {
				// Invalid upgrade
				uCarsRace.logger.info(uCarsRace.colors.getError()
						+ "[WARNING] Invalid upgrade: " + key);
				continue;
			}
			if (type == null || item == null) {
				// Invalid upgrade
				uCarsRace.logger.info(uCarsRace.colors.getError()
						+ "[WARNING] Invalid upgrade: " + key);
				continue;
			}
			String shortId = sect.getString("id");
			Boolean useItem = sect.getBoolean("useItem");
			Boolean useUpgrade = sect.getBoolean("useUpgrade");
			double price = sect.getDouble("price");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("upgrade.name", name);
			data.put("upgrade.useItem", useItem);
			data.put("upgrade.useUpgrade", useUpgrade);
			if (sect.contains("power")) {
				data.put("upgrade.power", sect.getDouble("power"));
			}
			if (sect.contains("length")) {
				data.put("upgrade.length", sect.getLong("length"));
			}
			Unlockable unlock = new Unlockable(type, data, price, name,
					shortId, item);
			unlockables.put(shortId, unlock);
		}
		unlocks = unlockables;
		return unlockables;
	}

}
