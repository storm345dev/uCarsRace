package net.stormdev.ucars.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.stormdev.ucars.race.Race;
import net.stormdev.ucars.race.main;

public class RaceMethods {
	private main plugin = null;
	public RaceMethods(){
		this.plugin = main.plugin;
	}
    public Race inAGame(String playername){
    	HashMap<String, Race> games = plugin.gameScheduler.getGames();
    	Set<String> keys = games.keySet();
    	Boolean inAGame = false;
        Race mgame = null;
    	for(String key:keys){
    		Race game = games.get(key);
    		if(game.getPlayers().contains(playername)){
    			inAGame = true;
    			mgame = game;
    		}
    	}
    	if(inAGame){
    	return mgame;
    	}
    	return null;
    }
    public String inGameQue(String playername){
    	ArrayList<String> arenaNames = plugin.trackManager.getRaceTrackNames();
    	for(String arenaName:arenaNames){
    		List<String> que = plugin.gameScheduler.getQue(plugin.raceQues.getQue(arenaName));
    		if(que.contains(playername)){
    			return arenaName;
    		}
    	}
    	return null;
    }
}