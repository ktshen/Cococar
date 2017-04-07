<?php

require"init.php";

$u_url=$_POST["url"];

//$u_rand="12";

$u_longitude=$_POST["longitude"];

$u_latitude=$_POST["latitude"];
//$u_talk="fuck you";


//$sql_query="insert into map values('$u_id','$u_rand','$u_longitude','$u_latitude','$u_url',NOW());";//sql的語法 現在是放入db
 
$sql_query="UPDATE map SET longitude='$u_longitude',latitude='$u_latitude' WHERE url ='$u_url';";
 
if (mysqli_query($connection,$sql_query))
{
echo"data inserted";		
}	
else{
echo"error";
}



?>
