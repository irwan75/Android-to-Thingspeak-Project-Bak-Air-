package com.ar_dev.www.bakair;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    EditText tmbAir;
    CheckBox status;
    ProgressBar mProgressBar;
//    EditText codeCommand;
    CountDownTimer mCountDownTimer;
    Boolean check = false;
    int i = 0;
    String url = "https://api.thingspeak.com/channels/915630/feeds/last.json?api_key=";
    String apikey = "E415HV5WX0EU0OS4";
    String writeApiKey = "DZYQO0UVSRNY49LL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tmbAir = findViewById(R.id.tambah_air);
        status = findViewById(R.id.otomatis);
        GetData();
    }

    public void btnKirim (View view){
        i = 0;
        check = true;

        String c = tmbAir.getText().toString();
        String sts = null;

        if(status.isChecked()){
            sts = "1";
        }else if(!status.isChecked()){
            sts = "0";
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder.url("https://api.thingspeak.com/update?api_key=" + writeApiKey + "&field2=" + c + "&field3=" + sts).build();
        /////
        if(check == true) {

            mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
            mProgressBar.setProgress(i);
            mCountDownTimer = new CountDownTimer(20000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    Log.v("Log_tag", "Tick of Progress" + i + millisUntilFinished);
                    i++;
                    mProgressBar.setProgress(i);
//                    button.setEnabled(false);
                    tmbAir.setEnabled(false);
//                    writeAPI.setEnabled(false);
//                    codeCommand.setEnabled(false);


                }

                @Override
                public void onFinish() {
                    //Do what you want
//                    button.setEnabled(true);
                    tmbAir.setEnabled(true);
//                    codeCommand.setEnabled(true);
                    i++;
                    mProgressBar.setProgress(i);

                }
            };
            mCountDownTimer.start();
        }
        ////

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                updateView("Error - " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    try {
                        Thread.sleep(10000);
                        updateView(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        updateView("Error - " + e.getMessage());
                    }catch (InterruptedException ex){

                    }
                } else {
                    updateView("Not Success - code : " + response.code());
                }
            }

            public void updateView(final String strResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Suksess", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public void GetData(){
        final UriApi uriapi01 = new UriApi();

        uriapi01.setUri(url,apikey);
        Timer timer = new Timer();
        TimerTask tasknew = new TimerTask(){
            public void run() {
                LoadJSON task = new LoadJSON();
                task.execute(uriapi01.getUri());
            }
        };
        timer.scheduleAtFixedRate(tasknew,15*1000,15*1000);
    }

    private class UriApi {

        private String uri,url,apikey;

        protected void setUri(String url, String apikey){
            this.url = url;
            this.apikey = apikey;
            this.uri = url + apikey;
        }

        protected  String getUri(){
            return uri;
        }

    }

    private class LoadJSON extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return getText(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            /*TextView textview = (TextView) findViewById(R.id.textJSON);
            textview.setText(result);*/
            String msg01 = "";
            String msg02 = "";
            try {
                JSONObject json = new JSONObject(result);

                msg01 += String.format("%s", json.getString("entry_id"));
                msg02 += String.format("%s", json.getString("field1"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
//            TextView TextID01 = findViewById(R.id.number);
//            TextID01.setText(msg01);
            TextView text = findViewById(R.id.number);
            text.setText(msg02);
        }
    }

    private String getText(String strUrl) {
        String strResult = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            strResult = readStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strResult;
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
