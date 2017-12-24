package com.nightrosexl.ua;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimateArrow extends JavaPlugin {
	private final String uaPrefix = "[Ultimate Arrow] ";
	private List<UAPlayer> ultimateArrowGeneralPlayerRoster = new ArrayList<UAPlayer>();
	private Location redTeamPlateLoc, redTeamSide, blueTeamPlateLoc, blueTeamSide, uaTeamSelectArea, viewing_deck1;
	private World w;
	private Gameplay gp;
	private TTHandler tt;
	
	@Override
	public void onEnable() {
		w = getServer().getWorld("world"); // Centralize the world object for all the locations
		redTeamPlateLoc = new Location(w, 408, 64, 636);
		redTeamSide = new Location(w, 317.5, 64, -350.5, 180f, 0f);
		blueTeamPlateLoc = new Location(w, 404, 64, 632);
		blueTeamSide = new Location(w, 317.5, 65, -442.5, 0f, 0f);
		uaTeamSelectArea = new Location(w, 406.5, 64, 634.5); // Teleport location is now in the middle of that block
		viewing_deck1 = new Location(w, 316, 72, -342);
		this.getCommand("ultimatearrow").setExecutor(new Commands(this));
		getServer().getPluginManager().registerEvents(tt = new TTHandler(this), this);
		getServer().getPluginManager().registerEvents(gp = new Gameplay(this), this);
	}

	@Override
	public void onDisable() {
		tt.cleanup();
	}

	public Location getRedSide() {
		return redTeamSide;
	}

	public Location getBlueSide() {
		return blueTeamSide;
	}

	public Location getRedPlate() {
		return redTeamPlateLoc;
	}

	public Location getBluePlate() {
		return blueTeamPlateLoc;
	}

	public Location getViewingArea() {
		return viewing_deck1;
	}

	public Location getSelectArea() {
		return uaTeamSelectArea;
	}

	public String getPrefix() {
		return uaPrefix;
	}

	public Gameplay getGameplay() {
		return gp;
	}

	// add
	/** @return true if the player wasn't already in-game, false if they were in-game */
	public boolean addToUAGeneralRoster(Player player, String team) {
		if (getPlayer(player) != null) return false; // They're already in the list, don't want a duplicate!
		ultimateArrowGeneralPlayerRoster.add(new UAPlayer(player, team));
		return true;
	}

	// remove
	/** @return true if the player was in-game and got removed, false if they weren't in-game */
	public boolean removeFromUAGeneralRoster(Player player) {
		if (getPlayer(player) == null) return false; // Don't want to remove player if they're not in the game!
		ultimateArrowGeneralPlayerRoster.remove(getPlayer(player));
		return true;
	}

	public UAPlayer getPlayer(Player player) {
		for (UAPlayer gamePlayer : getUAGeneralPlayerRoster()) {
			if (gamePlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) return gamePlayer;
		}
		return null;
	}

	public List<UAPlayer> getUAGeneralPlayerRoster() {
		return ultimateArrowGeneralPlayerRoster;
	}

	public List<UAPlayer> getRedTeamPlayers() {
		List<UAPlayer> redTeam = new ArrayList<UAPlayer>();
		for (UAPlayer gamePlayer : getUAGeneralPlayerRoster()) {
			if (gamePlayer.getTeam().equalsIgnoreCase("Red")) redTeam.add(gamePlayer);
		}
		return redTeam;
	}

	public List<UAPlayer> getBlueTeamPlayers() {
		List<UAPlayer> blueTeam = new ArrayList<UAPlayer>();
		for (UAPlayer gamePlayer : getUAGeneralPlayerRoster()) {
			if (gamePlayer.getTeam().equalsIgnoreCase("Blue")) blueTeam.add(gamePlayer);
		}
		return blueTeam;
	}
}

/*
 * Ultimate Arrow a mini-game conceived by NightRoseXL.
 * 
 * Objective of Ultimate Arrow:
 * 
 * Everybody has a bow
 * There will be 1-5 arrows in play depending on the number of players and preferences set beforehand.
 * Different colored arrows are potion arrows. (Perhaps it gives particle, but does not give effects though).
 * You 'pass' the arrow by shooting it.
 * When hit, you don't take knockback, and the arrow appears in your inventory.
 * When you have it, you can only walk four steps before being frozen by the game until you shoot it.
 * Once you get it to the other side of the arena, it is given to a random player.
 * If you miss your teammate, the arrow goes to the nearest opponent.
 * If a member of the opposite team is able to hit you five times, they get the arrow you have.
 * You are only allowed to have one arrow at a time.
 */

/*
 * TODO:
 * - Test out teleport stuff. -DONE-
 * - Add in team-balancing later on. -Work in Progress-
 * -
 */