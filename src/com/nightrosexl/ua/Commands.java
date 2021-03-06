package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nightrosexl.ua.Gameplay.GameState;

public class Commands implements CommandExecutor {
    private UltimateArrow ua;

    public Commands(UltimateArrow ua) {
        this.ua = ua;
    }

    // TODO Evaluate functionality thoroughly
    public boolean onCommand(CommandSender console, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("UltimateArrow")) return false;
        if (!(console instanceof Player)) {
            console.sendMessage("You can't play this game kiddo, nice try though!");
            return true;
        }
        Player pSender = (Player) console;

        String playerName = pSender.getName();
        String[] uaArgs = {"join", "leave"};

        if (args.length == 0) {
            pSender.sendMessage(ChatColor.RED + ua.getPrefix() + "Additional arguments are required.");
            pSender.sendMessage(ChatColor.RED + ua.getPrefix() + "Usage /" + label + " [" + uaArgs[0] + ", " + uaArgs[1] + "]");
            return true;
        }
        Gameplay gp = ua.getGameplay();
        if (args[0].equalsIgnoreCase(uaArgs[0])) {
            if(gp.getState() == GameState.WAITING_READY) {
                gp.getQueue().add(pSender);
                if(gp.canBegin()) {
                    for(Player qPlayer : gp.getQueue().toArray(new Player[gp.getQueue().size()])) {
                        gp.tpToSelectArea(qPlayer);
                        gp.addToRoster(qPlayer, "");
                    }
                    return true;
                }
                pSender.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + playerName + ", you will be teleported momentarily.");
            }
            else {
                pSender.sendMessage(ChatColor.RED + ua.getPrefix() + "Unable to add you into the queue, as the game is currently underway.");
            }
        } else if (args[0].equalsIgnoreCase(uaArgs[1])) {
            gp.tpToSelectArea(pSender, false);
            if(gp.getState() == GameState.IN_GAME) gp.revokeEquipment(pSender);
            gp.removeFromRoster(pSender);
            pSender.sendMessage(ChatColor.DARK_RED + ua.getPrefix() + playerName + ", you have been removed from the game!");
        }
        else {
            pSender.sendMessage(ChatColor.RED + ua.getPrefix() + "Invalid argument. Usage: /" + label + " [" + uaArgs[0] + ", " + uaArgs[1] + "]");
        }
        return true;
    }
}