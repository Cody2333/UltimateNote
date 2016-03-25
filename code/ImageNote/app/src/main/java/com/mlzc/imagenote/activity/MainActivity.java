package com.mlzc.imagenote.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.mlzc.imagenote.R;
import com.mlzc.imagenote.entity.Note;
import com.mlzc.imagenote.fragment.CloudNoteFragment;
import com.mlzc.imagenote.fragment.NoteFragment;
import com.mlzc.imagenote.fragment.OtherNoteFragment;


public class MainActivity extends AppCompatActivity {
    protected static final int GET_LOGIN_INFO = 1;
    protected static final int GET_SETTING_INFO = 3;
    protected static final int CREATE_NEW_NOTE = 2;
    public static final int SHOW_NOTE = 3;
    public static final int SHOW_OTHER_NOTE = 4;
    private static final int OTHER_NOTE = 0;
    public static final String NOTE_REVISED = "NOTE_REVISED";
    public static final String TIME = "TIME";
    public static final String C_TIME = "C_TIME";
    public static final String USER_NAME = "USER_NAME";
    private Dialog waitDialog;
    private List<Note> notes;
    private List<Note> otherNotes;
    private NoteFragment selfNoteFragment;
    private CloudNoteFragment cloudNoteFragment;
    private static Adapter mAdapter;
    private AsyncTask task;
    private static ViewPager mViewPager;
    TextView navHeadText;
    DrawerLayout mDrawerLayout;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
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

        //navHeadText是左侧导航栏显示用户名的TextView
        navHeadText = (TextView) findViewById(R.id.nav_header_text);


        //初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化ActionBar
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_show_bar_white);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //初始化navigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        //初始化float_btn
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActivityAddNote(view);
            }
        });
        //fab.setRippleColor(Color.parseColor("#ffab00"));

        //获取用户登录信息
        SharedPreferences settings = this.getPreferences(Activity.MODE_PRIVATE);

        boolean isLogin = false;
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            isLogin = true;
        }

        //如果用户名为空，设置navHeadText为点击登录，并设置跳转到LoginActivity的监听器
        if (!isLogin) {
            navHeadText.setText("点击登录");
            navHeadText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    turnToLogin();
                }
            });
        }//如果用户名不为空，则设置navHeadText为用户名
        else {
            navHeadText.setText(currentUser.getUsername());
        }
        mViewPager = (ViewPager) findViewById(R.id.vp);
        setFragment();
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
                Intent intent = new Intent(this,SearchableActivity.class);
                startActivityForResult(intent, SHOW_OTHER_NOTE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_synchronize:
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
                            case R.id.nav_setting:
                                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                                startActivityForResult(intent, GET_SETTING_INFO);
                                break;
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
                    navHeadText.setOnClickListener(null);
                }
            }
        } else if (requestCode == GET_SETTING_INFO) {
            finish();
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
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
            otherNotes = new ArrayList<>();
            selfNoteFragment = new NoteFragment();
            cloudNoteFragment = new CloudNoteFragment();

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
                        notes.add(0, note);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            selfNoteFragment.setNoteList(notes);
            cloudNoteFragment.setNoteList(otherNotes);
            mAdapter.addFragment(selfNoteFragment, "我的笔记");
            mAdapter.addFragment(cloudNoteFragment, "动态");
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
}


