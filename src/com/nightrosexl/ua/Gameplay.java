package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Gameplay implements Listener {
	public enum GameState {WAITING_READY, IN_GAME, ENDED};
	
    private UltimateArrow ua;
    private boolean gameStarted, timerStarted, readyPeriod;
    private int timeUntilStart, minPlayersToBegin, arrowHitRadius;
    private BukkitTask readyCheckTask;
    private UAPlayer arrowPlayer;
    private GameState state;

    private ItemStack arrow = new ItemStack(Material.ARROW, 1);
    
    public Gameplay(UltimateArrow ua) {
        this.ua = ua;
        this.readyPeriod = false;
        this.timerStarted = false;
        this.gameStarted = false;
        this.timeUntilStart = 60;
        this.minPlayersToBegin = 2; /*official version< 5*/
        state = GameState.WAITING_READY;
    }
    
    public ItemStack getArrowItem() {
        return arrow;
    }

    public void distributeEquipment(Player player) {
        // distribute bow to all players
        ItemStack bow = new ItemStack(Material.BOW, 1);
        player.getInventory().addItem(bow);
        player.updateInventory();
    }
    
    public void revokeEquipment(Player player) {
        player.getInventory().remove(Material.BOW);
        player.updateInventory();
    }
    
    public void broadcastMessage(String message) { // Tells the same message to all players in-game.
        for(UAPlayer gamePlayer : ua.getUAGeneralPlayerRoster()) {
            gamePlayer.getPlayer().sendMessage(message.replace("{PLAYER}", gamePlayer.getPlayer().getName())); // You could add other formatting stuff here later, like time left, kills, etc. Up to you.
        }
    }
    
    public void checkReadyPeriod() {
        if(readyPeriod) return;
        
        readyPeriod = true;
        broadcastMessage(ChatColor.GREEN + ua.getPrefix() + "{PLAYER}, the game will begin in one minute!");
        readyCheckTask = new BukkitRunnable() {
            int timerVal = timeUntilStart;
            public void run() {
                if(ua.getUAGeneralPlayerRoster().size() < minPlayersToBegin) { // Waiting on more players to begin counting down.
                    timerVal = timeUntilStart;
                    return;
                }
                if (!timerStarted) timerStarted = true;
                if (--timerVal > 0 && getReadyAmount() < ua.getUAGeneralPlayerRoster().size()) return; // This will continue going down if the timer is 0 or less, or if the amount of players ready are the same as who's in-game (AKA everyone's ready)
                if (timerVal <= 0) { // Time's up! We're not waiting on readying players anymore, we're kicking them.
                    for (UAPlayer gamePlayer : ua.getUAGeneralPlayerRoster()) { // Loop through all players in game
                        if (!gamePlayer.isReady()) { // This player isn't ready, screw em
                            ua.removeFromUAGeneralRoster(gamePlayer.getPlayer()); // Possible concurrent modification problem, calling it
                            revokeEquipment(gamePlayer.getPlayer());
                            gamePlayer.getPlayer().teleport(ua.getViewingArea());
                            gamePlayer.getPlayer().sendMessage(ChatColor.DARK_RED + ua.getPrefix() + gamePlayer.getPlayer().getName() + ", you have been removed from the match as you are not ready.");
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
        UAPlayer randomlySelectedGamePlayer = ua.getUAGeneralPlayerRoster().get(listSize);

        // distribute arrow
        setArrowPlayer(randomlySelectedGamePlayer);
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
    
    public void setArrowPlayer(UAPlayer newArrowPlayer) {
        if(arrowPlayer != null) {
            arrowPlayer.getPlayer().getInventory().remove(Material.ARROW); // Ensures that our previous holder gets no duplicate arrows
            arrowPlayer.getPlayer().updateInventory();
        }
        arrowPlayer = newArrowPlayer;
        newArrowPlayer.getPlayer().getInventory().addItem(arrow);
        newArrowPlayer.getPlayer().updateInventory();
        broadcastMessage(ChatColor.LIGHT_PURPLE + ua.getPrefix() + newArrowPlayer.getPlayer().getName() + " has the arrow!"); // This method already paid off
    }
    public int getReadyAmount() {
        int readyAmt = 0;
        for(UAPlayer gamePlayer : ua.getUAGeneralPlayerRoster()) { //Run through all in-game players
            if (gamePlayer.isReady()) readyAmt++;
        }
        return readyAmt;
    }
    
    public GameState getState() {
    	return state;
    }
    
    public void freezePlayer() {
        // freeze player after walking four blocks
    }
    
    @EventHandler
    public void arrowBehavior(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() != EntityType.ARROW) return; // Make sure to import EntityType, not a damaging arrow
        if (e.getEntity().getType() != EntityType.PLAYER) return; // Not a damaged player
        Player damaged = (Player) e.getEntity();
        if (ua.getPlayer(damaged) == null) return; // Not in general roster of players, so not in game.
        Arrow a = (Arrow) e.getDamager();
        a.setKnockbackStrength(0);
        e.setDamage(0.0);
        a.remove();
        e.setCancelled(true);
        
        damaged.damage(1.0); // if we call this with 0, it doesn't do anything, so we call it with 1, and then heal the player of the damage.
        damaged.setHealth(damaged.getHealth()+1);
        
        // get player hit with arrow, give them arrow.
        setArrowPlayer(ua.getPlayer(damaged));
    }

    @EventHandler
    public void onArrowHitBlock(ProjectileHitEvent e) {
        if(e.getHitEntity() != null) return; // We only want to handle this event if it strikes a block, not a player or something.
        if (!(e.getEntity() instanceof Arrow) || !((Arrow)e.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) (Arrow)e.getEntity().getShooter();
        Location hit = e.getHitBlock().getLocation();
        double nearestDistanceSquared = Double.MAX_VALUE;
        UAPlayer nearestPlayerToArrow = null;
        for(Entity ent : e.getEntity().getNearbyEntities(arrowHitRadius, arrowHitRadius, arrowHitRadius)) {
            if(!(ent instanceof Player)) continue;
            if(ent.getUniqueId().equals((shooter.getUniqueId()))) continue; //Don't want to do this, just take care of it below
            double distSquared = hit.distanceSquared(ent.getLocation());
            if(distSquared < nearestDistanceSquared) { // We've found an entity that's nearer than our previous one
                nearestDistanceSquared = distSquared;
                nearestPlayerToArrow = ua.getPlayer((Player)ent);
            }
        }
        
        if(nearestPlayerToArrow == null || nearestDistanceSquared == Double.MAX_VALUE) { // No player found/nearest distance still farthest possible
            getArrowPlayer().getPlayer().getInventory().addItem(getArrowItem()); // give them another arrow since no player can take possession
            e.getEntity().remove(); // Remove the old arrow
            return;
        }
        // Player must have been found if we're here
        setArrowPlayer(nearestPlayerToArrow);
    }
}