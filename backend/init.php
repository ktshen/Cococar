<?php

$db_name="map";
$mysql_user="root";
$server_name="localhost";
$connection=mysqli_connect($server_name,$mysql_user,"",$db_name);//連線db



if(!$connection){
	
	echo"connection not successful";
}
else {
	echo"connection successful";
}


?>