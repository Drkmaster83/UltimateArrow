package com.nightrosexl.ua;

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
import org.bukkit.event.player.PlayerInteractEvent;

public class GameplayEvents implements Listener {
    private UltimateArrow ua;
    private Gameplay gp;
    
    public GameplayEvents(UltimateArrow ua, Gameplay gp) {
        this.ua = ua;
        this.gp = gp;
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
        gp.setArrowPlayer(ua.getPlayer(damaged));
    }

    @EventHandler
    public void onArrowHitBlock(ProjectileHitEvent e) {
        if (e.getHitEntity() != null) return; // We only want to handle this event if it strikes a block, not a player or something.
        if (!(e.getEntity() instanceof Arrow) || !(((Arrow)e.getEntity()).getShooter() instanceof Player)) return;
        Player shooter = (Player) ((Arrow)e.getEntity()).getShooter();
        Location hit = e.getHitBlock().getLocation();
        double nearestDistanceSquared = Double.MAX_VALUE;
        UAPlayer nearestPlayerToArrow = null;
        for(Entity ent : e.getEntity().getNearbyEntities(gp.getArrowRadius(), gp.getArrowRadius(), gp.getArrowRadius())) {
            if (!(ent instanceof Player)) continue;
            if (ent.getUniqueId().equals((shooter.getUniqueId()))) continue; // Don't want to do this, just take care of it below
            double distSquared = hit.distanceSquared(ent.getLocation());
            if (distSquared < nearestDistanceSquared) { // We've found an entity that's nearer than our previous one
                Player p = (Player) ent;
                nearestDistanceSquared = distSquared;
                nearestPlayerToArrow = ua.getPlayer(p);
            }
        }

        if (nearestPlayerToArrow == null || nearestDistanceSquared == Double.MAX_VALUE) { // No player found/nearest distance still farthest possible
            gp.getArrowPlayer().getPlayer().getInventory().addItem(gp.getArrowItem()); // give them another arrow since no player can take possession
            e.getEntity().remove(); // Remove the old arrow
            return;
        }
        // Player must have been found if we're here
        gp.setArrowPlayer(nearestPlayerToArrow);
        e.getEntity().remove();
    }

    @EventHandler
    public void onBuggedArrow(PlayerInteractEvent event) {
        Player clicker = event.getPlayer();
        if(event.getItem() == null) return; // Nothing in hand
        if(ua.getPlayer(clicker) == null) return; // Not in-game
        if(event.getItem().getType() != Material.BOW) return; // Item clicked was not a bow
        if(!clicker.getInventory().contains(gp.getArrowItem())) return; // They don't have an arrow in inv
        if(clicker.getUniqueId().equals(gp.getArrowPlayer().getPlayer().getUniqueId())) return; // Shooting player isn't the arrow player
        clicker.getInventory().remove(gp.getArrowItem()); // Remove arrow if they're in-game, have a bow in hand, and have an arrow
    }
}