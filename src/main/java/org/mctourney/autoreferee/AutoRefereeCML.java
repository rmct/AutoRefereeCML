package org.mctourney.autoreferee;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.mctourney.autoreferee.commands.LeagueCoreCommands;
import org.mctourney.autoreferee.commands.LeaguePluginCommands;
import org.mctourney.autoreferee.listeners.MatchListener;
import org.mctourney.autoreferee.util.QueryServer;

import java.util.Map;
import java.util.logging.Level;

public class AutoRefereeCML extends JavaPlugin
{
	public static String API_SERVER = null;

	private static AutoRefereeCML instance = null;

	public String serverkey;

	public static AutoRefereeCML getInstance()
	{ return instance; }

	@Override
	public void onEnable()
	{
		// set singleton instance
		AutoRefereeCML.instance = this;

		this.serverkey = this.getConfig().getString("server-key", "");

		AutoReferee ar = AutoReferee.getInstance();

		// register commands
		ar.getCommandManager().registerCommands(new LeagueCoreCommands(ar), ar);
		ar.getCommandManager().registerCommands(new LeaguePluginCommands(this), this);

		// register events
		Bukkit.getPluginManager().registerEvents(new MatchListener(ar), ar);

		// get the auth server url from the config (default: localhost)
		AutoRefereeCML.API_SERVER = this.getConfig().getString("auth-server", "http://localhost/");

		// check connection to the auth server
		Map<String, String> ackParams = Maps.newHashMap();
		ackParams.put("serverkey", this.getConfig().getString("server-key", ""));

		String response = QueryServer.syncGetQuery(AutoRefereeCML.API_SERVER + "/ack.php",
			QueryServer.prepareParams(ackParams));

		if (response != null && !Boolean.parseBoolean(response))
			AutoReferee.log(this.getName() + " could not connect to auth server.", Level.SEVERE);
		else AutoReferee.log(this.getName() + " connected to auth server.");
	}

	@Override
	public void onDisable()
	{
	}
}
