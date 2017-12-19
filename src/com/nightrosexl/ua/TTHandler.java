package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

<<<<<<< HEAD
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
        if (e.getClickedBlock().getType() == Material.STONE_PLATE && ua.getPlayer(p) != null) { // If they stepped on a stone pressure plate and we've added them to our roster
            UAPlayer player = ua.getPlayer(p);
            if (e.getClickedBlock().getLocation().equals(ua.getRedPlate())) {
                player.setTeam("Red"); // Set their team!
                p.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + p.getName() + ", you have joined the Red Team!");
                p.teleport(ua.getRedSide());
            }
            
            if (e.getClickedBlock().getLocation().equals(ua.getBluePlate())) {
                player.setTeam("Blue"); //Remember, we could also teleport the player to a location if we had a UAPlayer#joinGame() method
                p.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + p.getName() + ", you have joined the Blue Team!");
                p.teleport(ua.getBlueSide());
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
        
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR) return;
        if (ua.getPlayer(p) == null) return; // Player's not in-game/in-arena
        UAPlayer player = ua.getPlayer(p);
        if(e.getClickedBlock().getType() == Material.STONE_BUTTON && !player.isReady()) {
            player.setReady(true);
            p.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + p.getName() + ", you are ready!");
            Gameplay gp = ua.getGameplay();
            gp.distributeEquipment(p);
            if (gp.getReadyAmount() == ua.getUAGeneralPlayerRoster().size()) {
            	gp.endReadyPeriod();
            }
            
            // TODO: send message to everyone in match of players that are ready...
        }
    }
=======
import net.md_5.bungee.api.ChatColor;

public class TTHandler implements Listener {

	UltimateArrow ua;
	Gameplay gp;
	Player p;
	Location redTeamPlateLoc, redTeamSide, blueTeamPlateLoc, blueTeamSide, uaTeamSelectArea, viewing_deck1;
	World w;
	
	UUID playerUUID;
	String uaPrefix = "[Ultimate Arrow] ";
	String playerName;
	
	boolean isReady = false;
	
	public TTHandler(UltimateArrow ua) {
		this.ua = ua;
		gp = new Gameplay(this.ua);
	}
	
	// team handling stuff...
	@EventHandler
	public void teamHandling(PlayerInteractEvent e) {
		p = e.getPlayer();
		w = p.getWorld();
		playerUUID = p.getUniqueId();
		playerName = p.getName();
		redTeamPlateLoc = new Location(w, 408, 64, 636);
        blueTeamPlateLoc = new Location(w, 404, 64, 632);
        
        	// check if block has been clicked first...
        	if (e.getClickedBlock().getType() == Material.STONE_PLATE && ua.getUAGeneralPlayerRoster().contains(playerUUID)) {
    			if (e.getClickedBlock().getLocation().equals(redTeamPlateLoc)) {
    				ua.addToRedTeamRoster(playerUUID);
    				p.sendMessage(ChatColor.DARK_GREEN + uaPrefix + playerName + ", you have joined the Red Team!");
    				teleportPlayer(317, 65, -351);
    			}
    			
    			if (e.getClickedBlock().getLocation().equals(blueTeamPlateLoc)) {
    				ua.addToBlueTeamRoster(playerUUID);
    				p.sendMessage(ChatColor.DARK_GREEN + uaPrefix + playerName + ", you have joined the Blue Team!");
    				teleportPlayer(317, 65, -443);
    			}
    		
    		// not in list and trigger plate? send message.
        	} else if (!(ua.getUAGeneralPlayerRoster().contains(playerUUID)) && e.getClickedBlock().getLocation().equals(redTeamPlateLoc) || e.getClickedBlock().getLocation().equals(blueTeamPlateLoc)) {
    				p.sendMessage(ChatColor.RED + uaPrefix +  "Please join the game to select a team!");
    				p.sendMessage(ChatColor.RED + uaPrefix + "Usage: /ua join");
    		}
	}
	
	// ready up method
	@EventHandler
	public void readyUp(PlayerInteractEvent e) {
		p = e.getPlayer();
		
		if (ua.getUAGeneralPlayerRoster().contains(playerUUID) && e.getClickedBlock().getType() == Material.STONE_BUTTON && !isReady) {
			isReady = true;
			p.sendMessage(ChatColor.DARK_GREEN + uaPrefix + playerName + ", you are ready!");
			gp.distributeEquipment(p);
			
			// TODO: send message to everyone in match of players that are ready...
		} //else {}
		
		// kick players after 1 minute, if not ready
		Bukkit.getScheduler().scheduleSyncDelayedTask(ua, new Runnable() {
			public void run() {
				if (!isReady && ua.getUAGeneralPlayerRoster().contains(playerUUID)) {
					ua.getUAGeneralPlayerRoster().remove(playerUUID);
					teleportPlayer(316, 72, -342);
					p.sendMessage(ChatColor.DARK_RED + uaPrefix + playerName + ", you have been removed from the match as you are not ready.");
				}
			}
		}, 1200);
	}


	// teleport stuff...
	public void teleportPlayer(int x, int y, int z) {
		redTeamSide = new Location(w, 317, 65, -351, -179.7F, 1.2F);
		blueTeamSide = new Location(w, 317, 65, -443);
		uaTeamSelectArea = new Location(w, 406, 64, 634);
		viewing_deck1 = new Location(w, 316, 72, -342);
			
		if (x == 317 && y == 65 && z == -351) {
			p.teleport(redTeamSide);
		}
			
		if (x == 317 && y == 65 && z == -443) {
			p.teleport(blueTeamSide);
		}
			
		if (x == 406 && y == 64 && z == 634) {
			p.teleport(uaTeamSelectArea);
		}
			
		if (x == 316 && y == 72 && z == -342) {
			p.teleport(viewing_deck1);
		}
	}


>>>>>>> d11b82c7fed0e9d624402ba9fc274270831d3b9d
}

/*

TTHandler is responsible for handling teleportation and team management aspects of the Ultimate Arrow mini-game.

TODO:
- check if player steps on pressure plates -DONE-
<<<<<<< HEAD
- tidy this class up and do some major refactoring.
*/
=======
*/
>>>>>>> d11b82c7fed0e9d624402ba9fc274270831d3b9d
