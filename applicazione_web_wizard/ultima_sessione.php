<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8" />
        <title>Applicazione Wizard</title>
        <link rel="icon" href="wizard_img.png" type="image/png"/>
        
        <link href="https://cdn.jsdelivr.net/npm/simple-datatables@latest/dist/style.css" rel="stylesheet" />
        <link href="css/styles.css" rel="stylesheet" />
        <script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/js/all.min.js" crossorigin="anonymous"></script>
    </head>
    <body class="sb-nav-fixed">
        <nav class="sb-topnav navbar navbar-expand navbar-dark bg-dark">
            <!-- Navbar Brand-->
            <a class="navbar-brand ps-3" href="home.php">Applicazione Wizard</a>
            <!-- Sidebar Toggle-->
            <button class="btn btn-link btn-sm order-1 order-lg-0 me-4 me-lg-0" id="sidebarToggle" href="#!" style="float: left;" ><i class="fas fa-bars"></i></button>
            
            <div class="d-none d-md-inline-block form-inline ms-auto me-0 me-md-3 my-2 my-md-0"></div>
            <!-- Navbar-->
            <div class="navbar-nav ms-auto ms-md-0 me-3 me-lg-4">
                <a class="navbar-brand ps-3" href="destroy_session.php" ><img src="off.png" style="float:right; width: 15%;"></a>
            </div>
            
                        
        </nav>
        <div id="layoutSidenav">
            <div id="layoutSidenav_nav">
                <nav class="sb-sidenav accordion sb-sidenav-dark" id="sidenavAccordion">
                    <div class="sb-sidenav-menu">
                        <div class="nav">
                            <a class="nav-link" href="home.php">
                                <div class="sb-nav-link-icon"><i class="fas fa-tachometer-alt"></i></div>
                                Home
                            </a>
                            <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#collapseLayouts" aria-expanded="false" aria-controls="collapseLayouts">
                                <div class="sb-nav-link-icon"><i class="fas fa-columns"></i></div>
                                Storico
                                <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                            </a>
                            <div class="collapse" id="collapseLayouts" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordion">
                                <nav class="sb-sidenav-menu-nested nav">
                                    <a class="nav-link" href="ultima_sessione.php">Dell'ultima sessione</a>
                                    <a class="nav-link" href="storico.php">Di tutte le sessioni</a>
                                </nav>
                            </div>
                        </div>
                    </div>
                    <div class="sb-sidenav-footer">
                        <center>ID wizard
                        <?php 
                            session_start();
                            echo "0" . $_SESSION['usr'];
                        ?>
                        </center>
                    </div>
                </nav>
            </div>
            
            
            <div id="layoutSidenav_content" >
                <main>

                <?php

                    function differanza_data($prima,$seconda){
                        //funziona se il separatore è : altrimenti modificare H:i:s  ore:minuti:secondi
                        $p=explode(":", $prima);
                        $s=explode(":", $seconda);
                        $prima_sec=$p[0]*60*60 + $p[1]*60 + $p[2];
                        $seconda_sec=$s[0]*60*60 + $s[1]*60 + $s[2];
                        $diff_sec=abs($prima_sec - $seconda_sec);
                        $dif_ore=(int)($diff_sec/3600); // 3600 = 60*60
                        $resto=$diff_sec-$dif_ore*3600;
                        $dif_minuti=(int)($resto/60);
                        $dif_secondi=abs($diff_sec-$dif_ore*3600-$dif_minuti*60);

                        // se il numero è < 10 aggiungo 0 davanti
                        $dif_ore=($dif_ore<10 ? "0" : "").$dif_ore;
                        $dif_minuti=($dif_minuti<10 ? "0" : "").$dif_minuti;
                        $dif_secondi = number_format($dif_secondi, 3); // visualizzo 3 decimali dopo la virgola
                        $dif_secondi=($dif_secondi<10 ? "0" : "").$dif_secondi;

                        return "$dif_ore:$dif_minuti:$dif_secondi";
                    }


                    echo "<div style=\"margin: 2% 2% 2% 2%; padding: 1% 2% 1% 2%; border: 1px black solid;\">";
                    echo "<b>Ultima sessione (n° ";
                    
                    session_start();
                    if(isset($_SESSION['usr'])){
                        $conn = pg_connect("host=localhost port=5432 dbname=applicazione_wizard user=postgres password=BDDWLab20");

                        if (!$conn){
                            echo 'Connessione al database fallita.';
                            exit();
                        } else {
                            $idwizard = $_SESSION['usr'];
                            $query="SELECT *
                                    FROM inf_sessione
                                    WHERE id_wizard = '$idwizard' AND id_sessione = (SELECT MAX(id_sessione)
                                                         FROM inf_sessione)";
                            $result =  pg_query($conn, $query);
                            $row = pg_fetch_array($result);
                            $idsessione = intval($row[0]);

                            if ($result) {
                                echo $idsessione . ")</b><br>";
                                echo "</div>";

                                // INFORMAZIONI DI SESSIONE
                                echo "<div class=\"row\">";
                                echo "<nav class=\"sb-sidenav accordion\" id=\"sidenavAccordion\" >";
                                echo "<div class=\"sb-sidenav-menu\">";
                                echo "<div class=\"nav\" style=\"width: 90%; margin-left: 4%; \" >";
                                echo "<a class=\"nav-link collapsed\" href=\"#\" data-bs-toggle=\"collapse\" data-bs-target=\"#collapseLayouts0\" aria-expanded=\"false\" aria-controls=\"collapseLayouts\" style=\"color: black;\">";
                                echo "<div class=\"sb-sidenav-collapse-arrow\" style=\"margin-left: 0%; margin-right: 2%;\"><i class=\"fas fa-angle-down\"></i></div>";
                                echo "Informazioni di sessione";
                                echo "</a> <hr>";
                                echo "<div class=\"collapse\" id=\"collapseLayouts0\" aria-labelledby=\"headingOne\" data-bs-parent=\"#sidenavAccordion\" style=\"margin-bottom: 3%; margin-left: 5%;\">";       
                                echo "<b>Tipologia di notifica: </b>";
                                if (trim($row["tipologia_notifica"]) == "durante_attivita") {
                                    echo  "notifica singola, durante l'attività <br>";
                                } 
                                    if (trim($row["tipologia_notifica"]) == "fine_attivita") {
                                    echo "notifica singola, ad attività conclusa <br>";
                                } 
                                    if (trim($row["tipologia_notifica"]) == "coda_notifiche") {
                                    echo "coda di notifiche <br>";
                                }

                                echo "<b>Time to live: </b>" . $row["ttl"] . " sec.";
                                echo "</div> </div> </div> </nav></div> ";

                                // QUERY
                                $query2 = "SELECT *
                                            FROM inf_query
                                           WHERE id_sessione = '$idsessione'";
                                $result2 =  pg_query($conn, $query2);

                                if ($result2) {
                                    while($row2 = pg_fetch_array($result2)){
                                        $numquery = $row2["num_query"];

                                        echo "<div class=\"row\">";
                                        echo "<nav class=\"sb-sidenav accordion\" id=\"sidenavAccordionq". $numquery . "\" >";
                                        echo "<div class=\"sb-sidenav-menu\">";
                                        echo "<div class=\"nav\" style=\"width: 90%; margin-left: 4%;\" >";
                                        echo "<a class=\"nav-link collapsed\" href=\"#\" data-bs-toggle=\"collapse\" data-bs-target=\"#collapseLayoutsq". $numquery . "\" aria-expanded=\"false\" aria-controls=\"collapseLayouts\" style=\"color: black;\">";
                                        echo "<div class=\"sb-sidenav-collapse-arrow\" style=\"margin-left: 0%; margin-right: 2%;\"><i class=\"fas fa-angle-down\"></i></div>";
        
                                        echo "Query " . $numquery;
                                        echo "</a> <hr>";
                                        echo "<div class=\"collapse\" id=\"collapseLayoutsq". $numquery . "\" aria-labelledby=\"headingOne\" data-bs-parent=\"#sidenavAccordionq". $numquery . "\" style=\"margin-bottom: 3%; margin-left: 5%;\">";       
                                        
                                            echo "<div class=\"row\">";
                                            echo "<nav class=\"sb-sidenav accordion\" id=\"sidenavAccordionInf". $numquery . "\" >";
                                            echo "<div class=\"sb-sidenav-menu\">";
                                            echo "<div class=\"nav\" style=\"width: 90%; margin-left: 4%;\" >";
                                            echo "<a class=\"nav-link collapsed\" href=\"#\" data-bs-toggle=\"collapse\" data-bs-target=\"#collapseLayoutsInf". $numquery . "\" aria-expanded=\"false\" aria-controls=\"collapseLayouts\" style=\"color: black;\">";
                                            echo "<div class=\"sb-sidenav-collapse-arrow\" style=\"margin-left: 0%; margin-right: 2%;\"><i class=\"fas fa-angle-down\"></i></div>";
                                                echo "Informazioni di query<br> </a> ";
                                                echo "<div class=\"collapse\" id=\"collapseLayoutsInf". $numquery . "\" aria-labelledby=\"headingOne\" data-bs-parent=\"#sidenavAccordionInf". $numquery . "\" style=\"margin-bottom: 3%; margin-left: 5%;\">";               
                                                echo "<div style=\"margin-left:3%; margin-top:2%; margin-bottom:2%;\">";
                                                echo "<b>Attività di opzione:</b> " . $row2["attivita_opz1"] . ", " . $row2["attivita_opz2"] . "<br>";
                                                echo "<b>Attività corretta: </b>" .$row2["attivita_corretta"]. "<br>";
                                                echo "<b>Contesto di notifica:</b><br>";
                                                echo "&ensp; - <b>Posizione semantica: </b>" . $row2["posizione"] . "<br>";
                                                echo "&ensp; - <b>Momento della giornata:</b> " . $row2["daytime"] . "<br>";
                                                echo "&ensp; - <b>Tempo metereologico: </b>" . $row2["meteo"] . "<br>";
                                                echo "&ensp; - <b>Presenza di persone: </b>" . $row2["persone"] . "<br>";
                                            echo "</div></div></div></nav>";
                                            
                                        echo "</div>";

                                        // INFORMAZIONI DI RISPOSTA
                                        $query3 = "SELECT *
                                                FROM inf_risposta
                                                WHERE id_sessione = '$idsessione' AND num_query = '$numquery'";
                                        $result3 =  pg_query($conn, $query3);
                                        if ($result3) {
                                            while($row3 = pg_fetch_array($result3)){
                                                echo "<div class=\"row\">";
                                                echo "<nav class=\"sb-sidenav accordion\" id=\"sidenavAccordionRisp". $numquery . "\" >";
                                                echo "<div class=\"sb-sidenav-menu\">";
                                                echo "<div class=\"nav\" style=\"width: 90%; margin-left: 4%;\" >";
                                                echo "<hr><a class=\"nav-link collapsed\" href=\"#\" data-bs-toggle=\"collapse\" data-bs-target=\"#collapseLayoutsRisp". $numquery . "\" aria-expanded=\"false\" aria-controls=\"collapseLayouts\" style=\"color: black;\">";
                                                echo "<div class=\"sb-sidenav-collapse-arrow\" style=\"margin-left: 0%; margin-right: 2%;\"><i class=\"fas fa-angle-down\"></i></div>";
    
                                                    echo "Informazioni di risposta<br> </a> ";
                                                    echo "<div class=\"collapse\" id=\"collapseLayoutsRisp". $numquery . "\" aria-labelledby=\"headingOne\" data-bs-parent=\"#sidenavAccordionRisp". $numquery . "\" style=\"margin-bottom: 3%; margin-left: 5%;\">";                   
                                                    echo "<div style=\"margin-left:3%; margin-top:2%; margin-bottom:2%;\">";
                                                    echo "<b>Attività selezionata dall'utente: </b>" . $row3["attivita_scelta"] . "<br>";
                                                    echo "<b>Sentimento:</b> " . $row3["sentimento"] . "<br>";
                                                    echo "<b>Timestamp: </b> t1 = " . $row3["timestamp1"] . ", t2 = " . $row3["timestamp2"] . "<br>";

                                                    $latenza = differanza_data($row3["timestamp2"],$row3["timestamp1"]);
                                                    echo "<b>Latenza: </b> ∆t = t2 - t1 = " . $latenza . "<br>";
                                                    echo "<b>Contesto di risposta:</b><br>";
                                                    echo "&ensp; &ensp; - <b>Posizione semantica: </b>" . $row3["posizione"] . "<br>";
                                                    echo "&ensp; &ensp; - <b>Momento della giornata:</b> " . $row3["daytime"] . "<br>";
                                                    echo "&ensp; &ensp; - <b>Tempo metereologico: </b>" . $row3["meteo"] . "<br>";
                                                    echo "&ensp; &ensp; - <b>Presenza di persone: </b>" . $row3["persone"] . "<br>";
                                                    echo "</div>";
                                                echo "</div></div></div></div></nav>";
                                            }
                                        } else {
                                            echo "Si è verificato un errore.<br/>";
                                            echo pg_last_error($conn);
                                        }
                                        echo "</div> </div> </div> </nav> </div>";

                                    }
                                } else {
                                    echo "Si è verificato un errore.<br/>";
                                    echo pg_last_error($conn);
                                }
                                
                            } else{
                                echo "Si è verificato un errore.<br/>";
                                echo pg_last_error($conn);
                            }
                        }
                    }
                ?> 
                    
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
        <script src="js/scripts.js"></script>

    </body>
</html>
