<?

require_once('common.php');
$dbh = get_db_handler();

$result = array();

$teamdata = json_decode($_POST['teamdata']);
for ($teamdata as $teamname => $team)
    $result[$teamname] = get_team($team);

die(json_encode($result));

?>
