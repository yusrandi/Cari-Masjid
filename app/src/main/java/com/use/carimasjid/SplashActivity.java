package com.use.carimasjid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.use.carimasjid.handler.SessionManager;
import com.use.carimasjid.manager.ManagerMainActivity;
import com.use.carimasjid.user.MapsActivity;

import java.util.HashMap;

public class SplashActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        doWork();
    }

    private void doWork() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startApp();
            }
        }, 4*1000);

    }

    private void startApp() {
        session = new SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {


            HashMap<String, String> user = session.getUserDetails();


            String RULE = user.get(SessionManager.ROLE);


            // rule


            if (RULE.equals("2")) {
                Intent intent = new Intent(SplashActivity.this, ManagerMainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {

                Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }



        }else {
            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
