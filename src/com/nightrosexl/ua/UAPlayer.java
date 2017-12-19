package com.nightrosexl.ua;

import org.bukkit.entity.Player;

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
	
}
