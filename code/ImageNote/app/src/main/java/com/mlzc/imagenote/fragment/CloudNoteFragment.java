package com.mlzc.imagenote.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.mlzc.imagenote.R;
import com.mlzc.imagenote.activity.MainActivity;
import com.mlzc.imagenote.entity.Note;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by myc on 2015/9/9.
 */
public class CloudNoteFragment extends Fragment {
    private List<Note> notes;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private boolean isRefreshing;
    private HeaderFooterViewAdapter mAdapter;
    private View footerView;
    private int loading;
    private ArrayList<Note> newData;
    private ArrayList<Note> newDataRefresh;
    private static final int REFRESH = 0;
    private static final int LOAD_NEW = 1;

    public void setNoteList(List<Note> noteList) {
        notes = noteList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View content = inflater.inflate(
                R.layout.note_swipe_refresh_fragment, container, false);
        isRefreshing = false;
        newData = new ArrayList<>();
        newDataRefresh = new ArrayList<>();
        initView(content);
        return content;
    }

    private void initView(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            if (!isRefreshing) {
                isRefreshing = true;
                onRefreshData();
            }
            }
        });
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setupRecyclerView(recyclerView);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mAdapter = new HeaderFooterViewAdapter(getActivity(), notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new OnRcvScrollListener(OnRcvScrollListener.LAYOUT_MANAGER_TYPE.LINEAR, new OnBottomListener() {
            @Override
            public void onBottom() {
            if (!isRefreshing) {
                mAdapter.addFooter(footerView);
                isRefreshing = true;
                onLoadMoreData();
            }
            }
        }));
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.listview_footer, null);

    }


    private void onLoadMoreData() {
        //需要异步加载，要开个runnable或者asyncTask
        loading = 1;
        newData.clear();
        LoadNewDataTask task = new LoadNewDataTask();
        task.execute();

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

    private void onRefreshData() {
        swipeRefreshLayout.setRefreshing(true);//播放刷新动画
        newDataRefresh.clear();
        loading = 1;
        RefreshDataTask task = new RefreshDataTask();
        task.execute();

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


    class LoadNewDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            AVQuery<AVObject> query = new AVQuery<>("Notes");
            AVUser user = AVUser.getCurrentUser();
            if (user != null)
                query.whereNotEqualTo("username", user.getUsername());
            query.whereEqualTo("isPublic", true);
            query.orderByDescending("createTime");
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

    class RefreshDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            AVQuery<AVObject> query = new AVQuery<>("Notes");
            AVUser user = AVUser.getCurrentUser();
            if (user != null)
                query.whereNotEqualTo("username", user.getUsername());
            query.whereEqualTo("isPublic", true);
            query.orderByDescending("createTime");
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
                    swipeRefreshLayout.setRefreshing(false);
                    isRefreshing = false;
                    break;
                case LOAD_NEW:
                    if(newData.size() != 0) {
                        mAdapter.addDatas(newData);//这会调用notifyItemInserted
                    }
                    mAdapter.removeFooter(footerView);
                    isRefreshing = false;
                    break;
                default:
                    break;
            }
        }
    };
}
