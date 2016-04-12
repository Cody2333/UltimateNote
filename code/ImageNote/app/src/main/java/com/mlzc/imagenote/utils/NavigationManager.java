package com.mlzc.imagenote.utils;

import android.content.Context;
import android.content.Intent;

import com.mlzc.imagenote.activity.LoginActivity;
import com.mlzc.imagenote.activity.MainActivity;

/**
 * Created by cody_local on 2016/4/6.
 */
public class NavigationManager {
    public static void toLoginForResult(Context context,int type){
        context.startActivity(new Intent(context, LoginActivity.class));

    }

    public static void toMain(Context context){
        context.startActivity(new Intent(context, MainActivity.class));

    }
}
