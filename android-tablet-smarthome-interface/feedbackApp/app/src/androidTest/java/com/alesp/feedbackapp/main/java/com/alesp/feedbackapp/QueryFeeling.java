package com.alesp.feedbackapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.Enumeration;

public class QueryFeeling extends Activity {
    static ImageButton happyButton;
    static ImageButton plainButton;
    static ImageButton sadButton;

    RecognitionProgressView progress;
    TextView userinput;
    TextView title;
    static TextView happyButtonText;
    static TextView plainButtonText;
    static TextView sadButtonText;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_feeling);

        Bundle datipassati = getIntent().getExtras();
        String risposta = datipassati.getString("risposta");

        System.out.println("dato -> " + risposta);

        happyButton = findViewById(R.id.happy_button);
        plainButton = findViewById(R.id.plain_button);
        sadButton = findViewById(R.id.sad_button);
        title = findViewById(R.id.title);
        progress = findViewById(R.id.progress);
        userinput = findViewById(R.id.userinput);
        happyButtonText = findViewById(R.id.happy_text);
        plainButtonText = findViewById(R.id.plain_text);
        sadButtonText = findViewById(R.id.sad_text);

        title.setVisibility(View.VISIBLE);
        happyButton.setVisibility(View.VISIBLE);
        plainButton.setVisibility(View.VISIBLE);
        sadButton.setVisibility(View.VISIBLE);

        happyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                sendResponse(1); //happy
            }
        });

        plainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(2); //plain
            }
        });

        // selezione di none/other attraverso button click
        sadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(3); //sad
            }
        });


    }

    private void sendResponse(int numBottone) {
        //new SendResponseAsyncTask().execute(risultato);
        new Thread(new Runnable(){
            @Override
            public void run() {
                // Do network action in this function
                Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());

                if (numBottone == 1) {
                    FeedbackOutput output = new FeedbackOutput(ServerSocketTask.numquery, ServerSocketTask.opzione1, "soddisfatto", ServerSocketTask.timestamp1.toString(), timestamp2.toString());
                    Gson g = new Gson();
                    ServerSocketTask.printWriter.write(g.toJson(output));
                } else if (numBottone == 2) {
                    FeedbackOutput output = new FeedbackOutput(ServerSocketTask.numquery, ServerSocketTask.opzione2, "discretamente soddisfatto", ServerSocketTask.timestamp1.toString(), timestamp2.toString());
                    Gson g = new Gson();
                    ServerSocketTask.printWriter.write(g.toJson(output));
                } else if (numBottone == 3) {
                    FeedbackOutput output = new FeedbackOutput(ServerSocketTask.numquery, "nessuna", "non soddisfatto", ServerSocketTask.timestamp1.toString(), timestamp2.toString());
                    Gson g = new Gson();
                    ServerSocketTask.printWriter.write(g.toJson(output));
                }

                ServerSocketTask.printWriter.flush(); // svuota tutto e torna indietro
                // ServerSocketTask.printWriter.close();

                Intent torna_alla_home = new Intent(QueryFeeling.this, HomeActivity.class);
                startActivity(torna_alla_home);
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

class FeedbackOutput {
    private final int numquery;
    private final String risposta;
    private final String sentimento;
    private final String timestamp1;
    private final String timestamp2;

    public FeedbackOutput(int numquery, String risposta, String sentimento, String timestamp1, String timestamp2) {
        this.numquery = numquery;
        this.risposta = risposta;
        this.sentimento = sentimento;
        this.timestamp1 = timestamp1;
        this.timestamp2 = timestamp2;
    }
}

