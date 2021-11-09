package com.alesp.feedbackapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alesp.feedbackapp.utils.CertainOrder;
import com.alesp.feedbackapp.utils.InsertionOrder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by alesp on 20/03/2017.
 */

public class WakeUpService extends Service {

    //WakeUpService si occupa di chiamare, con un criterio non ancora deciso, l'attività QueryUser, per interrogarlo sull'attività
    //che sta facendo. Questo servizio girerà esclusivamente su IdleActivity.

    //UPDATE 22/03: WakeupService viene integrato a connectionService: in questo modo esso gestisce anche la connessione con il server.

    //Variabili utilizzate per la connessione TCP
    Client client;
    private String ip =  "159.149.145.59"; //"159.149.152.241";
    private int port = 3616; //1808

    boolean connected = false;
    // singleton access, devi modificare

    // singleton access, devi modificare
    private final IBinder binder = new WakeUpBinder();

    //Definisco variabile che gestisce il broadcaster (inviare messaggi alle activity)
    LocalBroadcastManager broadcast;




    @Override
    public void onCreate(){
        super.onCreate();

        //Inizializzo broadcaster
        broadcast = LocalBroadcastManager.getInstance(this);

        //Eseguo connessione
        connect();
    }

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }

    public class WakeUpBinder extends Binder {

        WakeUpService getService() {
            //In questo metodo ritorno un'istanza di WakeUpService in modo che le activity possano chiamare
            //i relativi metodi

            return WakeUpService.this;
        }

    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v("WakeUpService","Servizio Chiuso");
            client.disconnect();
    }

    //Definisco metodi custom che le activity chiameranno

    //Forse questo non serve
    public boolean isConnected(){
        return connected;
    }

    public boolean sendData(String message){
        //message = message.replaceAll("(.{1000})","$1;");

        Log.v("WakeUpService",message);

        //in questo metodo invio i dati al raspberry.
        if(isConnected()){
            client.send(message);
            return true;
        }
        else{
            return false;
        }

    }

    //Definisco metodo per inviare dati ad Activity Recognition
public void sendToActivity(String message){
       //In questo metodo "avviso" l'app che è arrivato un nuovo dato dal raspberry.
        Log.v ("message", message);
       Intent intent = new Intent("NOTIFY_ACTIVITY");

       if(message != null){
           intent.putExtra("CURRENT_ACTIVITY",message);
       }

       //invio messaggio tramite broadcaster
       broadcast.sendBroadcast(intent);

   }


    // come gestisco se activity è già aperta ?
    /*
        1. utilizzo sharedpreferences e quando rispondo\passa il tempo
           guardo se ci sono inserimenti ( regola accesso concorrente
           a shared prefs )
        2. utilizza priority queue condivisa a service e activity ( anche in questo
            caso va regolato l'accesso concorrente). Da fare con asynctask perchè non posso mettere
            ui-thread in wait
     */
    public boolean connect(){

        if(!connected) {

            //Effettuo connessione
            client = new Client(ip, port);

            //Setto callbacks
            client.setConnectionListener(new ConnectionListener() {
                @Override
                public void onMessage(String message) {
                    message = message.replace("\\","").substring(1);
                    message = message.substring(0,message.length()-1);
                    // String type;
                    Log.v("WakeUpService", "Ricevuto: " + message.replace("\\",""));

                    JSONObject currentActivities;
                    boolean uncertainty;


                    //Controllo il tipo di richiesta
                    try{
                    //    type = new JSONObject(message).getString("requestType");
                        currentActivities = new JSONObject(message);
                        uncertainty = currentActivities.getBoolean("uncertainty");
                       // Log.d("Type",type);

                        // ricevo dati incerti
                        if (uncertainty) {
                            //Invio query all'utente
                            //Se l'activity queryUser non è attiva, sveglio subito l'activity:
                            //altrimenti aspetto (?)
                            JSONArray data = currentActivities.getJSONArray("data");
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            Log.d("inQuery", "inQuery");



                            for (int i=0;i<data.length();i++){
                                JSONObject item = data.getJSONObject(i);
                                // c'è incertezza, esiste il campo priority
                                if ( item.has("priority")){
                                    JSONArray users_array = item.getJSONArray("users");
                                    ArrayList<String> users = new ArrayList<>();
                                    for (int j=0;j<users_array.length();j++){
                                        users.add(users_array.getString(j));
                                    }
                                    double priority = item.getDouble("priority");
                                    boolean flag = item.getBoolean("flag");
                                    String doubt = item.getString("doubt");
                                    JSONArray availabilities = item.getJSONArray("availabilities");
                                    ArrayList<Double> avails = new ArrayList<Double>();
                                    for (int j=0;j<availabilities.length();j++){
                                        avails.add(availabilities.getDouble(j));
                                    }
                                    JSONArray user_feedback_ids = item.getJSONArray("user_feedback_ids");
                                    ArrayList<String> user_ids = new ArrayList<>();
                                    for (int j=0;j<user_feedback_ids.length();j++){
                                        user_ids.add (user_feedback_ids.getString(j));
                                    }
                                    String id = currentActivities.getString("feedback_id");
                                    long time = item.getLong("time");
                                    int requests = item.getInt("requests");
                                    long latency = item.getLong("latency");
                                    long rtw = item.getLong("rtw");
                                    String user = item.getString("user");
                                    String activity = "";
                                    if (item.has("activity")){
                                        activity=item.getString("activity");
                                    }

                                    // Aggiungo in coda FIFO oggetto raprresentante un dubbio
                                    UsefulData uncertainData = new UsefulData(doubt,user,users,avails,priority,flag,id,time,requests,latency,rtw,user_ids,activity);
                                    InsertionOrder insertion_order = InsertionOrder.getInstance();

                                    insertion_order.addDoubt(uncertainData);

                                }
                                // non c'è campo priority, mi è arrivato un dato certo
                                else{
                                    String place = item.getString("place");
                                    String activity = item.getString("activity");
                                    JSONArray users = item.getJSONArray("users");
                                    ArrayList<String> sure_users = new ArrayList<>();
                                    for (int j=0;j<users.length();j++){
                                        sure_users.add(users.getString(j));
                                    }
                                    long time = item.getLong("time");
                                    String feedback_id = currentActivities.getString("feedback_id");
                                    CertainData sure = new CertainData(sure_users,place,activity,feedback_id,time);
                                    // aggiungo in coda FIFO oggetto rappresentante dato certo
                                    CertainOrder sure_order = CertainOrder.getInstance();
                                    sure_order.addCertain(sure);

                                }

                            }
                            // dopo aver aggiunto i dati alla coda FIFO , vedo se query user è già aperta o meno
                            // nel caso sia chiusa, avvio l'activity
                            if (!prefs.getBoolean("active", false)) {

                                Intent intent = new Intent("android.intent.category.LAUNCHER");

                            //Aggiungo all'intent, per ogni iterazione, ogni elemento del JSONarray
                                intent.putExtra("Data", message);


                                intent.setClassName("com.alesp.feedbackapp", "com.alesp.feedbackapp.QueryUser");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                        // i dati che ricevo sono sicuri
                        }else{


                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                // se query user non è attiva, sono i primi dati che ricevo e
                                // possono essere mostrati subito

                            if (!prefs.getBoolean("active",false)) {
                                sendToActivity(message);
                                    //Eseguo suono di notifica di nuova attività ricevuta
                                    //Faccio partire suono notifica
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();
                                // altrimenti conservo i dati, che verrano eventualmente mostrati
                            }else {
                                    // aggrego dati sicuri a copywritearraydata
                                JSONArray data = currentActivities.getJSONArray("data");
                                for (int i=0;i<data.length();i++){
                                    JSONObject item = data.getJSONObject(i);
                                    String place = item.getString("place");
                                    String activity = item.getString("activity");
                                    JSONArray users = item.getJSONArray("users");
                                    ArrayList<String> sure_users = new ArrayList<>();
                                    for (int j=0;j<users.length();j++){
                                        sure_users.add(users.getString(j));
                                    }
                                    long time = item.getLong("time");
                                    String feedback_id = currentActivities.getString("feedback_id");
                                    CertainData sure = new CertainData(sure_users,place,activity,feedback_id,time);
                                    CertainOrder order =CertainOrder.getInstance();
                                    order.addCertain(sure);
                                }


                            }




                        }

                    } catch (JSONException e){
                        Log.e("WakeUpService",Log.getStackTraceString(e));
                    }




                }

                @Override
                public void onConnect(Socket socket) {
                    Log.v("WakeUpService", "Connected to the server");
                    connected = true;
                }

                @Override
                public void onDisconnect(Socket socket, String message) {
                    Log.v("WakeUpService", "Disconnected");
                    connected = false;

                    //In questo metodo "avviso" l'app che è caduta la connessione

                    Intent intent = new Intent("NOTIFY_ACTIVITY");

                    if(message != null){
                        intent.putExtra("CONNECTION_LOST",true);
                    }

                    //invio messaggio tramite broadcaster
                    broadcast.sendBroadcast(intent);
                }

                @Override
                public void onConnectError(Socket socket, String message) {
                    Log.e("WakeUpService", "Connection Error: " + message);
                    connected = false;

                }
            });

            //Connetto al Server
            client.connect();

            Log.v("Service, onCreate", "Connessione TCP iniziata");
        }

        return connected;
    }

    public void disconnect(){
        if(connected) {
            client.disconnect();
        }
    }



    // Ordinamento per definire la priorità di accodamento
    public static class sortData implements Comparator<UsefulData> {

        @Override
        public int compare(UsefulData usefulData, UsefulData t1) {

            if (usefulData.priority < t1.priority)
                return 1;
            if (usefulData.priority > t1.priority)
                return -1;

            return 0;
        }
    }



}
