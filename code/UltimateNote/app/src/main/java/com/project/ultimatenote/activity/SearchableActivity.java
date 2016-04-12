package com.project.ultimatenote.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.project.ultimatenote.R;
import com.project.ultimatenote.entity.Note;
import com.project.ultimatenote.fragment.HeaderFooterViewAdapter;
import com.project.ultimatenote.fragment.OnBottomListener;
import com.project.ultimatenote.fragment.OnRcvScrollListener;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends ActionBarActivity {

    private List<Note> notes;
    private RecyclerView recyclerView;
    private HeaderFooterViewAdapter mAdapter;
    private SearchView searchView;
    private ArrayList<Note> newData;
    private ArrayList<Note> newDataRefresh;
    private int loading;
    private boolean isRefreshing;
    private View footerView;
    private String loadStr;
    private static final int REFRESH = 0;
    private static final int LOAD_NEW = 1;
    private int revised;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        newData = new ArrayList<>();
        newDataRefresh = new ArrayList<>();
        recyclerView = (RecyclerView)findViewById(R.id.result_rcv);
        notes = new ArrayList<>();
        mAdapter = new HeaderFooterViewAdapter(this,notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new OnRcvScrollListener(OnRcvScrollListener.LAYOUT_MANAGER_TYPE.LINEAR, new OnBottomListener() {
            @Override
            public void onBottom() {
                if (!isRefreshing) {
                    mAdapter.addFooter(footerView);
                    isRefreshing = true;
                    onLoadMoreData(loadStr);
                }
            }
        }));
        footerView = LayoutInflater.from(this).inflate(R.layout.listview_footer, null);
        isRefreshing = false;
        initActionBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.SHOW_OTHER_NOTE) {
            if(data != null){
                revised = data.getIntExtra(MainActivity.NOTE_REVISED, 0);
            }
        }
    }

    private void initActionBar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //初始化ActionBar
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back_white);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true);
        View mView = LayoutInflater.from(this).inflate(R.layout.custom_search_view,null);
        ab.setCustomView(mView,new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        searchView = (SearchView)mView.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!isRefreshing) {
                    isRefreshing = true;
                    loadStr = query;
                    search(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /*if (!TextUtils.isEmpty(newText)) {
                    if (!isRefreshing) {
                        isRefreshing = true;
                        search(newText);
                    }
                }*/
                return false;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_searchable, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Intent intent = getIntent();
            intent.putExtra(MainActivity.NOTE_REVISED, revised);
            setResult(MainActivity.SHOW_OTHER_NOTE, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onLoadMoreData(String str) {
        //需要异步加载，要开个runnable或者asyncTask
        loading = 1;
        newData.clear();
        LoadNewDataTask task = new LoadNewDataTask();
        task.execute(str);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (loading == 1) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(LOAD_NEW);
            }
        }).start();
    }


    private void search(String queryStr){
        loading = 1;
        newDataRefresh.clear();
        RefreshDataTask task = new RefreshDataTask();
        task.execute(queryStr);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (loading == 1) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(REFRESH);
            }
        }).start();
    }

    class RefreshDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            AVQuery<AVObject> query = new AVQuery<>("Notes");
            AVUser user = AVUser.getCurrentUser();
            if (user != null)
                query.whereNotEqualTo("username", user.getUsername());
            query.whereEqualTo("isPublic", true);
            query.whereContains("title", strings[0]);
            query.setLimit(10);
            List<AVObject> avObjects = null;
            try {
                avObjects = query.find();
                if (avObjects != null) {
                    for (int i = 0; i < avObjects.size(); ++i) {
                        AVObject avObject = avObjects.get(i);
                        Note note = new Note();
                        note.setCreateTime(Long.parseLong(avObject.getString("createTime")));
                        note.setTitle(avObject.getString("title"));
                        note.setUserName(avObject.getString("username"));
                        newDataRefresh.add(note);
                    }
                }
            } catch (AVException e) {
                e.printStackTrace();
            }
            loading = 0;
            return "";
        }
    }

    class LoadNewDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            AVQuery<AVObject> query = new AVQuery<>("Notes");
            AVUser user = AVUser.getCurrentUser();
            if (user != null)
                query.whereNotEqualTo("username", user.getUsername());
            query.whereEqualTo("isPublic", true);
            query.whereContains("title", strings[0]);
            query.setLimit(10);
            query.setSkip(notes.size());
            List<AVObject> avObjects = null;
            try {
                avObjects = query.find();
                if (avObjects != null) {
                    for (int i = 0; i < avObjects.size(); ++i) {
                        AVObject avObject = avObjects.get(i);
                        Note note = new Note();
                        note.setCreateTime(Long.parseLong(avObject.getString("createTime")));
                        note.setTitle(avObject.getString("title"));
                        note.setUserName(avObject.getString("username"));
                        newData.add(note);
                    }
                }
            } catch (AVException e) {
                e.printStackTrace();
            }
            loading = 0;
            return "";
        }

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case REFRESH:
                    if(newDataRefresh.size() != 0) {
                        notes.clear();
                        notes.addAll(newDataRefresh);
                        mAdapter.notifyDataSetChanged();
                    }
                    isRefreshing = false;
                    break;
                case LOAD_NEW:
                    if(newData.size() != 0) {
                        mAdapter.addDatas(newData);
                    }
                    mAdapter.removeFooter(footerView);
                    isRefreshing = false;
                    break;
                default:
                    break;
            }
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
