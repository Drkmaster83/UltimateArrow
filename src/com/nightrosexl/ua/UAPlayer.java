package com.nightrosexl.ua;

import org.bukkit.entity.Player;

/** This class stores our additional data while being hooked up with a Bukkit Player object*/
public class UAPlayer {
	private Player player;
	private boolean isReady;
	private String team;
	
	public UAPlayer(Player initialPlayer) {
        this(initialPlayer, "");
    }
	
	public UAPlayer(Player initialPlayer, String team) {
		this.player = initialPlayer;
		this.isReady = false;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public void setReady(boolean ready) {
		this.isReady = ready;
	}
	
	public String getTeam() {
		return team;
	}
	
	public void setTeam(String newTeam) { // can set behaviors here too
		this.team = newTeam;
	}
	
	/** Override default object toString() method (which normally returns the memory address for the object) */
	@Override
	public String toString() {
		return "{UAPlayer {Name: " + player.getName() + ", UUID: " + player.getUniqueId().toString() + ", isReady: " + isReady + ", Team: " + team + "} }";
	}
}