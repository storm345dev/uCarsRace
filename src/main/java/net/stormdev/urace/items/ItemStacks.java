package net.stormdev.urace.items;

import net.stormdev.urace.uCarsRace.uCarsRace;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStacks {
	public static ItemStack get(String raw) {
		String[] parts = raw.split(":");
		String m = parts[0];
		Material mat = Material.getMaterial(m);
		if(mat == null){
			uCarsRace.plugin.getLogger().info("[WARNING] Invalid config value: "+raw+" ("+m+")");
			return new ItemStack(Material.STONE);
		}
		short data = 0;
		Boolean hasdata = false;
		if (parts.length > 1) {
			hasdata = true;
			data = Short.parseShort(parts[1]);
		}
		ItemStack item = new ItemStack(mat);
		if (hasdata) {
			item.setDurability(data);
		}
		return item;
	}

	public static Boolean equals(String rawid, String materialName, int tdata) {
		String[] parts = rawid.split(":");
		String m = parts[0];
		int data = 0;
		Boolean hasdata = false;
		if (parts.length > 1) {
			hasdata = true;
			data = Integer.parseInt(parts[1]);
		}
		if (materialName.equalsIgnoreCase(m)) {
			Boolean valid = true;
			if (hasdata) {
				if (!(tdata == data)) {
					valid = false;
				}
			}
			if (valid) {
				return true;
			}
		}
		return false;
	}
}
