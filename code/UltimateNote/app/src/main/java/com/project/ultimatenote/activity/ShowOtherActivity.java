package com.project.ultimatenote.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.project.ultimatenote.R;
import com.project.ultimatenote.entity.Note;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ShowOtherActivity extends ActionBarActivity {
    private long cTime;                    //创建时间
    private String username;               //用户名字
    private String title;                  //题目
    private ArrayList<String> tagsArray;   //标题
    private Context context;
    private Dialog waitDialog;             //等待对话
    private AsyncTask task;                //任务
    private int revised;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_show_other);

        //初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //初始化ActionBar
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back_white);
        ab.setDisplayHomeAsUpEnabled(true);

        //init note
        Intent intent = getIntent();
        cTime = intent.getLongExtra(MainActivity.C_TIME, 0);
        username = intent.getStringExtra(MainActivity.USER_NAME);

        waitDialog = ProgressDialog.show(context, "", "获取中", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (task != null && !task.isCancelled())
                    task.cancel(true);
            }
        });
        task = new DownloadNoteTask();
        ((DownloadNoteTask)task).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_other, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == android.R.id.home){
            Intent intent = getIntent();
            intent.putExtra(MainActivity.NOTE_REVISED, revised);
            setResult(MainActivity.SHOW_OTHER_NOTE, intent);
            finish();
        }else if(id == R.id.save_to_local){
            if(Note.saveOtherToLocal(context.getFilesDir().getAbsolutePath(), context.getCacheDir().getAbsolutePath(), title) != 0){
                revised = 1;
                new AlertDialog.Builder(context)
                        .setMessage("保存成功")
                        .setPositiveButton("确认", null).show();
            }else{
                new AlertDialog.Builder(context)
                        .setMessage("保存失败")
                        .setPositiveButton("返回", null).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private int initNote(long cTime, String username){
        String cachePath = context.getCacheDir().getAbsolutePath();
        String describe = null;
        String document = null;

        try {
            FileInputStream fis = new FileInputStream(cachePath + "/describe");
            if(fis.available() != 0) {
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                describe = new String(b, "utf-8");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //draw
        Bitmap bitmap = null;
        String[] counts = describe.split("\n");

        //title
        title = counts[1];
        ((TextView)findViewById(R.id.text_title)).setText(title);
        //tag
        String[] tags = counts[2].split(" ");
        if(tagsArray == null)
            tagsArray = new ArrayList<>();
        else
            tagsArray.clear();
        for (int i = 0; i < tags.length; ++i) {
            if(!tags[i].equals(""))
                tagsArray.add(tags[i]);
        }
        createTag(tagsArray);

        //time
        Date date = new Date(cTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        setTime(sdf.format(date) + "  " + username);

        //word
        ViewGroup group = (ViewGroup) findViewById(R.id.noteGroup);
        while(group.getChildCount() > 1){
            group.removeViewAt(1);
        }

        //pic and word
        try {
            FileInputStream fis = new FileInputStream(cachePath + "/document");

            //first paragraph
            byte[] b = new byte[Integer.parseInt(counts[3])];
            fis.read(b);
            document = new String(b, "utf-8");
            ((TextView)group.getChildAt(0)).setText(document);

            for(int i= 4; i< counts.length; ++i){
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(cachePath + "/image_" + String.valueOf(i - 4), opt);
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setImageBitmap(bitmap);
                imageView.setAdjustViewBounds(true);
                group.addView(imageView);


                //textView
                b = new byte[Integer.parseInt(counts[i])];
                fis.read(b, 0, b.length);
                document = new String(b, "utf-8");

                TextView textView = new TextView(this);
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setTextSize(20);
                textView.setBackgroundDrawable(null);
                textView.setMinEms(2);
                textView.setText(document);
                textView.setTextColor(Color.rgb(0, 0, 0));

                group.addView(textView);
            }

            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    protected void createTag(ArrayList<String> tag) {
        TableLayout tableLayout = (TableLayout)findViewById(R.id.tableLayout);
        int cnt = tableLayout.getChildCount();
        if(cnt != 0)
            tableLayout.removeViewAt(cnt-1);

        if(tag.size() == 0)
            return;

        LinearLayout tagRow=new LinearLayout(this);
        tableLayout.addView(tagRow);
        int number = tag.size();
        int now = 0;
        int row = 0;
        int line = 0;
        while(now<number)
        {
            TextView textView = new TextView(this);

            textView.setText(tag.get(now));
            textView.setTextColor(Color.parseColor("#616161"));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            Drawable drawable = this.getResources().getDrawable(R.drawable.ic_tag_small_amber);
            drawable.setBounds(0, 0, dip2px(this, 16), dip2px(this, 16));

            textView.setCompoundDrawables(drawable, null, null, null);
            textView.setCompoundDrawablePadding(dip2px(this, 8));

            textView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagRow.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int tag_width;
            if(line!=0) tag_width = textView.getMeasuredWidth()+dip2px(this,16);
            else tag_width = textView.getMeasuredWidth();
            int table_width = dip2px(this,px2dip(this,this.getResources().getDisplayMetrics().widthPixels)-32);
            int now_width = tagRow.getMeasuredWidth();

            if(now_width+tag_width>table_width) {
                tagRow = new LinearLayout(this);
                tableLayout.addView(tagRow);
                tagRow.setPadding(0, dip2px(this, 8),0,0);
                line=0;
                row++;
            }

            if(line!=0)
                textView.setPadding(dip2px(this,16),0,0,0);

            tagRow.addView(textView);
            line++;
            now++;
        }
    }

    public static int dip2px(Context context, float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale + 0.5f);
    }

    protected void setTime(String time) {
        TextView editTime = (TextView)findViewById(R.id.text_time);
        editTime.setText(time);
    }

    class DownloadNoteTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Note.getOtherNote(cTime, username, context.getCacheDir().getAbsolutePath());
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(waitDialog!=null){
                waitDialog.dismiss();
            }
            handler.sendEmptyMessage(0);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            initNote(cTime, username);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = getIntent();
            intent.putExtra(MainActivity.NOTE_REVISED, revised);
            setResult(MainActivity.SHOW_OTHER_NOTE, intent);
            finish();
            return false;
        }
        return false;
    }
}
