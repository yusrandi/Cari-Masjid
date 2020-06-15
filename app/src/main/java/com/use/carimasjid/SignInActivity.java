package com.use.carimasjid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.handler.MessageHandler;
import com.use.carimasjid.handler.SessionManager;
import com.use.carimasjid.manager.ManagerMainActivity;
import com.use.carimasjid.user.MapsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignInActivity extends AppCompatActivity implements MessageHandler.alertSuccess {

    private final String TAG = "SignInActivity";
    private TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText etEmail, etPass;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");

    private SessionManager sessionManager;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA
    };
    private SweetAlertDialog successLoading;
    private String role;

    private MessageHandler messageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        layoutEmail = findViewById(R.id.etEmailLayout);
        layoutPassword = findViewById(R.id.etPasswordLayout);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);

        sessionManager = new SessionManager(SignInActivity.this);
        messageHandler = new MessageHandler(SignInActivity.this);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        findViewById(R.id.btn_signin_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String passwordInput = etPass.getText().toString().trim();


                if(validateEmail(email) || validatePassword(passwordInput)){

                    loginUser(email, passwordInput);
                }
            }
        });

        findViewById(R.id.btn_signup_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });


    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    void loginUser(String email, String passwordInput) {

        messageHandler.alertLoading();

        AndroidNetworking.post(UrlApi.SIGN_IN_URL)
                .addBodyParameter("email", email)
                .addBodyParameter("password", passwordInput)
                .addHeaders("X-CSRF-Token", "74b2cb4106d14be9b8e39920b5465f3ea8b0b4ba")
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "loginUser : "+response.toString());

                        messageHandler.closeAlert();

                        try {
                            boolean success = response.getBoolean("success");
                            String msg = response.getString("message");

                            Toast.makeText(SignInActivity.this, "succes "+success+" message "+msg, Toast.LENGTH_LONG).show();

                            Log.d(TAG, "success "+success);
                            if(success){

                                JSONArray array = response.getJSONArray("data");

                                JSONObject object = array.getJSONObject(0);
                                String name = object.getString("name");
                                role = object.getString("role");
                                String id = object.getString("id");
                                Log.d(TAG, "name "+name);

                                sessionManager.createLoginSession(role, id);
                                alertSuccess(msg);

                            }else {
                                Log.d(TAG, "success "+success);
                                messageHandler.alertError(msg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, anError.getErrorBody());

                    }
                });
    }

    boolean validateEmail(String email){


        if(email.isEmpty()){
            layoutEmail.setError("Field can't be empty !");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            layoutEmail.setError("Please enter a valid email adress !");
            return false;
        }else{
            layoutEmail.setError(null);
            return true;
        }


    }
    boolean validatePassword(String passwordInput) {

//
        if (passwordInput.isEmpty()) {
            layoutPassword.setError("Field can't be empty");
            return false;
        }  else {
            layoutPassword.setError(null);
            return true;
        }
    }

    @Override
    public void alertSuccess(String msg) {
        successLoading = new SweetAlertDialog(SignInActivity.this, SweetAlertDialog.SUCCESS_TYPE);
        successLoading.setTitleText("Berhasil");
        successLoading.setContentText(msg);

        successLoading.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                successLoading.dismissWithAnimation();
                if (role.equals("2")) {
                    Intent intent = new Intent(SignInActivity.this, ManagerMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }
            }
        });
        try {
            successLoading.show();
        }catch (Exception e){

        }
    }
}
