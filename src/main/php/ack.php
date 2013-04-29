<?php

require_once('common.php');
$dbh = get_db_handler();

$server_id = get_server_id($_GET['serverkey'], $dbh);
die(json_encode($server_id != NULL));

?>
