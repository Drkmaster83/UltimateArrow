package com.nightrosexl.ua;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	private UltimateArrow ua;

	public Commands(UltimateArrow ua) {
		this.ua = ua;
	}

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

		if (args[0].equalsIgnoreCase(uaArgs[0])) {
			ua.addToUAGeneralRoster(pSender, "");
			pSender.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + playerName + ", you have been teleported to the team selection area!");
			pSender.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + "The game will begin momentarily.");
			pSender.teleport(ua.getSelectArea());
			// check team size, start game, if conditions are met.
		} else if (args[0].equalsIgnoreCase(uaArgs[1])) {
			// TODO: remove lines 50-52, put them in a cleanup method of some sort.
			pSender.getInventory().remove(Material.BOW);
			pSender.teleport(ua.getSelectArea());
			ua.getGameplay().revokeEquipment(pSender);
			ua.removeFromUAGeneralRoster(pSender);
			pSender.sendMessage(ChatColor.DARK_RED + ua.getPrefix() + playerName + ", you have been removed from the game!");
		}
		else {
			pSender.sendMessage(ChatColor.RED + ua.getPrefix() + "Invalid argument. Usage: /" + label + " [" + uaArgs[0] + ", " + uaArgs[1] + "]");
		}
		return true;
	}
}

/*
TODO:
 if /ua leave and you aren't in game, send message
 if /ua solely, send message
 if players leave the server, remove from game.
*/