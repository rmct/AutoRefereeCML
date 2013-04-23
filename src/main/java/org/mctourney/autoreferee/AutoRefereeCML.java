package org.mctourney.autoreferee;

import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

import org.mctourney.autoreferee.commands.LeagueCommands;

public class AutoRefereeCML extends JavaPlugin
{
	private Map<AutoRefTeam, Integer> leagueIDs = Maps.newHashMap();

	@Override
	public void onEnable()
	{
		AutoReferee ar = AutoReferee.getInstance();

		ar.getCommandManager().registerCommands(new LeagueCommands(ar), ar);

		getLogger().info(this.getName() + " enabled.");
	}

	@Override
	public void onDisable()
	{
		getLogger().info(this.getName() + " disabled.");
	}

	public Integer getLeagueID(AutoRefTeam team)
	{ return leagueIDs.get(team); }

	public void setLeagueID(AutoRefTeam team, int id)
	{ leagueIDs.put(team, id); }
}
