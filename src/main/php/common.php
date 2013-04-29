<?php

// file not provided, must provide a method get_db_handle()
// that returns a valid, connected DBO object
require_once('database.php');

function get_team($players, $dbh)
{
	if (count($players) == 0) return NULL;
	$elements = array_fill(0, count($players), '?');

	// query returns the name of the team, id, and count of players
	$get_team_query = 'SELECT COUNT(*), teamid, teamname, rating '.
		'FROM TeamUser NATURAL JOIN User NATURAL JOIN Team WHERE timequit IS NULL '.
		'AND ign IN (' . implode(',', $elements) . ') GROUP BY teamid';

	$lower_igns = array();
	foreach ($players as $p)
		$lower_igns[] = strtolower($p);

	$get_team = $dbh->prepare($get_team_query);
	$get_team->execute($lower_igns);

	// if nothing is returned, or all players are not from same team, no team
	if (!(list($count, $teamid, $team, $rating) = $get_team->fetch(PDO::FETCH_NUM))) return NULL;
	if ((int) $count < count($players)) return NULL;

	// return team info object
	return array('name' => $team, 'id' => (int) $teamid, 'rating' => (int) $rating);
}

function get_team_rating($teamid, $dbh)
{
	// get rating from team table
	$get_team_query = 'SELECT rating FROM Team WHERE teamid = ?';
	$get_team = $dbh->prepare($get_team_query);

	// return NULL if no match, otherwise convert the rating
	$get_team->execute(array( $teamid ));
	return (list($rating) = $get_team->fetch(PDO::FETCH_NUM)) ? (int) $rating : NULL;
}

function get_num_matches($teamid, $dbh)
{
	// get number of matches the team has played in
	$get_match_count_query = 'SELECT COUNT(DISTINCT matchid) FROM MatchTeam WHERE teamid = ?';
	$get_match_count = $dbh->prepare($get_match_count_query);

	// return 0 if nothing is returned, otherwise convert the number of matches
	$get_match_count->execute(array( $teamid ));
	return (list($c) = $get_match_count->fetch(PDO::FETCH_NUM)) ? (int) $c : 0;
}

function set_team_rating($teamid, $rating, $dbh)
{
	// get rating from team table
	$set_team_query = 'UPDATE Team SET rating = ? WHERE teamid = ?';
	$set_team = $dbh->prepare($set_team_query);

	// set the rating and return the success
	return $set_team->execute(array( $rating, $teamid ));
}

function get_server_id($serverkey, $dbh)
{
	// match server key up with a server id
	$get_server_query = 'SELECT serverid FROM Server WHERE serverkey = ?';
	$get_server = $dbh->prepare($get_server_query);

	// return NULL if no match, otherwise convert the server id
	$get_server->execute(array( $serverkey ));
	return (list($serverid) = $get_server->fetch(PDO::FETCH_NUM)) ? (int) $serverid : NULL;
}

function get_map_id($mapname, $dbh)
{
	// find the map id that matches the map name
	$get_map_query = 'SELECT mapid FROM Map WHERE mapname = ?';
	$get_map = $dbh->prepare($get_map_query);

	// return NULL if no match, otherwise convert the map id
	$get_map->execute(array( $mapname ));
	return (list($mapid) = $get_map->fetch(PDO::FETCH_NUM)) ? (int) $mapid : NULL;
}

function add_match($matchname, $mapid, $serverid, $teams, $dbh)
{
	// insert match details into the database
	$add_match_query = 'INSERT INTO RankedMatch(matchname, matchdate, mapid, serverid) ' .
		'VALUES(?, ?, ?, ?)';
	$add_match = $dbh->prepare($add_match_query);

	// if this was successful, we will move on to adding the team info
	if (!$add_match->execute(array( $matchname, time(), $mapid, $serverid ))) return;
	$matchid = $dbh->lastInsertId();

	// prepare a query to insert team information for the match
	$add_team_result_query = 'INSERT INTO MatchTeam(matchid, teamid, score, ' .
		'prevrating, nextrating) VALUES(?, ?, ?, ?, ?)';
	$add_team_result = $dbh->prepare($add_team_result_query);

	// insert the data for each team
	foreach ($teams as $teamname => $teamdata)
		$add_team_result->execute(array( $matchid, $teamdata['id'],
			$teamdata['score'], $teamdata['prevrating'], $teamdata['rating']));
}

?>
