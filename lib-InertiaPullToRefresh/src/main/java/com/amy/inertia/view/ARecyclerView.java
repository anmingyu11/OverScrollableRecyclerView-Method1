package com.amy.inertia.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.IAnimatorController;
import com.amy.inertia.interfaces.IPullToRefreshContainer;
import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;
import com.amy.inertia.util.ScrollUtil;
import com.amy.inertia.util.Util;

import static com.amy.inertia.view.AViewState.SCROLL_STATE_DRAGGING_IN_CONTENT;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_FLING_FOOTER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_FLING_HEADER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_SCROLL_FOOTER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_SCROLL_HEADER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_SETTLING_IN_CONTENT;

public class ARecyclerView extends RecyclerView implements IAView {

    private boolean isInTouching = true;

    private IPullToRefreshContainer mPullToRefreshContainer = null;

    private IAnimatorController mAnimatorController = null;

    private final AViewState mAViewState = new AViewState(this);

    public ARecyclerView(Context context) {
        super(context);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        if (state == SCROLL_STATE_IDLE) {
            int currentScrollState = mAViewState.CurrentScrollState;
            if (currentScrollState == AViewState.SCROLL_STATE_DRAGGING_IN_CONTENT
                    || currentScrollState == AViewState.SCROLL_STATE_SETTLING_IN_CONTENT) {
                mAViewState.notifyScrollStateChanged(state);//Scroll state to idle
            }
        }

    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        LogUtil.d("onScrolled dy : " + dy + " state : " + mAViewState.CurrentScrollState);
        mAViewState.storeVy(dy);

        if (getScrollState() == SCROLL_STATE_SETTLING) {
            boolean isScrollToTop = ScrollUtil.isChildScrollToTop(this) && !ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollToBottom = !ScrollUtil.isChildScrollToTop(this) && ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollInContent = ScrollUtil.isChildScrollToBottom(this) && ScrollUtil.isChildScrollToTop(this);

            if (isScrollInContent) {
                mAViewState.notifyScrollStateChanged(SCROLL_STATE_SETTLING_IN_CONTENT);
            } else if (isScrollToBottom) {
                LogUtil.d("IsScrollToBottom : " + Util.spellArray(mAViewState.VYArray));
                mAViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_FLING_FOOTER);
            } else if (isScrollToTop) {
                LogUtil.d("IsScrollToTop : " + Util.spellArray(mAViewState.VYArray));
                mAViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_FLING_HEADER);
            }
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        //LogUtil.i("AView intercepting inTouching : " + isInTouching);
        if (isInTouching) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // LogUtil.i("AView onTouching inTouching : " + isInTouching);
        if (!isInTouching) {
            mPullToRefreshContainer.handleTouchEvent(e);
            return false;
        }

        return onHandleTouchEvent(e);

    }

    public AViewState getAViewState() {
        return mAViewState;
    }

    @Override
    public IPullToRefreshContainer getContainer() {
        return mPullToRefreshContainer;
    }

    @Override
    public void handleTouchEvent(MotionEvent e) {
        onHandleTouchEvent(e);
    }

    private boolean onHandleTouchEvent(MotionEvent e) {

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mAViewState.setTouchLastXY(e);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mAViewState.setTouchLastXY(e);
            }
            case MotionEvent.ACTION_MOVE: {
                boolean fingerScrollUp;
                boolean fingerScrollDown;

                mAViewState.setTouchDXY(e);

                if (mAViewState.touchDY > 0) {
                    fingerScrollDown = true;
                    fingerScrollUp = false;
                } else if (mAViewState.touchDY < 0) {
                    fingerScrollDown = false;
                    fingerScrollUp = true;
                } else {
                    fingerScrollDown = fingerScrollUp = false;
                }

                if (ScrollUtil.isChildScrollToBottom(this)
                        && !ScrollUtil.isChildScrollToTop(this)
                        && fingerScrollUp
                        && !fingerScrollDown) {
                    //Dragging to bottom
                    mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_OVER_SCROLL_FOOTER);
                } else if (!ScrollUtil.isChildScrollToBottom(this)
                        && ScrollUtil.isChildScrollToTop(this)
                        && !fingerScrollUp
                        && fingerScrollDown) {
                    //Dragging to top
                    mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_OVER_SCROLL_HEADER);
                } else if (ScrollUtil.isChildScrollToTop(this)
                        && ScrollUtil.isChildScrollToBottom(this)) {
                    //Dragging in content
                    mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_DRAGGING_IN_CONTENT);
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                int indexOfUpPointer = e.getActionIndex();
                if (indexOfUpPointer == 0) {
                    mAViewState.setTouchLastXY(e);
                }
                break;
            }
            //case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mAViewState.resetTouch();
            }
        }

        LogUtil.d("super called action : " + e.getAction());
        switch (mAViewState.CurrentScrollState) {
            case SCROLL_STATE_DRAGGING_IN_CONTENT:
            case SCROLL_STATE_SETTLING_IN_CONTENT:
            case SCROLL_STATE_IDLE: {
                return super.onTouchEvent(e);
            }
            case SCROLL_STATE_OVER_FLING_FOOTER:
            case SCROLL_STATE_OVER_FLING_HEADER:
            case SCROLL_STATE_OVER_SCROLL_FOOTER:
            case SCROLL_STATE_OVER_SCROLL_HEADER: {
                return false;
            }
        }

        return false;
    }

    @Override
    public void setInTouching(boolean inTouching) {
        isInTouching = inTouching;
        if (isInTouching) {
            mPullToRefreshContainer.setInTouching(false);
        }
    }

    @Override
    public void attachToParent(IPullToRefreshContainer iPullToRefresh) {
        mPullToRefreshContainer = iPullToRefresh;
        mPullToRefreshContainer.attachToAView(this);
    }

    @Override
    public void attachToAnimatorController(IAnimatorController iAnimatorController) {
        mAnimatorController = iAnimatorController;
        mAnimatorController.attachAView(this);
    }

    @Override
    public void addScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
        mAViewState.addScrollDetectorListener(onScrollDetectorListener);
    }

    @Override
    public void removeScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
        mAViewState.removeScrollDetectorListener(onScrollDetectorListener);
    }

    @Override
    public void clearScrollDetectorListener() {
        mAViewState.clearScrollDetectorListener();
    }

    @Override
    public void setViewTranslationY(float translationY) {
        //LogUtil.d("currentTranslation Y : " + translationY + " lastTranslation Y : " + getTranslationY());
        int transY = -100;
        if (translationY > 0f && getTranslationY() < 0f) {
            transY = 0;
        } else if (translationY < 0f && getTranslationY() > 0f) {
            transY = 0;
        } else {
            transY = (int) translationY;
        }

        if (transY == 0) {
            setTranslationY(transY);
            mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_IDLE);
            setInTouching(true);
        } else {
            setTranslationY(transY);
        }
    }

    @Override
    public float getViewTranslationY() {
        return getTranslationY();
    }

}
