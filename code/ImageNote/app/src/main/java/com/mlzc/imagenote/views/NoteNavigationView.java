package com.mlzc.imagenote.views;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.NavigationView;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mlzc.imagenote.R;
import com.mlzc.imagenote.utils.NavigationManager;
import com.mlzc.imagenote.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by cody_local on 2016/4/6.
 */
public class NoteNavigationView extends NavigationView {
    public NoteNavigationView(Context context) {
        super(context);
        init(context);
    }

    public NoteNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NoteNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context){
        View header = inflateHeaderView(R.layout.nav_header_issue);
        header.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //NavigationManager.toUserInfo(getContext());
                ToastUtils.showShort("go to user page");
            }
        });
    }

    @Override
    protected void onDetachedFromWindow (){
        super.onDetachedFromWindow();
    }


}
