package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Gameplay implements Listener {

    private UltimateArrow ua;
    private boolean gameStarted, timerStarted, readyPeriod;
    private int timeUntilStart, minPlayersToBegin;
    private BukkitTask readyCheckTask;
    private UAPlayer arrowPlayer;

    private ItemStack arrow = new ItemStack(Material.ARROW, 1);
    
    public Gameplay(UltimateArrow ua) {
        this.ua = ua;
        this.readyPeriod = false;
        this.timerStarted = false;
        this.gameStarted = false;
        this.timeUntilStart = 60;
        this.minPlayersToBegin = 2; /*official version< 5*/
    }
    
    public ItemStack getArrowItem() {
        return arrow;
    }

    public void distributeEquipment(Player p) {
        // distribute bow to all players
        ItemStack bow = new ItemStack(Material.BOW, 1);
        p.getInventory().addItem(bow);
        p.updateInventory();
    }
    
    public void revokeEquipment(Player p) {
        p.getInventory().remove(Material.BOW);
        p.updateInventory();
    }
    
    public void broadcastMessage(String message) { // Tells the same message to all players in-game.
        for(UAPlayer uap : ua.getUAGeneralPlayerRoster()) {
            uap.getPlayer().sendMessage(message.replace("{PLAYER}", uap.getPlayer().getName())); // You could add other formatting stuff here later, like time left, kills, etc. Up to you.
        }
    }
    
    public void checkReadyPeriod() {
        if(readyPeriod) return;
        readyPeriod = true;
        broadcastMessage(ChatColor.GREEN + ua.getPrefix() + "{PLAYER}, the game will begin in one minute!");
        readyCheckTask = new BukkitRunnable() {
            int timerVal = timeUntilStart;
            @Override
            public void run() {
                if(ua.getUAGeneralPlayerRoster().size() < minPlayersToBegin) { // Waiting on more players to begin counting down.
                    timerVal = timeUntilStart;
                    return;
                }
                if (!timerStarted) timerStarted = true;
                if (--timerVal > 0 && getReadyAmount() < ua.getUAGeneralPlayerRoster().size()) return; // This will continue going down if the timer is 0 or less, or if the amount of players ready are the same as who's in-game (AKA everyone's ready)
                if (timerVal <= 0) { // Time's up! We're not waiting on readying players anymore, we're kicking them.
                    for (UAPlayer uap : ua.getUAGeneralPlayerRoster()) { // Loop through all players in game
                        if (!uap.isReady()) { // This player isn't ready, screw em
                            ua.removeFromUAGeneralRoster(uap.getPlayer()); // Possible concurrent modification problem, calling it
                            revokeEquipment(uap.getPlayer());
                            uap.getPlayer().teleport(ua.getViewingArea());
                            uap.getPlayer().sendMessage(ChatColor.DARK_RED + ua.getPrefix() + uap.getPlayer().getName() + ", you have been removed from the match as you are not ready.");
                        }
                    }
                }
                this.cancel();
                endReadyPeriod();
            }
        }.runTaskTimer(ua, 0L, 20L); // run every 1 second
    }
    
    public void endReadyPeriod() { // Essentially these chained methods run the game. startReadyPeriod() -> endReadyPeriod() -> selectArrowPlayer()
        if (gameStarted) return; // Don't want to redo this method if the game's already began
        if (readyCheckTask != null) readyCheckTask.cancel();
        readyCheckTask = null;
        selectRandArrowPlayer();
        broadcastMessage(ChatColor.DARK_GREEN + ua.getPrefix() + "GAME BEGIN!");
        timerStarted = false;
        readyPeriod = false;
        gameStarted = true;
    }
    
    public void selectRandArrowPlayer() {
        // give one random in-match player in game an arrow
        int listSize = (int) (Math.random() * ua.getUAGeneralPlayerRoster().size());
        UAPlayer uapArrow = ua.getUAGeneralPlayerRoster().get(listSize);

        // distribute arrow
        setArrowPlayer(uapArrow);
    }
    
    public void endGame() { // You choose when to call this I guess
        gameStarted = false;
        readyPeriod = false;
        timerStarted = false;
        // Do whatever else clean up required, remove items, teleport players to lobby, etc
    }
    
    public UAPlayer getArrowPlayer() {
        return arrowPlayer;
    }
    
    public void setArrowPlayer(UAPlayer uapArrow) {
        if(arrowPlayer != null) {
            arrowPlayer.getPlayer().getInventory().remove(Material.ARROW); // Ensures that our previous holder gets no duplicate arrows
            arrowPlayer.getPlayer().updateInventory();
        }
        arrowPlayer = uapArrow;
        uapArrow.getPlayer().getInventory().addItem(arrow);
        uapArrow.getPlayer().updateInventory();
        broadcastMessage(ChatColor.LIGHT_PURPLE + ua.getPrefix() + uapArrow.getPlayer().getName() + " has the arrow!"); // This method already paid off
    }
    public int getReadyAmount() {
        int readyAmt = 0;
        for(UAPlayer uap : ua.getUAGeneralPlayerRoster()) {
            if (uap.isReady()) readyAmt++;
        }
        return readyAmt;
    }
    
    public void freezePlayer() {
        // freeze player after walking four blocks
    }
}
