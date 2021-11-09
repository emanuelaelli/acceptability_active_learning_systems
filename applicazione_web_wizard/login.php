<HTML>
    <HEAD>
		<style>
			input.left {
				float:left;
				border-radius: 12px;
			}
			input.right {
				float:right;
				border-radius: 12px;
			}
		</style>
    </HEAD>
    <BODY> 

	<?php 
		session_start();
		if(isset($_SESSION['usr'])) {
            header("location: home.php");
            exit;

		} else {
            $conn = pg_connect("host=localhost port=5432 dbname=applicazione_wizard user=postgres password=BDDWLab20");
            if (!$conn){
                echo 'Connessione al database fallita.';
                exit();
            } else if (isset($_POST['id'])) {
                $id = $_POST["id"];

                $query="SELECT * FROM wizard WHERE id_wizard = '$id' ";
                $result =  pg_query($conn, $query);
                
                if (!$result) {
                    echo "Si è verificato un errore.<br/>";
                    echo pg_last_error($conn);
                    exit();
                } else {
                    $bdu = '';

                    while($row = pg_fetch_array($result)) {
                        $bdu = $row['id_wizard'];
                    };

                    if ($id == $bdu ) {
                        $username = $_POST["id"];
                        $_SESSION['usr']=$username;
                        $_SESSION['rispostaUtente'] = "";

                        header("location: home.php");
                        
                    } else {
                        echo "L'id wizard inserito non risulta registrato. Se vuoi puoi <a href='login.html'>riprovare</a> ad accedere.";
                    } 
                }
            }  else {
                echo "Non risultano dati passati o memorizzati in una variabile di sessione valida<br>";
                echo "Se vuoi puoi <a href='login.html'>riprovare</a> ad accedere";
            }
		}
	?> 
    </BODY>
</HTML>
