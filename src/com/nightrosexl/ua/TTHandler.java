package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TTHandler implements Listener {
    private UltimateArrow ua;

    int minutes; // I strongly recommend to just store seconds, and divide it out into minutes and seconds.
    int seconds;
    int score;
    int arrows;
    
    Scoreboard sboard; // This is the universal scoreboard, we don't want to let this go.
    Score scoreDisplay;
    Team kdratio, time; // If you have a K/D ratio, that has to be a player-specific scoreboard.
    
    BukkitTask scoreboardUpdate;

    public TTHandler(UltimateArrow ua) {
    	this.ua = ua;
    	
    	sboard = ua.getServer().getScoreboardManager().getNewScoreboard();
    	Objective obj = sboard.registerNewObjective("test", "dummy");
    	obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.YELLOW + "Ultimate Arrow");
		kdratio = sboard.registerNewTeam("kdratio");
		time = sboard.registerNewTeam("time");
		time.setDisplayName("Test");
		scoreboardUpdate = new BukkitRunnable() {
			public void run() {
				updateScoreboard();
			}
		}.runTaskTimer(ua, 0L, 10L);
    }

    // team handling stuff...
    @EventHandler
    public void teamHandling(PlayerInteractEvent e) {
        Player clickingPlayer = e.getPlayer();
        
        if (e.getAction() != Action.PHYSICAL) return;
        if (e.getClickedBlock().getType() == Material.STONE_PLATE && ua.getPlayer(clickingPlayer) != null) { // If they stepped on a stone pressure plate and we've added them to our roster
            UAPlayer gamePlayer = ua.getPlayer(clickingPlayer);
            ua.getGameplay().checkReadyPeriod();
            if (e.getClickedBlock().getLocation().equals(ua.getRedPlate())) {
                gamePlayer.setTeam("Red"); //Set their team!
                clickingPlayer.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + clickingPlayer.getName() + ", you have joined the Red Team!");
                clickingPlayer.teleport(ua.getRedSide());
            }
            
            if (e.getClickedBlock().getLocation().equals(ua.getBluePlate())) {
                gamePlayer.setTeam("Blue"); // Remember, we could also teleport the player to a location if we had a UAPlayer#joinGame() method
                clickingPlayer.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + clickingPlayer.getName() + ", you have joined the Blue Team!");
                clickingPlayer.teleport(ua.getBlueSide());
            }
        // not in list and trigger plate? send message.
        } else if (ua.getPlayer(clickingPlayer) == null && (e.getClickedBlock().getLocation().equals(ua.getRedPlate()) || e.getClickedBlock().getLocation().equals(ua.getBluePlate()))) {
            clickingPlayer.sendMessage(ChatColor.RED + ua.getPrefix() +  "Please join the game to select a team!");
            clickingPlayer.sendMessage(ChatColor.RED + ua.getPrefix() + "Usage: /ua join");
        }
    }
    
    // if player leaves server, remove information
    @EventHandler
    public void removeUponDisconnection(PlayerQuitEvent e) {
    	Player leavingPlayer = e.getPlayer();

    	ua.removeFromUAGeneralRoster(leavingPlayer);
    }
    
    // ready up method
    @EventHandler
    public void readyUp(PlayerInteractEvent e) {
        Player clickingPlayer = e.getPlayer();
        
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR) return;
        if (ua.getPlayer(clickingPlayer) == null) return; // Player's not in-game/in-arena
        UAPlayer gamePlayer = ua.getPlayer(clickingPlayer);
        if (e.getClickedBlock().getType() == Material.STONE_BUTTON && !gamePlayer.isReady()) {
            gamePlayer.setReady(true);
            clickingPlayer.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + clickingPlayer.getName() + ", you are ready!");
            Gameplay gp = ua.getGameplay();
            gp.distributeEquipment(clickingPlayer); // Give the bow
            if (gp.getReadyAmount() == ua.getUAGeneralPlayerRoster().size()) {
                gp.endReadyPeriod(); // When this is called, a randomly selected player will receive the arrow.
            }
            // TODO: send message to everyone in match of players that are ready...
        }
    }
    
    /** Updates scoreboard data for all gameplayers */
    public void updateScoreboard() {
    	String time = minutes + ":" + seconds;
    	this.time.setDisplayName(time);
		for(UAPlayer gamePlayer : ua.getUAGeneralPlayerRoster()) {
			gamePlayer.getPlayer().setScoreboard(sboard);
		}
	}
    
    public void cleanup() {
    	scoreboardUpdate.cancel();
    }
}

/*
TTHandler is responsible for handling teleportation and team management aspects of the Ultimate Arrow mini-game.

TODO:
- check if player steps on pressure plates -DONE-
- tidy this class up and do some major refactoring.
*/
