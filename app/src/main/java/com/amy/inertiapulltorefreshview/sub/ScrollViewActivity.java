package com.amy.inertiapulltorefreshview.sub;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.amy.inertiapulltorefreshview.LogUtil;
import com.amy.inertiapulltorefreshview.R;

public class ScrollViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("ScrollView");
        setContentView(R.layout.scroll_view_layout);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.container);
        final int childCount = scrollView.getChildCount();
        LogUtil.d(" scroll view child count : " + childCount);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                LogUtil.d("ScrollView scroll changed  scroll view : " + scrollView.getScrollY());
                LogUtil.d("Height : " + scrollView.getHeight());
            }
        });
    }

}
