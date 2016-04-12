package com.project.ultimatenote;

import android.app.Application;
import android.content.Context;

import com.avos.avoscloud.AVOSCloud;
import com.project.ultimatenote.utils.ToastUtils;

public class MyApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        AVOSCloud.useAVCloudCN();
        AVOSCloud.initialize(this, "acGkoA3JyUn0VFTjDlOdFETM-gzGzoHsz", "DH6yMP9TBoSXd0tH9JiN3iPq");
        ToastUtils.register(this);
    }

    public static Context getInstance(){
        return context;
    }
}
