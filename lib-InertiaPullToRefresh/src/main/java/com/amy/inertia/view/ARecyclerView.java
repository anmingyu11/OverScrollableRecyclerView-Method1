package com.amy.inertia.view;

import android.content.Context;
import android.os.Handler;
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

    final Handler mHandler = new Handler();

    @Override
    public void onScrolled(int dx, final int dy) {
        super.onScrolled(dx, dy);

        //LogUtil.d("onScrolled dy : " + dy + " state : " + mAViewState.CurrentScrollState);
        if (mAViewState.CurrentScrollState == SCROLL_STATE_IDLE) {
            //Todo:
            //LogUtil.printTraceStack("idle");
        }
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result;

        if (isInTouching) {
            result = super.dispatchTouchEvent(ev);
        } else {
            //If not is in touching, this touch event send back to parent view.
            result = false;
        }

        //LogUtil.i("TouchingTest AView dispatching inTouching : " + isInTouching + " result : " + result);
        //LogUtil.w("TouchingTest AView dispatching : " + ev.getY() + " action : " + ev.getActionMasked());
        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean result;

        result = super.onInterceptTouchEvent(e);

        //LogUtil.i("TouchingTest AView intercepting inTouching : " + isInTouching + " result : " + result);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        boolean result;

        result = handleTouchEvent(e);

        // LogUtil.i("TouchingTest AView onTouching inTouching : " + isInTouching + " result : " + result);
        return result;

    }

    public AViewState getAViewState() {
        return mAViewState;
    }

    @Override
    public IPullToRefreshContainer getContainer() {
        return mPullToRefreshContainer;
    }

    private boolean handleTouchEvent(MotionEvent e) {

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
            case MotionEvent.ACTION_UP: {
                mAViewState.resetTouch();
            }
        }

        return super.onTouchEvent(e);
    }

    @Override
    public void setInTouching(boolean inTouching) {
        if (isInTouching == inTouching) {
            return;
        }
        LogUtil.d("ARecyclerView inTouching : " + inTouching);

        isInTouching = inTouching;
        if (isInTouching) {
            mPullToRefreshContainer.setInTouching(false);
        }
    }

    @Override
    public boolean aViewDispatchTouch(MotionEvent e) {
        dispatchTouchEvent(e);
        return false;
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
        //LogUtil.d("TranslationY : " + translationY);
        int transY = -100;
        if (translationY > 0f && getTranslationY() < 0f) {
            transY = 0;
        } else if (translationY < 0f && getTranslationY() > 0f) {
            transY = 0;
        } else if (Float.isNaN(translationY)) {
            transY = 0;
        } else {
            transY = (int) translationY;
        }

        if (transY == 0) {
            setTranslationY(transY);
            mAnimatorController.cancelAllAnim();
            mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_IDLE);
            //Todo my code worked but i don't know why.
            if (mAViewState.LastScrollState == SCROLL_STATE_OVER_SCROLL_FOOTER) {
                dispatchTouchEvent(Util.fakeAMotionEventForOverScrollFooter(mAViewState.lastMotionEvent));
            } else if (mAViewState.LastScrollState == SCROLL_STATE_OVER_SCROLL_HEADER) {
                dispatchTouchEvent(Util.fakeAMotionEventForOverScrollHeader(mAViewState.lastMotionEvent));
            }
        } else {
            setTranslationY(transY);
        }
    }

    @Override
    public int getViewTranslationY() {
        return (int) getTranslationY();
    }

    @Override
    public int getViewHeight() {
        return getHeight();
    }

}
