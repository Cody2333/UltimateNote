package com.mlzc.imagenote.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestEmailVerifyCallback;
import com.avos.avoscloud.SignUpCallback;
import com.mlzc.imagenote.R;

/**
 * Created by Administrator on 2015-7-17.
 */
public class RegisterActivity extends ActionBarActivity {
    private boolean isProcessing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        isProcessing = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void success(){
        new AlertDialog.Builder(this)
                .setTitle("注册确认")
                .setMessage("请前往邮箱验证")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
        isProcessing = false;
    }

    private void fail(){
        new AlertDialog.Builder(this)
                .setTitle("注册确认")
                .setMessage("注册失败，请检查网络设置\n或该邮箱已被注册")
                .setPositiveButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO donothing
                        return;
                    }
                }).show();
        isProcessing = false;
    }
    private void pwdNotEqual(){
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("两次输入的密码不同！")
                .setPositiveButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO donothing
                        return;
                    }
                }).show();
        isProcessing = false;
    }

    public void onActivityRegister(View view){
        if(isProcessing)
            return;
        isProcessing = true;
        EditText inputEmail;
        inputEmail = (EditText)findViewById(R.id.email_input);
        final String email = inputEmail.getText().toString();

        EditText inputPassword= (EditText)findViewById(R.id.password_input);
        String password = inputPassword.getText().toString();

        EditText inputConfirmPassword = (EditText)findViewById(R.id.password_confirm);
        String confirmPassword = inputConfirmPassword.getText().toString();

        if(!password.equals(confirmPassword)) {
            pwdNotEqual();
            return;
        }

        AVUser user = new AVUser();
        user.setUsername(email);
        user.setPassword(password);
        user.setEmail(email);
        user.signUpInBackground(new SignUpCallback() {
            public void done(AVException e) {
                if (e == null) {
                    AVUser.requestEmailVerfiyInBackground(email, new RequestEmailVerifyCallback() {
                        @Override
                        public void done(AVException e) {
                            success(); // successfully
                        }
                    });
                } else {
                   fail(); // failed
                }
            }
        });
    }
}
