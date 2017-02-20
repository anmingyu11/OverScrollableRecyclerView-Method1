package com.amy.inertia.view;

import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

final class ScrollViewState {
    private final List<OnScrollDetectorListener<ARecyclerView>> mOnScrollDetectorListeners = new ArrayList<OnScrollDetectorListener<ARecyclerView>>();

    //Touch params
    float touchLastX;
    float touchLastY;
    float touchDX;
    float touchDY;

    void clearTouch() {
        touchLastX = 0;
        touchLastY = 0;
        touchDX = 0;
        touchDY = 0;
    }

    void setTouchLastXY(float X, float Y) {
        touchLastX = X;
        touchLastY = Y;
    }

    void setTouchDXY(float X, float Y) {
        touchDX = X - touchLastX;
        touchDY = Y - touchLastY;
        touchLastX = X;
        touchLastY = Y;
    }

    // DY array use this to store scroll velocity.
    final int[] VYArray = new int[VY_SIZE];
    static final int VY_SIZE = 2;
    int CurrentIndexOfVyArray = 0;

    void storeVy(int vY) {
        if (CurrentIndexOfVyArray > 1) {
            CurrentIndexOfVyArray = 0;
        }

        VYArray[CurrentIndexOfVyArray++] = vY;
    }

    //For debug
    static final String[] SCROLL_STATES = new String[]{
            "SCROLL_STATE_IDLE",//0
            "SCROLL_STATE_DRAGGING_IN_CONTENT",//1
            "SCROLL_STATE_SETTLING_IN_CONTENT",//2
            "SCROLL_STATE_OVER_SCROLL_HEADER",//3
            "SCROLL_STATE_OVER_SCROLL_FOOTER",//4
            "SCROLL_STATE_OVER_FLING_HEADER",//5
            "SCROLL_STATE_OVER_FLING_FOOTER"//6
    };

    int ScrollState = SCROLL_STATE_IDLE;

    static final int SCROLL_STATE_IDLE = 0;
    static final int SCROLL_STATE_DRAGGING_IN_CONTENT = 1;
    static final int SCROLL_STATE_SETTLING_IN_CONTENT = 2;
    static final int SCROLL_STATE_OVER_SCROLL_HEADER = 3;
    static final int SCROLL_STATE_OVER_SCROLL_FOOTER = 4;
    static final int SCROLL_STATE_OVER_FLING_HEADER = 5;
    static final int SCROLL_STATE_OVER_FLING_FOOTER = 6;

    void notifyScrollStateChanged(int newState) {
        if (ScrollState != newState) {
            LogUtil.d("Scroll state : " + SCROLL_STATES[newState]);
            for (OnScrollDetectorListener onScrollDetectorListener : mOnScrollDetectorListeners) {
                onScrollDetectorListener.onScrollStateChanged(newState);
            }
        }
        ScrollState = newState;
    }

    public void addScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
        if (mOnScrollDetectorListeners.contains(onScrollDetectorListener)) {
            String msg = "onScrollDetectorListener has contained.";
            LogUtil.e(msg);
            try {
                throw new IllegalArgumentException(msg);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {
            mOnScrollDetectorListeners.add(onScrollDetectorListener);
            LogUtil.d("add onScrollDetectorListener success.");
        }
    }

    public void removeScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
        if (!mOnScrollDetectorListeners.contains(onScrollDetectorListener)) {
            String msg = "onScrollDetectorListener not contained.";
            LogUtil.e(msg);
            try {
                throw new IllegalArgumentException(msg);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {
            mOnScrollDetectorListeners.remove(onScrollDetectorListener);
            LogUtil.d("remove onScrollDetectorListener success.");
        }
    }

}
