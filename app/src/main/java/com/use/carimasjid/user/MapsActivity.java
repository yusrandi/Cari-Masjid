package com.use.carimasjid.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.widget.ANImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.use.carimasjid.R;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.handler.SessionManager;
import com.use.carimasjid.manager.ManagerMainActivity;
import com.use.carimasjid.manager.fragment.StepOneFragment;
import com.use.carimasjid.manager.fragment.StepTwoFragment;
import com.use.carimasjid.model.DataFilter;
import com.use.carimasjid.model.DataMasjid;
import com.use.carimasjid.user.adapter.FacilitiesAdapter;
import com.use.carimasjid.user.adapter.FilterAdapter;
import com.use.carimasjid.user.adapter.HomeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    private static final String TAG = "MapsActivity";

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
    private static final float DEFAULT_ZOOM = 16f;
    private static final long UPDATE_INTERVAL = 0;
    private static final long FASTEST_UPDATE_INTERVAL = 1000 * 120 * 1;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5;

    double myLat, myLng;
    private MapView mapView;
    private GoogleMap mMap;

    private ProgressBar mProgressBar;
    private Dialog filterDialog, filterDetailDialog;

    private ArrayList<DataFilter> listProvinsi;
    private ArrayList<DataFilter> listKabupaten;
    private ArrayList<DataFilter> listKecamatan;
    private ArrayList<DataFilter> listFilter;

    private TextView tvProv, tvKab, tvKec;
    private SessionManager sessionManager;

    private TextView tvName;
    private CircleImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow();
//            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        mapView = findViewById(R.id.mapview);
        mProgressBar = findViewById(R.id.spin_kit);
        tvName = findViewById(R.id.user_name);
        img = findViewById(R.id.home_img);
        sessionManager = new SessionManager(MapsActivity.this);

        if(sessionManager.isLoggedIn()){
            HashMap<String, String> user = sessionManager.getUserDetails();


            String id = user.get(SessionManager.ID);
            initUser(id);
        }

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }


        buildGoogleApiClient();

        list = new ArrayList<>();
        listProvinsi = new ArrayList<>();
        listKabupaten = new ArrayList<>();
        listKecamatan = new ArrayList<>();

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
//        LinearLayoutManager manager = new LinearLayoutManager(this);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 1);
        mRecyclerView.setLayoutManager(manager);

        findViewById(R.id.maps_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(MapsActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_user, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:

                                sessionManager.logoutUser();
                                finish();
                                return true;
                            case R.id.action_filter:

                                filterDialog();

                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });

        initDataFilter();

        EditText etSearch = findViewById(R.id.txt_search);

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

    private void initUser(String id) {

        String url = UrlApi.PERSONS_URL+id+"/check";
        Log.d(TAG, "initUser : url "+url);
        AndroidNetworking.get(UrlApi.PERSONS_URL+id+"/check")
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

                            Log.d(TAG, "success initUser by Id "+success);
                            if(success){
                                JSONArray array = response.getJSONArray("data");
                                for(int i = 0; i<array.length(); i++){

                                    JSONObject object = array.getJSONObject(i);
                                    String name = object.getString("name");
                                    String photo = object.getString("photo");

                                    Log.d(TAG, "name "+name);
                                    tvName.setText(name);

                                    Glide.with(MapsActivity.this)
                                            .load(UrlApi.BASE_URL_IMAGES_PERSONS+photo)
                                            .apply(new RequestOptions()
                                                    .placeholder(R.mipmap.my_location)
                                                    .error(R.mipmap.my_location))
                                            .into(img);
                                }

                            }else {
//                                Toast.makeText(MapsActivity.this, "not found ", Toast.LENGTH_LONG).show();
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

    private void initDataMosques() {

        list = new ArrayList<>();
        mMap.clear();


        AndroidNetworking.get(UrlApi.MOSQUE_CREATE_URL)
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



                                }

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
    private void initDataMosques(String id) {

        list = new ArrayList<>();
        mMap.clear();


        String url = UrlApi.MOSQUE_CREATE_URL+"/"+id+"/all";
        Log.d(TAG, "initDataMosques : url "+url);
        AndroidNetworking.get(UrlApi.MOSQUE_CREATE_URL+"/"+id+"/all")
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



                                }

                            }else {
//                                Toast.makeText(MapsActivity.this, "not found ", Toast.LENGTH_LONG).show();
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

    private void initDataFilter() {

        AndroidNetworking.get(UrlApi.PROVINSIS_URL)
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // do anything with response
//                        Log.d(TAG, response.toString());

                        try {
                        for(int a=0; a<response.length(); a++){

                                JSONObject provObj = response.getJSONObject(a);

                            listProvinsi.add(new DataFilter(provObj.getString("id"),provObj.getString("nama")));

                            JSONArray kabArr = provObj.getJSONArray("kabupaten");
                            for(int b = 0; b<kabArr.length(); b++){
                                JSONObject kabObj = kabArr.getJSONObject(b);
//                                Log.d(TAG, "name "+kabObj.getString("nama"));
                                listKabupaten.add(new DataFilter(kabObj.getString("id"), kabObj.getString("provinsi_id"), kabObj.getString("nama")));

                                JSONArray kecArr = kabObj.getJSONArray("kecamatan");
                                for(int c = 0; c<kecArr.length(); c++){
                                    JSONObject kecObj = kecArr.getJSONObject(c);
//                                    Log.d(TAG, "name "+kecObj.getString("nama"));
                                    listKecamatan.add(new DataFilter(kecObj.getString("id"), kecObj.getString("kabupaten_id"), kecObj.getString("nama")));
                                }
                            }
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

        moveCamera(new LatLng(myLat, myLng), DEFAULT_ZOOM);
        mProgressBar.setVisibility(View.GONE);


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
        final String loc[] = d.getLocation().split(",");

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
                        mProgressBar.setVisibility(View.GONE);
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
                            adapter = new HomeAdapter(MapsActivity.this, list);
                            mRecyclerView.setAdapter(adapter);
                            adapter.setOnClickListener(new HomeAdapter.OnClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    DataMasjid d = list.get(position);
                                    detailDialog(d);
                                }
                            });

                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(loc[0]), Double.valueOf(loc[1]))).title(d.getName()).snippet(d.getType()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mosque_location)));

                            moveCamera(new LatLng(myLat, myLng), DEFAULT_ZOOM);

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

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera  : moving camera to lat " + latLng.latitude + " and lng " + latLng.longitude);
        double latPos = latLng.latitude;
        double longPos = latLng.longitude;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        mMap.addMarker(new MarkerOptions().position(new LatLng(myLat, myLng)).title("My Position").snippet("here is your").icon(BitmapDescriptorFactory.fromResource(R.mipmap.my_location)));


    }

    void detailDialog(final DataMasjid d){
        final Dialog dialog = new Dialog(MapsActivity.this);

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
        RatingBar ratingBar = dialog.findViewById(R.id.detail_ratingbar);

        RecyclerView mRecyclerView = dialog.findViewById(R.id.layout_home_user_rv);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(MapsActivity.this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ArrayList<String> list = new ArrayList<>();

        String[] facilities = d.getFacilities().split(",");
        for(String s : facilities){
            list.add(s);
        }

        Log.d(TAG, "List Size "+list.size());

        com.use.carimasjid.user.adapter.FacilitiesAdapter mAdapter = new FacilitiesAdapter(MapsActivity.this, list);
        mRecyclerView.setAdapter(mAdapter);
        adapter.notifyDataSetChanged();

        tvName.setText(d.getName());
        tvAdress.setText(d.getAdress());
        tvType.setText("Type : "+d.getType());
        tvCeo.setText(d.getCeo_name()+" | "+d.getCeo_contact());
        tvVice.setText(d.getVice_name()+" | "+d.getVice_contact());
        ratingBar.setRating((float) d.getRating());

        img.setErrorImageResId(R.drawable.masjid);
        img.setImageUrl(UrlApi.BASE_URL_IMAGES+d.getImage());


        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });dialog.findViewById(R.id.btn_direction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] loc = d.getLocation().split(",");
                showNavigation(new LatLng(Double.valueOf(loc[0]), Double.valueOf(loc[1])));
            }
        });dialog.findViewById(R.id.layout_rating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingDialog(d.getId(), d.getRating());
            }
        });
        dialog.show();
    }

    void filterDialog() {
        filterDialog = new Dialog(MapsActivity.this);

        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_filter);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = filterDialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(layoutParams);
        filterDialog.setCancelable(true);

        tvProv = filterDialog.findViewById(R.id.filter_prov);
        tvKab = filterDialog.findViewById(R.id.filter_kab);
        tvKec = filterDialog.findViewById(R.id.filter_kec);

        filterDialog.findViewById(R.id.layout_select_provinsi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDetailDialog(listProvinsi, "prov");
            }

        });
        filterDialog.findViewById(R.id.layout_select_kabupaten).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDetailDialog(listFilter, "kab");
            }

        });
        filterDialog.findViewById(R.id.layout_select_kecamatan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDetailDialog(listFilter, "kec");
            }

        });

        filterDialog.show();
    }

    void filterDetailDialog(ArrayList<DataFilter> list, final String filter) {
        filterDetailDialog = new Dialog(MapsActivity.this);

        filterDetailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDetailDialog.setContentView(R.layout.layout_filter_detail);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = filterDetailDialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        filterDetailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(layoutParams);
        filterDetailDialog.setCancelable(true);


        RecyclerView rv = filterDetailDialog.findViewById(R.id.filter_detail_rv);
        rv.setLayoutManager(new LinearLayoutManager(MapsActivity.this));
        rv.setItemAnimator(new DefaultItemAnimator());

        FilterAdapter adapter = new FilterAdapter(MapsActivity.this, list);
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnClickListener(new FilterAdapter.OnClickListener() {
            @Override
            public void onItemClick(int position, DataFilter d) {

//                Toast.makeText(MapsActivity.this, "u'r click at "+d.getName(), Toast.LENGTH_SHORT).show();

                if(filter.equals("prov")){
                    tvProv.setText(d.getName());
                    initDetailFilter(d.getId(), filter);

                }else if(filter.equals("kab")){
                    tvKab.setText(d.getName());
                    initDetailFilter(d.getId(), filter);

                }else {
                    tvKec.setText(d.getName());
                    Log.d(TAG, "kec ID "+d.getId());
                    initDataMosques(d.getId());
                    mProgressBar.setVisibility(View.VISIBLE);
                    filterDialog.dismiss();
                }
                filterDetailDialog.dismiss();
            }
        });

        filterDetailDialog.show();
    }


    void initDetailFilter(String id, String filter){
        listFilter = new ArrayList<>();

        if(filter.equals("prov")){
            for(DataFilter d : listKabupaten){
                if(d.getParentId().equals(id)){
                    listFilter.add(d);
                }
            }
        }else if(filter.equals("kab")){
            for(DataFilter d : listKecamatan){
                if(d.getParentId().equals(id)){
                    listFilter.add(d);
                }
            }
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady");

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng latLng = marker.getPosition();

                for (DataMasjid d : list) {
                    String loc[] = d.getLocation().split(",");

                    if (loc[0].contains(String.valueOf(latLng.latitude))) {
                        detailDialog(d);
                    }
                }
            }
        });

    }

    public void showNavigation(LatLng latTo) {

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://ditu.google.cn/maps?f=d&source=s_d" +
                        "&saddr=" + myLat + "," + myLng +
                        "&daddr=" + latTo.latitude + "," + latTo.longitude +
                        "&hl=zh&t=m&dirflg=d"
                ));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);

    }

    void ratingDialog(final String id, final double rateBefore){
        final Dialog dialog = new Dialog(MapsActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rating);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(layoutParams);
        dialog.setCancelable(true);

        final RatingBar ratingBar = dialog.findViewById(R.id.dialog_rating_bar);

        dialog.findViewById(R.id.dialog_rating_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float rate = ratingBar.getRating();

                double resultRate = (rateBefore + rate)/2;

                Toast.makeText(MapsActivity.this, "rating "+rate, Toast.LENGTH_SHORT).show();
                updateRating(resultRate, id);
                dialog.dismiss();
            }
        });
        dialog.show();


    }

    void updateRating(double number, String id){
        String url = UrlApi.MOSQUE_CREATE_URL+"/"+number+"/"+id+"/rate";
        Log.d(TAG, "updateRating : url "+url);
        AndroidNetworking.get(url)
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
                            String msg = response.getString("message");

                            Log.d(TAG, "success initDataMosques by Id "+success);
                            Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_LONG).show();


                        } catch (Exception e) {
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
