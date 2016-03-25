package com.mlzc.imagenote;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by c on 2015/9/9.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.useAVCloudCN();
        AVOSCloud.initialize(this, "acGkoA3JyUn0VFTjDlOdFETM-gzGzoHsz", "DH6yMP9TBoSXd0tH9JiN3iPq");
    }
}
