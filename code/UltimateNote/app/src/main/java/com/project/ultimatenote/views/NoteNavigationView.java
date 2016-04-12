package com.project.ultimatenote.views;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.util.AttributeSet;
import android.view.View;

import com.project.ultimatenote.R;
import com.project.ultimatenote.utils.ToastUtils;

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
