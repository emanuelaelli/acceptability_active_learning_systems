package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alesp.feedbackapp.utils.CertainOrder;
import com.alesp.feedbackapp.utils.InsertionOrder;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by alesp on 14/03/2017.
 */

public class QueryUser extends Activity implements RecognitionListener {
    public static final int ANSWER = 0;
    public static final int NOT_ANSWER = 1;
    public static final int NOT_QUERIED = 2;


    int counter = 0;
    int selected = -1;
    boolean answered_flag = true;




    //Definisco variabili per la UI
    ImageButton firstactivity;
    ImageButton secondactivity;
    Button thirdactivity;
    Button otheractivity;

    ImageButton noneactivity;

    boolean firstOrSecond = false;


    RecognitionProgressView progress;
    TextView userinput;
    TextView title;
    TextView firstactivityText;
    TextView secondactivityText;
    TextView noneActivityText;
    UsefulData currentData;
    //creo variabile per text to speech
    TextToSpeech textToSpeech;


    //Creo costanti per l'attività selezionata


    //definisco altre variabili
    JSONObject receivedData;

    JSONArray data;


    String [] placeIndecision ;
    String [] activityIndecision;


    long startedTime;
    long current;

    long nextCurrent = 15000;

   // JSONArray probActivities;
    //int maxIndex;
    int index = 0;

    SharedPreferences sp;






    //definisco variabile per il riconoscimento vocale
    SpeechRecognizer recognizer;
    SpeechRecognizer yesornorecognizer;
    Intent recognitionIntent;
    RecognitionListener yesornolistener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int errorCode) {
            String message = "No Input";

            //controllo i vari codici di errore ed agisco di conseguenza
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    //Se non ho la permission, la chiedo all'utente
                    //Controllo se l'app ha il permesso di utilizzare il microfono, altrimenti lo chiedo.
                    //Ciò è indispenabile per il riconoscimento vocale (altrimento non funziona

                    new AlertDialog.Builder(QueryUser.this)
                            .setTitle(getString(R.string.insuffpermtitle))
                            .setMessage(getString(R.string.insuffpermdescr))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();

                    //FAI ALERT DIALOG CHIEDENDO PERMISSIONS
                    message = "Insufficient permissions";
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network error";
                    //esco da queryUser
                    new AlertDialog.Builder(QueryUser.this)
                            .setTitle("Connection not available")
                            .setMessage("Couldn't connect to the server. Please try later.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(false);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error from server";
                    break;
                default:
                    //counter++;

                    //textToSpeech.speak(getString(R.string.retry), TextToSpeech.QUEUE_ADD, null, "RETRY");
                    progress.stop();
                    progress.play();
                    answered_flag=false;
                    if (currentData.status==NOT_QUERIED){
                        currentData.status=NOT_ANSWER;
                    }
                    setAnswersForJSON(currentData,true);
                    goToSendFeedback();
                    break;
            }
            Log.e("QueryUser", "FAILED " + message+" "+counter);
        }

        @Override
        public void onResults(Bundle results) {
            //qui gestisco i risultati.
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String result = matches.get(0);

            userinput.setText(result);
            Log.i("QueryUser","onResults");
            //qui gestisco i risultati.

            if(result.toLowerCase().contains("yes")){
                //textToSpeech.speak("Ok",TextToSpeech.QUEUE_ADD,null,null);
               // yesornorecognizer.destroy();
                //finish();
                answered_flag=false;
                if (currentData.status==NOT_QUERIED){
                    currentData.status=NOT_ANSWER;
                }
                setAnswersForJSON(currentData,true);
                goToSendFeedback();
            }
            else if(result.toLowerCase().contains("no")){

                yesornorecognizer.stopListening();
                yesornorecognizer.destroy();


                progress.stop();
                progress.setSpeechRecognizer(recognizer);
                progress.setRecognitionListener(QueryUser.this);
                progress.play();




                counter++;
                if (counter<3) {


                    if (currentData.getFlag())
                        textToSpeech.speak(getString(R.string.whichActivity), TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");
                    else
                        textToSpeech.speak(getString(R.string.whichPlace), TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");
                }else{
                    answered_flag=false;
                    if (currentData.status==NOT_QUERIED){
                        currentData.status=NOT_ANSWER;
                    }
                    setAnswersForJSON(currentData,true);
                    goToSendFeedback();
                }
            }
            else{
                // tecniche di nlp anche per gestire yesornorecognizer
                answered_flag=false;
                if (currentData.status==NOT_QUERIED){
                    currentData.status=NOT_ANSWER;
                }
                setAnswersForJSON(currentData,true);
                goToSendFeedback();
            }



        }


        @Override
        public void onPartialResults(Bundle partialResults) {
            //qui gestisco i risultati.
            ArrayList<String> matches = partialResults
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            userinput.setText(matches.get(0));
            Log.i("QueryUser","onPartialResults");
            //qui gestisco i risultati.
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    //Questa variabile serve per tenere traccia del bottone other cliccato. se esso è stato cliccato, vuole dire che si stanno
    //guardando le attività 4-5-6 e quindi bisognerà riferirsi a quegli indici. altrimenti le attività interessate saranno la 1-2-3.
    boolean otherbuttonPressed = false;

    //Questa variabile serve per ottenere i dati dal service
    Bundle datafromService;

    //Definisco il mio service e il boolean boundtoactivity per indicare se il processo
    // è collegato all'activity
    private WakeUpService wakeService;
    private boolean wakeupBoundToActivity = false;

    private ServiceConnection wakeupConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("WakeUpServ_QueryUs", "Servizio connesso");

            //Setto il flag boundtoprocess = true
            wakeupBoundToActivity = true;

            //Effettuo il collegamento (giusto?)
            WakeUpService.WakeUpBinder binder = (WakeUpService.WakeUpBinder) service;
            wakeService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("WakeUpServ_QueryUs", "Servizio disconnesso");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_user);

        //faccio collegamenti vari
        firstactivity = (ImageButton) findViewById(R.id.firstactivity_button);
        secondactivity = (ImageButton) findViewById(R.id.secondactivity_button);
        noneactivity = (ImageButton) findViewById(R.id.noneactivity_button);
        // thirdactivity = (Button) findViewById(R.id.third_activity);
        // otheractivity = (Button) findViewById(R.id.other);
        title = (TextView) findViewById(R.id.title);
        progress = (RecognitionProgressView) findViewById(R.id.progress);
        userinput = (TextView) findViewById(R.id.userinput);
        firstactivityText = (TextView) findViewById(R.id.firstactivity_text);
        secondactivityText = (TextView) findViewById(R.id.secondactivity_text);
        noneActivityText = (TextView) findViewById(R.id.noneactivity_text);
        //Effettuo il binding con WakeUpService
        Intent servIntent = new Intent(QueryUser.this, WakeUpService.class);
        bindService(servIntent, wakeupConnection, Context.BIND_AUTO_CREATE);
        wakeupBoundToActivity = true;




        //Prendo sharedpreferences
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.commit();

        // prendi da sharedpreferences la dimensione della finestra temporale
        // relativa alla singola richiesta





        //Faccio partire suono notificai
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        //Se l'opzione per lo speechrecognizer è attiva
        if (sp.getBoolean("voiceEnabled", true)) {
            //Setto intent per lo speechrecognizer

            recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());


            //Inizializzo lo speech recognizer e setto il RecognitionprogressView
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(this);
            progress.setSpeechRecognizer(recognizer);
            progress.setRecognitionListener(this);

            progress.setVisibility(View.VISIBLE);

            //Inizializzo speech recognizer per il yes or no
            yesornorecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            yesornorecognizer.setRecognitionListener(yesornolistener);




        }


        //Inizializo grafica speechrecognizerview
        int[] colors = {
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };

        int[] heights = {60, 76, 58, 80, 55};

        progress.setColors(colors);
        progress.setBarMaxHeightsInDp(heights);
        progress.play();


        title.setVisibility(View.VISIBLE);
        firstactivity.setVisibility(View.VISIBLE);
        secondactivity.setVisibility(View.VISIBLE);
        noneactivity.setVisibility(View.VISIBLE);




            //DoubtQueue queue = DoubtQueue.getInstance();
        InsertionOrder queue = InsertionOrder.getInstance();



        // prendo dubbio in cima coda FIFO
        final UsefulData getFirst = queue.getList().get(0);
        currentData = getFirst;



            // place or activity
        if (!getFirst.getFlag()){
                // dubbio relativo a posizione
            if(getFirst.getUsers().size()==1) {
                title.setTextSize(70);
                title.setText(nameFromCode(getFirst.user) + " where are you ? ");
                settingTTS(getFirst.user,"");
            }
            else {

                String usernames="";
                for (int i=0;i<getFirst.getUsers().size();i++){
                    if (!(getFirst.getUsers().get(i).equals(getFirst.user)))
                        usernames+=nameFromCode(getFirst.getUsers().get(i))+",";
                    }
                    usernames=usernames.substring(0,usernames.length()-1);
                    int last = usernames.lastIndexOf(",");
                    if(last!=-1)
                        usernames = usernames.substring(0,last)+" and "+usernames.substring(last+1,usernames.length());
                    title.setText(nameFromCode(getFirst.user) + ",  where are you with " + usernames + "?");
                    title.setTextSize(40);
                    settingTTS(getFirst.user,usernames);
            }
        }else{
            // activity doubt;
            Log.v("Activity","Activity");

            // solo un utente coinvolto nel dubbio
            if (getFirst.getUsers().size()==1){
                title.setTextSize(70);
                title.setText(nameFromCode(getFirst.user) + " what are you doing ? ");
                settingTTS(getFirst.user,"");
            }else{


                String usernames="";
                for (int i=0;i<getFirst.getUsers().size();i++){
                    if (!(getFirst.getUsers().get(i).equals(getFirst.user)))
                        usernames+=nameFromCode(getFirst.getUsers().get(i))+",";
                }

                usernames=usernames.substring(0,usernames.length()-1);
                int last = usernames.lastIndexOf(",");
                // se last è -1 sono solo due utenti coinvolti nel dubbio
                if (last!=-1)
                    //Log.v("QueryUser",usernames+" Index:"+last);
                    usernames = usernames.substring(0,last)+" and "+usernames.substring(last+1,usernames.length());

                title.setText(nameFromCode(getFirst.user)+ ",  what are you doing with " + usernames + "?");
                title.setTextSize(40);

                settingTTS(getFirst.user,usernames);

            }
        }

        setLayout(getFirst.getDoubt());

        startedTime = System.currentTimeMillis();
        current = startedTime;



        // selezione della prima attività tramite button click
        firstactivity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){
                selected=0;
                firstOrSecond=false;
                Log.v("QueryUser","TaskIndex "+index);

                setAnswer(currentData,false);
                goToSendFeedback();
            }
        });

        // selezione della prima attività tramite button click
        secondactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected=1;
                firstOrSecond=true;
                Log.v("QueryUser","TaskIndex "+index);

                setAnswer(currentData,false);
                goToSendFeedback();
                }
        });

        // selezione di none/other attraverso button click
        noneactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected=2;
                setAnswer(currentData,true);
                goToSendFeedback();
            }
        });






    }



    @Override
    protected void onStart() {
        super.onStart();

        //Inserisco nelle sharedpref che l'activity sta andando (mi serve nel wakeup service)
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    }




    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.commit();

        //Stoppo TTS
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (wakeupBoundToActivity) {
            unbindService(wakeupConnection);
            wakeupBoundToActivity = false;
        }

        //Stoppo speechrecognition
        if (recognizer != null) {
            recognizer.destroy();
            Log.v("QueryUser", "Recognizer stoppato");
        }

        InsertionOrder insertion_list = InsertionOrder.getInstance();
        insertion_list.clear();
        // rimuovi anche i dati sicuri


    }

    public void animate(final boolean expired) {
        //Questo metodo contiene tutte le animazioni che vengono effettuate una volta toccata l'attività corrispondente.

        if (sp.getBoolean("voiceEnabled", true)) {

            YoYo.with(Techniques.FadeOut)
                    .duration(700)
                    .playOn(progress);

            //Stoppo speechview e recognizer
            progress.stop();
            recognizer.destroy();
        }

        //Faccio animazioni dei bottoni, ecc, che scompaiono, e poi li rimuovo

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(firstactivity);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(secondactivity);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(firstactivityText);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(secondactivityText);

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.titleDescr));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.userinput));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.title));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.userinput));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.noneactivity_button));

        YoYo.with(Techniques.FadeOut)
                .duration(700)
                .playOn(findViewById(R.id.noneactivity_text));

        //Aspetto la fine dell'animazione, e rimuovo tutto
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //rimuovo i vari elementi dalla view, per fare posto alla scritta con "feedback ricevuto"
                firstactivity.setVisibility(View.GONE);
                secondactivity.setVisibility(View.GONE);
                firstactivityText.setVisibility(View.GONE);
                secondactivityText.setVisibility(View.GONE);
                noneactivity.setVisibility(View.GONE);
                noneActivityText.setVisibility(View.GONE);
                //thirdactivity.setVisibility(View.GONE);
                findViewById(R.id.titleDescr).setVisibility(View.GONE);
                findViewById(R.id.title).setVisibility(View.GONE);
                findViewById(R.id.userinput).setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                TextView final_view = findViewById(R.id.feedbackReceived);

                //faccio comparire la scritta di feedback ricevuto
                findViewById(R.id.feedbackReceived).setVisibility(View.VISIBLE);

                // se non viene fornita una risposta
                if(expired){
                    final_view.setText("Time expired! Feedback is not complete!");

                }
                YoYo.with(Techniques.FadeIn)
                        .duration(700)
                        .playOn(findViewById(R.id.feedbackReceived));

                /*Faccio partire suono notifica
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play(); TROVA MODO DI CAMBIARE SUONERIA*/

                if(sp.getBoolean("voiceEnabled",true)) {
                    // se invece viene fornita una risposta
                    if (!expired) {
                        switch(selected){
                            case 0:
                                final_view.setText("You've selected : "+ firstactivityText.getText().toString());
                                break;
                            case 1:
                                final_view.setText("You've selected : "+ secondactivityText.getText().toString());
                                break;
                            case 2:
                                final_view.setText("You've selected : "+ noneActivityText.getText().toString());
                                break;

                        }
                        textToSpeech.speak(final_view.getText().toString(), TextToSpeech.QUEUE_ADD, null, "FEEDBACKRECEIVED");

                    }
                    else
                        textToSpeech.speak("Time expired! Feedback is not complete!", TextToSpeech.QUEUE_ADD, null, "FEEDBACKRECEIVED");

                }
                else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //faccio passsare un secondo (o poco meno) e termino l'activity
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    },1200);
                }
                    }

        }, 700);


    }





    // Questo metodo modifica l'oggetto corrispondente al dubbio inserendo la risposta
    public void setAnswer (UsefulData data, boolean flag){
        // se fornisco una risposta ( prima o seconda attività più probabile)
        if(!flag) {
            // prima attività proposta
            if (!firstOrSecond) {
                StringTokenizer tokenizer = new StringTokenizer(data.getDoubt(), ",");
                data.setAnswer(tokenizer.nextToken());
                data.setStatus(ANSWER);

            }
            // seconda attività proposta
            else {
                StringTokenizer tokenizer = new StringTokenizer(currentData.getDoubt(), ",");
                tokenizer.nextToken();
                data.setAnswer(tokenizer.nextToken());
                data.setStatus(ANSWER);

                firstOrSecond = true;

            }
        }
        // fornisco None/Other come risposta
        else{
            for (int i=0;i<currentData.users.size();i++){
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(nameFromCode(currentData.users.get(i)),"");
                editor.commit();
            }
            data.setAnswer("");
            data.setStatus(ANSWER);
        }
        setAnswersForJSON(data,false);
    }







    // metodo utilizzato per mandare feedback ad ActivityRecognitionFragment e WakeUpService
    // Il Service si occuperà di inoltrarlo al server
    public void sendFeedback() throws JSONException{



        // Prende le code FIFO di dati sicuri e insicuri,
        // questo perchè mentre QueryUser è attiva possono arrivare altri dubbi
        // devo quindi mostrare a server e fragment come sono stati gestiti dati non mostrati
        // (una risposta può anche essere valida per più domande, se sono uguali)

        InsertionOrder order = InsertionOrder.getInstance();
        CopyOnWriteArrayList<UsefulData> list = order.getList();
        CertainOrder sure_order = CertainOrder.getInstance();
        CopyOnWriteArrayList<CertainData> sure_list = sure_order.getList();



        JSONObject answer = new JSONObject();

        JSONArray all_answers = new JSONArray();

        for (int i=0;i<list.size();i++){
            JSONObject item = new JSONObject();
            item.put("status",list.get(i).status);
            item.put("doubt",list.get(i).doubt);
            item.put("user",list.get(i).user);
            //item.put("priority",list.get(i).priority);
            item.put("flag",list.get(i).flag);
            item.put("answer",list.get(i).answer);
            JSONArray users=new JSONArray();
            for (int j=0;j<list.get(i).users.size();j++){
                users.put(list.get(i).users.get(j));
            }
            JSONArray user_feedback_ids = new JSONArray();
            for (int j=0;j<list.get(i).user_feedback_ids.size();j++){
                user_feedback_ids.put(list.get(i).user_feedback_ids.get(j));
            }
            item.put("user_feedback_ids",user_feedback_ids);
            item.put("users",users);
            item.put("feedback_id",list.get(i).feedback_id);
            item.put("time",list.get(i).time);
            item.put("latency",list.get(i).latency);
            item.put("activity",list.get(i).activity);
            all_answers.put(item);
        }
        answer.put("answers",all_answers);


        JSONObject sure_data = new JSONObject();
        JSONArray all_sure = new JSONArray();

        for (int i=0;i<sure_list.size();i++){
            JSONObject item = new JSONObject();
            item.put("place",sure_list.get(i).place);
            item.put("activity",sure_list.get(i).activity);
            JSONArray users=new JSONArray();
            for (int j=0;j<sure_list.get(i).users.size();j++){
                users.put(sure_list.get(i).users.get(j));
            }
            item.put("users",users);
            item.put("feedback_id",sure_list.get(i).feedback_id);
            item.put("time",sure_list.get(i).time);
            all_sure.put(item);
        }
        sure_data.put("sure_data",all_sure);



        Log.v("multi_feedback",answer.toString());

        // sendData inoltra eventuali risposte al server,
        // quindi mando solo dati relativi a domande ricevute
        wakeService.sendData(answer.toString()+"\n");



        JSONObject toActivity = new JSONObject();
        toActivity.put("certain",sure_data);
        toActivity.put("uncertain",answer);
        toActivity.put("uncertainty",true);
        Log.v("toFragment",toActivity.toString());


        // all'activity recognition fragment interessano anche dati sicuri
        // che sono arrivati nel frattempo
        wakeService.sendToActivity(toActivity.toString());


        animate (!answered_flag) ;


    }

    // al termine dell'aggiornamento dei dati, si passa all'invio del feedback
    public void goToSendFeedback() {

        Log.v("QueryUser","executeNextData");
        if (textToSpeech != null) {
            textToSpeech.stop();
        }

        if (sp.getBoolean("voiceEnabled", true)) {
            recognizer.destroy();
            progress.stop();


        }




        Log.d("FEEDBACK", "GO_TO_FEEDBACK");



        try {
            sendFeedback();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }











    //INIZIO METODI PER LA SPEECH RECOGNITION

    //in questo metodo faccio partire il listening, imposto l'icona e faccio partire il suono
    public void startRecognition(){
        recognizer.startListening(recognitionIntent);
        userinput.setText("");
    }


    @Override
    public void onError(int errorCode) {
        String message = "No Input";

        //controllo i vari codici di errore ed agisco di conseguenza
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                //Se non ho la permission, la chiedo all'utente
                //Controllo se l'app ha il permesso di utilizzare il microfono, altrimenti lo chiedo.
                //Ciò è indispenabile per il riconoscimento vocale (altrimento non funziona

                new AlertDialog.Builder(QueryUser.this)
                        .setTitle(getString(R.string.insuffpermtitle))
                        .setMessage(getString(R.string.insuffpermdescr))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show()
                        .setCanceledOnTouchOutside(false);

               //FAI ALERT DIALOG CHIEDENDO PERMISSIONS
                message = "Insufficient permissions";
                break;

            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network error";
                //esco da queryUser
                new AlertDialog.Builder(QueryUser.this)
                        .setTitle("Connection not available")
                        .setMessage("Couldn't connect to the server. Please try later.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            default:

                counter++;
                Log.v("QueryUser"," default speech recognizer");
                if (counter < 2) {
                    textToSpeech.speak(getString(R.string.retry), TextToSpeech.QUEUE_ADD, null, "RETRY");
                    progress.stop();
                    recognizer.destroy();
                    recognizer = SpeechRecognizer.createSpeechRecognizer(this);
                    recognizer.setRecognitionListener(this);
                    progress.setSpeechRecognizer(recognizer);
                    progress.setRecognitionListener(this);


                    //progress.play();


                    progress.play();
                }else{
                    //progress.stop();
                    updateUserdata();
                    answered_flag=false;
                    if (currentData.status==NOT_QUERIED){
                        currentData.status=NOT_ANSWER;
                    }
                    setAnswersForJSON(currentData,true);
                    goToSendFeedback();
                }
                break;



        }
        Log.e("QueryUser", "FAILED " + message);

    }


    @Override
    public void onPartialResults(Bundle arg0) {
        //qui gestisco i risultati.
        ArrayList<String> matches = arg0
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        userinput.setText(matches.get(0));
        Log.i("QueryUser","onPartialResults");
        //qui gestisco i risultati.
    }


    // Il semplice contains non è in grado di distinguere
    // dining room / living room
    // setting up the table / clearing up the table
    @Override
    public void onResults(Bundle results) {


        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);


        String result;





        result = matches.get(0).toLowerCase();


        // chiamata ad asynctask dove viene effettuata chiamata a server rest per fare controllo semantico della risposta
        new Rest_NLP (firstactivityText.getText().toString().toLowerCase(),secondactivityText.getText().toString().toLowerCase(),result.toLowerCase()).execute();

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i("QueryUser", "onRmsChanged: " + rmsdB);
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i("QueryUser", "onReadyForSpeech");
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i("QueryUser", "onEvent");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i("QueryUser", "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i("QueryUser", "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i("QueryUser", "onEndOfSpeech");;
    }

    // animazione che intercorre tra un dubbio e l'altro





    // setta texttospeech
    public void settingTTS(String name, final String usernames) {
        final String username=name;
        if (sp.getBoolean("voiceEnabled", true)) {
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    //qui posso cambiare impostazioni, come il locale e altro
                    textToSpeech.setLanguage(Locale.getDefault());
                    Log.v("QueryUser", "TTS inizializzato");
                   // String other_names = queryMoreThanOne();

                    //Faccio partire la vocina
                    if(currentData.getFlag()) {
                        if (usernames.equals(""))
                            textToSpeech.speak(nameFromCode(username) + ", " + getString(R.string.whichActivity), TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");
                        else
                            textToSpeech.speak(nameFromCode(username) + ", " +  "which activity are you doing with "+usernames+" ?", TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");

                    }else {
                        if (usernames.equals(""))
                            textToSpeech.speak(nameFromCode(username) + ", " + getString(R.string.whichPlace), TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");
                        else
                            textToSpeech.speak(nameFromCode(username) + ", where are you with "+usernames+"?", TextToSpeech.QUEUE_FLUSH, null, "WHICH_ACTIVITY");

                    }
                    //Faccio partire listener una volta che il TTS finisce di parlare
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            //Quando finisco di pronunciare la frase, inizio con il listening

                            //NOTA BENE: per qualche motivo strano questo codice è eseguito in un altro thread. mi tocca fare un runonuithread per far
                            //andare lo speech recognizer.

                            switch (utteranceId) {


                                case "WHICH_ACTIVITY":

                                        if (currentData.getFlag()) {
                                            textToSpeech.speak("Are you " + firstactivityText.getText().toString()
                                                    + " ?", TextToSpeech.QUEUE_ADD, null, null);
                                            textToSpeech.speak("Or are you" + secondactivityText.getText().toString() + " ?", TextToSpeech.QUEUE_ADD, null, "SPECIFIC");
                                        } else {
                                            textToSpeech.speak("Are you in the " + firstactivityText.getText().toString()
                                                    + " ?", TextToSpeech.QUEUE_ADD, null, null);
                                            textToSpeech.speak("Or are you in the " + secondactivityText.getText().toString() + " ?", TextToSpeech.QUEUE_ADD, null, "SPECIFIC");
                                        }



                                    break;

                                case "SPECIFIC":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            //faccio partire il listening
                                            //startRecognition();

                                            progress.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                   // progress.play();
                                                    startRecognition();
                                                }
                                            }, 50);
                                        }
                                    });
                                    break;

                                case "RETRY":

                                    //qua è da modificare
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            //faccio partire il listening
                                            startRecognition();

                                            progress.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startRecognition();
                                                }
                                            }, 50);
                                        }
                                    });

                                    break;

                                case "MISTAKE":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //fermo progressbar e recognizer, faccio partire yesornorecognizer
                                            userinput.setText("");
                                            recognizer.stopListening();
                                            recognizer.destroy();
                                            progress.stop();
                                            progress.setSpeechRecognizer(yesornorecognizer);
                                            progress.setRecognitionListener(yesornolistener);
                                            progress.play();
                                            progress.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    yesornorecognizer.startListening(recognitionIntent);
                                                }
                                            }, 50);
                                        }
                                    });


                                    break;

                                case "FEEDBACKRECEIVED":
                                    //faccio passsare un secondo (o poco meno) e termino l'activity
                                    finish();
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    break;
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                }
            });
        }
    }

    // setta image resource del primo e del secondo bottone
    public void setLayout (String token){
        StringTokenizer tokenizer = new StringTokenizer(token,",");
        switch(tokenizer.nextToken()){
            case "OUT":
               firstactivityText.setText("OUT");
                firstactivity.setImageResource(R.drawable.ic_entrance);
                break;

            case "DINING_ROOM":
                firstactivity.setImageResource(R.drawable.ic_restaurant);
                firstactivityText.setText("DINING ROOM");
                break;
            case "OFFICE":
                firstactivityText.setText("OFFICE");
                firstactivity.setImageResource(R.drawable.ic_office);
                break;
            case "LIVING_ROOM":
                firstactivity.setImageResource(R.drawable.ic_living_room);
                firstactivityText.setText("LIVING ROOM");

                break;

            case "GETTING_IN":
                firstactivityText.setText("Getting in");
                firstactivity.setImageResource(R.drawable.ic_knocking);
                break;
            case "GETTING_OUT":
                firstactivityText.setText("Getting out");
                firstactivity.setImageResource(R.drawable.ic_getting_out);

                break;
            case "COOKING":
                firstactivity.setImageResource(R.drawable.ic_lunch);
                firstactivityText.setText("Cooking");
                break;
            case "PREPARING_COLD_MEAL":
                firstactivityText.setText("Preparing cold meal");
                firstactivity.setImageResource(R.drawable.ic_meal);
                break;
            case "WASHING_DISHES":
                firstactivityText.setText("Washing dishes");
                firstactivity.setImageResource(R.drawable.ic_dish);
                break;
            case "SETTING_UP_TABLE":
                firstactivity.setImageResource(R.drawable.ic_table);
                firstactivityText.setText("Setting up the table");
                break;
            case "CLEARING_TABLE":
                firstactivity.setImageResource(R.drawable.ic_clear_table);
                firstactivityText.setText("Clearing the table");
                break;

            case "EATING":
                firstactivity.setImageResource(R.drawable.ic_restaurant);
                firstactivityText.setText("Eating");
                break;
            case "USING_PC":
                firstactivityText.setText("Using PC");
                firstactivity.setImageResource(R.drawable.ic_analytics);
                break;
            case "MAKING_PHONE_CALL":
                firstactivityText.setText("Making phone call");
                firstactivity.setImageResource(R.drawable.ic_make_call);
                break;
            case "ANSWERING_PHONE":
                firstactivityText.setText("Answering phone call");
                firstactivity.setImageResource(R.drawable.ic_answer);
                break;
            case "WATCHING_TV":
                firstactivityText.setText("Watching TV");
                firstactivity.setImageResource(R.drawable.ic_tv_screen);
                break;
            case "TAKING_MEDICINES":
                firstactivity.setImageResource(R.drawable.ic_drugs);
                firstactivityText.setText("Taking medicines");
                break;
        }
        switch(tokenizer.nextToken()){
            case "OUT":
                secondactivityText.setText("OUT");
                secondactivity.setImageResource(R.drawable.ic_entrance);
                break;


            case "DINING_ROOM":
                secondactivity.setImageResource(R.drawable.ic_restaurant);
                secondactivityText.setText("DINING ROOM");

                break;
            case "OFFICE":
                secondactivityText.setText("OFFICE");
                secondactivity.setImageResource(R.drawable.ic_office);
                break;
            case "LIVING_ROOM":
                secondactivity.setImageResource(R.drawable.ic_living_room);
                secondactivityText.setText("LIVING ROOM");

                break;

            case "GETTING_IN":
                secondactivityText.setText("Getting in");
                secondactivity.setImageResource(R.drawable.ic_knocking);
                break;
            case "GETTING_OUT":
                secondactivityText.setText("Getting out");
                secondactivity.setImageResource(R.drawable.ic_getting_out);
                break;
            case "COOKING":
                secondactivity.setImageResource(R.drawable.ic_lunch);
                secondactivityText.setText("Cooking");
                break;
            case "PREPARING_COLD_MEAL":
                secondactivityText.setText("Preparing cold meal");
                secondactivity.setImageResource(R.drawable.ic_meal);
                break;
            case "WASHING_DISHES":
                secondactivityText.setText("Washing dishes");
                secondactivity.setImageResource(R.drawable.ic_dish);
                break;
            case "SETTING_UP_TABLE":
                secondactivity.setImageResource(R.drawable.ic_table);
                secondactivityText.setText("Setting up the table");
                break;
            case "CLEARING_TABLE":
                secondactivity.setImageResource(R.drawable.ic_clear_table);
                secondactivityText.setText("Clearing the table");
                break;

            case "EATING":
                secondactivity.setImageResource(R.drawable.ic_restaurant);
                secondactivityText.setText("Eating");
                break;
            case "USING_PC":
                secondactivityText.setText("Using PC");
                secondactivity.setImageResource(R.drawable.ic_analytics);
                break;
            case "MAKING_PHONE_CALL":
                secondactivityText.setText("Making phone call");
                secondactivity.setImageResource(R.drawable.ic_make_call);
                break;
            case "ANSWERING_PHONE":
                secondactivityText.setText("Answering phone call");
                secondactivity.setImageResource(R.drawable.ic_answer);
                break;
            case "WATCHING_TV":
                secondactivityText.setText("Watching TV");
                secondactivity.setImageResource(R.drawable.ic_tv_screen);
                break;
            case "TAKING_MEDICINES":
                secondactivity.setImageResource(R.drawable.ic_drugs);
                secondactivityText.setText("Taking medicines");
                break;
        }
    }




    // Modifica i campi della prima occorrenza dell'oggetto in analisi
    // I dati vengono modificati in base all'eventuale feedback fornito
    // dall'utente designato come destinatario della richiesta
    public void setAnswersForJSON (UsefulData data, boolean outOfTime){
        InsertionOrder order = InsertionOrder.getInstance();
        CopyOnWriteArrayList<UsefulData> insertion_list = order.getList();

        ArrayList <Integer> equals_indexes = new ArrayList<>();
        int index_to_set=0;
        boolean out_flag=false;
        // trovo prima occorrenza del dubbio e modifico elemento nella coda FIFO
        for (int i = 0; i<insertion_list.size() && !out_flag;i++){
            UsefulData useful = insertion_list.get(i);
            if (data.equals(useful) && useful.feedback_id==data.feedback_id) {
                index_to_set = i;
                out_flag=true;
                Log.v("QueryUser",index_to_set+" index");
            }
        }
        UsefulData useful_at_i = insertion_list.get(index_to_set);

        // se non c'è stata risposta
        if (outOfTime){
            Log.v("QueryUser","not_answer");
            Log.v("QueryUser",Long.toString(nextCurrent));
            Log.v("QueryUser",Integer.toString(useful_at_i.status));



            useful_at_i.status=NOT_ANSWER;
            useful_at_i.latency = System.currentTimeMillis()-current;



        }
        // c'è stata risposta
        else{
            Log.v("QueryUser","answer");
            useful_at_i.status=ANSWER;
            useful_at_i.answer=currentData.answer;

            // cerco anche occorrenze dello stesso dubbio, con stessi utenti, o sottoinsieme di utenti coinvolti
            for (int i=0;i<insertion_list.size();i++){
                if (useful_at_i.users.containsAll(insertion_list.get(i).users) && useful_at_i.doubt.equals(insertion_list.get(i).doubt)){
                    equals_indexes.add(i);
                }
            }

            long latency = System.currentTimeMillis()-current;
            useful_at_i.latency=latency;
            //useful_at_i.user=useful_at_i.users.get(user_index);


            for (int j=0; j<equals_indexes.size();j++){
                UsefulData useful_index = insertion_list.get(equals_indexes.get(j));
                useful_index.answer = currentData.answer;
                useful_index.latency = latency;
                order.setAnswer(equals_indexes.get(j),useful_index);
            }


        }



        order.setAnswer(index_to_set,useful_at_i);
    }



    // Restituisce il nome corrispondente al codice (almeno in fase di richiesta vengono
    // mostrati i nomi e non i codici)
    public static String nameFromCode(String code){
        String name ="";
        switch (code){
            case "309": name="Alice"; break;
            case "501": name= "Bob"; break;
            case "502": name= "Chris"; break;
            case "503": name= "David"; break;
            case "202": name="Alice"; break;
            case "205": name= "Bob"; break;
            case "208": name= "Chris"; break;
            case "209": name= "David"; break;
            default: name=code; break;
        }
        return name;
    }





    // chiamata a server rest per controllo semantico
    class Rest_NLP extends AsyncTask<Void,Void,Void>{
        String sentence1,sentence2,sentence3;
        String response_;
        boolean correct_response;
        Rest_NLP(String s1,String s2, String s3){
            this.sentence1=s1;
            this.sentence2=s2;
            this.sentence3=s3;
        }
        @Override
        protected Void doInBackground(Void... strings) {
            try {
                HttpClient httpClient = new DefaultHttpClient();


                String URL = "https://82ba181b.ngrok.io/nlp/similarity";


                //URL url = new URL();
                HttpPost httpPost = new HttpPost(URL);

                JSONArray array = new JSONArray();

                JSONObject json_1 = new JSONObject();
                json_1.put("sentence1",sentence1);
                array.put(json_1);

                JSONObject json_2 = new JSONObject();
                json_2.put("sentence2",sentence2);
                array.put(json_2);

                JSONObject json_3 = new JSONObject();
                json_3.put("sentence3",sentence3);
                array.put(json_3);


                StringEntity postingString = new StringEntity(array.toString());
                httpPost.addHeader("content-type","application/json");

                httpPost.setEntity(postingString);




                HttpResponse response = httpClient.execute(httpPost);

                int responseCode = response.getStatusLine().getStatusCode();

                switch (responseCode){
                    case 200:
                        correct_response=true;
                        HttpEntity entity = response.getEntity();
                        if (entity!=null){
                            response_ = EntityUtils.toString(entity);
                        Log.v("QueryUser",response_);
                        }
                        break;
                    case 400:
                        correct_response=false;
                        break;
                    case 500:
                        correct_response=false;
                        break;

                }

            }catch(MalformedURLException e){
                Log.e("QueryUser","MalformedUrl",e.fillInStackTrace());
            }catch (IOException e){
                Log.e("QueryUser","IOException",e.fillInStackTrace());

            }catch(JSONException e){
                Log.e("QueryUser","JSONException",e.fillInStackTrace());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            try {
                // http response with  code 200
                if(correct_response) {
                    Log.v("QueryUser", response_);
                    JSONObject jsonObject = new JSONObject(response_);
                    switch (jsonObject.getInt("sentence")) {
                        // primo campo come risposta
                        case 0:
                            //found = true;

                            firstOrSecond = false;

                            selected=0;
                            setAnswer(currentData, false);
                            goToSendFeedback();

                            break;
                        // secondo campo fornito come risposta
                        case 1:
                            //found = true;
                            firstOrSecond = true;
                            selected=1;
                            setAnswer(currentData, false);
                            goToSendFeedback();

                            break;

                        // terzo campo come risposta
                        case -1:
                            selected=2;
                            setAnswer(currentData,true);
                            goToSendFeedback();
                            break;

                    }
                }else{
                    counter++;
                    if (counter < 2) {
                        textToSpeech.speak(getString(R.string.retry), TextToSpeech.QUEUE_ADD, null, "RETRY");

                        progress.stop();
                        recognizer.destroy();
                        recognizer = SpeechRecognizer.createSpeechRecognizer(QueryUser.this);
                        recognizer.setRecognitionListener(QueryUser.this);
                        progress.setSpeechRecognizer(recognizer);
                        progress.setRecognitionListener(QueryUser.this);


                        //progress.play();


                        progress.play();

                    }else{

                        // update sharedPreferences with activity
                        updateUserdata();
                        answered_flag=false;
                        if (currentData.status==NOT_QUERIED){
                            currentData.status=NOT_ANSWER;
                        }
                        setAnswersForJSON(currentData,true);
                        goToSendFeedback();
                    }
                }

            }catch (JSONException e){
                Log.e("QueryUser","JSONException",e.fillInStackTrace());
            }
        }

    }

    // remove previous pair place_activity when user doesn't answer
    public void updateUserdata (){
        for (int i=0;i<currentData.users.size();i++){
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(nameFromCode(currentData.users.get(i)),"");
            editor.commit();
        }
    }

}
