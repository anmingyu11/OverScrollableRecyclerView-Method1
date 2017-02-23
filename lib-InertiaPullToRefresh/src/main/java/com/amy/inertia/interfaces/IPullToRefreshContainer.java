package com.amy.inertia.interfaces;

import android.view.MotionEvent;

public interface IPullToRefreshContainer {

    void attachToAView(IAView iaView);

    void setInTouching(boolean inTouching);

    void handleTouchEvent(MotionEvent e);
}
