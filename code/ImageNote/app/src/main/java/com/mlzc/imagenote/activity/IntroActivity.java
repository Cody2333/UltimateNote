package com.mlzc.imagenote.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;
import com.mlzc.imagenote.R;
import com.mlzc.imagenote.utils.NavigationManager;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends OnboarderActivity {
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        OnboarderPage onboarderPage1 = new OnboarderPage("文字识别", "基于google tesseract", R.drawable.planet1);
        OnboarderPage onboarderPage2 = new OnboarderPage("语音识别", "给你更简单的创作环境", R.drawable.planet2);
        OnboarderPage onboarderPage3 = new OnboarderPage("云同步", "云端同步笔记", R.drawable.planet3);

        onboarderPage1.setBackgroundColor(R.color.onboarder_bg_1);
        onboarderPage2.setBackgroundColor(R.color.onboarder_bg_2);
        onboarderPage3.setBackgroundColor(R.color.onboarder_bg_3);

        List<OnboarderPage> pages = new ArrayList<>();

        pages.add(onboarderPage1);
        pages.add(onboarderPage2);
        pages.add(onboarderPage3);

        for (OnboarderPage page : pages) {
            page.setTitleColor(R.color.primary_text);
            page.setDescriptionColor(R.color.secondary_text);
        }

        setOnboardPagesReady(pages);
        shouldUseFloatingActionButton(true);

    }

    @Override
    public void onSkipButtonPressed() {
        Toast.makeText(this, "Skip button was pressed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFinishButtonPressed() {
        NavigationManager.toMain(context);
    }

}