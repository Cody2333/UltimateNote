package com.project.ultimatenote.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.project.ultimatenote.MyApplication;
import com.project.ultimatenote.R;
import com.project.ultimatenote.entity.Note;
import com.project.ultimatenote.utils.ImageUtil;
import com.project.ultimatenote.utils.ProgressDialogUtil;
import com.project.ultimatenote.utils.ToastUtils;
import com.project.ultimatenote.utils.ViewUtil;
import com.project.ultimatenote.views.CircleTransformation;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingActivity extends ActionBarActivity {

    TextView navHeadText;
    ImageView headerImage;
    DrawerLayout mDrawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    FloatingActionButton fab;
    Context context;
    ViewPager mViewPager;
    Boolean isProcessing;
    @Bind(R.id.a1)
    TextView a1;
    @Bind(R.id.a2)
    TextView a2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isProcessing = false;
        context = this;
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back_white);
        ab.setDisplayHomeAsUpEnabled(true);


//        findViewById(R.id.nav_icon_new).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(SettingActivity.this, EditProfileActivity.class));
//            }
//        });

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogout(v);
            }
        });

        initView();

        toolbar.setTitle("Setting");

        a1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showShort("清除缓存成功");
            }
        });
        a2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActivityForgetPassword();
            }
        });

    }

    public void onActivityForgetPassword() {
        if (isProcessing)
            return;
        isProcessing = true;
        if (AVUser.getCurrentUser() != null) {
            String email = AVUser.getCurrentUser().getUsername();
            ProgressDialogUtil.progressDialogShow(context);
            AVUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                public void done(AVException e) {
                    ProgressDialogUtil.progressDialogDismiss();
                    if (e == null) {
                        sendMessage(); // 已发送一份重置密码的指令到用户的邮箱
                    } else {
                        errorMessage(); // 重置密码出错。
                    }
                }
            });
        }else {
            ToastUtils.showShort("请先登录");
        }


    }

    private void sendMessage() {
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("邮件已发送")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
        isProcessing = false;
    }

    private void errorMessage() {
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("重置密码出错")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
        isProcessing = false;
    }


    private void initView() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        headerImage = (ImageView) findViewById(R.id.nav_icon_new);
        navHeadText = (TextView) findViewById(R.id.nav_email_new);
        setSupportActionBar(toolbar);

        AVUser currentUser = AVUser.getCurrentUser();
        //如果用户名为空，设置navHeadText为点击登录，并设置跳转到LoginActivity的监听器
        if (currentUser == null) {
            navHeadText.setText("点击登录");
            navHeadText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    turnToLogin();
                }
            });
        } else {
            //设置navHeadText为用户名
            navHeadText.setText(currentUser.getUsername());

            //异步获取头像图片并调整分辨率
            Picasso picasso = new Picasso
                    .Builder(MyApplication.getInstance())
                    .downloader(new OkHttpDownloader(new OkHttpClient()))
                    .build();
            picasso.load(ImageUtil.getAvatarUrl(currentUser.getUsername(), 100))
                    .config(Bitmap.Config.RGB_565)
                    .resize(ViewUtil.dp2px(100), ViewUtil.dp2px(100))
                    .centerCrop()
                    .transform(new CircleTransformation())
                    .into(headerImage);
        }
    }

    private void turnToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onLogout(View view) {
        AVUser avUser = AVUser.getCurrentUser();
        if (avUser != null) {
            Note.removeLocalCloudNotes(this.getFilesDir().getAbsolutePath());
            avUser.logOut();
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("退出登录完成")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }


}
