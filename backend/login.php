<?php
require"init.php";

$name="timmy";
$password="a0189149";
$contact="0000";
$country="taiwan";

$sql_query="select name from users where name like '$name' and password like '$password'; ";//sql語法

$result=mysqli_query($connection,$sql_query);

if(mysqli_num_rows($result)>0)
{
	
 $row=mysqli_fetch_assoc($result);//mysql_fetch_assoc — 从结果集中取得一行作为关联数组(assoc)row的Index只能是charater	
 $name=$row["name"];//row{name}的原因，在於從sql參數中拿出的叫"name"
 echo"$name";
 
}
else{
echo "error";	
	
	
}

?>