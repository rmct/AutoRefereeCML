package org.mctourney.autoreferee.commands;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.mctourney.autoreferee.AutoRefMatch;
import org.mctourney.autoreferee.AutoRefPlayer;
import org.mctourney.autoreferee.AutoRefTeam;
import org.mctourney.autoreferee.AutoRefereeCML;
import org.mctourney.autoreferee.util.commands.AutoRefCommand;
import org.mctourney.autoreferee.util.commands.AutoRefPermission;
import org.mctourney.autoreferee.util.commands.CommandHandler;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.apache.commons.cli.CommandLine;

public class LeaguePluginCommands implements CommandHandler
{
	AutoRefereeCML plugin;

	public LeaguePluginCommands(Plugin plugin)
	{
		this.plugin = (AutoRefereeCML) plugin;
	}

	@AutoRefCommand(name={"forfeit"}, argmax=0, options="yn",
			description="Vote to forfeit the match. Requires more than 50% vote.")
	@AutoRefPermission(console=false)

	public boolean voteForfeit(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
	{
		if (match == null) return false;

		// forfeits are only allowed for ranked matches, and only if the server permits them
		if (!match.hasMetadata("isranked") || !(Boolean) match.getMetadata("isranked") ||
			!plugin.getConfig().getBoolean("allow-forfeits", true)) return false;

		AutoRefPlayer apl = match.getPlayer((Player) sender);
		if (apl == null) return false;

		AutoRefTeam team = apl.getTeam();
		boolean forfeit = !options.hasOption('n');
		apl.addMetadata("forfeit", forfeit);

		int forfeitVotes = 0;
		for (AutoRefPlayer teammate : team.getPlayers())
		{
			if (forfeit && !teammate.equals(apl)) teammate.getPlayer().sendMessage(
				apl.getDisplayName() + " has voted to forfeit the match.");
			if (teammate.hasMetadata("forfeit") && (Boolean) teammate.getMetadata("forfeit"))
				++forfeitVotes;
		}

		team.addMetadata("forfeit", 2 * forfeitVotes > team.getPlayers().size());

		Set<AutoRefTeam> activeTeams = Sets.newHashSet();
		for (AutoRefTeam t : match.getTeams())
			if (!t.hasMetadata("forfeit") || !(Boolean) t.getMetadata("forfeit"))
				activeTeams.add(t);

		if (activeTeams.size() == 1)
			match.endMatch(Iterables.getOnlyElement(activeTeams));

		return true;
	}
}
