package com.alesp.feedbackapp.utils;

import com.alesp.feedbackapp.CertainData;

import java.util.concurrent.CopyOnWriteArrayList;


// Singleton per la gestione dei dati certi
public class CertainOrder {
    private static CertainOrder instance = null;


    private  CopyOnWriteArrayList<CertainData> concurrency_list ;


    public CertainOrder(){
        concurrency_list = new CopyOnWriteArrayList<>();
    }

    public synchronized static CertainOrder getInstance(){
        if (instance==null)
            instance = new CertainOrder();

        return instance;
    }


    public  void addCertain(CertainData data){
        concurrency_list.add(data);
    }

    public CopyOnWriteArrayList<CertainData> getList(){
        return concurrency_list;
    }

    public  void clear(){
        concurrency_list.clear();
    }



}
