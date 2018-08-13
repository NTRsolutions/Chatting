package com.enhabyto.chatting;


import android.content.Context;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NoInternetConnectionAlert {

    private Context context;

    NoInternetConnectionAlert(Context context){
        this.context = context;
    }

    public void DisplayNoInternetConnection(){
        try {

            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("No Internet Access!")
                    .setContentText("No internet connectivity detected. Please make sure you have working internet connection and try again.")
                    .setConfirmText("Okay!")
                    .show();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}