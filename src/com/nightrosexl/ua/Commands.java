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

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("ua")) {
			return false;
			sender.sendMessage("testing...");
		}
		Player p = (Player) sender;

		String playerName = p.getName();
		String[] uaArgs = {"join", "leave"};

		if (args.length == 0) {
			p.sendMessage(ChatColor.RED + ua.getPrefix() + "Additional arguments are required.");
			p.sendMessage(ChatColor.RED + ua.getPrefix() + "Usage /ua [" + uaArgs[0] + ", " + uaArgs[1] + "]");
			return true;
		}

		if (args[0].equalsIgnoreCase(uaArgs[0])) {
			ua.addToUAGeneralRoster(p, "");
			p.sendMessage(ChatColor.DARK_GREEN + ua.getPrefix() + playerName + ", you will be teleported to the team selection area soon!");

			// check team size, start game, if conditions are met.
			// loop through list and teleport everyone in it.
			if (ua.getUAGeneralPlayerRoster().size() >= 2/*ua.getUAGeneralPlayerRoster().size() >= 10 && ua.getUAGeneralPlayerRoster().size() <= 20*/) {
				for (UAPlayer uap : ua.getUAGeneralPlayerRoster()) {
					uap.getPlayer().teleport(ua.getSelectArea());
				}
			}
		} else if (args[0].equalsIgnoreCase(uaArgs[1])) {
			ua.removeFromUAGeneralRoster(p);

			// TODO: remove lines 50-52, put them in a cleanup method of some sort.
			p.getInventory().remove(Material.BOW);
			p.teleport(ua.getSelectArea());
			p.sendMessage(ChatColor.DARK_RED + ua.getPrefix() + playerName + ", you have been removed from the game!");
		}
		else {
			p.sendMessage(ChatColor.RED + ua.getPrefix() + "Invalid argument. Usage: /ua [" + uaArgs[0] + ", " + uaArgs[1] + "]");
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