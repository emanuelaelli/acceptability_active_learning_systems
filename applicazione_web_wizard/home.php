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
                            <?php
                            session_start();
                            if($_SESSION['idsessione'] == 0){
                                echo "<a class=\"nav-link\" href=\"home.php\">
                                    <div class=\"sb-nav-link-icon\"><i class=\"fas fa-tachometer-alt\"></i></div>
                                        Home
                                    </a>";
                            } else {
                                echo "<a class=\"nav-link\" href=\"home_sessione.php\">
                                    <div class=\"sb-nav-link-icon\"><i class=\"fas fa-tachometer-alt\"></i></div>
                                        Home
                                    </a>";
                            }
                            ?>
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
                <main >
                    <div style="text-align: center; padding-top: 20%;">
                    <button onclick="location.href='nuova_sessione.html'" type="submit" class="btn btn-block" 
                    style="background-color:#5271FF; color: white; padding: 3% 7% 3% 7%; font-weight: bold;">
                        Genera nuova sessione
                    </button>
                </div>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
        <script src="js/scripts.js"></script>

    </body>
</html>
