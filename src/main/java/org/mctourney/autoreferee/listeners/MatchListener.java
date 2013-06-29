package org.mctourney.autoreferee.listeners;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.mctourney.autoreferee.AutoRefMatch;
import org.mctourney.autoreferee.AutoRefPlayer;
import org.mctourney.autoreferee.AutoRefTeam;
import org.mctourney.autoreferee.AutoReferee;
import org.mctourney.autoreferee.AutoRefereeCML;
import org.mctourney.autoreferee.event.match.MatchCompleteEvent;
import org.mctourney.autoreferee.event.match.MatchLoadEvent;
import org.mctourney.autoreferee.event.match.MatchStartEvent;
import org.mctourney.autoreferee.util.QueryUtil;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MatchListener implements Listener
{
	AutoReferee plugin;

	public MatchListener(Plugin plugin)
	{
		this.plugin = (AutoReferee) plugin;
	}

	@EventHandler
	public void matchLoad(MatchLoadEvent event)
	{
		event.getMatch().addMetadata("isranked", true);
	}

	private class IdentifyTeamsTask extends BukkitRunnable
	{
		private AutoRefMatch match;

		public IdentifyTeamsTask(AutoRefMatch match)
		{ this.match = match; }

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
			//	if (apls.size() < team.getMinSize()) continue;

				Set<String> players = Sets.newHashSet();
				for (AutoRefPlayer apl : apls)
					players.add(apl.getName());
				teamdata.put(team.getName(), players);
			}

			if (teamdata.size() == 0) return;
			Gson gson = new Gson();

			Map<String, String> request = Maps.newHashMap();
			request.put("teamdata", gson.toJson(teamdata));

			try
			{
				String response = QueryUtil.syncPostQuery(AutoRefereeCML.API_SERVER + "/get-teams.php",
					QueryUtil.prepareParams(request));

				Type collectionType = new TypeToken<Map<String, TeamData>>(){}.getType();
				Map<String, TeamData> teamresult = gson.fromJson(response, collectionType);

				Set<Integer> matchteams = Sets.newHashSet();
				for (Map.Entry<String, TeamData> entry : teamresult.entrySet())
				{
					AutoRefTeam team = match.getTeam(entry.getKey());
					TeamData data = entry.getValue();

					if (team == null || data == null || matchteams.contains(data.id))
					{ match.addMetadata("isranked", false); continue; }

					team.setName(data.name);
					team.addMetadata("leagueid", data.id);
					matchteams.add(data.id);
				}

				if (matchteams.size() < 2)
					match.addMetadata("isranked", false);

				if (match.hasMetadata("isranked") && (Boolean) match.getMetadata("isranked"))
				{
					match.broadcastSync(ChatColor.DARK_RED + "[!!] This match will be ranked. Leaving this match",
						ChatColor.DARK_RED + "[!!] without completion will be reflected in your rating.");
					AutoRefereeCML.getInstance().getLogger().info(
						String.format("-- Ranked match on '%s'", match.getMapName()));
				}
			}
			catch (IOException e) { e.printStackTrace(); }
		}
	}

	@EventHandler
	public void matchStart(MatchStartEvent event)
	{
		AutoRefMatch match = event.getMatch();
		new IdentifyTeamsTask(match).runTaskAsynchronously(plugin);
	}

	private class UpdateRatingsTask extends BukkitRunnable
	{
		private AutoRefMatch match;
		private AutoRefTeam winner;

		public UpdateRatingsTask(AutoRefMatch match, AutoRefTeam winner)
		{ this.match = match; this.winner = winner; }

		class TeamData
		{
			public double rating, prevrating;
			public double k, e, score, q;

			public int id;
			TeamData() {  }

			public TeamData(int id, double score)
			{ this.id = id; this.score = score; }
		}

		class RankingResponse
		{
			public boolean success;
			public String response;
			public Map<String, TeamData> data;

			RankingResponse() {  }
		}

		@Override
		public void run()
		{
			Map<String, TeamData> teams = Maps.newHashMap();
			for (AutoRefTeam team : match.getTeams())
			{
				Integer id = (Integer) team.getMetadata("leagueid");
				if (id == null) continue;

				TeamData teamdata = new TeamData(id, team.equals(winner) ? 1.0 : 0.0);
				teams.put(team.getName(), teamdata);
			}

			Gson gson = new Gson();

			Map<String, String> request = Maps.newHashMap();
			request.put("teams", gson.toJson(teams));
			request.put("serverkey", AutoRefereeCML.getInstance().serverkey);
			request.put("mapname", match.getMapName());

			try
			{
				String response = QueryUtil.syncPostQuery(AutoRefereeCML.API_SERVER + "/ratings.php",
					QueryUtil.prepareParams(request));
				AutoReferee.log(response);

				RankingResponse ranking = gson.fromJson(response, RankingResponse.class);
				if (ranking != null) for (Map.Entry<String, TeamData> entry : ranking.data.entrySet())
				{
					AutoRefTeam team = match.getTeam(entry.getKey());
					TeamData data = entry.getValue();

					if (team == null || data == null) continue;

					String updateMessage = String.format("%s rating: %d -> %d",
						team.getDisplayName(), (int) data.prevrating, (int) data.rating);

					for (AutoRefPlayer apl : team.getPlayers())
						AutoReferee.getInstance().sendMessageSync(apl.getPlayer(), updateMessage);
					AutoReferee.log(updateMessage);
				}
			}
			catch (IOException e) { e.printStackTrace(); }
		}
	}

	@EventHandler
	public void matchComplete(MatchCompleteEvent event)
	{
		AutoRefMatch match = event.getMatch();

		if (match.hasMetadata("isranked") && (Boolean) match.getMetadata("isranked"))
			new UpdateRatingsTask(match, event.getWinner()).runTaskAsynchronously(plugin);
	}
}
