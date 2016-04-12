package com.mlzc.imagenote.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mlzc.imagenote.R;
import com.mlzc.imagenote.activity.MainActivity;
import com.mlzc.imagenote.activity.ShowActivity;
import com.mlzc.imagenote.entity.Note;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by myc on 2015/7/27.
 */
public class NoteFragment extends Fragment {
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
            Bitmap picture = null;
            try {
                //picture
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPurgeable = true;
                options.inInputShareable = true;
                BitmapFactory.decodeFile(items.get(i).getPicturePaths().get(0), options);
                options.inSampleSize = calculateInSampleSize(options, 80, 80);
                picture = BitmapFactory.decodeFile(items.get(i).getPicturePaths().get(0), options);
            } catch (Exception e) {
                e.printStackTrace();
            }

            viewHolder.mTextView.setText(items.get(i).getTitle());
            Date date = new Date(items.get(i).getReviseTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if(items.get(i).getIsPublic() && items.get(i).isCloudNote())
                viewHolder.mTimeView.setText(sdf.format(date) + "  @cloud  P");
            else if(items.get(i).isCloudNote())
                viewHolder.mTimeView.setText(sdf.format(date) + "  @cloud");
            else
                viewHolder.mTimeView.setText(sdf.format(date));

            if(picture != null)
                viewHolder.mImageView.setImageBitmap(picture);

            viewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ShowActivity.class);
                    intent.putExtra(MainActivity.TIME, items.get(i).getReviseTime());
                    intent.putExtra(MainActivity.C_TIME, items.get(i).getCreateTime());
                    ((Activity)context).startActivityForResult(intent, MainActivity.SHOW_NOTE);

                }
            });
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ShowActivity.class);
                    intent.putExtra(MainActivity.TIME, items.get(i).getReviseTime());
                    intent.putExtra(MainActivity.C_TIME, items.get(i).getCreateTime());
                    ((Activity)context).startActivityForResult(intent, MainActivity.SHOW_NOTE);
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
