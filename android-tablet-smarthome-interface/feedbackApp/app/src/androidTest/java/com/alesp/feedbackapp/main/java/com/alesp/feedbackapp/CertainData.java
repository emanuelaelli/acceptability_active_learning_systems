package com.alesp.feedbackapp;

import java.util.ArrayList;

public class CertainData {

    ArrayList<String> users = new ArrayList<>();
    String place;
    String activity;
    String feedback_id;
    long time;

    public CertainData(ArrayList<String> users,String place,String activity,String feedback_id,long time){
        this.users=users;
        this.place=place;
        this.activity=activity;
        this.feedback_id=feedback_id;
        this.time=time;
    }

}
