package com.amy.inertia.interfaces;

import android.view.View;

public interface IHeaderView {

    View getView();

    void onPullingHeader(float fraction);

    void onPullReleasing(float fraction);

    void startAnim(float maxHeight, float currentHeight);

    void onFinish();
}
