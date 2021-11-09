<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
    <style type="text/css">
        .titolo {
            font-size: 130%;
        }
        .cella {
            padding-bottom: 1.5%;
        }
    </style>
</head>
<body>
<?php
    session_start();

    if ($_SESSION['rispostaUtente'] != "") {
        //echo $_SESSION['rispostaUtente'];
        //echo $numquery .  $risposta .  $t1 .  $t2; 

        echo '<div style="width: 90%; border: 1px black solid; padding-top: 3%;  margin: 2% 2% 2% 2%;" >
        
        <div class="titolo"><center><b>Inserisci le informazioni del contesto di risposta:</b> <hr></center>  </div>
            
            <FORM method="POST" action="salvaRisposta.php" style="margin-top: 3%;">
                
                <table style="width: 90%;" align="center">
                <tr>
                    <th> - &nbsp; posizione semantica</th> 
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td class="cella"> <input type="radio" name="pos_semantica_risp" value="parco" style="margin-left: 5%;" required> parco </td>
                    <td class="cella"> <input type="radio" name="pos_semantica_risp" value="casa" style="margin-left: 5%;"> casa </td>
                    <td class="cella"> <input type="radio" name="pos_semantica_risp" value="ufficio" style="margin-left: 5%;"> ufficio  </td>
                </tr>
                <tr>
                    <th> - &nbsp; momento della giornata </th> 
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td class="cella"> <input type="radio" name="daytime_risp" value="mattina" style="margin-left: 5%;" required> mattina </td>
                    <td class="cella"> <input type="radio" name="daytime_risp" value="pomeriggio" style="margin-left: 5%;"> pomeriggio </td>
                    <td class="cella"> <input type="radio" name="daytime_risp" value="sera" style="margin-left: 5%;"> sera </td>
                </tr>
                <tr>
                    <th> - &nbsp; tempo metereologico </th>
                    <td></td>
                    <td></td>
                </tr>
                <tr> 
                    <td class="cella"> <input type="radio" name="meteo_risp" value="sereno" style="margin-left: 5%;" required> sereno </td>
                    <td class="cella"> <input type="radio" name="meteo_risp" value="variabile" style="margin-left: 5%;"> variabile </td>
                    <td class="cella"> <input type="radio" name="meteo_risp" value="pioggia" style="margin-left: 5%;"> pioggia </td>
                </tr>
                <tr>
                    <th> - &nbsp; presenza di altre persone </th> 
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td class="cella"> <input type="radio" name="persone_risp" value="sì, senza interazione" style="margin-left: 5%;" required> sì, <b>senza</b> interazione </td>
                    <td class="cella"> <input type="radio" name="persone_risp" value="sì, con interazione" style="margin-left: 5%;"> sì, <b>con</b> interazione </td>
                    <td class="cella"> <input type="radio" name="persone_risp" value="no" style="margin-left: 5%;"> no </td>
                </tr>
            </table>

            <div style="margin-top: 5%;" align="center">
            <center>
                <button type="submit" name="conferma" class="btn btn-block btn-success" style=" width: 50%;" ><span class="glyphicon glyphicon-edit"></span> Conferma </button>
                <button type="reset" class="btn btn-primary" ><span class="glyphicon glyphicon-refresh"></span> Pulisci form </button>
            </center>
            </div>
            </FORM><br>
            </div>';
        

    }
?>

</body>
</html>