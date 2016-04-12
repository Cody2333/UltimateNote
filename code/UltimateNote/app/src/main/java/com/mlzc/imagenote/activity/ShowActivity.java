package com.mlzc.imagenote.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.mlzc.imagenote.R;
import com.mlzc.imagenote.entity.Note;
import com.mlzc.imagenote.fragment.NoteFragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ShowActivity extends ActionBarActivity {
    public final static int EDIT_NOTE = 1;
    private long cTime;
    private long rTime;
    private long newTime;
    private int revised = 0;
    private Note note;
    private AsyncTask task;
    private ArrayList<String> tagsArray;
    Context context;
    private Dialog waitDialog;
    @Bind (R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.viewlayout)
    RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_show);
        ButterKnife.bind(this);
        //初始化toolbar
        setSupportActionBar(toolbar);
        //初始化ActionBar
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back_white);
        ab.setDisplayHomeAsUpEnabled(true);

        //初始化float_btn
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActivityEdit(view);
            }
        });

        //init note
        Intent intent = getIntent();
        cTime = intent.getLongExtra(MainActivity.C_TIME, 0);
        rTime = intent.getLongExtra(MainActivity.TIME, 0);
        initNote(rTime);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation shake = AnimationUtils.loadAnimation(ShowActivity.this, R.anim.shake);//加载动画资源文件
                findViewById(R.id.fab).startAnimation(shake); //给组件播放动画效果
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.menu_delete){
            final String[] deleteItem = {"删除本地笔记", "删除云端和本地笔记", "取消"};
            new AlertDialog.Builder(this)
                    .setItems(deleteItem, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    new AlertDialog.Builder(context)
                                            .setTitle("提示")
                                            .setMessage("确认删除本地笔记吗?")
                                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    deleteNote();
                                                    myFinishActivity();
                                                }
                                            })
                                            .setNegativeButton("取消", null).show();
                                    break;
                                case 1:
                                    final AVUser user = AVUser.getCurrentUser();
                                    if (user == null) {
                                        new AlertDialog.Builder(context)
                                                .setTitle("提示")
                                                .setMessage("请先登录")
                                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    } else {
                                        new AlertDialog.Builder(context)
                                                .setTitle("提示")
                                                .setMessage("确认删除云端和本地笔记吗?")
                                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface innerDialog, int which) {
                                                        note.setUserName(user.getEmail());
                                                        waitDialog = ProgressDialog.show(context, "", "删除中", true, true, new DialogInterface.OnCancelListener() {
                                                            @Override
                                                            public void onCancel(DialogInterface dialog) {
                                                                if(task != null && !task.isCancelled())
                                                                    task.cancel(true);
                                                            }
                                                        });
                                                        task = new DeleteTask();
                                                        ((DeleteTask)task).execute();
                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    while(task != null && !task.isCancelled()) {
                                                                        Thread.sleep(1000);
                                                                    }
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                if(waitDialog != null)
                                                                    waitDialog.dismiss();
                                                            }

                                                        }).start();
                                                    }
                                                })
                                                .setNegativeButton("取消", null).show();

                                    }
                                    dialog.dismiss();
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;

                            }
                        }
                    }).show();
        }else if(id == android.R.id.home){
            myFinishActivity();
        }else if(id == R.id.menu_share){
            final String[] cloudItem = {"上传为私人笔记", "上传为公开笔记", "取消"};
            new AlertDialog.Builder(this)
                    .setItems(cloudItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    new AlertDialog.Builder(context)
                                            .setTitle("提示")
                                            .setMessage("上传为私人笔记吗?")
                                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    AVUser user = AVUser.getCurrentUser();
                                                    if (user == null) {
                                                        new AlertDialog.Builder(context)
                                                                .setTitle("提示")
                                                                .setMessage("请先登录")
                                                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                }).show();
                                                    } else {
                                                        note.setUserName(user.getEmail());
                                                        revised = 1;
                                                        waitDialog = ProgressDialog.show(context, "", "上传中", true, true, new DialogInterface.OnCancelListener() {
                                                            @Override
                                                            public void onCancel(DialogInterface dialog) {
                                                                if (task != null && !task.isCancelled())
                                                                    task.cancel(true);
                                                            }
                                                        });
                                                        task = new UploadTask();
                                                        ((UploadTask) task).execute("private");
                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    while (task != null && !task.isCancelled()) {
                                                                        Thread.sleep(1000);
                                                                    }
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                if (waitDialog != null)
                                                                    waitDialog.dismiss();
                                                            }

                                                        }).start();
                                                    }
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                    break;
                                case 1:
                                    new AlertDialog.Builder(context)
                                            .setTitle("提示")
                                            .setMessage("上传为公开笔记吗?")
                                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    AVUser user = AVUser.getCurrentUser();
                                                    if (user == null) {
                                                        new AlertDialog.Builder(context)
                                                                .setTitle("提示")
                                                                .setMessage("请先登录")
                                                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                }).show();
                                                    } else {
                                                        note.setUserName(user.getEmail());
                                                        revised = 1;
                                                        waitDialog = ProgressDialog.show(context, "", "上传中", true, true, new DialogInterface.OnCancelListener() {
                                                            @Override
                                                            public void onCancel(DialogInterface dialog) {
                                                                if (task != null && !task.isCancelled())
                                                                    task.cancel(true);
                                                            }
                                                        });
                                                        task = new UploadTask();
                                                        ((UploadTask) task).execute("public");
                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    while (task != null && !task.isCancelled()) {
                                                                        Thread.sleep(1000);
                                                                    }
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                if (waitDialog != null)
                                                                    waitDialog.dismiss();
                                                            }

                                                        }).start();
                                                    }
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    }).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_NOTE) {
            Long newTime = data.getLongExtra(MainActivity.TIME, 0);
            if(newTime != rTime) {
                rTime = newTime;
                revised = 1;
                initNote(newTime);
            }
        }
    }

    public void onActivityEdit(View view){
        Intent intent=new Intent(this,EditActivity.class);
        intent.putExtra(MainActivity.TIME, rTime);
        intent.putExtra(MainActivity.C_TIME, cTime);
        startActivityForResult(intent, EDIT_NOTE);
    }

    private void initNote(long time){
        try {
            note = new Note();
            note.setReviseTime(time);
            note.setCreateTime(cTime);
            Context ctx = this;
            String filePath = ctx.getFilesDir().getAbsolutePath();

            FileInputStream fis = new FileInputStream(filePath+"/describe/"+time);
            note.setDescribeFilePath(filePath+"/describe/"+time);
            byte[] b2 = new byte[fis.available()];
            fis.read(b2);
            fis.close();
            String describe = new String(b2, "utf-8");
            if(describe.charAt(0) == 'P') {
                note.setCloudNote(true);
                note.setIsPublic(true);
            }
            else if (describe.charAt(0) == 'Y') {
                note.setCloudNote(true);
                note.setIsPublic(false);
            }
            else {
                note.setCloudNote(false);
                note.setIsPublic(false);
            }

            //draw the note
            //clean
            ViewGroup group = (ViewGroup) findViewById(R.id.noteGroup);

            group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation shake = AnimationUtils.loadAnimation(ShowActivity.this, R.anim.shake);//加载动画资源文件
                    findViewById(R.id.fab).startAnimation(shake); //给组件播放动画效果
                }
            });
            while(group.getChildCount() > 1){
                group.removeViewAt(1);
            }

            //draw
            Bitmap bitmap = null;
            String[] counts = describe.split("\n");
            String document = null;

            //title
            ((TextView)findViewById(R.id.text_title)).setText(counts[1]);
            fis = new FileInputStream(filePath+"/document/"+time);
            note.setDocumentPath(filePath+"/document/"+time);

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
            Date date = new Date(rTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(note.getIsPublic() && note.isCloudNote())
                setTime(sdf.format(date) + "  @cloud  P");
            else if(note.isCloudNote())
                setTime(sdf.format(date) + "  @cloud");
            else
                setTime(sdf.format(date));
            //word
            byte[] b = new byte[Integer.parseInt(counts[3])];
            fis.read (b);
            document = new String(b, "utf-8");
            ((TextView)group.getChildAt(0)).setText(document);


            //pic and word
            int pictureCnt = 0;
            ArrayList<String> picturePaths = new ArrayList();
            for(int i= 4; i< counts.length; ++i){
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(filePath + "/image/" + time + "_" + String.valueOf(i - 4), opt);
                picturePaths.add(filePath + "/image/" + time + "_" + String.valueOf(i - 4));
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
                pictureCnt++;
            }
            note.setPicturePaths(picturePaths);
            note.setPictureCnt(pictureCnt);

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            myFinishActivity();
            return false;
        }
        return false;
    }

    private void myFinishActivity(){
        Intent intent = getIntent();
        intent.putExtra(MainActivity.NOTE_REVISED, revised);
        setResult(MainActivity.SHOW_NOTE, intent);
        finish();
    }

    private void deleteNote(){
        try{
            revised = 1;
            Note.deleteNote(rTime, cTime, this.getFilesDir().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class UploadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            newTime = note.saveInCloud(strings[0]);
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(waitDialog!=null){
                waitDialog.dismiss();
            }
            if(newTime == 0){
                //fail
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("上传失败")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }else{
                //success
                if(cTime != newTime) {
                    //same cTime is in cloud, revise it in local
                    //newTime here means cTime, not rTime like other places
                    //and now Note.saveInCloud always return 1
                    //todo
                }
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("上传成功")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    }

    class DeleteTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            newTime = note.deleteInCloud();
            if(newTime != 0)
                deleteNote();
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(waitDialog!=null){
                waitDialog.dismiss();
            }
            if(newTime == 0){
                //fail
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("删除失败")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }else{
                myFinishActivity();
            }
        }
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
}
