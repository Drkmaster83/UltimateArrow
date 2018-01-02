package com.nightrosexl.ua;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** This class stores our additional data while being hooked up with a Bukkit Player object*/
public class UAPlayer {
    private Player player;
    private boolean isReady;
    private String team;
    private Inventory inv;
    private Location prevLocation;

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

    public Location getOldLocation() {
        return prevLocation == null ? UltimateArrow.getInstance().getSelectArea() : prevLocation;
    }

    public void setOldLocation(Location prevLoc) {
        this.prevLocation = prevLoc;
    }

    public void giveInventoryBack() {
        for(int i = 0; i < inv.getContents().length; i++) {
            //inv.
        }
    }

    /** Override default object toString() method (which normally returns the memory address for the object) */
    @Override
    public String toString() {
        return "{UAPlayer {Name: " + player.getName() + ", UUID: " + player.getUniqueId().toString() + ", isReady: " + isReady + ", Team: " + team + "} }";
    }
}