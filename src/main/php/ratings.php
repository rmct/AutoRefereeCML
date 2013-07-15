<?php

define('MEAN', 1500.0);

function fail($resp)
{
	die(json_encode(array(
		'success' => false,
		'response' => $resp
	)));
}


require_once('common.php');
$dbh = get_db_handler();

$server_id = get_server_id($_POST['serverkey'], $dbh);

if ($server_id == NULL)
	fail('Authentication server could not validate server key');

$map_name = $_POST['mapname'];
$map_id = get_map_id($map_name, $dbh);

if ($map_id == NULL)
	fail("Unrecognized map: $map_name");

$teams = json_decode($_POST['teams'], true);
if ($teams == NULL) fail("No teams given.");

$all_scores = array();
$steams = array();

require_once('skills/Skills/TrueSkill/FactorGraphTrueSkillCalculator.php');
require_once('skills/Skills/Team.php');

$calculator = new Moserware\Skills\TrueSkill\FactorGraphTrueSkillCalculator();
$gameInfo = new Moserware\Skills\GameInfo(MEAN, MEAN/3, MEAN/6, MEAN/300, 0.0);

foreach ($teams as $teamname => &$teamdata)
{
	// get the team's rating, save old copy of it
	list($r, $sigma) = get_team_rating($teamdata['id'], $dbh);

	$teamdata['prevrating'] = $teamdata['rating'] = $r;
	$teamdata['prevsigma'] = $teamdata['sigma'] = $sigma;

	$all_scores[] = (int) $teamdata['score'];
	$teamdata['#team'] = $s = new Moserware\Skills\Player($teamname);
	$steams[] = new Moserware\Skills\Team($s, new Moserware\Skills\Rating($r, $sigma));
}

$all_scores = array_unique($all_scores, SORT_NUMERIC);
rsort($all_scores, SORT_NUMERIC);

$sranks = array();
foreach ($teams as $teamname => &$teamdata)
	$sranks[] = 1+array_search((int) $teamdata['score'], $all_scores);

$newRatings = $calculator->calculateNewRatings($gameInfo, $steams, $sranks);

foreach ($teams as $teamname => &$teamdata)
{
	$rating = $newRatings->getRating($teamdata['#team']);
	unset($teamdata['#team']);

	$r = $teamdata['rating'] = $rating->getMean();
	$s = $teamdata['sigma'] = $rating->getStandardDeviation();

	set_team_rating($teamdata['id'], $r, $s, $dbh);
}

// add match description information
add_match(null, $map_id, $server_id, $teams, $dbh);

die(json_encode(array(
	'success' => true,
	'response' => 'Ratings recalculated.',
	'data' => $teams
)));

?>
