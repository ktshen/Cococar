<?php

require"init.php";
$u_id=$_POST["id"];
//$u_id="id";
$u_rand=$_POST["rand"];
//$u_rand="12";
$u_longitude=$_POST["longitude"];
//$u_longitude="121.1918424";
$u_latitude=$_POST["latitude"];
//$u_latitude="23.9678546";
$u_url=$_POST["url"];
//$u_url="dfsd";
//$u_time=date('Y-m-d H:i:s');

$sql_query="insert into map values('$u_id','$u_rand','$u_longitude','$u_latitude','$u_url',NOW(),'');";//sql的語法 現在是放入db
  
if (mysqli_query($connection,$sql_query))
{
	
echo"data inserted";		
}	
else{
echo"error";
}



?>
