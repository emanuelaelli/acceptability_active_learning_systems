<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
</head>
<body>
<?php
	session_start();
	if(isset($_SESSION['idsessione']) && $_SESSION['rispostaUtente'] != "") { 
	    $json = $_SESSION['rispostaUtente'];
        $array = json_decode($json, true);
        
        $numquery = intval($array["numquery"]);
        $risposta = $array["risposta"];

        $rispostaQuery = "";
        if (trim($risposta) == 'salire con l\'ascensore') {
            $rispostaQuery = 'salire con l\'\'ascensore';
        } else if (trim($risposta) == 'scendere con l\'ascensore') {
            $rispostaQuery = 'scendere con l\'\'ascensore';
        } else {
        	$rispostaQuery = $risposta;
        }

		$sentimento = $array["sentimento"];

        $t1 = $array["timestamp1"];
        $t2 = $array["timestamp2"];

        $id_sessione = $_SESSION['idsessione'];
        $posizione = $_POST['pos_semantica_risp'];
        $daytime = $_POST['daytime_risp'];
        $meteo = $_POST['meteo_risp'];
        $persone = $_POST['persone_risp'];

	    $conn = pg_connect("host=localhost port=5432 dbname=applicazione_wizard user=postgres password=BDDWLab20");

	    if (!$conn){
	        echo 'Connessione al database fallita.';
	        exit();
	    } else {
	    	$query = null;
	    	if (trim($sentimento) == '' && trim($t2) == '' && trim($risposta) == '') {
	    		$query="INSERT INTO inf_risposta 
	                VALUES('$id_sessione', '$numquery', NULL, NULL, '$t1', NULL, '$posizione', '$daytime', '$meteo', '$persone')";
        	} else {
	        	$query="INSERT INTO inf_risposta 
	                VALUES('$id_sessione', '$numquery', '$rispostaQuery', '$sentimento', '$t1', '$t2', '$posizione', '$daytime', '$meteo', '$persone')";
	        }
	        $result = pg_query($conn, $query);
	        if ($result) {
	           $_SESSION['rispostaUtente'] = "";
	           header("location: home_sessione.php");
               exit(); 
	        } else {
	        	echo $query . "<br>";
	            echo "Si è verificato un errore.<br/>";
	            echo pg_last_error($conn);
	        }
	    }
	}
?>
</body>
</html>