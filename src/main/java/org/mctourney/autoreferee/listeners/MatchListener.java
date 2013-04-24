package org.mctourney.autoreferee.listeners;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import org.bukkit.scheduler.BukkitRunnable;
import org.mctourney.autoreferee.AutoRefMatch;
import org.mctourney.autoreferee.AutoRefPlayer;
import org.mctourney.autoreferee.AutoRefTeam;
import org.mctourney.autoreferee.AutoReferee;
import org.mctourney.autoreferee.AutoRefereeCML;
import org.mctourney.autoreferee.event.match.MatchStartEvent;
import org.mctourney.autoreferee.util.QueryServer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class MatchListener implements Listener
{
	AutoReferee plugin;

	public MatchListener(Plugin plugin)
	{
		this.plugin = (AutoReferee) plugin;
	}

	private class IdentifyTeamsTask extends BukkitRunnable
	{
		private AutoRefMatch match;

		public IdentifyTeamsTask(AutoRefMatch match)
		{ this.match = match; }

		@SuppressWarnings("unused")
		class TeamData
		{
			public int id, score;
			public String name;

			TeamData() {  }
		}

		@Override
		public void run()
		{
			Map<String, Set<String>> teamdata = Maps.newHashMap();
			for (AutoRefTeam team : this.match.getTeams())
			{
				Set<AutoRefPlayer> apls = team.getPlayers();
				if (apls.size() < team.getMinSize()) continue;

				Set<String> players = Sets.newHashSet();
				for (AutoRefPlayer apl : apls)
					players.add(apl.getName());
				teamdata.put(team.getName(), players);
			}

			if (teamdata.size() == 0) return;
			Gson gson = new Gson();

			Map<String, String> request = Maps.newHashMap();
			request.put("teamdata", gson.toJson(teamdata));

			AutoReferee.log(QueryServer.prepareParams(request));
			String response = QueryServer.syncPostQuery(AutoRefereeCML.API_SERVER + "/get-teams.php",
				QueryServer.prepareParams(request));

			AutoReferee.log("" + response);
			Type collectionType = new TypeToken<Map<String, TeamData>>(){}.getType();
			Map<String, TeamData> teamresult = gson.fromJson(response, collectionType);

			for (Map.Entry<String, TeamData> entry : teamresult.entrySet())
			{
				AutoRefTeam team = match.teamNameLookup(entry.getKey());
				TeamData data = entry.getValue();

				if (team == null || data == null) continue;
				team.setName(data.name);
			}
		}
	}

	@EventHandler
	public void matchStart(MatchStartEvent event)
	{
		AutoRefMatch match = event.getMatch();
		new IdentifyTeamsTask(match).runTaskAsynchronously(plugin);
	}
}
