package com.use.carimasjid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private static int progress;
    private ProgressBar progressBar;

    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        progressBar = findViewById(R.id.progress);


        progress = 0;
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setProgress(progress);
        progressBar.setMax(49);


        final long period = 50;
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //this repeats every 100 ms
                if (progress<50){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                      }
                    });
                    progressBar.setProgress(progress);
                    progress++;
                }else{
                    //closing the timer
                    timer.cancel();
                    starApp();

                }
            }
        }, 0, period);
//

    }
    private void starApp() {
        startActivity(new Intent(MainActivity.this, SignInActivity.class));
        finish();
    }
}
