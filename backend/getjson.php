<?php
$database="map";
$username="root";
$password="";
$host="localhost";

$sql="select * from map;";
$sql_query="DELETE FROM map WHERE time<NOW() - INTERVAL 0.5 HOUR;";
$con=mysqli_connect($host,$username,$password,$database);
$result1=mysqli_query($con,$sql_query);
$result= mysqli_query($con,$sql);

$responce=array();

while($row=mysqli_fetch_array( $result) )
{
	
	
	$p =array();
	$p["id"]=$row["id"];
	$p["rand"]=$row["rand"];
	$p["longitude"]=$row["longitude"];
	$p["latitude"]=$row["latitude"];
	$p["url"]=$row["url"];
	$p["time"]=$row["time"];
    $p["talk"]=$row["talk"];
	
	array_push($responce,$p);
	
}

echo json_encode ($responce);

mysqli_close($con);

?>






























