package com.amy.inertia.view;

import android.content.Context;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.util.LogUtil;

public final class AScrollerController {
    int ScrollState;
    final static int FLING = 0;
    final static int OVER_FLING = 1;
    final static int SPRING_BACK = 2;
    //final static int OVER_SCROLL = 3;
    final static String[] ScrollStates = new String[]{
            "FLING", "OVER_FLING", "SPRING_BACK", "OVER_SCROLL"
    };

    private Context mContext;
    private AViewParams mParams;

    private final AScroller mScroller;
    private IAView mIAView;

    public int overFlingDistance;
    public int overFlingDuration;

    AScrollerController(Context context,
                        AViewParams params) {
        mScroller = new AScroller(context, null, false);
        mScroller.attachAScrollerController(this);
        mContext = context;
        mParams = params;
    }

    void attachAView(IAView iaView) {
        mIAView = iaView;
    }

    void setScrollState(int state) {
        ScrollState = state;
    }

    void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, minY, minY, maxY);
        setScrollState(FLING);
    }

    boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        boolean springBackResult = mScroller.springBack(startX, startY, minX, maxX, minY, maxY);
        if (springBackResult) {
            setScrollState(SPRING_BACK);
        }
        return springBackResult;
    }

    void abort() {
        mScroller.abortAnimation();
    }

    boolean isFinished() {
        return mScroller.isFinished();
    }

    int timePassed() {
        return mScroller.timePassed();
    }

    int getCurrY() {
        return mScroller.getCurrY();
    }

    int getBallisticDistance() {
        return mScroller.getBallisticDistance();
    }

    int getSpringBackDuration() {
        return mScroller.getSpringBackDuration();
    }

    int getCurrVelocity() {
        return (int) mScroller.getCurrVelocity();
    }

    boolean computeOffset() {
        return mScroller.computeScrollOffset();
    }

    private int getOverFlingDistance() {
        final int vel = (int) mScroller.getCurrVelocity();
        final int overDistance = (int) (vel / (float) mParams.maxVelocity * mParams.maxOverFlingDistance);
        return overDistance;
    }

    int[] notifyEdgeReached() {
        //final int transY = mAView.getViewTranslationY();Todo : what?
        final int overFlingDistance = getOverFlingDistance();
        LogUtil.e("EdgeReached : " + " overFling distance " + overFlingDistance);
        return mScroller.notifyVerticalEdgeReached(0, 0, overFlingDistance);
    }

    void notifyTopEdgeReached() {
        //final int transY = mAView.getViewTranslationY();Todo : what?
        final int overFlingDistance = getOverFlingDistance();
        LogUtil.e("topEdgeReached : " + " overFling distance " + overFlingDistance);
        mScroller.notifyVerticalEdgeReached(0, 0, overFlingDistance);

        setScrollState(OVER_FLING);
    }

    void notifyBottomEdgeReached() {
        //final int transY = mAView.getViewTranslationY();Todo : what?
        final int overFlingDistance = getOverFlingDistance();
        LogUtil.e("bottomEdgeReached : " + " overFling distance " + overFlingDistance);
        mScroller.notifyVerticalEdgeReached(0, 0, overFlingDistance);

        setScrollState(OVER_FLING);
    }

}
