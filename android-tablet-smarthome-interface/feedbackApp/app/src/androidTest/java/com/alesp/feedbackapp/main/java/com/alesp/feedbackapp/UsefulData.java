package com.alesp.feedbackapp;

import java.util.ArrayList;


// Definita classe per la gestione di dati incerti ricevuti dal server
public class UsefulData implements Comparable<UsefulData>{

    String doubt;
    ArrayList<String> users=new ArrayList<>();
    double priority;
    ArrayList<Double> availabilities = new ArrayList<Double>();
    ArrayList<String> user_feedback_ids = new ArrayList<>();
    boolean flag;
    // utente che risponde
    String user;
    // answer, not answer, not queried
    int status = 2;
    // eventuale risposta fornita
    String answer;
    String feedback_id;
    long time;
    int requests;
    long latency;
    long rtw;
    String activity;

    public UsefulData(String doubt,String user,ArrayList<String> users,ArrayList<Double> availability,double priority,boolean flag,String id,long time, int requests,long latency,long rtw
    ,ArrayList<String> user_feedback_ids,String activity) {
        this.doubt = doubt;
        this.user = user;
        this.users=users;
        this.availabilities=availability;
        this.priority=priority;
        this.flag=flag;
        this.answer="";
        this.feedback_id=id;
        this.time=time;
        this.requests=requests;
        this.latency = latency;
        this.rtw = rtw;
        this.user_feedback_ids=user_feedback_ids;
        this.activity=activity;

    }


    public void setUser(String user){
        this.user=user;
    }

    public void setStatus(int status){
        this.status=status;
    }

    public void setAnswer(String answer){
        this.answer=answer;
    }



    @Override
    public int compareTo(UsefulData other) {
        //multiplied to -1 as the author need descending sort order
        return  Double.valueOf(this.priority).compareTo(other.priority);
    }

    public ArrayList<String> getUsers() {return this.users;}
    public String getDoubt() {return this.doubt;}
    public boolean getFlag(){return this.flag;}



    // override di equals
    // un dubbio è uguale ad un altro se le due attività/posti sono uguali e
    // gli utenti coinvolti sono uguali o sono un sottoinsieme di utenti di un altro dubbio

    @Override
    public boolean equals(Object other){
        UsefulData other_one = (UsefulData) other;
        if (this.doubt.equals(other_one.doubt) && this.users.containsAll(other_one.users) /*&& this.feedback_id==((UsefulData) other).feedback_id*/)
            return true;
        return false;

    }
}

