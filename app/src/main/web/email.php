<?php
	/*Database connection*/
	$user = "ist166392";        /* username sigma */
    $host = "db.ist.utl.pt"; 
    $port = 5432; 
    $password = "inrq1320";     /* password psql_reset */ 
    $dbname = $user;            /* nome da BD = nome user */ 
    $connection = pg_connect("host=$host port=$port user=$user password=$password dbname=$dbname") or die(pg_last_error());

    /*Variables*/
	$hash = md5(rand(0,1000));
	$nickname = $_GET['nickname'];
	$email = $_GET['email'];
	$password = $_GET['password'];

	/*Execute Query*/
    $queryn = "SELECT * FROM cmov_insert_login('$email', '$nickname', '$password', '$hash');";
	$r = pg_query($queryn) or die(pg_last_error());
	pg_free_result($r);
	pg_close();

    /*Send email*/
	$title = "[Airdesk] Email Verification Security Key";
	$msg = "Hello, $nickname!\n\nThank you for using Airdesk.\nYour credentials are:\n\nEmail: $email\nPassword: $password\n\nPlease enter the following security key in your application to verify this email address:\n$hash\n\nThank you!";
	$msg = wordwrap($msg,230);
	mail($email, $title, $msg);
?>