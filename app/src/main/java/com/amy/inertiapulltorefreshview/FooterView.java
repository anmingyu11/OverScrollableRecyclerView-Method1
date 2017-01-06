package com.amy.inertiapulltorefreshview;

import android.view.View;

import com.amy.library.interfaces.IFooterView;

public class FooterView implements IFooterView {

    @Override
    public View getView() {
        return null;
    }

    @Override
    public void onPullingUp(float fraction, float maxHeadHeight, float headHeight) {

    }

    @Override
    public void startAnim(float maxHeadHeight, float headHeight) {

    }

    @Override
    public void onPullReleasing(float fraction, float maxHeadHeight, float headHeight) {

    }

    @Override
    public void onFinish() {

    }

}
