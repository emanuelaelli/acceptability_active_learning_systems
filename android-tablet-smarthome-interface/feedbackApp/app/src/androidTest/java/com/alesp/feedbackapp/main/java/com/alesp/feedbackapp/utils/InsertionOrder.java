package com.alesp.feedbackapp.utils;

import com.alesp.feedbackapp.UsefulData;

import java.util.concurrent.CopyOnWriteArrayList;


// Singleton per la gestione della lista dei dubbi
public class InsertionOrder {
    private static InsertionOrder instance = null;

    private CopyOnWriteArrayList<UsefulData> concurrency_list;// null;// CopyOnWriteArrayList<>();


    public InsertionOrder(){
        concurrency_list = new CopyOnWriteArrayList<>();
    }

    public static synchronized InsertionOrder getInstance(){
        if (instance==null)
            instance = new InsertionOrder();
        return instance;
    }

    public void setAnswer (int index,UsefulData data){
        concurrency_list.set(index,data);
    }

    public void addDoubt (UsefulData data){
        concurrency_list.add(data);
    }

    public CopyOnWriteArrayList<UsefulData> getList(){
        return concurrency_list;
    }

    public void clear (){
        concurrency_list.clear();
    }

    public boolean contains (UsefulData data) {
        return concurrency_list.contains(data);
    }
}
