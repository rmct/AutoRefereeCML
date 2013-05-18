package org.mctourney.autoreferee.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import org.apache.commons.cli.CommandLine;

import org.mctourney.autoreferee.AutoRefMatch;
import org.mctourney.autoreferee.AutoReferee;
import org.mctourney.autoreferee.util.commands.AutoRefCommand;
import org.mctourney.autoreferee.util.commands.AutoRefPermission;
import org.mctourney.autoreferee.util.commands.CommandHandler;

import java.util.Date;

public class LeagueCoreCommands implements CommandHandler
{
	AutoReferee plugin;

	public LeagueCoreCommands(Plugin plugin)
	{
		this.plugin = (AutoReferee) plugin;
	}

	@AutoRefCommand(name={"autoref", "rankings"}, argmax=0,
			description="Get your team's league status.")
	@AutoRefPermission(console=false)

	public boolean getRankings(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
	{
		return true;
	}
}
