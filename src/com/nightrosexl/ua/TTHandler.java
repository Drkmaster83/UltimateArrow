package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TTHandler implements Listener {    
    private UltimateArrow ua;
    
    public TTHandler(UltimateArrow ua) {
        this.ua = ua;
    }
    
    // team handling stuff...
    @EventHandler
    public void teamHandling(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        
        if (e.getAction() != Action.PHYSICAL) return;
        if (e.getClickedBlock().getType() == Material.STONE_PLATE && ua.getPlayer(p) != null) { //If they stepped on a stone pressure plate and we've added them to our roster
            UAPlayer player = ua.getPlayer(p);
            ua.getGameplay().checkReadyPeriod();
            if (e.getClickedBlock().getLocation().equals(ua.getRedPlate())) {
                player.setTeam("Red"); //Set their team!
                p.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + p.getName() + ", you have joined the Red Team!");
                p.teleport(ua.getRedSide()); //bad
            }
            
            if (e.getClickedBlock().getLocation().equals(ua.getBluePlate())) {
                player.setTeam("Blue"); //Remember, we could also teleport the player to a location if we had a UAPlayer#joinGame() method
                p.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + p.getName() + ", you have joined the Blue Team!");
                p.teleport(ua.getBlueSide()); //bad
            }
        // not in list and trigger plate? send message.
        } else if (ua.getPlayer(p) == null && (e.getClickedBlock().getLocation().equals(ua.getRedPlate()) || e.getClickedBlock().getLocation().equals(ua.getBluePlate()))) {
            p.sendMessage(ChatColor.RED + ua.getPrefix() +  "Please join the game to select a team!");
            p.sendMessage(ChatColor.RED + ua.getPrefix() + "Usage: /ua join");
        }
    }
    
    // ready up method
    @EventHandler
    public void readyUp(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        
        if(e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR) return;
        if(ua.getPlayer(p) == null) return; //Player's not in-game/in-arena
        UAPlayer player = ua.getPlayer(p);
        if(e.getClickedBlock().getType() == Material.STONE_BUTTON && !player.isReady()) {
            player.setReady(true);
            p.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + p.getName() + ", you are ready!");
            Gameplay gp = ua.getGameplay();
            gp.distributeEquipment(p); //Give the bow
            if(gp.getReadyAmount() == ua.getUAGeneralPlayerRoster().size()) {
                gp.endReadyPeriod(); //When this is called, a randomly selected player will receive the arrow.
            }
            // TODO: send message to everyone in match of players that are ready...
        }
    }
}

/*
TTHandler is responsible for handling teleportation and team management aspects of the Ultimate Arrow mini-game.

TODO:
- check if player steps on pressure plates -DONE-
- tidy this class up and do some major refactoring.
*/
