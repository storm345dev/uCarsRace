package net.stormdev.ucars.utils;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

public class shellUpdateEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	Entity shell = null;
	String targetName = null;
	public Vector direction = null;
	
	public shellUpdateEvent(Entity shell, String targetName, Vector direction){
		this.shell = shell;
		this.targetName = targetName;
		this.direction = direction;
	}
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
        return handlers;
    }
	public Entity getShell(){	
		return this.shell;
	}
	public String getTarget(){
		return this.targetName;
	}

}
