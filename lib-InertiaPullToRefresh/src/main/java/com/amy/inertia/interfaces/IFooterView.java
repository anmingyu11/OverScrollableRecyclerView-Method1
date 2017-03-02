package com.amy.inertia.interfaces;

import android.view.View;

public interface IFooterView {

    View getView();

    void onPullingUp(float fraction,float maxHeadHeight,float headHeight);

    void startAnim(float maxHeight,float currentHeight);

    void onPullReleasing(float fraction,float maxHeadHeight,float headHeight);

    void onFinish();
}
