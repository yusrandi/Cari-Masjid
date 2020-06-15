package com.use.carimasjid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.handler.MessageHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignUpActivity extends AppCompatActivity implements MessageHandler.alertSuccess {

    private String TAG = "SignUpActivity";
    private ImageView img;

    private static int PICK_IMAGE_REQUEST = 1;
    private static int CAMERA_PIC_REQUEST = 2;

    private Uri filePath;
    private Bitmap bitmap = null;
    private File resFile;

    public static String resPhoto = "", resName="", resEmail="", resPass="", resRole="3";
    public static Uri GAMBAR_URI;

    private ImageView imgAsUser, imgAsManager;

    private TextInputLayout layoutName, layoutEmail, layoutPassword;
    private TextInputEditText etName, etEmail, etPass;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
//                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");

    private SweetAlertDialog successLoading;
    private MessageHandler messageHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        img = findViewById(R.id.signup_img_user);
        imgAsUser = findViewById(R.id.img_as_user);
        imgAsManager = findViewById(R.id.img_as_manager);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);

        layoutName = findViewById(R.id.etNameLayout);
        layoutEmail = findViewById(R.id.etEmailLayout);
        layoutPassword = findViewById(R.id.etPasswordLayout);


        messageHandler = new MessageHandler(SignUpActivity.this);


        findViewById(R.id.signup_step1_btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resFile!=null){
                    findViewById(R.id.layout_step_2).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_step_1).setVisibility(View.GONE);
                }else {
                    messageHandler.alertError("Silahkan memilih foto");
                }

            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                CropImage.activity(filePath)
                        .setAspectRatio(1, 1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SignUpActivity.this);
            }
        });

        imgAsUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgAsUser.setImageResource(R.mipmap.user_deselect);
                imgAsManager.setImageResource(R.mipmap.user_select);

//                Toast.makeText(SignUpActivity.this, "your sign up As User", Toast.LENGTH_SHORT).show();
                resRole = "3";
            }
        });imgAsManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgAsUser.setImageResource(R.mipmap.user_select);
                imgAsManager.setImageResource(R.mipmap.user_deselect);

//                Toast.makeText(SignUpActivity.this, "your sign up As Manager", Toast.LENGTH_SHORT).show();
                resRole = "2";

            }
        });

        findViewById(R.id.signup_choice_btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.layout_choice).setVisibility(View.GONE);
                findViewById(R.id.layout_step_2).setVisibility(View.GONE);
                findViewById(R.id.layout_step_1).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btn_signup_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resName = etName.getText().toString().trim();
                resEmail = etEmail.getText().toString().trim();
                resPass= etPass.getText().toString().trim();
                if(validateName(resName) || validateEmail(resEmail) || validatePassword(resPass)){

                    registerUser();

                }
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null){
                    layoutName.setError(null);
                }
            }
        });
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null){
                    layoutEmail.setError(null);
                }
            }
        });
        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s!=null){
                    layoutPassword.setError(null);
                }
            }
        });

    }

    private void registerUser() {


        messageHandler.alertLoading();

        Log.d(TAG, "registerUser name "+resName+" email "+resEmail+" pass "+resPass+" role "+resRole);

        AndroidNetworking.upload(UrlApi.SIGN_UP_URL)
                .addMultipartFile("photo",resFile)
                .addMultipartParameter("name",resName)
                .addMultipartParameter("email",resEmail)
                .addMultipartParameter("password",resPass)
                .addMultipartParameter("role",resRole)
                .setTag("uploadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d(TAG, "bytesUploaded "+bytesUploaded+" totalBytes "+totalBytes);
                        if(bytesUploaded == totalBytes){

                        }


                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d(TAG, "registerUser : "+response.toString());

                        messageHandler.closeAlert();

                        try {
                            boolean success = response.getBoolean("success");
                            String msg = response.getString("message");

//                            Toast.makeText(SignUpActivity.this, "Message "+msg, Toast.LENGTH_LONG).show();
                            Log.d(TAG, "success "+success);

                            if(success){
                                JSONObject array = response.getJSONObject("data");

                                String name = array.getString("name");
                                Log.d(TAG, "name "+name);

                               alertSuccess(msg);

                            }else {
                                messageHandler.alertError(msg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d(TAG, error.getErrorBody());

                    }
                });
    }

    void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }
    }

    String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    @SuppressLint("MissingSuperCall")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        try {
            filePath = data.getData();


            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
                filePath = data.getData();


                CropImage.activity(filePath)
                        .setAspectRatio(1, 1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);

            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {

                    Uri resultUri = result.getUri();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(SignUpActivity.this.getContentResolver(), resultUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    resFile = new File(resultUri.getPath());
                    long size = resFile.length() / 1024;


                    Log.d(TAG, " path " + resultUri.getPath() + " Size " + resFile.length() / 1024);

                    if (size < 512) {

                        img.setImageBitmap(bitmap);
                        resPhoto = getStringImage(bitmap);
//                        Log.d(TAG, "result gambar " + resPhoto);

                    } else
                        Toast.makeText(SignUpActivity.this, "Maaf size maximum 500 Kb", Toast.LENGTH_SHORT).show();

                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "Silahkan pilih foto", Toast.LENGTH_SHORT).show();
        }

    }


    boolean validateName(String name){


        if(name.isEmpty()){
            layoutName.setError("Field can't be empty !");
            return false;
        }else{
            layoutEmail.setError(null);
            return false;
        }

    }

    boolean validateEmail(String email) {


        if (email.isEmpty()) {
            layoutEmail.setError("Field can't be empty !");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Please enter a valid email adress !");
            return false;
        } else {
            layoutEmail.setError(null);
            return true;
        }

    }

    boolean validatePassword(String passwordInput) {

//
        if (passwordInput.isEmpty()) {
            layoutPassword.setError("Field can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            layoutPassword.setError("Password too weak\n- no white spaces\n- at least 4 characters");
            return false;
        } else {
            layoutPassword.setError(null);
            return true;
        }
    }


    @Override
    public void alertSuccess(String msg) {
        successLoading = new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.SUCCESS_TYPE);
        successLoading.setTitleText("Berhasil");
        successLoading.setContentText(msg);

        successLoading.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                successLoading.dismissWithAnimation();
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });
        try {
            successLoading.show();
        }catch (Exception e){

        }
    }
}
