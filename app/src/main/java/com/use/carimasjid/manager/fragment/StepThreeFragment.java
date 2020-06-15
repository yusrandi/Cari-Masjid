package com.use.carimasjid.manager.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;
import com.use.carimasjid.R;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.handler.MessageHandler;
import com.use.carimasjid.manager.ManagerMainActivity;
import com.use.carimasjid.manager.adapter.FacilitiesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class StepThreeFragment extends Fragment implements BlockingStep, MessageHandler.alertSuccess {

    private RecyclerView mRecyclerView;
    private FacilitiesAdapter mAdapter;
    private ArrayList<String> list = new ArrayList<>();
    private StringBuffer sb;
    private String TAG = "StepThreeFragment";
    public static String RESFACILITIES;
    private String RESSTATUS = "no";

    private SweetAlertDialog  successLoading;
    private MessageHandler messageHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(
                R.layout.step_three_fragment, container, false);

        mRecyclerView = v.findViewById(R.id.fr_step_three_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        messageHandler = new MessageHandler(getActivity());


        initDataFacilities();

        Log.d(TAG, "res Image "+StepOneFragment.RESIMAGE);
//        Log.d(TAG, "res MyImage "+StepOneFragment.RESMYIMAGE);
        if(StepOneFragment.RESIMAGE == null){
            RESSTATUS = "no";
        }else {
            RESSTATUS = "yes";
        }


        return v;
    }

    private void initDataFacilities() {

        list = new ArrayList<>();

        AndroidNetworking.get(UrlApi.FACILITIES_UP_URL)
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d(TAG, response.toString());
                        try {
                            boolean success = response.getBoolean("success");

                            Log.d(TAG, "success "+success);
                            if(success){
                                JSONArray array = response.getJSONArray("data");

                                for(int i = 0; i<array.length(); i++){

                                    JSONObject object = array.getJSONObject(i);
                                    String name = object.getString("name");
                                    Log.d(TAG, "name "+name);
                                    list.add(name);
                                }


                            }

                            mAdapter = new FacilitiesAdapter(getContext(), list);
                            mRecyclerView.setAdapter(mAdapter);

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
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //you can do anythings you want
                callback.goToNextStep();
            }
        }, 0L);//
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        sb = new StringBuffer();

        for(int i = 0; i<mAdapter.itemChecked.size(); i++){
            if(i>0){
                sb.append(",");

            }
            sb.append(mAdapter.itemChecked.get(i));

        }

        Log.d(TAG, "status "+ManagerMainActivity.STATUS);

        if(mAdapter.itemChecked.size()>0){
            Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_SHORT).show();
            RESFACILITIES = sb.toString();

            if(ManagerMainActivity.STATUS.equals("update")){
                updateMosque();
            }else {
                createMosque();
            }
        }else {
            messageHandler.alertError("Silahkan pilih fasilitas");
//            Toast.makeText(getContext(), "Please Select Facilities", Toast.LENGTH_SHORT).show();

        }

    }

    private void createMosque() {

        messageHandler.alertLoading();

        AndroidNetworking.upload(UrlApi.MOSQUE_CREATE_URL)
                .addMultipartFile("image",StepOneFragment.RESIMAGE)
                .addMultipartParameter("person_id",ManagerMainActivity.ID)
                .addMultipartParameter("kecamatan_id",StepOneFragment.RESIDKECAMATAN)
                .addMultipartParameter("name",StepOneFragment.RESNAME)
                .addMultipartParameter("type",StepOneFragment.RESTYPE)
                .addMultipartParameter("adress",StepOneFragment.RESADRESS)
                .addMultipartParameter("location",StepOneFragment.RESLOCATION)
                .addMultipartParameter("ceo_name",StepTwoFragment.RESCEONAME)
                .addMultipartParameter("ceo_contact",StepTwoFragment.RESCEOCONTACT)
                .addMultipartParameter("vice_name",StepTwoFragment.RESVICENAME)
                .addMultipartParameter("vice_contact",StepTwoFragment.RESVICECONTACT)
                .addMultipartParameter("facilities",RESFACILITIES)
                .setTag("uploadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d(TAG, "bytesUploaded "+bytesUploaded+" totalBytes "+totalBytes);

                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d(TAG, response.toString());

                        messageHandler.closeAlert();

                        try {
                            boolean success = response.getBoolean("success");
                            String msg = response.getString("message");

                            Log.d(TAG, "success "+success);
                            if(success){
                                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
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
                        Log.d(TAG, error.toString());

                    }
                });

    }

    private void updateMosque(){
        messageHandler.alertLoading();

        AndroidNetworking.patch(UrlApi.MOSQUE_CREATE_URL+"/"+ManagerMainActivity.IDMOSQUE)
                .addBodyParameter("id", ManagerMainActivity.IDMOSQUE)
                .addBodyParameter("person_id", ManagerMainActivity.ID)
                .addBodyParameter("kecamatan_id", StepOneFragment.RESIDKECAMATAN)
                .addBodyParameter("name", StepOneFragment.RESNAME)
                .addBodyParameter("type", StepOneFragment.RESTYPE)
                .addBodyParameter("adress", StepOneFragment.RESADRESS)
                .addBodyParameter("location", StepOneFragment.RESLOCATION)
                .addBodyParameter("ceo_name", StepTwoFragment.RESCEONAME)
                .addBodyParameter("ceo_contact", StepTwoFragment.RESCEOCONTACT)
                .addBodyParameter("vice_name", StepTwoFragment.RESVICENAME)
                .addBodyParameter("vice_contact", StepTwoFragment.RESVICECONTACT)
                .addBodyParameter("facilities", StepThreeFragment.RESFACILITIES)
                .addBodyParameter("status", RESSTATUS)
                .addBodyParameter("image", StepOneFragment.RESMYIMAGE)
                .setTag("test")
                .addHeaders("X-CSRF-Token", "74b2cb4106d14be9b8e39920b5465f3ea8b0b4ba")
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response updateMosque "+response.toString());

                        messageHandler.closeAlert();

                        try {
                            boolean success = response.getBoolean("success");
                            String msg = response.getString("message");

                            Log.d(TAG, "success "+success);
                            if(success){
                                alertSuccess(msg);

                            }else {
                                messageHandler.alertError(msg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "updateMosque "+anError.getErrorBody());

                    }
                });
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


    @Override
    public void alertSuccess(String msg) {
        successLoading = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
        successLoading.setTitleText("Berhasil");
        successLoading.setContentText(msg);

        successLoading.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                successLoading.dismissWithAnimation();
                startActivity(new Intent(getContext(), ManagerMainActivity.class));
                getActivity().finish();
            }
        });

        successLoading.show();
    }
}
