<?php

require"init.php";

$u_rand=$_POST["rand"];

//$u_rand="12";

$u_talk=$_POST["talk"];

//$u_talk="fuck you";


//$sql_query="insert into map values('$u_id','$u_rand','$u_longitude','$u_latitude','$u_url',NOW());";//sql的語法 現在是放入db
 
$sql_query="UPDATE map SET talk='$u_talk' WHERE rand ='$u_rand';";
 
if (mysqli_query($connection,$sql_query))
{
echo"data inserted";		
}	
else{
echo"error";
}



?>
