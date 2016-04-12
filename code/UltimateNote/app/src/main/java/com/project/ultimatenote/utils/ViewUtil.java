package com.project.ultimatenote.utils;

import android.util.TypedValue;

import com.project.ultimatenote.MyApplication;


public class ViewUtil {

    public static int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MyApplication.getInstance().getResources().getDisplayMetrics());
    }

    public static int sp2px(int sp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, MyApplication.getInstance().getResources().getDisplayMetrics());
    }

}
