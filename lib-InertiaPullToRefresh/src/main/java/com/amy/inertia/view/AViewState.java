package com.amy.inertia.view;

import android.view.MotionEvent;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public final class AViewState {

    private final List<OnScrollDetectorListener<ARecyclerView>> mOnScrollDetectorListeners = new ArrayList<OnScrollDetectorListener<ARecyclerView>>();

    private IAView mIAView;

    AViewState(IAView iaView) {
        mIAView = iaView;
    }

    MotionEvent lastMotionEvent = null;

    //Touch params
    float touchLastX;
    float touchLastY;
    float touchDX;
    float touchDY;

    void resetTouch() {
        touchLastX = 0f;
        touchLastY = 0f;
        touchDX = 0f;
        touchDY = 0f;
    }

    void setTouchLastXY(MotionEvent e) {
        touchLastX = e.getRawX();
        touchLastY = e.getRawY();
        lastMotionEvent = e;
        //LogUtil.i("LastY : " + touchLastY);
    }

    void setTouchDXY(MotionEvent e) {
        float X = e.getRawX();
        float Y = e.getRawY();
        touchDX = X - touchLastX;
        touchDY = Y - touchLastY;
        setTouchLastXY(e);
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

    int getVy() {
        int max = VYArray[0];

        for (int i = 0; i < VY_SIZE; i++) {
            if (VYArray[i] > max) {
                max = VYArray[i];
            }
        }
        return max;
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

    int LastScrollState = SCROLL_STATE_IDLE;
    int CurrentScrollState = SCROLL_STATE_IDLE;

    static final int SCROLL_STATE_IDLE = 0;
    static final int SCROLL_STATE_DRAGGING_IN_CONTENT = 1;
    static final int SCROLL_STATE_SETTLING_IN_CONTENT = 2;
    static final int SCROLL_STATE_OVER_SCROLL_HEADER = 3;
    static final int SCROLL_STATE_OVER_SCROLL_FOOTER = 4;
    static final int SCROLL_STATE_OVER_FLING_HEADER = 5;
    static final int SCROLL_STATE_OVER_FLING_FOOTER = 6;

    void setScrollState(int newState) {
        LastScrollState = CurrentScrollState;
        CurrentScrollState = newState;
    }

    void notifyScrollStateChanged(int newState) {
/*        if (newState == SCROLL_STATE_IDLE) {
            LogUtil.printTraceStack("state idle changed.");
        }*/
        if (CurrentScrollState != newState) {
            setScrollState(newState);
            LogUtil.e("----------------");
            LogUtil.d("CurrentScrollState : " + SCROLL_STATES[CurrentScrollState]);
            LogUtil.i("LastScrollState : " + SCROLL_STATES[LastScrollState]);
            LogUtil.e("----------------");
            for (OnScrollDetectorListener onScrollDetectorListener : mOnScrollDetectorListeners) {
                onScrollDetectorListener.onScrollStateChanged(newState);
            }
        }
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
            LogUtil.v("add onScrollDetectorListener success.");
        }
    }

    public void clearScrollDetectorListener() {
        mOnScrollDetectorListeners.clear();
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
