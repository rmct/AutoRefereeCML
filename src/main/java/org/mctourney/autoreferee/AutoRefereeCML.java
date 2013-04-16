package org.mctourney.autoreferee;

import org.bukkit.plugin.java.JavaPlugin;
import org.mctourney.autoreferee.commands.PluginCommands;

public class AutoRefereeCML extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		AutoReferee ar = AutoReferee.getInstance();

		ar.getCommandManager().registerCommands(new RankCommands(ar), ar);

		getLogger().info(this.getName() + " enabled.");
	}

	@Override
	public void onDisable()
	{
		getLogger().info(this.getName() + " disabled.");
	}
}
