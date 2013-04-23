<?

// file not provided, must provide a method get_db_handle(void)
// that returns a valid, connected DBO object
require_once('database.php');

function get_team($players, $dbh=get_db_handle())
{
	$elements = array_fill(0, count($players), '?');

	// query returns the name of the team, id, and count of players
	$get_team_query = 'SELECT COUNT(*), teamid, teamname FROM TeamUser NATURAL JOIN User NATURAL JOIN Team '.
		'WHERE ign IN (' . $elements . ') GROUP BY teamid';

	$get_team = $dbh->prepare($get_team_query);
	$get_team->execute($players);

	// if nothing is returned, or all players are not from same team, no team
	if (!(list($count, $teamid, $team) = $get_team->fetch(PDO::FETCH_NUM))) return NULL;
	if ((int) $count < count($players)) return NULL;

	// return team info object
	return array('name' => $team, 'id' => (int) $teamid);
}

?>
