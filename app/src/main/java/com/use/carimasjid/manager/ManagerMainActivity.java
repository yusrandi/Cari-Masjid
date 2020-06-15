package com.use.carimasjid.manager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;
import com.use.carimasjid.R;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.handler.SessionManager;
import com.use.carimasjid.manager.adapter.StepperAdapter;
import com.use.carimasjid.manager.fragment.StepOneFragment;
import com.use.carimasjid.manager.fragment.StepTwoFragment;
import com.use.carimasjid.model.DataFilterDetail;
import com.use.carimasjid.model.DataMasjid;
import com.use.carimasjid.user.MapsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ManagerMainActivity extends   AppCompatActivity  implements StepperLayout.StepperListener{

    private final String TAG = "ManagerMainActivity";
    private StepperLayout mStepperLayout;
    private StepperAdapter mStepperAdapter;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA
    };

    private SessionManager manager;

    public static String LIST_FACILITIES = "", STATUS = "create", IDMOSQUE="", ID="", MYIMAGE="";
    public static DataMasjid d;
    public static DataFilterDetail fd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mStepperLayout = (StepperLayout) findViewById(R.id.stepperLayout);
        mStepperAdapter = new StepperAdapter(getSupportFragmentManager(), this);
        mStepperLayout.setAdapter(mStepperAdapter);
        mStepperLayout.setListener(this);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        manager = new SessionManager(ManagerMainActivity.this);
        if(manager.isLoggedIn()){
            HashMap<String, String> user = manager.getUserDetails();


            ID = user.get(SessionManager.ID);
            initDataMosques(ID);
        }

        findViewById(R.id.layout_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(ManagerMainActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:

                                manager.logoutUser();
                                finish();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popup.show();
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

    @Override
    public void onCompleted(View completeButton) {
        Toast.makeText(this, "onCompleted!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(VerificationError verificationError) {
        Toast.makeText(this, "onError! -> " + verificationError.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStepSelected(int newStepPosition) {

    }

    @Override
    public void onReturn() {
        finish();
    }

    private void initDataMosques(String id) {

        String url = UrlApi.MOSQUE_CREATE_URL+"/"+id+"/check";
        Log.d(TAG, "initDataMosques : url "+url);
        AndroidNetworking.get(UrlApi.MOSQUE_CREATE_URL+"/"+id+"/check")
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
//                        Log.d(TAG, response.toString());
                        try {
                            boolean success = response.getBoolean("success");

                            Log.d(TAG, "success initDataMosques by Id "+success);
                            if(success){
                                JSONArray array = response.getJSONArray("data");
                                for(int i = 0; i<array.length(); i++){

                                    JSONObject object = array.getJSONObject(i);
                                    String name = object.getString("name");

                                    Log.d(TAG, "name "+name);
                                    d = new DataMasjid(
                                            object.getString("id"),
                                            object.getString("person_id"),
                                            object.getString("kecamatan_id"),
                                            object.getString("name"),
                                            object.getString("type"),
                                            object.getString("image"),
                                            object.getString("adress"),
                                            object.getString("location"),
                                            object.getString("ceo_name"),
                                            object.getString("ceo_contact"),
                                            object.getString("vice_name"),
                                            object.getString("vice_contact"),
                                            object.getString("facilities"),
                                            object.getDouble("rating")
                                    );

                                    StepOneFragment.etName.setText(name);
                                    StepOneFragment.tvType.setText(d.getType());
                                    StepOneFragment.tvLokasi.setText(d.getLocation());
                                    StepOneFragment.tvAlamat.setText(d.getAdress());

                                    Glide.with(ManagerMainActivity.this)
                                            .load(UrlApi.BASE_URL_IMAGES+ManagerMainActivity.d.getImage())
                                            .apply(new RequestOptions()
                                                    .placeholder(R.drawable.masjid)
                                                    .error(R.mipmap.add))
                                            .into(StepOneFragment.img);

                                    StepTwoFragment.etCeoName.setText(d.getCeo_name());
                                    StepTwoFragment.etCeoContact.setText(d.getCeo_contact());
                                    StepTwoFragment.etViceName.setText(d.getVice_name());
                                    StepTwoFragment.etViceContact.setText(d.getVice_contact());


                                    LIST_FACILITIES = d.getFacilities();
                                    STATUS = "update";
                                    IDMOSQUE = d.getId();
                                    MYIMAGE = d.getImage();

                                    initDataKecamatanId(d.getKecamatan_id());

                                }

                            }else {
                                Toast.makeText(ManagerMainActivity.this, "not found ", Toast.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d(TAG, error.toString());

                    }
                });



    }

    private void initDataKecamatanId(String kecamatan_id) {
        fd = new DataFilterDetail();

        String url = UrlApi.KECAMATAN_URL+"/"+kecamatan_id+"/all";
        Log.d(TAG, "initDataMosques : url "+url);
        AndroidNetworking.get(UrlApi.KECAMATAN_URL+"/"+kecamatan_id+"/all")
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
//                        Log.d(TAG, response.toString());
                        try {
                            boolean success = response.getBoolean("success");

                            Log.d(TAG, "success initDataMosques by Id "+success);
                            if(success){

                                JSONArray array = response.getJSONArray("data");
                                JSONObject object = array.getJSONObject(0);

                                String nama = object.getString("nama");
                                String id = object.getString("id");

                                StepOneFragment.tvKec.setText(nama);

                                StepOneFragment.RESIDKECAMATAN = id;

                                fd.setNamaKec(nama);
                                fd.setIdKec(id);

                                JSONObject objKab = object.getJSONObject("kabupaten");

                                String namaKab = objKab.getString("nama");
                                String idKab = objKab.getString("id");

                                fd.setNamaKab(namaKab);
                                fd.setIdKab(idKab);

                                StepOneFragment.tvKab.setText(namaKab);


                                JSONObject objProv = objKab.getJSONObject("provinsi");

                                String namaProv = objProv.getString("nama");
                                String idProv = objProv.getString("id");

                                fd.setNamaProv(namaProv);
                                fd.setIdProv(idProv);

                                StepOneFragment.tvProv.setText(namaProv);


                            }else {
                                Toast.makeText(ManagerMainActivity.this, "not found ", Toast.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d(TAG, error.toString());

                    }
                });
    }

}
