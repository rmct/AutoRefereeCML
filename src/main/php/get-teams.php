<?php

require_once('common.php');
$dbh = get_db_handler();

$result = array();

$teamdata = json_decode($_POST['teamdata']);
foreach ($teamdata as $teamname => $team)
    $result[$teamname] = get_team($team, $dbh);

die(json_encode($result));

?>
