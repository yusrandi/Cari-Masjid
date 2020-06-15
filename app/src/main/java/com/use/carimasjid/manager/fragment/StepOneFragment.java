package com.use.carimasjid.manager.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;
import com.theartofdev.edmodo.cropper.CropImage;
import com.use.carimasjid.R;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.handler.MessageHandler;
import com.use.carimasjid.manager.ManagerMainActivity;
import com.use.carimasjid.manager.adapter.TypeAdapter;
import com.use.carimasjid.model.DataFilter;
import com.use.carimasjid.model.DataFilterDetail;
import com.use.carimasjid.user.adapter.FilterAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;


public class StepOneFragment extends Fragment implements BlockingStep, MessageHandler.alertSuccess {


    private final String TAG = "StepOneFragment";
    private int PLACE_PICKER_REQUEST = 1;
    private static int PICK_IMAGE_REQUEST = 2;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private Uri filePath;
    private Bitmap bitmap = null;
    public static File RESIMAGE=null;
    public static String RESNAME, RESLOCATION, RESADRESS, RESTYPE, RESIDKECAMATAN, RESMYIMAGE="";

    public static ImageView img;
    public static EditText etName;
    public static TextView tvLokasi, tvAlamat, tvType;

    private Dialog dialogDetail;
    private RecyclerView mRecyclerView;
    private ArrayList<String> listType;
    private TypeAdapter adapter;


    private ArrayList<DataFilter> listProvinsi;
    private ArrayList<DataFilter> listKabupaten;
    private ArrayList<DataFilter> listKecamatan;
    private ArrayList<DataFilter> listFilter;

    public static TextView tvProv, tvKab, tvKec;
    private Dialog filterDialog, filterDetailDialog;

    private SweetAlertDialog loadingDialog, successLoading, errorLoading;
    private MessageHandler messageHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_one_fragment, container, false);

        img = v.findViewById(R.id.img_add);
        etName = v.findViewById(R.id.etName);
        tvLokasi = v.findViewById(R.id.etLokasi);
        tvAlamat = v.findViewById(R.id.etAlamat);
        tvType = v.findViewById(R.id.etType);

        listProvinsi = new ArrayList<>();
        listKabupaten = new ArrayList<>();
        listKecamatan = new ArrayList<>();

        tvProv = v.findViewById(R.id.filter_prov);
        tvKab = v.findViewById(R.id.filter_kab);
        tvKec = v.findViewById(R.id.filter_kec);

        messageHandler = new MessageHandler(getActivity());

        if(ManagerMainActivity.d!=null){

            Glide.with(getContext())
                    .load(UrlApi.BASE_URL_IMAGES+ManagerMainActivity.d.getImage())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.masjid)
                            .error(R.mipmap.add))
                    .into(img);

            tvType.setText(ManagerMainActivity.d.getType());
            tvLokasi.setText(ManagerMainActivity.d.getLocation());
            tvAlamat.setText(ManagerMainActivity.d.getAdress());


            DataFilterDetail fd = ManagerMainActivity.fd;
            tvKec.setText(fd.getNamaKec());
            tvKab.setText(fd.getNamaKab());
            tvProv.setText(fd.getNamaProv());
            RESIDKECAMATAN = fd.getIdKec();
        }
        initDataFilter();
        v.findViewById(R.id.layout_select_provinsi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDetailDialog(listProvinsi, "prov");
            }

        });
        v.findViewById(R.id.layout_select_kabupaten).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDetailDialog(listFilter, "kab");
            }

        });
        v.findViewById(R.id.layout_select_kecamatan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDetailDialog(listFilter, "kec");
            }

        });

        v.findViewById(R.id.etLokasi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    //menjalankan place picker
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);

                    // check apabila <a title="Solusi Tidak Bisa Download Google Play Services di Android" href="https://www.twoh.co/2014/11/solusi-tidak-bisa-download-google-play-services-di-android/" target="_blank">Google Play Services tidak terinstall</a> di HP
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });

        v.findViewById(R.id.img_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = CropImage.activity()
                        .setAspectRatio(16,9)
                        .getIntent(getContext());

                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                    }
                });

        tvType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailDialog();
            }
        });

        return  v;

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

    void filterDetailDialog(ArrayList<DataFilter> list, final String filter) {
        filterDetailDialog = new Dialog(getContext());

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
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setItemAnimator(new DefaultItemAnimator());

        FilterAdapter adapter = new FilterAdapter(getContext(), list);
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnClickListener(new FilterAdapter.OnClickListener() {
            @Override
            public void onItemClick(int position, DataFilter d) {

                Toast.makeText(getContext(), "u'r click at "+d.getName(), Toast.LENGTH_SHORT).show();

                if(filter.equals("prov")){
                    tvProv.setText(d.getName());
                    initDetailFilter(d.getId(), filter);

                }else if(filter.equals("kab")){
                    tvKab.setText(d.getName());
                    initDetailFilter(d.getId(), filter);

                }else {
                    tvKec.setText(d.getName());
                    Log.d(TAG, "kec ID "+d.getId());
                    RESIDKECAMATAN = d.getId();
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

    String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult resultCode "+requestCode);

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getContext());

                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
                Date date = new Date();
                String tgl = dateFormat.format(date);

                String toastMsg = String.format( "Alamat "+place.getName(), place.getAddress(), place.getLatLng().latitude+" "+place.getLatLng().longitude)+"\nTanggal "+tgl;

                Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();

                tvAlamat.setText(place.getAddress());
                tvLokasi.setText(place.getLatLng().latitude+","+place.getLatLng().longitude);

            }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                RESIMAGE = new File(resultUri.getPath());
                long size = RESIMAGE.length() / 1024;


                Log.d(TAG, " path " + resultUri.getPath() + " Size " + RESIMAGE.length() / 1024);

                if (size < 1024) {

                    img.setImageBitmap(bitmap);
                    RESMYIMAGE = getStringImage(bitmap);


                } else
                    Toast.makeText(getContext(), "Maaf size maximum 1024 Kb", Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //you can do anythings you want
                RESNAME = etName.getText().toString();
                RESLOCATION = tvLokasi.getText().toString();
                RESADRESS = tvAlamat.getText().toString();
                RESTYPE = tvType.getText().toString();

                if(!TextUtils.isEmpty(RESNAME) &&!TextUtils.isEmpty(RESTYPE) &&!TextUtils.isEmpty(RESLOCATION) &&!TextUtils.isEmpty(RESADRESS) &&!TextUtils.isEmpty(RESIDKECAMATAN)){
                    callback.goToNextStep();

                }else {
                    messageHandler.alertError("Silahkan mengisi semua kolom");
                }
            }
        }, 0L);//
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        Toast.makeText(getContext(), "onCompleted!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackClicked(final StepperLayout.OnBackClickedCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //you can do anythings you want
                callback.goToPrevStep();
            }
        }, 0L);//
    }

    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

    private void detailDialog() {
        dialogDetail = new Dialog(getContext());

        dialogDetail.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDetail.setContentView(R.layout.dialog_type);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialogDetail.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogDetail.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(layoutParams);
        dialogDetail.setCancelable(true);


        mRecyclerView = dialogDetail.findViewById(R.id.rv_search);
        listType = new ArrayList<>();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
//        LinearLayoutManager manager = new LinearLayoutManager(this);
        RecyclerView.LayoutManager manager = new GridLayoutManager(getContext(), 1);
        mRecyclerView.setLayoutManager(manager);

        initType();

        dialogDetail.show();

    }

    private void initType() {
        listType.add("Masjid Negara");
        listType.add("Masjid Raya");
        listType.add("Masjid Besar");
        listType.add("Masjid Jami");
        listType.add("Masjid Bersejarah");
        listType.add("Masjid Di Tempat Publik");
        listType.add("Masjid Nasional");
        listType.add("Musholla Di Tempat Publik");
        listType.add("Musholla Perkantoran");
        listType.add("Musholla Pendidikan");
        listType.add("Musholla Perumahan");


        adapter = new TypeAdapter(getContext(), listType);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new TypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                tvType.setText(listType.get(position));
                dialogDetail.dismiss();
            }
        });
    }

    @Override
    public void alertSuccess(String msg) {

    }
}
