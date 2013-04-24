package org.mctourney.autoreferee;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.mctourney.autoreferee.commands.LeagueCommands;
import org.mctourney.autoreferee.listeners.MatchListener;

public class AutoRefereeCML extends JavaPlugin
{
	public static String API_SERVER = null;

	private static AutoRefereeCML instance = null;

	public static AutoRefereeCML getInstance()
	{ return instance; }

	@Override
	public void onEnable()
	{
		// set singleton instance
		AutoRefereeCML.instance = this;

		AutoReferee ar = AutoReferee.getInstance();

		ar.getCommandManager().registerCommands(new LeagueCommands(ar), ar);

		Bukkit.getPluginManager().registerEvents(new MatchListener(ar), ar);

		AutoRefereeCML.API_SERVER = this.getConfig().getString("auth-server");

		getLogger().info(this.getName() + " enabled.");
	}

	@Override
	public void onDisable()
	{
		getLogger().info(this.getName() + " disabled.");
	}
}
