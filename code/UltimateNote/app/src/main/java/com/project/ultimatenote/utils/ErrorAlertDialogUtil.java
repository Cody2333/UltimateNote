package com.project.ultimatenote.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.project.ultimatenote.R;


public class ErrorAlertDialogUtil {

    public static void showErrorDialog(Context ctx, String errorStr,DialogInterface.OnClickListener listener) {
        if (listener!= null){
            new AlertDialog.Builder(ctx)
                    .setTitle(
                            ctx.getResources().getString(
                                    R.string.dialog_error_title))
                    .setMessage(
                            errorStr)
                    .setNegativeButton(android.R.string.ok,
                            listener).show();
        }else{
            new AlertDialog.Builder(ctx)
                    .setTitle(
                            ctx.getResources().getString(
                                    R.string.dialog_error_title))
                    .setMessage(
                            errorStr)
                    .setNegativeButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            }).show();
        }


    }
}
