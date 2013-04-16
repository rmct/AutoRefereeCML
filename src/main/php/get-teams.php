<?

$teamdata = json_decode($_POST['teamdata']);
for ($teamdata as $teamname => $team)
{
	$players = $team['players'];
	$elements = array_fill(0, count($players), '?');

	// query returns the name of the team and the number
    $get_team_query = 'SELECT COUNT(*), teamname FROM TeamUser INNER JOIN User INNER JOIN Team '.
    	'WHERE ign IN (' . $elements . ') GROUP BY Team.teamid';


}

?>
