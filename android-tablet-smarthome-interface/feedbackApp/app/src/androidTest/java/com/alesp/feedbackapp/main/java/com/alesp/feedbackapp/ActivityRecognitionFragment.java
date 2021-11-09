package com.alesp.feedbackapp;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alesp.feedbackapp.utils.CertainOrder;
import com.alesp.feedbackapp.utils.InsertionOrder;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alessandro on 04.05.17.
 */

public class ActivityRecognitionFragment extends Fragment {
    //Inizializzo variabili per l'update della UI
  //  ImageButton currentActivity;
    ImageButton back;
    ImageButton only_one;
    ImageButton first,second;
    TextView firstPlace,secondPlace,onlyPlace;
    TextView firstActivity,secondActivity,onlyActivity;
    ImageButton user0f,user1f,user2f,user3f;
    ImageButton user0s,user1s,user2s,user3s;
    ImageButton user0o,user1o,user2o,user3o;

    TextView currentActivityText;
    GridView currentActivitiesView;
    AVLoadingIndicatorView avi;
    TextView title;
    LinearLayout firstLayout,secondLayout,onlyLayout;


    SharedPreferences sharedPreferences;

    JSONArray userdata;


    String [] names = {"Alice","Bob","Chris","David"};

    ArrayList<String> places;
    ArrayList<String> activities;
    ArrayList<String> users;


    //Gestisco "scomparsa" delle attività riconosciute dopo tot secondi
    TimerTask refreshView;

    //definisco receiver per ricevere dati da wakeupservice
    BroadcastReceiver receiver;
    boolean firstDataReceived = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_activityrecognition, container, false);

        //attivo il flag per cui lo schermo non si spegne
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        title = (TextView) view.findViewById(R.id.title);
        //Setto collegamenti vari con bottoni e textview
       // currentActivity = (ImageButton) view.findViewById(R.id.first_activity);
        currentActivityText = (TextView) view.findViewById(R.id.currentActivity);
        currentActivitiesView = (GridView) view.findViewById(R.id.scrollActivities);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        firstLayout = (LinearLayout) view.findViewById(R.id.item_first);
        secondLayout = (LinearLayout) view.findViewById(R.id.item_second);
        onlyLayout = (LinearLayout) view.findViewById(R.id.item_only);

       // out_of_time = (TextView) view.findViewById(R.id.notAnswerText);

        initialize(view);

        only_one = (ImageButton) view.findViewById(R.id.only_one);
        first = (ImageButton) view.findViewById(R.id.first_ac);
        second = (ImageButton) view.findViewById(R.id.second_ac);
        //da cambiare: recupera lo stato del fragment sharedpref
        avi = (AVLoadingIndicatorView) view.findViewById(R.id.avi);
        avi.show();

        //inizializzo receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Controllo prima di tutto se ho ricevuto un avviso di connessione persa:
                //se si termino tutto.

                Log.d("ActRecFrag","ricevuto broadcast da service");
                if(intent.getBooleanExtra("CONNECTION_LOST",false)){
                    //creo alertdialog e faccio terminare il servizio
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Lost Connection")
                            .setMessage("Lost connection to the server.\nPlease try later.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //restarto l'activity
                                    getActivity().recreate();

                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(false);
                }
                else {



                    //Ricevo dati dal service ed aggiorno l'UI
                    String result = intent.getStringExtra("CURRENT_ACTIVITY");
                    // Log.v("ActRecFragMsg",result);

                    JSONObject obj;

                    //Cancello eventuale timertask, se sta aspettando di essere eseguito
                    if(refreshView!=null){
                        refreshView.cancel();
                    }


                    //controllo se sono i primi dati ricevuti (se si rimuovo scritte e robe varie)
                    if (firstDataReceived) {
                        //rimuovo cose varie e aggiungo altre cose
                        //view.findViewById(R.id.currently).setVisibility(View.VISIBLE);
                       // currentActivity.setVisibility(View.VISIBLE);
                        title.setVisibility(View.GONE);
                        currentActivityText.setVisibility(View.GONE);
                        firstLayout.setVisibility(View.GONE);
                        secondLayout.setVisibility(View.GONE);
                        onlyLayout.setVisibility(View.GONE);
                        //currentActivitiesView.setVisibility(View.VISIBLE);
                        avi.setVisibility(View.GONE);
                        //out_of_time.setVisibility(View.GONE);

                        //diminuisco margin di currentactivitytext
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp.setMargins(0, 20, 0, 0); // llp.setMargins(left, top, right, bottom);
                      //  currentActivityText.setLayoutParams(llp);

                        firstDataReceived = false;
                    }

                    //Faccio partire timer che, dopo 16 secondi, toglie l'activity corrente e ritorna in listening for data
                     refreshView = new TimerTask() {
                        @Override
                        public void run() {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Faccio ritornare la view come all'apertura del fragment
                                    title.setVisibility(View.VISIBLE);
                                    view.findViewById(R.id.currently).setVisibility(View.GONE);
                                    currentActivityText.setVisibility(View.VISIBLE);
                                    currentActivitiesView.setVisibility(View.GONE);
                                    first.setVisibility(View.GONE);
                                    firstLayout.setVisibility(View.GONE);
                                    secondLayout.setVisibility(View.GONE);
                                    onlyLayout.setVisibility(View.GONE);
                                    second.setVisibility(View.GONE);
                                    only_one.setVisibility(View.GONE);
                                   // out_of_time.setVisibility(View.GONE);
                                    if (HomeActivity.elementTTS!=null) {
                                        HomeActivity.elementTTS.stop();
                                        HomeActivity.elementTTS.shutdown();
                                    }
                                   // avi.setVisibility(View.VISIBLE);

                                    //diminuisco margin di currentactivitytext
                                    LinearLayout.LayoutParams lls = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lls.setMargins(10, 70, 0, 0); // llp.setMargins(left, top, right, bottom);
                                    //currentActivityText.setLayoutParams(lls);
                                    // currentActivityText.setText("Listening for data...");

                                    firstDataReceived = true;
                                }
                            });

                        }
                    };
                    //faccio partire il timertask
                    new Timer().schedule(refreshView,16000);

                    try {

                        Log.v("ActivityRecognition",result);
                        obj = new JSONObject(result);


                        //out_of_time.setVisibility(View.GONE);


                        places = new ArrayList<>();
                        activities = new ArrayList<>();
                        users = new ArrayList<>();

                        // se ricevo dati sicuri dal service, mostro le attività
                        if (!(obj.getBoolean("uncertainty"))){
                            userdata = obj.getJSONArray("data");

                            for (int i=0;i<userdata.length();i++){
                                JSONObject item = userdata.getJSONObject(i);
                                JSONArray user_json = item.getJSONArray("users");
                                String users_string="";
                                for (int j=0;j<user_json.length();j++){
                                    users_string+=QueryUser.nameFromCode(user_json.getString(j))+" ";
                                }
                                String place = item.getString("place");
                                String activity = item.getString("activity");

                                places.add(place);
                                activities.add(showActivityName(activity));
                                users.add(users_string);
                            }


                            showPreviousSavedData();
                        }

                        //
                        else {


                            Log.v("ActivityRecognition",obj.toString());
                            if (obj.getJSONObject("uncertain").getJSONArray("answers").getJSONObject(0).getInt("status")==0){
                                // vado a ricercare sempre i dati più recenti
                                JSONObject dataToShow = getLastData(obj);

                                ArrayList<UncertainData> notsureData = new ArrayList<>();
                                ArrayList<CertainData> sureData = new ArrayList<CertainData>();

                                JSONArray sure = dataToShow.getJSONArray("sure");


                                // i dati più recenti sono relativi ad un attività incerta
                                if (dataToShow.has("not_sure")){
                                    JSONArray not_sure = dataToShow.getJSONArray("not_sure");


                                    for (int i = 0; i < not_sure.length(); i++) {
                                        JSONObject item = not_sure.getJSONObject(i);
                                        // dati incerti
                                        if (item.has("doubt")) {
                                            ArrayList<String> users = new ArrayList<String>();
                                            JSONArray user_array = item.getJSONArray("users");
                                            for (int j = 0; j < user_array.length(); j++) {
                                                users.add(user_array.getString(j));
                                            }

                                            // quando aggiungi not_answer/not_queried guarda se la stringa è uguale a ""
                                            // se non c'è answer, nel fragment devo mostrare activity/place più probabile (first token of doubt field)
                                            String answer = item.getString("answer");

                                            boolean actOrPlace = item.getBoolean("flag");
                                            UncertainData not_sure_data = new UncertainData(users, item.getString("doubt"), 0, actOrPlace);
                                            not_sure_data.answer = answer;
                                            notsureData.add(not_sure_data);
                                            //
                                        }
                                    }

                                    // inserisco in place, activity e users i dati sicuri più recenti
                                    for (int i=0;i<sure.length();i++) {
                                        JSONObject item = sure.getJSONObject(i);
                                        JSONArray user_array = item.getJSONArray("users");
                                        ArrayList<String> users = new ArrayList<String>();
                                        for (int j = 0; j < user_array.length(); j++) {
                                            users.add(user_array.getString(j));
                                        }
                                        String place = item.getString("place");
                                        String activity = item.getString("activity");
                                        sureData.add(new CertainData(users, place, activity));
                                    }


                                    if (sureData.size() > 0) {
                                        for (int i = 0; i < sureData.size(); i++) {
                                            CertainData item = sureData.get(i);
                                            String user = item.users.get(0);

                                            // se place == "" significa che la posizione era coinvolta in dubbio
                                            if (item.place.equals("")) {
                                                // controllo se in notsureData è stata data risposta al dubbio
                                                for (int j = 0; j < notsureData.size(); j++) {
                                                    if (notsureData.get(j).users.contains(user) && !notsureData.get(j).flag)
                                                        // se not_queried o not_answer prendi il più probabile (first token)
                                                        if (!( notsureData.get(j).answer.equals("")))
                                                            item.place = notsureData.get(j).answer;
                                                    /*else {
                                                        StringTokenizer tokenizer = new StringTokenizer(notsureData.get(j).doubt,",");
                                                        item.place = tokenizer.nextToken();
                                                    }*/

                                                }
                                            }

                                            // analogamente per l'attività
                                            if (item.activity.equals("")) {
                                                for (int j = 0; j < notsureData.size(); j++) {
                                                    if (notsureData.get(j).users.contains(user) && notsureData.get(j).flag)
                                                        // se not_queried o not_answer prendi il più probabile (first token)
                                                        if (!(notsureData.get(j).answer.equals("")))
                                                            item.activity = notsureData.get(j).answer;
                                                    /*else{
                                                        StringTokenizer tokenizer = new StringTokenizer(notsureData.get(j).doubt,",");
                                                        item.activity = tokenizer.nextToken();
                                                    }*/
                                                }
                                            }
                                        }

                                        // se con la risposta ottenuta noto che ci sono utenti che stanno
                                        // svolgendo la stessa attività, devono essere mostrati insieme
                                        int variable_size = sureData.size();
                                        for (int i = 0; i < variable_size; i++) {
                                            for (int j = i + 1; j < variable_size; j++) {
                                                String place_at_i = sureData.get(i).place;
                                                String place_at_j = sureData.get(j).place;

                                                String activity_at_i = sureData.get(i).activity;
                                                String activity_at_j = sureData.get(j).activity;

                                                if ((!place_at_i.equals("")) && place_at_i.equals(place_at_j) && (!activity_at_i.equals("")) && activity_at_i.equals(activity_at_j)) {
                                                    ArrayList<String> users_at_j = sureData.get(j).users;
                                                    for (int x = 0; x < users_at_j.size(); x++) {
                                                        sureData.get(i).users.add(users_at_j.get(x));
                                                    }

                                                    sureData.remove(j);
                                                    variable_size--;
                                                }
                                            }
                                        }


                                        // preparo i campi che devono essere mostrati
                                        for (int i=0;i<sureData.size();i++) {

                                            String users_json = "";
                                            for (int j = 0; j < sureData.get(i).users.size(); j++) {
                                                users_json += QueryUser.nameFromCode(sureData.get(i).users.get(j)) + " ";
                                            }
                                            String place = sureData.get(i).place;
                                            String activity = sureData.get(i).activity;
                                            if (!place.equals("") && !activity.equals("")){
                                                places.add(place);
                                                activities.add(showActivityName(activity));
                                                users.add(users_json);
                                            }
                                            Log.v("activity_log",users_json+" are in the "+ place +" and you "+activity);
                                        }


                                    }
                                }else{

                                    // i dati sono ricevuti da query user, ma i più recenti
                                    // sono relativi solo a dati certi ( ricevuto dati mentre query user
                                    // era aperta
                                    for (int i=0;i<sure.length();i++) {
                                        JSONObject item = sure.getJSONObject(i);
                                        JSONArray user_array = item.getJSONArray("users");
                                        String users_json="";
                                        //ArrayList<String> users = new ArrayList<String>();
                                        for (int j = 0; j < user_array.length(); j++) {
                                            users_json+=QueryUser.nameFromCode(user_array.getString(j))+" ";
                                        }
                                        String place = item.getString("place");
                                        String activity = item.getString("activity");

                                        places.add(place);
                                        activities.add(showActivityName(activity));
                                        users.add(users_json);
                                    }


                                }




                            }
                            // se non c'è risposta non mostrare niente
                            else{
                                title.setVisibility(View.VISIBLE);
                                view.findViewById(R.id.currently).setVisibility(View.GONE);
                                currentActivityText.setVisibility(View.VISIBLE);
                                currentActivitiesView.setVisibility(View.GONE);
                                first.setVisibility(View.GONE);
                                firstLayout.setVisibility(View.GONE);
                                secondLayout.setVisibility(View.GONE);
                                onlyLayout.setVisibility(View.GONE);
                                second.setVisibility(View.GONE);
                                only_one.setVisibility(View.GONE);
                            }

                            // Estrai i dati più recenti in modo tale da mostrare solo
                            // le attività relative all'ultimo riconoscimento

                            showPreviousSavedData();

                            // se not_sure non c'è ultima attività è sicura


                        }


                        // salvo nelle sharedpreferences le ultime attività eseguite per gli utenti




                        // cosa mostro se c'è solo un utente
                        if (activities.size() == 1) {
                            Log.v("ActRec",places.get(0));
                            Log.v("ActRec",activities.get(0));
                            Log.v("ActRec",users.get(0));
                            onlyPlace.setText(places.get(0));
                            onlyActivity.setText(activities.get(0));
                            onlyLayout.setVisibility(View.VISIBLE);
                            only_one.setVisibility(View.VISIBLE);
                            firstLayout.setVisibility(View.GONE);
                            secondLayout.setVisibility(View.GONE);
                            first.setVisibility(View.GONE);
                            second.setVisibility(View.GONE);
                            setUserIcons("ONLY");
                            currentActivitiesView.setVisibility(View.GONE);
                            currentActivityText.setVisibility(View.GONE);
                            title.setVisibility(View.GONE);

                            only_one.setImageResource(getImageId(activities.get(0)));

                        }
                        else{
                            // cosa mostro se sono due utenti
                            if (activities.size() == 2) {
                                firstPlace.setText(places.get(0));
                                secondPlace.setText(places.get(1));
                                firstActivity.setText(activities.get(0));
                                secondActivity.setText(activities.get(1));
                                firstLayout.setVisibility(View.VISIBLE);
                                secondLayout.setVisibility(View.VISIBLE);
                                onlyLayout.setVisibility(View.GONE);
                                first.setVisibility(View.VISIBLE);
                                second.setVisibility(View.VISIBLE);
                                only_one.setVisibility(View.GONE);
                                currentActivitiesView.setVisibility(View.GONE);
                                currentActivityText.setVisibility(View.GONE);
                                title.setVisibility(View.GONE);

                                first.setImageResource(getImageId(activities.get(0)));
                                second.setImageResource(getImageId(activities.get(1)));
                                setUserIcons("TWO");

                            }else{
                                    // Modifica parametri della grid view se user
                                    // sono tre o quattro (modifica numero di righe colonne)
                                    // se user sono tre forse è meglio fare due colonne

                                firstLayout.setVisibility(View.GONE);
                                secondLayout.setVisibility(View.GONE);
                                onlyLayout.setVisibility(View.GONE);
                                only_one.setVisibility(View.GONE);
                                first.setVisibility(View.GONE);
                                second.setVisibility(View.GONE);

                                if (activities.size()==3){
                                    currentActivitiesView.setNumColumns(3);
                                }else{
                                    currentActivitiesView.setNumColumns(2);
                                }


                                currentActivityText.setVisibility(View.GONE);
                                title.setVisibility(View.GONE);

                                currentActivitiesView.setVisibility(View.VISIBLE);
                                currentActivitiesView.setAdapter(new FeedbackAdapter(getActivity(), places, activities, users));

                            }
                        }

                        clearAll();
                            // currentActivitiesView.setAdapter(new FeedbackAdapter(getActivity(),adapterData,PLACES,ACTIVITIES));
                        //}



                    } catch (JSONException e) {
                        Log.e("ActivityRecognition", Log.getStackTraceString(e));
                    }
                }
            }
        };

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver),
                new IntentFilter("NOTIFY_ACTIVITY")
        );
        Log.d("Actrecfrag","onStart");
    }

    @Override
    public void onDestroy() {
        Log.d("Actrecfrag","onDestroy");
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);

    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver),
                new IntentFilter("NOTIFY_ACTIVITY")
        );
        Log.d("Actrecfrag","onResume");
    }


    // guarda se è già inserito utente non devo aggiornare niente
    public boolean alreadyInserted (ArrayList<String> users,String user){
        boolean inserted = false ;
        for (int i=0; i < users.size();i++){
            StringTokenizer tokenizer = new StringTokenizer(users.get(i), " ");
            while (tokenizer.hasMoreTokens()){
                if (tokenizer.nextToken().equals(user)){
                    inserted=true;
                    break;
                }
            }
        }

        return inserted;
    }



    // se utente da inserire sta facendo attività con altre persone, oppure no
    public int insertUserADL(ArrayList<String> places,ArrayList<String> activities, String place_activity){

        int indexOfInsertion = -1;

        for (int i=0;i<places.size();i++){
            if (place_activity.equals(places.get(i)+","+activities.get(i))) {
                indexOfInsertion = i;
                break;
            }
        }

        return indexOfInsertion;
    }







    // per mostrare agli utenti nomi human-readable
    public static String showActivityName(String activityCode){
        String result;
        switch (activityCode) {
            case "GETTING_IN":
                result = "GETTING IN";
                break;
            case "GETTING_OUT":
                result = "GETTING OUT";
                break;
            case "COOKING":
                result = "COOKING";
                break;
            case "PREPARING_COLD_MEAL":
                result = "PREPARING COLD MEAL";
                break;
            case "WASHING_DISHES":
                result="WASHING DISHES";
                break;
            case "SETTING_UP_TABLE":
                result="SETTING UP THE TABLE";
                break;
            case "CLEARING_TABLE":
                result="CLEARING THE TABLE";
                break;
            /*case "D":
                result="DRINKING";
                break;*/
            case "EATING":
                result="EATING";
                break;
            case "USING_PC":
                result="USING PC";
                break;
            case "MAKING_PHONE_CALL":
                result="MAKING PHONE CALL";
                break;
            case "ANSWERING_PHONE":
                result="ANSWERING PHONE CALL";
                break;
            case "WATCHING_TV":
                result="WATCHING TV";
                break;
            case "TAKING_MEDICINES":
                result="TAKING MEDICINES";
                break;
            default:
                result=activityCode.toUpperCase();
                break;
        }
        return result;
    }




    public int getImageId ( String activity ){
        int id=0;
        switch ((activity)){

            case "GETTING IN":  id=(R.drawable.ic_knocking); break;
            case "GETTING OUT": id=(R.drawable.ic_getting_out);break;
            case "COOKING": id=(R.drawable.ic_lunch); break;
            case "PREPARING COLD MEAL": id=(R.drawable.ic_meal);break;
            case "WASHING DISHES": id=(R.drawable.ic_dish);break;
            case "SETTING UP THE TABLE": id=(R.drawable.ic_table); break;
            case "CLEARING THE TABLE": id=(R.drawable.ic_clear_table); break;
            //case "DRINKING": id=(R.drawable.ic_water); break;
            case "EATING": id=(R.drawable.ic_restaurant); break;
            case "USING PC": id=(R.drawable.ic_analytics);break;
            case "MAKING PHONE CALL": id=(R.drawable.ic_make_call);break;
            case "ANSWERING PHONE CALL": id=(R.drawable.ic_answer); break;
            case "WATCHING TV": id=(R.drawable.ic_tv_screen);break;
            case "TAKING MEDICINES": id=(R.drawable.ic_drugs); break;
        }

        return id;
    }


    public void clearAll(){
        InsertionOrder.getInstance().clear();
        CertainOrder.getInstance().clear();
    }

    // inizializza tutte le view presenti nel fragment
    public void initialize (View view){
        firstPlace = (TextView) view.findViewById(R.id.firstPlaceData);
        secondPlace = (TextView) view.findViewById(R.id.secondPlaceData);
        onlyPlace = (TextView) view.findViewById(R.id.onlyPlaceData);

        firstActivity = (TextView) view.findViewById(R.id.first_activityData);
        secondActivity = (TextView) view.findViewById(R.id.second_activityData);
        onlyActivity = (TextView) view.findViewById(R.id.onlyActivityData);

        user0f = (ImageButton) view.findViewById(R.id.user0_frag);
        user1f = (ImageButton) view.findViewById(R.id.user1_frag);
        user2f = (ImageButton) view.findViewById(R.id.user2_frag);
        user3f = (ImageButton) view.findViewById(R.id.user3_frag);

        user0s = (ImageButton) view.findViewById(R.id.user0_frag2);
        user1s = (ImageButton) view.findViewById(R.id.user1_frag2);
        user2s = (ImageButton) view.findViewById(R.id.user2_frag2);
        user3s = (ImageButton) view.findViewById(R.id.user3_frag2);

        user0o = (ImageButton) view.findViewById(R.id.user0_frag_only);
        user1o = (ImageButton) view.findViewById(R.id.user1_frag_only);
        user2o = (ImageButton) view.findViewById(R.id.user2_frag_only);
        user3o = (ImageButton) view.findViewById(R.id.user3_frag_only);


    }

    // setta le icone degli utenti
    public void setUserIcons (String code){
        

        switch (code){
            case "ONLY":
                int token_index=0;
                StringTokenizer tokenizer = new StringTokenizer(users.get(0), " ");

                while (tokenizer.hasMoreTokens()){
                    switch (token_index){
                        case 0: user0o.setImageResource(userImageId(tokenizer.nextToken()));
                                user0o.setVisibility(View.VISIBLE);
                                token_index++; break;
                        case 1: user1o.setImageResource(userImageId(tokenizer.nextToken()));
                                user1o.setVisibility(View.VISIBLE);
                                token_index++; break;
                        case 2: user2o.setImageResource(userImageId(tokenizer.nextToken()));
                                user2o.setVisibility(View.VISIBLE);
                                token_index++; break;
                        case 3: user3o.setImageResource(userImageId(tokenizer.nextToken()));
                                user3o.setVisibility(View.VISIBLE);
                                token_index++; break;
                    }


                }
                for (int j=token_index ; j < 4 ; j++){
                    if (j==1) user1o.setVisibility(View.GONE);
                    if (j==2) user2o.setVisibility(View.GONE);
                    if (j==3) user3o.setVisibility(View.GONE);

                }

            break;    
            case "TWO":
                int token_first=0;
                StringTokenizer first_tokenizer = new StringTokenizer(users.get(0), " ");
                while (first_tokenizer.hasMoreTokens()){
                    switch (token_first){
                        case 0: user0f.setImageResource(userImageId(first_tokenizer.nextToken()));
                                user1f.setVisibility(View.VISIBLE);
                                token_first++; break;
                        case 1: user1f.setImageResource(userImageId(first_tokenizer.nextToken()));
                                user1f.setVisibility(View.VISIBLE);
                                token_first++; break;
                        case 2: user2f.setImageResource(userImageId(first_tokenizer.nextToken()));
                                user2f.setVisibility(View.VISIBLE);
                                token_first++; break;
                        case 3: user3f.setImageResource(userImageId(first_tokenizer.nextToken()));
                                user3f.setVisibility(View.VISIBLE);
                                token_first++; break;
                    }
                }

                for (int j=token_first ; j < 4 ; j++){
                    if (j==1) user1f.setVisibility(View.GONE);
                    if (j==2) user2f.setVisibility(View.GONE);
                    if (j==3) user3f.setVisibility(View.GONE);

                }

                int token_second=0;
                StringTokenizer second_tokenizer = new StringTokenizer(users.get(1), " ");
                while (second_tokenizer.hasMoreTokens()){
                    switch (token_second){
                        case 0: user0s.setImageResource(userImageId(second_tokenizer.nextToken())); token_second++; break;
                        case 1: user1s.setImageResource(userImageId(second_tokenizer.nextToken()));
                                user1s.setVisibility(View.VISIBLE);
                                token_second++; break;
                        case 2: user2s.setImageResource(userImageId(second_tokenizer.nextToken()));
                                user2s.setVisibility(View.VISIBLE);

                                token_second++; break;
                        case 3: user3s.setImageResource(userImageId(second_tokenizer.nextToken()));
                                user3s.setVisibility(View.VISIBLE);

                                token_second++; break;
                    }
                }

                for (int j=token_first ; j < 4 ; j++){
                    if (j==1) user1s.setVisibility(View.GONE);
                    if (j==2) user2s.setVisibility(View.GONE);
                    if (j==3) user3s.setVisibility(View.GONE);

                }
            break;

        }

    }

    public static int userImageId (String username){
        int id=0;
        switch (username){

            case "Alice" : id = R.drawable.ic_alice; break;
            case "Bob" : id = R.drawable.ic_bob; break;
            case "Chris" : id = R.drawable.ic_chris; break;
            case "David" : id = R.drawable.ic_david; break;


        }

        return id;
    }

    // dati certi
    public static class CertainData{
        ArrayList<String> users = new ArrayList<>();
        String place;
        String activity;

        public CertainData(ArrayList<String> users,String place,String activity){
            this.users=users;
            this.place=place;
            this.activity=activity;
        }
    }
    // dati non sicuri
    public static class UncertainData{
        String doubt;
        ArrayList<String> users=new ArrayList<>();
        double priority;
        boolean flag;
        // verrà riempito con l'username destinatario della domanda
        String user="";
        // answer, not answer, not queried        	int status = 2;
        // eventuale risposta fornita
        String answer;

        public UncertainData(ArrayList<String> users,String doubt,double priority,boolean flag){
            this.users=users;
            this.doubt=doubt;
            this.priority = priority;
            this.flag=flag;

        }

    }

    // ottieni i dati relativi al riconoscimento più recente
    public JSONObject getLastData (JSONObject data) throws JSONException{
        JSONObject sure_data = data.getJSONObject("certain");
        JSONObject uncertain_data = data.getJSONObject("uncertain");

        JSONObject lastObject = new JSONObject();

        ArrayList<Integer> sure_last_indexes = new ArrayList<>();
        ArrayList<Integer> uncertain_last_indexes = new ArrayList<>();

        long sure_greatest=0;
        long not_sure_greatest=0;


        JSONArray sure_array = sure_data.getJSONArray("sure_data");
        JSONArray uncertain_array = uncertain_data.getJSONArray("answers");

        for (int i=0;i<sure_array.length();i++){
            long time = sure_array.getJSONObject(i).getLong("time");
            if (time > sure_greatest) sure_greatest=time;
        }

        for (int i=0;i<uncertain_array.length();i++){
            long time = uncertain_array.getJSONObject(i).getLong("time");
            if (time > not_sure_greatest) not_sure_greatest=time;
        }

        // ultimi dati ricevuti mentre queryuser era aperto erano tutti sicuri
        //if (sure_greatest > not_sure_greatest){
        for (int i=0;i<sure_array.length();i++){
            if (sure_array.getJSONObject(i).getLong("time")==sure_greatest)
                sure_last_indexes.add(i);
        }

        JSONArray last = new JSONArray();
        for (int i=0;i<sure_last_indexes.size();i++){
            last.put(sure_array.getJSONObject(sure_last_indexes.get(i)));
        }
        lastObject.put("sure",last);
        //}else{

        if (sure_greatest==not_sure_greatest){
            for (int i=0;i<uncertain_array.length();i++){
                if (uncertain_array.getJSONObject(i).getLong("time")==not_sure_greatest)
                    uncertain_last_indexes.add(i);
            }

            JSONArray last_uncertain = new JSONArray();
            for(int i=0;i<uncertain_last_indexes.size();i++){
                last_uncertain.put(uncertain_array.getJSONObject(uncertain_last_indexes.get(i)));
            }

            lastObject.put("not_sure",last_uncertain);
        }
        Log.v("ActRec",lastObject.toString());

        return lastObject;
    }


    public void showPreviousSavedData(){
        for (int i=0;i<activities.size();i++){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            StringTokenizer tokenizer = new StringTokenizer(users.get(i)," ");
            while(tokenizer.hasMoreTokens()){
                editor.putString(QueryUser.nameFromCode(tokenizer.nextToken()),places.get(i)+","+activities.get(i));
            }
            Log.v("ActivityRecognitionFragment","COMMIT CHANGES");

            editor.commit();
        }

        // cerco nelle sharedPreferences se posso mostrare altre attività
        // (questo accade quando dal server non arrivano dati relativi ad
        //  uno o più utenti, in quel caso mostro l'ultima attività che hanno
        //  fatto)
        for (int i=0;i<names.length;i++){
            String place_activity = sharedPreferences.getString(names[i],"");
            Log.v("ActivityRecognitionFragment",names[i]+"\t"+place_activity);
            // se non è dato incerto e non ho ancora inserito utente devo farlo
            if (!(place_activity.equals("")) && !(alreadyInserted(users,names[i]))){
                int indexInsertion = insertUserADL(places,activities,place_activity);
                if (indexInsertion==-1){
                    int indexOfSeparator = place_activity.indexOf(",") ;
                    places.add(place_activity.substring(0,indexOfSeparator));
                    activities.add(place_activity.substring(indexOfSeparator+1,place_activity.length()));
                    users.add(names[i]+" ");
                }else{
                    String users_at_index = users.get(indexInsertion);
                    users_at_index+=names[i]+" ";
                    users.set(indexInsertion,users_at_index);
                }

            }


        }
    }


}
