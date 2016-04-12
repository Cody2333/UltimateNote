package com.project.ultimatenote.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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

public class OtherNoteFragment extends Fragment {
    private List<Note> notes;
    public void setNoteList(List<Note> noteList){
        notes = noteList;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.note_fragment, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new CardAdapter(getActivity(), notes));
    }

    public static class CardAdapter
            extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

        private List<Note> items;

        public CardAdapter( Context context , List<Note> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i ) {
            //set layout for viewHolder
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_card_view, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder( final ViewHolder viewHolder, final int i ) {
            //set content for viewHolder

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
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        //rewrite viewHolder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public TextView mTimeView;
            public ImageView mImageView;
            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.text);
                mTimeView = (TextView) v.findViewById(R.id.timeText);
                mImageView = (ImageView) v.findViewById(R.id.picture);
            }
        }

        public static int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;
            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        || (halfWidth / inSampleSize) > reqWidth) {
                    //inSampleSize *= 2;
                    inSampleSize ++;
                }
            }

            return inSampleSize;
        }
    }
}
