package com.use.carimasjid.handler;

import android.content.Context;
import android.graphics.Color;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MessageHandler {

    private SweetAlertDialog loadingDialog, errorLoading;

    private Context ctx;

    public MessageHandler(Context ctx) {
        this.ctx = ctx;
    }

    public void closeAlert(){
        loadingDialog.dismissWithAnimation();
    }
    public void alertLoading() {
        loadingDialog = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.getProgressHelper().setBarColor(Color.parseColor("#52993F"));
        loadingDialog.setTitleText("Loading");
        loadingDialog.setCancelable(true);
        loadingDialog.show();
    }

    public void alertError(String msg) {
        errorLoading = new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE);
        errorLoading.setTitleText("Terjadi Kesalahan !");
        errorLoading.setContentText(msg);
        errorLoading.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {

                errorLoading.dismissWithAnimation();
            }
        });
        errorLoading.show();
    }

    public interface alertSuccess{
        void alertSuccess(String msg);
    }
}
