package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.nightrosexl.ua.Gameplay.GameState;

public class PreGameplayEvents implements Listener {
    private UltimateArrow ua;

    int seconds;
    int score = 0;
    int arrows;

    private Scoreboard sboard; // This is the universal scoreboard, we don't want to let this go.
    private Objective obj;

    private BukkitTask scoreboardUpdate;
    private boolean enteredEndZone;
    
    public PreGameplayEvents(UltimateArrow ua) {
        this.ua = ua;

        sboard = ua.getServer().getScoreboardManager().getNewScoreboard();
        obj = sboard.registerNewObjective("test", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ChatColor.YELLOW + "Ultimate Arrow");
        scoreboardUpdate = new BukkitRunnable() {
            public void run() {
                updateScoreboard();
            }
        }.runTaskTimer(ua, 0L, 10L);
    }
    
    // ready up method
    @EventHandler
    public void readyUp(PlayerInteractEvent e) {
        Player clickingPlayer = e.getPlayer();

        if (e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR) return;
        if (ua.getPlayer(clickingPlayer) == null) return; // Player's not in-game/in-arena
        if (ua.getGameplay().getState() != GameState.WAITING_READY) return; //We don't want to react to the buttons if we're in this state
        UAPlayer gamePlayer = ua.getPlayer(clickingPlayer);
        if (e.getClickedBlock().getType() == Material.STONE_BUTTON && !gamePlayer.isReady()) {
            gamePlayer.setReady(true);
            clickingPlayer.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + clickingPlayer.getName() + ", you are ready!");
            Gameplay gp = ua.getGameplay();
            gp.distributeEquipment(clickingPlayer); // Give the bow
            if (gp.getReadyAmount() == ua.getGameplay().getPlayerRoster().size()) {
                gp.beginGame(); // When this is called, a randomly selected player will receive the arrow.
            }
            // TODO: send message to everyone in match of players that are ready...
        }
    }

    // team handling stuff...
    @EventHandler
    public void teamHandling(PlayerInteractEvent e) {
        Player clickingPlayer = e.getPlayer();

        if (e.getAction() != Action.PHYSICAL) return;
        if (ua.getGameplay().getState() != GameState.WAITING_READY) {
            clickingPlayer.sendMessage(ChatColor.RED + ua.getPrefix() + "Game already underway!");
            return; //Don't listen to this if the game is in progress
        }
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.STONE_PLATE) return;
        if (ua.getPlayer(clickingPlayer) != null) { // If they stepped on a stone pressure plate and we've added them to our roster
            UAPlayer gamePlayer = ua.getPlayer(clickingPlayer);
            ua.getGameplay().startReadyPeriod();
            if (e.getClickedBlock().getLocation().equals(ua.getRedPlate())) {
                gamePlayer.setTeam("Red"); //Set their team!
                clickingPlayer.teleport(ua.getRedSide());
                clickingPlayer.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + clickingPlayer.getName() + ", you have joined the Red Team!");
            }
            else if (e.getClickedBlock().getLocation().equals(ua.getBluePlate())) {
                gamePlayer.setTeam("Blue"); // Remember, we could also teleport the player to a location if we had a UAPlayer#joinGame() method
                clickingPlayer.teleport(ua.getBlueSide());
                clickingPlayer.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + clickingPlayer.getName() + ", you have joined the Blue Team!");
            }
            // not in list and trigger plate? send message.
        } else if (ua.getPlayer(clickingPlayer) == null && (e.getClickedBlock().getLocation().equals(ua.getRedPlate()) || e.getClickedBlock().getLocation().equals(ua.getBluePlate()))) {
            if(ua.getGameplay().getQueue().contains(clickingPlayer)) {
                clickingPlayer.sendMessage(ChatColor.RED + ua.getPrefix() + "Team selection will be unlocked once the other players have been teleported to the select area.");
                return;
            }
            clickingPlayer.sendMessage(ChatColor.RED + ua.getPrefix() + "Please join the game to select a team!");
            clickingPlayer.sendMessage(ChatColor.RED + ua.getPrefix() + "Usage: /ua join");
        }
    }

    // if player leaves server, remove information
    @EventHandler
    public void removeUponDisconnection(PlayerQuitEvent e) {
        Player leavingPlayer = e.getPlayer();
        ua.getGameplay().cleanup(leavingPlayer);
    }

    /** Updates scoreboard data for all gameplayers */
    public void updateScoreboard() {
        //String time = (seconds / 60) + ":" + (seconds % 60);
        this.obj.getScore("Score").setScore(score);
        for(UAPlayer gamePlayer : ua.getGameplay().getPlayerRoster()) {
            gamePlayer.getPlayer().setScoreboard(sboard);
        }
    }

    @EventHandler
    public void playerMovementDetection(PlayerMoveEvent e) {
        if(e.getTo().getBlock().equals(e.getFrom().getBlock())) return;
        if(ua.getPlayer(e.getPlayer()) == null) return; //Not a game player
        Player moving = e.getPlayer();
        UAPlayer gamePlayer = ua.getPlayer(moving);
        Block b = moving.getLocation().getBlock().getRelative(BlockFace.DOWN, 2);

        if (b.getType() == Material.REDSTONE_BLOCK && gamePlayer.getTeam().equalsIgnoreCase("Blue")) {
            moving.sendMessage("TEST: In the red zone, blue team is given one point!");
            // update scoreboard, increment score.
        }
        else if (b.getType() == Material.LAPIS_BLOCK && gamePlayer.getTeam().equalsIgnoreCase("Red")) {
            moving.sendMessage("TEST: In the blue zone, red team is given one point!");
            // update scoreboard, increment score.
        }
    }

    /*
     * Players need to pass the arrow to the opposite side of the arena to be rewarded with a point.
     * Get player and their team and check if they are on their team's side of the arena with arrow.
     * If not, then do not increase their team's score.
     * If player is on wooden plank, with blue glass underneath and is on the red team, give them point.
     * If player is on wooden plank, with red glass underneath and is on the blue team, give them point.
     */

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