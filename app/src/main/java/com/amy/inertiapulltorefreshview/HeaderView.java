package com.amy.inertiapulltorefreshview;

import android.view.View;

import com.amy.library.interfaces.IHeaderView;

public class HeaderView implements IHeaderView{

    @Override
    public View getView() {
        return null;
    }

    @Override
    public void onPullingDown(float fraction, float maxHeadHeight, float headHeight) {

    }

    @Override
    public void onPullReleasing(float fraction, float maxHeadHeight, float headHeight) {

    }

    @Override
    public void startAnim(float maxHeadHeight, float headHeight) {

    }

    @Override
    public void onFinish() {

    }

}
