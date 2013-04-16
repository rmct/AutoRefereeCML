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

public class PluginCommands implements CommandHandler
{
	AutoReferee plugin;

	public PluginCommands(Plugin plugin)
	{
		this.plugin = (AutoReferee) plugin;
	}

	@AutoRefCommand(name={"autoref", "command"}, argmax=0,
		description="An example command to add.")
	@AutoRefPermission(console=true, nodes={"autoreferee.admin"})

	public boolean command(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
	{
		return true;
	}
}
