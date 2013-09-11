package net.stormdev.ucars.race;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class URaceCommandExecutor implements CommandExecutor {
	main plugin = null;
	public URaceCommandExecutor(main plugin){
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		
		
		return false;
	}

}
