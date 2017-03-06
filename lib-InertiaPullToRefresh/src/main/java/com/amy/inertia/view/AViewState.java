package com.amy.inertia.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class AViewState {

    public final static int SCALED_MAX_FLING_V;

    public final static int SCALED_MIN_FLING_V;

    static {
        SCALED_MAX_FLING_V = ViewConfiguration.getMaximumFlingVelocity();
        SCALED_MIN_FLING_V = ViewConfiguration.getMinimumFlingVelocity();
        //LogUtil.d("scaled fling v : " + " max: " + SCALED_MAX_FLING_V + " min : " + SCALED_MIN_FLING_V);
    }

    private final List<OnScrollDetectorListener<ARecyclerView>> mOnScrollDetectorListeners = new ArrayList<OnScrollDetectorListener<ARecyclerView>>();

    private IAView mIAView;

    //final VelocityTracker VelocityTracker = android.view.VelocityTracker.obtain();

    AViewState(IAView iaView, Context context) {
        mIAView = iaView;
        TouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //LogUtil.d("touchSlop : " + TouchSlop);
    }

    int pointerId;

    private final static int EVENT_BUFFER_SIZE = 3;
    final Queue<MotionEvent> MotionEvents = new LinkedList<MotionEvent>();
    private MotionEvent lastMotionEvent;

    void storeMotionEvent(MotionEvent e) {
        //LogUtil.d("sto : " + MotionEvents.size());
        if (MotionEvents.size() == 3) {
            MotionEvents.poll();
            MotionEvents.offer(e);
        } else {
            MotionEvents.offer(e);
        }
        lastMotionEvent = e;
    }

    MotionEvent[] getMotionEvents() {
        MotionEvent[] motionEvents = new MotionEvent[3];
        for (int i = 0; i < EVENT_BUFFER_SIZE; i++) {
            motionEvents[i] = MotionEvents.peek();
            //LogUtil.i(" " + i + " : " + motionEvents[i].getActionMasked());
        }
        return motionEvents;
    }

    int getLastAction() {
        if (lastMotionEvent == null) {
            return -1;
        }
        return lastMotionEvent.getActionMasked();
    }

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
        //LogUtil.d("DY : " + touchDY);
    }

    void setTouchLastXY(float X, float Y) {
        touchLastX = (int) (X + 0.5f);
        touchLastY = (int) (Y + 0.5f);
    }

    void setTouchDXY(float X, float Y) {
        touchDX = (int) (X - touchLastX);
        touchDY = (int) (Y - touchLastY);
        setTouchLastXY(X, Y);
        //LogUtil.d("DY : " + touchDY);
    }

    void setTouchDXY(MotionEvent e, int pointerId) {
        float X = e.getX(pointerId);
        float Y = e.getY(pointerId);
        touchDX = (int) (X - touchLastX);
        touchDY = (int) (Y - touchLastY);
        setTouchLastXY(e, pointerId);
        //LogUtil.d("DY : " + touchDY);
    }

    // DY array use this to store scroll velocity.
    final int[] VYArray = new int[VY_SIZE];
    private static final int VY_SIZE = 2;
    private int CurrentIndexOfVyArray = 0;

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
            "STATE_IDLE",//0
            "STATE_DRAGGING_IN_CONTENT",//1
            "STATE_SETTLING_IN_CONTENT",//2
            "STATE_OVER_SCROLL_HEADER",//3
            "STATE_OVER_SCROLL_FOOTER",//4
            "STATE_OVER_FLING_HEADER",//5
            "STATE_OVER_FLING_FOOTER",//6
            "STATE_HEADER_REFRESHING",//7
            "STATE_FOOTER_REFRESHING"//8
    };

    int LastScrollState = STATE_IDLE;
    int CurrentState = STATE_IDLE;

    static final int STATE_IDLE = 0;
    static final int STATE_DRAGGING_IN_CONTENT = 1;
    static final int STATE_SETTLING_IN_CONTENT = 2;
    static final int STATE_OVER_SCROLL_HEADER = 3;
    static final int STATE_OVER_SCROLL_FOOTER = 4;
    static final int STATE_OVER_FLING_HEADER = 5;
    static final int STATE_OVER_FLING_FOOTER = 6;
    static final int STATE_HEADER_REFRESHING = 7;
    static final int STATE_FOOTER_REFRESHING = 8;

    void setScrollState(int newState) {
        LastScrollState = CurrentState;
        CurrentState = newState;
    }

    boolean notifyScrollStateChanged(int newState) {
        if (CurrentState != newState) {
            setScrollState(newState);
            //LogUtil.printTraceStack("where");
            LogUtil.e("----------------");
            LogUtil.d("CurrentState : " + SCROLL_STATES[CurrentState]);
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
            //LogUtil.v("add onScrollDetectorListener success.");
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
            //LogUtil.d("remove onScrollDetectorListener success.");
        }
    }

}