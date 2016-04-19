package com.project.ultimatenote.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.project.ultimatenote.MyApplication;
import com.project.ultimatenote.R;
import com.project.ultimatenote.entity.Note;
import com.project.ultimatenote.fragment.NoteFragment;
import com.project.ultimatenote.utils.ImageUtil;
import com.project.ultimatenote.utils.ViewUtil;
import com.project.ultimatenote.views.CircleTransformation;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    protected static final int GET_LOGIN_INFO = 1;
    protected static final int GET_SETTING_INFO = 5;
    protected static final int CREATE_NEW_NOTE = 2;
    public static final int SHOW_NOTE = 3;
    public static final int SHOW_OTHER_NOTE = 4;
    public static final String NOTE_REVISED = "NOTE_REVISED";
    public static final String TIME = "TIME";
    public static final String C_TIME = "C_TIME";
    public static final String USER_NAME = "USER_NAME";
    private Dialog waitDialog;
    private List<Note> notes;
    private NoteFragment selfNoteFragment;
    private static Adapter mAdapter;
    private AsyncTask task;
    ViewPager mViewPager;
    TextView navHeadText;
    ImageView headerImage;
    DrawerLayout mDrawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    FloatingActionButton fab;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initForFirstRun();
        initView();
        initListener();
        setFragment();

        findViewById(R.id.nav_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });

    }
    private void initListener() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActivityAddNote(view);
            }
        });
    }

    private void initView() {

        mViewPager = (ViewPager)findViewById(R.id.vp);
         mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        fab=(FloatingActionButton)findViewById(R.id.fab);
        headerImage = (ImageView) findViewById(R.id.nav_icon);
        navHeadText =(TextView)findViewById(R.id.nav_email);
        setSupportActionBar(toolbar);
        //初始化ActionBar
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_show_bar_white);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        setupDrawerContent(navigationView);
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

    private void initForFirstRun() {
        //create directory
        String filePath = this.getFilesDir().getAbsolutePath();
        File dir = new File(filePath + "/noteList");
        if (!dir.exists()) {
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dir = new File(filePath + "/image/");
        if (!dir.exists())
            dir.mkdirs();
        dir = new File(filePath + "/describe/");
        if (!dir.exists())
            dir.mkdirs();
        dir = new File(filePath + "/document/");
        if (!dir.exists())
            dir.mkdirs();
        dir = new File(filePath + "/tesseract/tessdata/");
        if (!dir.exists())
            dir.mkdirs();

        //extract resource files
        File checkDir = new File(filePath + "/tesseract/checkDir/");
        if (!checkDir.exists()) {
            waitDialog = ProgressDialog.show(this, "", "资源文件解压中", true, false);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(filePath);
            //to ensure file extracting succeed
            checkDir.mkdirs();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            //点击home按钮弹出侧边栏
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_search:
                //TODO
                //Intent intent = new Intent(this, SearchableActivity.class);
                //startActivityForResult(intent, SHOW_OTHER_NOTE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //这部分是点击主界面左上方按钮后弹出的菜单的事件
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_synchronize:     //synchronize同步界面
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
                                    waitDialog = ProgressDialog.show(context, "", "同步中", true, true, new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            if (task != null && !task.isCancelled())
                                                task.cancel(true);
                                        }
                                    });
                                    task = new DownloadNoteTask();
                                    ((DownloadNoteTask) task).execute(context.getFilesDir().getAbsolutePath(), user.getUsername());
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
                                        }

                                    }).start();
                                }
                                break;
                            case R.id.nav_settings:     //弹出用户界面
                                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                                startActivity(intent);
                                break;

                            case R.id.nav_about:
                                Intent intent1 = new Intent(MainActivity.this, TestCascadeActivity.class);
                                startActivity(intent1);
                                break;

                            case R.id.nav_logout:
                                AVUser.logOut();
                                navHeadText.setText("点击登录");
                                navHeadText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        turnToLogin();
                                    }
                                });
                                turnToLogin();

                            default:
                                break;

                        }

                        return true;
                    }
                });
    }

    private void turnToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, GET_LOGIN_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //获取登录结果，如果失败则不变，如果成功修改navHeadText
        if (requestCode == GET_LOGIN_INFO) {
            if (resultCode == LoginActivity.LOGIN_SUCCESS) {
                String userEmail = data.getStringExtra("USER_EMAIL");
                if (userEmail != null) {
                    navHeadText.setText(userEmail);
                    navHeadText.setOnClickListener(null);   //修改,点击邮箱或者图片都链接到userPage
                }
            }
        } else if (requestCode == GET_SETTING_INFO) {

        } else if (requestCode == CREATE_NEW_NOTE) {
            finish();
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            long retTime = data.getLongExtra(TIME, 0);
            long retCTime = data.getLongExtra(C_TIME, 0);

            //enter show activity
            if (retTime != 0) {
                Intent showIntent = new Intent(this, ShowActivity.class);
                showIntent.putExtra(TIME, retTime);
                showIntent.putExtra(C_TIME, retCTime);
                startActivityForResult(showIntent, MainActivity.SHOW_NOTE);
            }

        } else if (requestCode == SHOW_NOTE || requestCode == SHOW_OTHER_NOTE) {
            if (data != null) {
                int revised = data.getIntExtra(NOTE_REVISED, 0);
                if (revised != 0) {
                    try {
                        notes.clear();
                        FileInputStream fis = this.openFileInput("noteList");
                        if (fis != null && fis.available() != 0) {
                            byte[] b = new byte[fis.available()];
                            fis.read(b);
                            fis.close();
                            String[] notesStr = new String(b, "utf-8").split("\n");
                            for (int i = 0; i < notesStr.length; i += 2) {
                                Note note = new Note();
                                String crTime = notesStr[i];
                                int pos = crTime.indexOf("_");
                                String cTime = crTime.substring(0, pos);
                                String rTime = crTime.substring(pos + 1);
                                note.setCreateTime(Long.parseLong(cTime));
                                note.setReviseTime(Long.parseLong(rTime));
                                note.setDescribeFilePath(this.getFilesDir().getAbsolutePath() + "/describe/" + rTime);
                                note.setDocumentPath(this.getFilesDir().getAbsolutePath() + "/document/" + rTime);
                                ArrayList<String> picturePaths = new ArrayList<>();
                                picturePaths.add(this.getFilesDir().getAbsolutePath() + "/image/" + rTime + "_0");
                                note.setPicturePaths(picturePaths);
                                note.setTitle(notesStr[i + 1]);
                                FileInputStream fis2 = new FileInputStream(note.getDescribeFilePath());
                                byte[] b2 = new byte[fis2.available()];
                                fis2.read(b2);
                                fis2.close();
                                String describe = new String(b2, "utf-8");
                                if (describe.charAt(0) == 'P') {
                                    note.setCloudNote(true);
                                    note.setIsPublic(true);
                                } else if (describe.charAt(0) == 'Y') {
                                    note.setCloudNote(true);
                                    note.setIsPublic(false);
                                } else {
                                    note.setCloudNote(false);
                                    note.setIsPublic(false);
                                }
                                notes.add(0, note);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int pos = mViewPager.getCurrentItem();
                    mViewPager.setAdapter(mAdapter);
                    mViewPager.setCurrentItem(pos);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onActivityAddNote(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        startActivityForResult(intent, CREATE_NEW_NOTE);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    private void setFragment() {
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.vp);

        mTabLayout.setTabTextColors(Color.GRAY, Color.GRAY);//设置文本在选中和为选中时候的颜色
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        if (mViewPager != null) {
            mAdapter = new Adapter(getSupportFragmentManager());
            notes = new ArrayList<>();
            selfNoteFragment = new NoteFragment();

            //get note list of self
            try {
                FileInputStream fis = this.openFileInput("noteList");
                if (fis != null && fis.available() != 0) {
                    byte[] b = new byte[fis.available()];
                    fis.read(b);
                    fis.close();
                    String[] notesStr = new String(b, "utf-8").split("\n");
                    for (int i = 0; i < notesStr.length; i += 2) {
                        Note note = new Note();
                        String crTime = notesStr[i];
                        int pos = crTime.indexOf("_");
                        String cTime = crTime.substring(0, pos);
                        String rTime = crTime.substring(pos + 1);
                        note.setCreateTime(Long.parseLong(cTime));
                        note.setReviseTime(Long.parseLong(rTime));
                        note.setDescribeFilePath(this.getFilesDir().getAbsolutePath() + "/describe/" + rTime);
                        note.setDocumentPath(this.getFilesDir().getAbsolutePath() + "/document/" + rTime);
                        ArrayList<String> picturePaths = new ArrayList<>();
                        picturePaths.add(this.getFilesDir().getAbsolutePath() + "/image/" + rTime + "_0");
                        note.setPicturePaths(picturePaths);
                        note.setTitle(notesStr[i + 1]);
                        FileInputStream fis2 = new FileInputStream(note.getDescribeFilePath());
                        byte[] b2 = new byte[fis2.available()];
                        fis2.read(b2);
                        fis2.close();
                        String describe = new String(b2, "utf-8");
                        if (describe.charAt(0) == 'P') {
                            note.setCloudNote(true);
                            note.setIsPublic(true);
                        } else if (describe.charAt(0) == 'Y') {
                            note.setCloudNote(true);
                            note.setIsPublic(false);
                        } else {
                            note.setCloudNote(false);
                            note.setIsPublic(false);
                        }
                        notes.add(0, note);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            selfNoteFragment.setNoteList(notes);
            mAdapter.addFragment(selfNoteFragment, "我的笔记");
            mViewPager.setAdapter(mAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
        }
    }

    private void copyFile(String filename, String destPath) {
        try {
            //destPath should ended with "/"
            //will not copy if file exists
            if (!(new File(destPath + filename)).exists()) {
                InputStream is = this.getResources().getAssets()
                        .open(filename);
                FileOutputStream fos = new FileOutputStream(destPath + filename);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String filePath = strings[0];
            copyFile("chi_sim.traineddata", filePath + "/tesseract/tessdata/");
            copyFile("eng.traineddata", filePath + "/tesseract/tessdata/");
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (waitDialog != null) {
                waitDialog.dismiss();
            }
        }
    }

    class DownloadNoteTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Note.downloadFromCloud(strings[0], strings[1]);
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (waitDialog != null) {
                waitDialog.dismiss();
            }
            finish();
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();      //调用双击退出函数
        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            this.finish();
            System.exit(0);
            //MyApplication.getInstance().exit();
        }
    }
}


