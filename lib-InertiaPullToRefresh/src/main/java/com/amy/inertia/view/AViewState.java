package com.amy.inertia.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public final class AViewState {

    private final List<OnScrollDetectorListener<ARecyclerView>> mOnScrollDetectorListeners = new ArrayList<OnScrollDetectorListener<ARecyclerView>>();

    private IAView mIAView;

    AViewState(IAView iaView, Context context) {
        mIAView = iaView;
        TouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        LogUtil.d("touchSlop : " + TouchSlop);
    }

    int CurrentIndexOffsetYArray = 0;
    private int[] mYOffsets = new int[2];

    void storeYOffset(int offsetY) {
        if (CurrentIndexOffsetYArray > 1) {
            CurrentIndexOffsetYArray = 0;
        }

        VYArray[CurrentIndexOffsetYArray++] = offsetY;
    }

    private int getOffsetY() {
        return mYOffsets[1] - mYOffsets[0];
    }

    int pointerId;
    int lastAction = -1;

    //Touch params
    int touchLastX;
    int touchLastY;
    int touchDX;
    int touchDY;

    int TouchSlop;

    void resetTouch() {
        touchLastX = (int) 0f;
        touchLastY = (int) 0f;
        touchDX = (int) 0f;
        touchDY = (int) 0f;
    }

    void setTouchLastXY(MotionEvent e) {
        touchLastX = (int) (e.getRawX() + 0.5f);
        touchLastY = (int) (e.getRawY() + 0.5f);
    }

    void setTouchLastXY(MotionEvent e, int pointerId) {
        this.pointerId = pointerId;
        touchLastX = (int) (e.getX(pointerId) + 0.5f);
        touchLastY = (int) (e.getY(pointerId) + 0.5f);
    }

    void setTouchDXY(MotionEvent e) {
        float X = e.getRawX();
        float Y = e.getRawY();
        touchDX = (int) (X - touchLastX);
        touchDY = (int) (Y - touchLastY);
        setTouchLastXY(X, Y);
        LogUtil.d("DY : " + touchDY);
    }

    void setTouchLastXY(float X, float Y) {
        touchLastX = (int) (X + 0.5f);
        touchLastY = (int) (Y + 0.5f);
    }

    void setTouchDXY(float X, float Y) {
        touchDX = (int) (X - touchLastX);
        touchDY = (int) (Y - touchLastY);
        setTouchLastXY(X, Y);
        LogUtil.d("DY : " + touchDY);
    }

    void setTouchDXY(MotionEvent e, int pointerId) {
        float X = e.getX(pointerId);
        float Y = e.getY(pointerId);
        touchDX = (int) (X - touchLastX);
        touchDY = (int) (Y - touchLastY) + getOffsetY();
        setTouchLastXY(e, pointerId);
        LogUtil.d("DY : " + touchDY);
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

    boolean notifyScrollStateChanged(int newState) {
        if (CurrentScrollState != newState) {
            setScrollState(newState);
            LogUtil.e("----------------");
            LogUtil.d("CurrentScrollState : " + SCROLL_STATES[CurrentScrollState]);
            LogUtil.i("LastScrollState : " + SCROLL_STATES[LastScrollState]);
            LogUtil.e("----------------");
            for (OnScrollDetectorListener onScrollDetectorListener : mOnScrollDetectorListeners) {
                onScrollDetectorListener.onScrollStateChanged(newState);
            }
            return true;
        } else {
            return false;
        }
    }

    void addScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
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

    void clearScrollDetectorListener() {
        mOnScrollDetectorListeners.clear();
    }

    void removeScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
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