package com.project.ultimatenote.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestEmailVerifyCallback;
import com.avos.avoscloud.SignUpCallback;
import com.project.ultimatenote.R;
import com.project.ultimatenote.utils.ProgressDialogUtil;


public class RegisterActivity extends ActionBarActivity {
    private boolean isProcessing;

    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        isProcessing = false;
        context = this;
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
        ProgressDialogUtil.progressDialogShow(context);
        user.signUpInBackground(new SignUpCallback() {
            public void done(AVException e) {
                ProgressDialogUtil.progressDialogDismiss();
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
