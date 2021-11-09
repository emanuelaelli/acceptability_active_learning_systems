package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class QueryAnswer extends Activity {

    //Definisco variabili per la UI
    static ImageButton firstactivity;
    static ImageButton secondactivity;
    ImageButton noneactivity;

    RecognitionProgressView progress;
    TextView userinput;
    TextView title;
    static TextView firstactivityText;
    static TextView secondactivityText;
    TextView noneActivityText;


    //per sapere indirizzo IP
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_answer);

        firstactivity = (ImageButton) findViewById(R.id.firstactivity_button);
        secondactivity = (ImageButton) findViewById(R.id.secondactivity_button);
        noneactivity = (ImageButton) findViewById(R.id.noneactivity_button);
        title = (TextView) findViewById(R.id.title);
        progress = (RecognitionProgressView) findViewById(R.id.progress);
        userinput = (TextView) findViewById(R.id.userinput);
        firstactivityText = (TextView) findViewById(R.id.firstactivity_text);
        secondactivityText = (TextView) findViewById(R.id.secondactivity_text);
        noneActivityText = (TextView) findViewById(R.id.noneactivity_text);

        String ip = getIpAddress();
        System.out.println(ip);

        title.setVisibility(View.VISIBLE);
        firstactivity.setVisibility(View.VISIBLE);
        secondactivity.setVisibility(View.VISIBLE);
        noneactivity.setVisibility(View.VISIBLE);

        // parte suono notifica
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        setButton(1, ServerSocketTask.opzione1);
        setButton(2, ServerSocketTask.opzione2);

        // imposto timer, se il timer scade invia risposta nulla
        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            public void run() {
                if (risposta == "") {
                    System.out.println("qui");
                    FeedbackOutput output = new FeedbackOutput(ServerSocketTask.numquery, null, null, ServerSocketTask.timestamp1.toString(), null);
                    Gson g = new Gson();
                    ServerSocketTask.printWriter.write(g.toJson(output));
                    ServerSocketTask.printWriter.flush(); // svuota tutto e torna indietro

                    Intent torna_alla_home = new Intent(QueryAnswer.this, HomeActivity.class);
                    startActivity(torna_alla_home);
                }
            }
        };
        timer.schedule(tt, 5000);

        // selezione della prima attività tramite button click
        firstactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                sendResponse(1);
            }
        });

        // selezione della prima attività tramite button click
        secondactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(2);
            }
        });

        // selezione di none/other attraverso button click
        noneactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(0);
            }
        });


    }
    String risposta = "";

    private void sendResponse(int numBottone) {
        //new SendResponseAsyncTask().execute(risultato);
        new Thread(new Runnable(){
            @Override
            public void run() {
                // Do network action in this function
                Intent sentimento = new Intent(QueryAnswer.this, QueryFeeling.class);


                if (numBottone == 1) {
                    risposta = ServerSocketTask.opzione1;
                } else if (numBottone == 2) {
                    risposta = ServerSocketTask.opzione2;
                } else if (numBottone == 0) {
                    risposta = "nessuna";
                }

                ServerSocketTask.printWriter.flush(); // svuota tutto e torna indietro
                // ServerSocketTask.printWriter.close();

                sentimento.putExtra("risposta", risposta);
                startActivity(sentimento);
            }
        }).start();

    }

    /*static class SendResponseAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());

            ServerSocketTask.printWriter.write(ServerSocketTask.numquery + "/" + params[0] + "/" + ServerSocketTask.timestamp1.toString() + "/" + timestamp2.toString());
            ServerSocketTask.printWriter.flush(); // svuota tutto e torna indietro
            ServerSocketTask.printWriter.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

        }
    }*/

    public static void setButton(int i, String label) { // setta text e layout dei bottoni
        if ( i == 1 ) {
            firstactivityText.setText(label);
            setLayout(firstactivity, label);
        } else if (i == 2) {
            secondactivityText.setText(label);
            setLayout(secondactivity, label);
        }
    }

    // setta image resource del primo e del secondo bottone
    public static void setLayout(ImageButton activity, String label) {
        switch(label.trim()){
            case "cucinare":
                activity.setImageResource(R.drawable.ic_lunch);
                break;
            case "preparare un pasto freddo":
                activity.setImageResource(R.drawable.ic_meal);
                break;
            case "lavare i piatti":
                activity.setImageResource(R.drawable.ic_dish);
                break;
            case "apparecchiare la tavola":
                activity.setImageResource(R.drawable.ic_table);
                break;
            case "sparecchiare la tavola":
                activity.setImageResource(R.drawable.ic_clear_table);
                break;
            case "mangiare":
                activity.setImageResource(R.drawable.ic_restaurant);
                break;
            case "usare il computer":
                activity.setImageResource(R.drawable.ic_analytics);
                break;
            case "guardare la tv":
                activity.setImageResource(R.drawable.ic_tv_screen);
                break;
            case "prendere le medicine":
                activity.setImageResource(R.drawable.ic_drugs);
                break;
            case "camminare":
                activity.setImageResource(R.drawable.ic_camminare);
                break;
            case "correre":
                activity.setImageResource(R.drawable.ic_correre);
                break;
            case "stare sdraiato":
                activity.setImageResource(R.drawable.ic_stare_sdraiato);
                break;
            case "stare seduto":
                activity.setImageResource(R.drawable.ic_stare_seduto);
                break;
            case "salire le scale":
                activity.setImageResource(R.drawable.ic_salire_le_scale);
                break;
            case "scendere le scale":
                activity.setImageResource(R.drawable.ic_scendere_le_scale);
                break;
            case "salire con l'ascensore":
                activity.setImageResource(R.drawable.ic_salire_con_l_ascensore);
                break;
            case "scendere con l'ascensore":
                activity.setImageResource(R.drawable.ic_scendere_con_l_ascensore);
                break;
            case "andare in bicicletta":
                activity.setImageResource(R.drawable.ic_andare_in_bicicletta);
                break;
            case "guidare un mezzo di trasporto":
                activity.setImageResource(R.drawable.ic_guidare_un_mezzo_di_trasporto);
                break;
            case "stare seduti sui mezzi di trasporto":
                activity.setImageResource(R.drawable.ic_stare_seduti_sui_mezzi_di_trasporto);
                break;
            case "stare in piedi sui mezzi di trasporto":
                activity.setImageResource(R.drawable.ic_stare_in_piedi_sui_mezzi_di_trasporto);
                break;
            default:
                activity.setImageResource(R.drawable.ic_volume_off);
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

