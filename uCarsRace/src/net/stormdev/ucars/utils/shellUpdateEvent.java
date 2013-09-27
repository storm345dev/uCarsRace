package net.stormdev.ucars.utils;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class shellUpdateEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	Entity shell = null;
	String targetName = null;
	
	public shellUpdateEvent(Entity shell, String targetName){
		this.shell = shell;
		this.targetName = targetName;
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
