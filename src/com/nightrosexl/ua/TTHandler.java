package com.nightrosexl.ua;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class TTHandler implements Listener {
    private UltimateArrow ua;
    
    String title;
	String teams;
	
	int minutes;
	int seconds;
	int score;
	int arrows;
	
	String time = minutes + ":" + seconds;

	ScoreboardManager manager;
	Scoreboard sboard;
	Score scoreDisplay;
	Team kdratio;
	Objective obj;
    
    public TTHandler(UltimateArrow ua) {
        this.ua = ua;
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
                setUpScoreboard(clickingPlayer);
            }
            // TODO: send message to everyone in match of players that are ready...
        }
    }
    
    public void setUpScoreboard(Player player) {
		manager = Bukkit.getScoreboardManager();
		sboard = manager.getNewScoreboard();
		
		obj = sboard.registerNewObjective("test", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.YELLOW + "Ultimate Arrow");
		
		player.setScoreboard(sboard);
	}
}

/*
TTHandler is responsible for handling teleportation and team management aspects of the Ultimate Arrow mini-game.

TODO:
- check if player steps on pressure plates -DONE-
- tidy this class up and do some major refactoring.
*/
