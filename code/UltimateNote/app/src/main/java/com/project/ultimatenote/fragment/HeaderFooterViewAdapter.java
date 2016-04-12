package com.project.ultimatenote.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.ultimatenote.R;
import com.project.ultimatenote.activity.MainActivity;
import com.project.ultimatenote.activity.ShowOtherActivity;
import com.project.ultimatenote.entity.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HeaderFooterViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;

    private static final int TYPE_FOOTER = 1;

    private static final int TYPE_ITEM = 2;

    private static final int TYPE_EMPTY = 3;

    private View mHeaderView;

    private View mFooterView;

    private View mEmptyView;

    private List<Note> items;

    public HeaderFooterViewAdapter(Context context,List<Note> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_card_view, parent, false);
            return new VHItem(v);
        } else if (viewType == TYPE_HEADER) {
            View v = mHeaderView;
            return new VHHeader(v);
        } else if (viewType == TYPE_FOOTER) {
            View v = mFooterView;
            return new VHFooter(v);
        } else if (viewType == TYPE_EMPTY) {
            View v = mEmptyView;
            return new VHEmpty(v);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            final int i = position - getHeadViewSize();
            VHItem viewHolder = (VHItem)holder;
            viewHolder.mTextView.setText(items.get(i).getTitle() + "  " + items.get(i).getUserName());
            Date date = new Date(items.get(i).getCreateTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            viewHolder.mTimeView.setText(sdf.format(date));

            viewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ShowOtherActivity.class);
                    intent.putExtra(MainActivity.C_TIME, items.get(i).getCreateTime());
                    intent.putExtra(MainActivity.USER_NAME, items.get(i).getUserName());
                    ((Activity)context).startActivityForResult(intent, MainActivity.SHOW_OTHER_NOTE);
                }
            });
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ShowOtherActivity.class);
                    intent.putExtra(MainActivity.C_TIME, items.get(i).getCreateTime());
                    intent.putExtra(MainActivity.USER_NAME, items.get(i).getUserName());
                    ((Activity)context).startActivityForResult(intent, MainActivity.SHOW_OTHER_NOTE);
                }
            });
        } else if (holder instanceof VHHeader) {

        } else if (holder instanceof VHFooter) {

        } else if (holder instanceof VHEmpty) {

        }

    }

    @Override
    public int getItemCount() {
        if(items == null){
            return  0;
        }
        int count;
        int size = items.size();
        if (size == 0 && null != mEmptyView) {
            count = 1;
        } else {
            count = getHeadViewSize() + size + getFooterViewSize();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        int size = items.size();
        if (size == 0 && null != mEmptyView) {
            return TYPE_EMPTY;
        } else if (position < getHeadViewSize()) {
            return TYPE_HEADER;
        } else if (position >= getHeadViewSize() + items.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private int getHeadViewSize() {
        return mHeaderView == null ? 0 : 1;
    }

    private int getFooterViewSize() {
        return mFooterView == null ? 0 : 1;
    }

    private Note getItem(int position) {
        return items.get(position - getHeadViewSize());
    }


    //add a header to the adapter
    public void addHeader(View header) {
        mHeaderView = header;
        notifyItemInserted(0);
    }

    //remove a header from the adapter
    public void removeHeader(View header) {
        notifyItemRemoved(0);
        mHeaderView = null;
    }

    //add a footer to the adapter
    public void addFooter(View footer) {
        mFooterView = footer;
        notifyItemInserted(getHeadViewSize() + items.size());
    }

    //remove a footer from the adapter
    public void removeFooter(View footer) {
        notifyItemRemoved(getHeadViewSize() + items.size());
        mFooterView = null;
    }
    //add data
    public void addDatas(List<Note> data) {
        items.addAll(data);
        notifyItemInserted(getHeadViewSize() + items.size() - 1);
    }

    //add data
    public void addData(Note data) {
        items.add(data);
        notifyItemInserted(getHeadViewSize() + items.size() - 1);
    }

    //refresh data
    public void refreshData(List<Note> datas){
        items.clear();
        addDatas(datas);
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        notifyItemInserted(0);
    }


    class VHItem extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView mTimeView;
        public ImageView mImageView;
        public VHItem(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.text);
            mTimeView = (TextView) v.findViewById(R.id.timeText);
            mImageView = (ImageView) v.findViewById(R.id.picture);
        }
    }

    class VHHeader extends RecyclerView.ViewHolder {

        public VHHeader(View itemView) {
            super(itemView);
        }
    }

    class VHFooter extends RecyclerView.ViewHolder {
        public VHFooter(View itemView) {
            super(itemView);
        }
    }

    class VHEmpty extends RecyclerView.ViewHolder {
        public VHEmpty(View itemView) {
            super(itemView);
        }
    }

}
