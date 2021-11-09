package com.alesp.feedbackapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.StringTokenizer;



// estensione di baseadapter per la gridview nell'ActivityRecognitionFragment
public class FeedbackAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<String> places,activities,users;




    public FeedbackAdapter (Context context, ArrayList<String> places, ArrayList<String> activities, ArrayList<String> users){
        this.context=context;
        this.places=places;
        this.activities=activities;
        this.users=users;
    }

    // putting in ArrayList data from items' matrix



    @Override
    public int getCount() {
        //return elements.size();
        return places.size();
    }

    @Override
    public Object getItem(int i) {
        //return elements.get(i);
        String itemAtPositionI = places.get(i)+" "+activities.get(i)+" "+users.get(i);

        //return places.get(i);
        return itemAtPositionI;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }



    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.grid_view_element,viewGroup,false);

        }

        TextView placeView = (TextView) view.findViewById(R.id.placeData);
        TextView activityView = (TextView) view.findViewById(R.id.activityData);
        final ImageButton imgView = (ImageButton) view.findViewById(R.id.firstactivity_button);

        ImageButton user0 = (ImageButton) view.findViewById(R.id.user0);
        ImageButton user1 = (ImageButton) view.findViewById(R.id.user1);
        ImageButton user2 = (ImageButton) view.findViewById(R.id.user2);
        ImageButton user3 = (ImageButton) view.findViewById(R.id.user3);

        //String currentPosition = elements.get(i);
        //Log.v("element",elements.get(i));
        //StringTokenizer tokenizer = new StringTokenizer(currentPosition,",");
        //final String place =tokenizer.nextToken();
        final String place = places.get(i);
        placeView.setText(place);
        activityView.setText(activities.get(i));
        //final String activity = tokenizer.nextToken();
        final String activity = activities.get(i);
        Log.v("show",(activity));

        switch ((activity)){

            case "GETTING IN":  imgView.setImageResource(R.drawable.ic_knocking); break;
            case "GETTING OUT": imgView.setImageResource(R.drawable.ic_getting_out);break;
            case "COOKING": imgView.setImageResource(R.drawable.ic_lunch); break;
            case "PREPARING COLD MEAL": imgView.setImageResource(R.drawable.ic_meal);break;
            case "WASHING DISHES": imgView.setImageResource(R.drawable.ic_dish);break;
            case "SETTING UP THE TABLE": imgView.setImageResource(R.drawable.ic_table); break;
            case "CLEARING THE TABLE": imgView.setImageResource(R.drawable.ic_clear_table); break;
           // case "DRINKING": imgView.setImageResource(R.drawable.ic_water); break;
            case "EATING": imgView.setImageResource(R.drawable.ic_restaurant); break;
            case "USING PC": imgView.setImageResource(R.drawable.ic_analytics);break;
            case "MAKING PHONE CALL": imgView.setImageResource(R.drawable.ic_make_call);break;
            case "ANSWERING PHONE CALL": imgView.setImageResource(R.drawable.ic_answer); break;
            case "WATCHING TV": imgView.setImageResource(R.drawable.ic_tv_screen);break;
            case "TAKING MEDICINES": imgView.setImageResource(R.drawable.ic_drugs); break;




        }
        imgView.setVisibility(View.VISIBLE);
        imgView.setClickable(false);
        //final String users = tokenizer.nextToken();
        //final String userActivity = users.get(i);
        //current.setText((activity) + "\n made by " + userActivity);
        StringTokenizer tokenizer = new StringTokenizer(users.get(i)," ");
        int index=0;
        while(tokenizer.hasMoreTokens()){
            switch (index){
                case 0: user0.setImageResource(ActivityRecognitionFragment.userImageId(tokenizer.nextToken())); index++; break;
                case 1: user1.setImageResource(ActivityRecognitionFragment.userImageId(tokenizer.nextToken())); index++; break;
                case 2: user2.setImageResource(ActivityRecognitionFragment.userImageId(tokenizer.nextToken())); index++; break;
                case 3: user3.setImageResource(ActivityRecognitionFragment.userImageId(tokenizer.nextToken())); index++; break;
            }
        }





        return view;
    }

}
