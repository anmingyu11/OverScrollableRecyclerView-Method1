package com.amy.inertia.interfaces;

import android.view.View;

public interface IFooterView {

    View getView();

    void setVisible(boolean visible);

    //boolean isVisible();

    void onPulling(float fraction);

    void onRefresh(float maxHeight, float currentHeight);

    void onReleasing(float fraction);

    void onFinish();
}
