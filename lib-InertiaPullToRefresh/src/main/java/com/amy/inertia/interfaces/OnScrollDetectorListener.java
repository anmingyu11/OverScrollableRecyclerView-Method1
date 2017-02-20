package com.amy.inertia.interfaces;

import android.view.View;

public interface OnScrollDetectorListener<T extends View> {

    void onScrolled(T t, int dx, int dy);

    void onScrollStateChanged(int newState);

    void onNotFullVisible();

    void onFullVisible();

}
