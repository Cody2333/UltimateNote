package com.mlzc.imagenote.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.mlzc.imagenote.R;

public class LoginActivity extends ActionBarActivity {
    public static final int LOGIN_SUCCESS=1;
    public static final int LOGIN_FAILE=2;
    public static final int GET_LOGIN_INFO=1;
    public static final String USER_EMAIL="USER_EMAIL";
    private String email;
    private boolean isProcessing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isProcessing = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
                .setTitle("登录确认")
                .setMessage("登录成功")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent();
                        intent.putExtra(USER_EMAIL, email);
                        setResult(GET_LOGIN_INFO, intent);
                        finish();
                    }
                }).show();
        isProcessing = false;
    }
    private void fail(){
        new AlertDialog.Builder(this)
                .setTitle("登录失败")
                .setMessage("请检查网络设置\n或您的用户名及密码")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
        isProcessing = false;
    }
    private  void sendMessage(){
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
    private  void errorMessage(){
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
    public void onActivityLogin(View view){
        if(isProcessing)
            return;
        isProcessing = true;
        EditText inputEmail;
        inputEmail = (EditText)findViewById(R.id.email_input);
        email = inputEmail.getText().toString();
        EditText inputPassword;
        inputPassword= (EditText)findViewById(R.id.password_input);
        String password = inputPassword.getText().toString();
        if(email.equals("") || password.equals("")) {
            fail();
        } else {
            AVUser.logInInBackground(email, password, new LogInCallback() {
                public void done(AVUser user, AVException e) {
                    if (e == null) {
                        if (user != null) {
                            success(); // 登录成功
                        }
                    } else {
                        fail(); // 登录失败
                    }
                }
            });
        }
    }

    public void onActivityForgetPassword(View view){
        if(isProcessing)
            return;
        isProcessing = true;
        EditText inputEmail;
        inputEmail = (EditText)findViewById(R.id.email_input);
        String email = inputEmail.getText().toString();
        AVUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            public void done(AVException e) {
                if (e == null) {
                    sendMessage(); // 已发送一份重置密码的指令到用户的邮箱
                } else {
                    errorMessage(); // 重置密码出错。
                }
            }
        });

    }

    public void onActivityRegister(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
