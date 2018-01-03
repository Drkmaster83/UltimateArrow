package com.nightrosexl.ua;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Gameplay {
    public enum GameState {WAITING_READY, IN_GAME, ENDED};

    private UltimateArrow ua;
    private Set<Player> queued;

    private List<UAPlayer> ultimateArrowGeneralPlayerRoster;
    private int timeUntilStart, minPlayersToBegin, arrowHitRadius;
    private BukkitTask readyCheckTask;
    private UAPlayer arrowPlayer;
    private GameState state;

    private ItemStack arrow = new ItemStack(Material.ARROW, 1);
    private ItemStack bow = new ItemStack(Material.BOW, 1);

    public Gameplay(UltimateArrow ua) {
        this.ua = ua;
        
        ItemMeta bim = bow.getItemMeta();
        bim.setUnbreakable(true);
        bow.setItemMeta(bim);
        
        queued = new HashSet<Player>();
        ultimateArrowGeneralPlayerRoster = new ArrayList<UAPlayer>();
        this.timeUntilStart = 60;
        this.arrowHitRadius = 5;
        this.minPlayersToBegin = 1; /* TODO official version< 5*/
        state = GameState.WAITING_READY;
    }

    public void distributeEquipment(Player player) {
        // distribute bow to player
        player.getInventory().addItem(bow);
        player.updateInventory();
    }

    public void revokeEquipment(Player player) {
        player.getInventory().remove(Material.BOW);
        if(getArrowPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())) player.getInventory().remove(arrow);
        player.updateInventory();
    }

    public void broadcastMessage(String message) { // Tells the same message to all players in-game.
        for(UAPlayer gamePlayer : getPlayerRoster()) {
            gamePlayer.getPlayer().sendMessage(message.replace("{PLAYER}", gamePlayer.getPlayer().getName())); // You could add other formatting stuff here later, like time left, kills, etc. Up to you.
        }
    }

    public boolean canBegin() {
        return getPlayerRoster().size() >= minPlayersToBegin && getPlayerRoster().size() <= 10; // TODO perhaps add a check in addToRoster to check if size is at capacity
    }

    public boolean shouldEnd() {
        return getPlayerRoster().size() < minPlayersToBegin; // TODO: Too few players
    }

    public void startReadyPeriod() {
        if(state == GameState.WAITING_READY) return;
        state = GameState.WAITING_READY;
        broadcastMessage(ChatColor.GREEN + ua.getPrefix() + "{PLAYER}, the game will begin in one minute!");
        readyCheckTask = new BukkitRunnable() {
            int countdown = timeUntilStart;
            int intervalAnnounce = 0;
            boolean halted = false;
            public void run() {
                if(getPlayerRoster().size() < minPlayersToBegin) { // Waiting on more players to begin counting down.
                    countdown = timeUntilStart;
                    if(intervalAnnounce-- <= 0) {
                        intervalAnnounce = 20;
                        broadcastMessage(ChatColor.RED + ua.getPrefix() + "Countdown " + (halted ? "is currently halted" : "has been reset and halted") + " due to insufficient players.");
                        if(!halted) halted = true;
                    }
                    return;
                }
                if(halted) halted = false;
                if (--countdown > 0 && getReadyAmount() < getPlayerRoster().size()) return; // This will continue going down if the timer is 0 or less, or if the amount of players ready are the same as who's in-game (AKA everyone's ready)
                if (getReadyAmount() < getPlayerRoster().size()) { // Time's up! We're not waiting on readying players anymore, we're kicking them.
                    for (int i = 0; i < getPlayerRoster().size(); i++) { // Loop through all players in game
                        UAPlayer gamePlayer = getPlayerRoster().get(i);
                        if (!gamePlayer.isReady()) { // This player isn't ready, screw em
                            removeFromRoster(gamePlayer.getPlayer());
                            i--; // Since we removed them from the list, all the other entries in the list have shifted left, so we need to shift back one to counteract the i++ that happens after this cycle of the loop completes
                            revokeEquipment(gamePlayer.getPlayer());
                            gamePlayer.getPlayer().teleport(ua.getViewingArea());
                            gamePlayer.getPlayer().sendMessage(ChatColor.DARK_RED + ua.getPrefix() + gamePlayer.getPlayer().getName() + ", you have been removed from the match as you are not ready.");
                        }
                    }
                }
                this.cancel();
                beginGame();
            }
        }.runTaskTimer(ua, 0L, 20L); // run every 1 second
    }

    public void beginGame() { // Essentially, these chained methods run the game. startReadyPeriod() -> endReadyPeriod() -> selectArrowPlayer()
        if (state == GameState.IN_GAME) return; // Don't want to redo this method if the game's already began
        if (readyCheckTask != null) readyCheckTask.cancel();
        readyCheckTask = null;
        state = GameState.IN_GAME;
        selectRandArrowPlayer();
        broadcastMessage(ChatColor.DARK_GREEN + ua.getPrefix() + "GAME HAS BEGUN!");
        // TODO start timer task here
    }

    public void endGame() { // You choose when to call this I guess
        if (state == GameState.ENDED) return;

        // remove arrow
        getArrowPlayer().getPlayer().getInventory().removeItem(arrow);
        // Do whatever else clean up required, remove items, teleport players to lobby, etc

        state = GameState.ENDED;
        // TODO potentially schedule a nice delay task teleporting the player to their previous location before entering the game
        // at end of task, reset data to baseline.
    }

    public void selectRandArrowPlayer() {
        // give one random in-match player in game an arrow
        int listSize = (int) (Math.random() * getPlayerRoster().size());
        UAPlayer randomlySelectedGamePlayer = getPlayerRoster().get(listSize);

        // distribute arrow to random player
        setArrowPlayer(randomlySelectedGamePlayer);
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
    
    public void tpToSelectArea(Player player) {
        tpToSelectArea(player, true);
    }

    public void tpToSelectArea(Player player, boolean sendMessage) {
        player.teleport(ua.getSelectArea());
        if (sendMessage) player.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + player.getName() + ", you have been teleported to the team selection area!");
    }

    public void freezePlayer() {
        // freeze player after walking four blocks with arrow in inventory.
    }

    // add
    /** @return true if the player wasn't already in-game, false if they were in-game */
    public boolean addToRoster(Player player, String team) {
        if (getPlayer(player) != null) return false; // They're already in the list, don't want a duplicate!
        ultimateArrowGeneralPlayerRoster.add(new UAPlayer(player, team));
        return true;
    }

    // remove
    /** @return true if the player was in-game and got removed, false if they weren't in-game */
    public boolean removeFromRoster(Player player) {
        if (getPlayer(player) == null) return false; // Don't want to remove player if they're not in the game!
        ultimateArrowGeneralPlayerRoster.remove(getPlayer(player));
        queued.remove(player);
        player.setScoreboard(player.getServer().getScoreboardManager().getNewScoreboard());  // 'remove' player's scoreboard.
        return true;
    }

    public void cleanup(Player leavingPlayer) {
        if(queued.contains(leavingPlayer)) queued.remove(leavingPlayer);
        removeFromRoster(leavingPlayer);
        if(arrowPlayer.getPlayer().getUniqueId().equals(leavingPlayer.getUniqueId())) selectRandArrowPlayer();
    }

    public UAPlayer getPlayer(Player player) {
        for (UAPlayer gamePlayer : getPlayerRoster()) {
            if (gamePlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) return gamePlayer;
        }
        return null;
    }
    
    public UAPlayer getArrowPlayer() {
        return arrowPlayer;
    }

    public ItemStack getArrowItem() {
        return arrow;
    }

    public boolean isTimerRunning() {
        return readyCheckTask != null; // TODO || gameCountdownTask != null
    }

    public Set<Player> getQueue() {
        return queued;
    }
    
    public GameState getState() {
        return state;
    }

    public int getArrowRadius() {
        return arrowHitRadius;
    }
    
    public int getReadyAmount() {
        int readyAmt = 0;
        for(UAPlayer gamePlayer : getPlayerRoster()) { // Run through all in-game players
            if (gamePlayer.isReady()) readyAmt++;
        }
        return readyAmt;
    }

    public List<UAPlayer> getPlayerRoster() {
        return ultimateArrowGeneralPlayerRoster;
    }

    public List<UAPlayer> getRedTeamPlayers() {
        List<UAPlayer> redTeam = new ArrayList<UAPlayer>();
        for (UAPlayer gamePlayer : getPlayerRoster()) {
            if (gamePlayer.getTeam().equalsIgnoreCase("Red")) redTeam.add(gamePlayer);
        }
        return redTeam;
    }

    public List<UAPlayer> getBlueTeamPlayers() {
        List<UAPlayer> blueTeam = new ArrayList<UAPlayer>();
        for (UAPlayer gamePlayer : getPlayerRoster()) {
            if (gamePlayer.getTeam().equalsIgnoreCase("Blue")) blueTeam.add(gamePlayer);
        }
        return blueTeam;
    }
}