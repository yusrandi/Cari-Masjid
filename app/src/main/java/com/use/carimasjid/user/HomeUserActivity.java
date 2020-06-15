package com.use.carimasjid.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.widget.ANImageView;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.use.carimasjid.MainActivity;
import com.use.carimasjid.R;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.model.DataMasjid;
import com.use.carimasjid.user.adapter.FacilitiesAdapter;
import com.use.carimasjid.user.adapter.HomeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeUserActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "HomeUserActivity";
    private RecyclerView mRecyclerView;
    private HomeAdapter adapter;
    private ArrayList<DataMasjid> list;
    private ArrayList<DataMasjid> newList;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA
    };

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static final long UPDATE_INTERVAL = 0;
    private static final long FASTEST_UPDATE_INTERVAL = 1000 * 120 * 1;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5;

    double myLat, myLng;

    private SliderLayout imgSlider;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        buildGoogleApiClient();

        imgSlider = findViewById(R.id.img_slider);
        mProgressBar = findViewById(R.id.spin_kit);


        list = new ArrayList<>();
        mRecyclerView = findViewById(R.id.user_home_rv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
//        LinearLayoutManager manager = new LinearLayoutManager(this);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 1);
        mRecyclerView.setLayoutManager(manager);


//        initDataMosques();

        EditText etSearch = findViewById(R.id.home_user_etsearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s != null) {
                        newList = new ArrayList<>();

                        String a = s.toString().toLowerCase();

                        System.out.println(a);

                        for (DataMasjid userInfo : list) {
                            Log.d(TAG, "etsearch nama " + userInfo.getName());
                            String username = userInfo.getName().toLowerCase();
                            if (username.contains(a)) {
                                newList.add(userInfo);
                            }
                        }
                        adapter.setFilter(newList);
                    }
                } catch (Exception e) {

                }
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

    private void initDataMosques() {

        AndroidNetworking.get(UrlApi.MOSQUE_CREATE_URL)
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
//                        Log.d(TAG, response.toString());
                        mProgressBar.setVisibility(View.GONE);
                        try {
                            boolean success = response.getBoolean("success");

                            Log.d(TAG, "success "+success);
                            if(success){
                                JSONObject obj = response.getJSONObject("data");
                                JSONArray array = obj.getJSONArray("data");
                                for(int i = 0; i<array.length(); i++){

                                    JSONObject object = array.getJSONObject(i);
                                    String name = object.getString("name");

                                    Log.d(TAG, "name "+name);
                                    DataMasjid d = new DataMasjid(
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

                                    getDistance(d);

                                    TextSliderView textSliderView = new TextSliderView(HomeUserActivity.this);
                                    // initialize a SliderLayout
                                    textSliderView
                                            .description(d.getName())
                                            .image(UrlApi.BASE_URL_IMAGES+d.getImage())
                                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                                @Override
                                                public void onSliderClick(BaseSliderView slider) {

                                                }
                                            })
                                            .setScaleType(BaseSliderView.ScaleType.Fit);

                                    //add your extra information
//                        textSliderView.bundle(new Bundle());
//                        textSliderView.getBundle()
//                                .putString("extra",name);

                                    imgSlider.addSlider(textSliderView);


                                }
                                imgSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                                imgSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
//                    sliderIklan.setCustomAnimation(new DescriptionAnimation());
                                imgSlider.setDuration(2000);



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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "GoogleApiClient onLocationChanged ");

        myLat = location.getLatitude();
        myLng = location.getLongitude();


        Log.d(TAG, "latitude  "+myLat);
        initDataMosques();
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    private void getDistance(final DataMasjid d) {
        String loc[] = d.getLocation().split(",");

        StringBuilder url = new StringBuilder();
        url.append("https://maps.googleapis.com/maps/api/distancematrix/json?");
        url.append("origins=" + myLat + "," + myLng);
        url.append("&destinations=" + loc[0] + "," + loc[1]);
        url.append("&key=AIzaSyCwRAPxNQWM65Ze489cVGpGLkGgQqzcHX4");

        Log.d(TAG, "Url Distance "+url.toString());

        AndroidNetworking.get(url.toString())
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
//                        Log.d(TAG, response.toString());
                        try {

                            JSONArray jsonArray = response.getJSONArray("rows");
                            JSONArray dest = response.getJSONArray("destination_addresses");
                            JSONArray origin = response.getJSONArray("origin_addresses");
                            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                            JSONArray jsonArray1 = jsonObject1.getJSONArray("elements");

                            String dest_adress = String.valueOf(dest.get(0));
                            String ori_adress = String.valueOf(origin.get(0));
                            String jarak= jsonArray1.getJSONObject(0).getJSONObject("distance").getString("text");
                            double jarakKM= jsonArray1.getJSONObject(0).getJSONObject("distance").getDouble("value");
                            String waktu = jsonArray1.getJSONObject(0).getJSONObject("duration").getString("text");

                            Log.d(TAG, "Jarak "+jarak+" Waktu "+waktu);

                            d.setJarak(jarak+"\n"+waktu);
                            list.add(d);
                            adapter = new HomeAdapter(HomeUserActivity.this, list);
                            mRecyclerView.setAdapter(adapter);
                            adapter.setOnClickListener(new HomeAdapter.OnClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    DataMasjid d = list.get(position);
                                    detailDialog(d);
                                }
                            });


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

    void detailDialog(DataMasjid d){
        final Dialog dialog = new Dialog(HomeUserActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_home_user_detail);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(layoutParams);
        dialog.setCancelable(true);

        TextView tvName = dialog.findViewById(R.id.layout_home_user_name);
        TextView tvAdress = dialog.findViewById(R.id.layout_home_user_adress);
        TextView tvType = dialog.findViewById(R.id.layout_home_user_type);
        TextView tvCeo = dialog.findViewById(R.id.layout_home_user_ceo);
        TextView tvVice = dialog.findViewById(R.id.layout_home_user_vice);

        ANImageView img = dialog.findViewById(R.id.layout_home_user_img);

        RecyclerView mRecyclerView = dialog.findViewById(R.id.layout_home_user_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(HomeUserActivity.this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ArrayList<String> list = new ArrayList<>();

        String[] facilities = d.getFacilities().split(",");
        for(String s : facilities){
            list.add(s);
        }

        Log.d(TAG, "List Size "+list.size());

        com.use.carimasjid.user.adapter.FacilitiesAdapter mAdapter = new FacilitiesAdapter(HomeUserActivity.this, list);
        mRecyclerView.setAdapter(mAdapter);
        adapter.notifyDataSetChanged();

        tvName.setText(d.getName());
        tvAdress.setText(d.getAdress());
        tvType.setText(d.getType());
        tvCeo.setText(d.getCeo_name()+" | "+d.getCeo_contact());
        tvVice.setText(d.getVice_name()+" | "+d.getVice_contact());

        img.setDefaultImageResId(R.drawable.masjid);
        img.setErrorImageResId(R.drawable.masjid);
        img.setImageUrl(UrlApi.BASE_URL_IMAGES+d.getImage());


        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


}
