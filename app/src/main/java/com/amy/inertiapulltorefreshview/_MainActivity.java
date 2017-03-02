package com.amy.inertiapulltorefreshview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.amy.inertia.util.LogUtil;
import com.amy.inertiapulltorefreshview.sub.AbsListViewActivity;
import com.amy.inertiapulltorefreshview.sub.RecyclerViewActivity;
import com.amy.inertiapulltorefreshview.sub.ScrollViewActivity;
import com.amy.inertiapulltorefreshview.sub.WebViewActivity;

public class _MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._main_layout);
        LogUtil.setTAG("AMY");
        LogUtil.enableDebug(true);

        onClickRecyclerView(null);
        finish();
    }

    public void onClickScrollView(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ScrollViewActivity.class);
        startActivity(intent);
    }

    public void onClickRecyclerView(View view) {
        Intent intent = new Intent();
        intent.setClass(this, RecyclerViewActivity.class);
        startActivity(intent);
    }

    public void onClickWebView(View view) {
        Intent intent = new Intent();
        intent.setClass(this, WebViewActivity.class);
        startActivity(intent);
    }

    public void onClickAbsListView(View view) {
        Intent intent = new Intent();
        intent.setClass(this, AbsListViewActivity.class);
        startActivity(intent);
    }
}
