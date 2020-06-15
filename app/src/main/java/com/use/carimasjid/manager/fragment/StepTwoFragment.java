package com.use.carimasjid.manager.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;
import com.use.carimasjid.R;
import com.use.carimasjid.handler.MessageHandler;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class StepTwoFragment extends Fragment implements BlockingStep, MessageHandler.alertSuccess {

    public static TextInputEditText etCeoName, etCeoContact, etViceName, etViceContact;
    public static String RESCEONAME, RESCEOCONTACT, RESVICENAME, RESVICECONTACT;

    private SweetAlertDialog loadingDialog, successLoading, errorLoading;

    private MessageHandler messageHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(
                R.layout.step_two_fragment, container, false);

            etCeoName = v.findViewById(R.id.et_ceo_name);
            etCeoContact = v.findViewById(R.id.et_ceo_contact);
            etViceName = v.findViewById(R.id.et_vice_name);
            etViceContact = v.findViewById(R.id.et_vice_contact);

            messageHandler = new MessageHandler(getActivity());

        return v;
    }

    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                RESCEONAME = etCeoName.getText().toString();
                RESCEOCONTACT = etCeoContact.getText().toString();
                RESVICENAME = etViceName.getText().toString();
                RESVICECONTACT = etViceContact.getText().toString();

                if(!TextUtils.isEmpty(RESCEONAME) &&!TextUtils.isEmpty(RESCEOCONTACT) &&!TextUtils.isEmpty(RESVICENAME) &&!TextUtils.isEmpty(RESVICECONTACT)){
                    callback.goToNextStep();

                }else {
                    messageHandler.alertError("Silahkan mengisi semua kolom");
                }
                //you can do anythings you want

            }
        }, 0L);//
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {

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

    }
}
