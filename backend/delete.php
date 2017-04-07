<?php

require"init.php";

$u_liverand=$_POST["liverand"];

//$u_rand="12";

$u_fixrand=$_POST["fixrand"];

//$u_talk="fuck you";


//$sql_query="insert into map values('$u_id','$u_rand','$u_longitude','$u_latitude','$u_url',NOW());";//sql的語法 現在是放入db
 
$sql_query="DELETE FROM `map` WHERE `rand`='$u_liverand'";
$sql_query1="DELETE FROM `map` WHERE `rand`='$u_fixrand'";
 
if (mysqli_query($connection,$sql_query))
{
echo"data inserted";		
}	
else{
echo"error";
}
if (mysqli_query($connection,$sql_query1))
{
echo"data inserted";		
}	
else{
echo"error";
}



?>
