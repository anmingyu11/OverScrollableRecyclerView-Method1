package com.amy.inertia.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.amy.inertia.interfaces.IAnimatorController;
import com.amy.inertia.interfaces.IPullToRefreshContainer;
import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;
import com.amy.inertia.util.ScrollUtil;
import com.amy.inertia.util.Util;

import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_OVER_FLING_FOOTER;
import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_OVER_FLING_HEADER;
import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_SETTLING_IN_CONTENT;

public class ARecyclerView extends RecyclerView implements IAView {

    private boolean isInTouching;

    private IPullToRefreshContainer mPullToRefreshContainer = null;

    private IAnimatorController mAnimatorController = null;

    private final ScrollViewState mScrollViewState = new ScrollViewState();

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
            int currentScrollState = mScrollViewState.CurrentScrollState;
            if (currentScrollState == ScrollViewState.SCROLL_STATE_DRAGGING_IN_CONTENT
                    || currentScrollState == ScrollViewState.SCROLL_STATE_SETTLING_IN_CONTENT) {
                mScrollViewState.notifyScrollStateChanged(state);//Scroll state to idle
            }
        }

    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        mScrollViewState.storeVy(dy);

        if (getScrollState() == SCROLL_STATE_SETTLING) {
            boolean isScrollToTop = ScrollUtil.isChildScrollToTop(this) && !ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollToBottom = !ScrollUtil.isChildScrollToTop(this) && ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollInContent = ScrollUtil.isChildScrollToBottom(this) && ScrollUtil.isChildScrollToTop(this);

            if (isScrollInContent) {
                mScrollViewState.notifyScrollStateChanged(SCROLL_STATE_SETTLING_IN_CONTENT);
            } else if (isScrollToBottom) {
                LogUtil.d("IsScrollToBottom : " + Util.spellArray(mScrollViewState.VYArray));
                mScrollViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_FLING_FOOTER);
            } else if (isScrollToTop) {
                LogUtil.d("IsScrollToTop : " + Util.spellArray(mScrollViewState.VYArray));
                mScrollViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_FLING_HEADER);
            }
        }

    }

/*
    @Override
    public boolean canScrollVertically(int direction) {
        if (Math.abs(getTranslationY()) > 1f) {
            return false;
        }
        return super.canScrollVertically(direction);
    }

    @Override
    public boolean isCanScrollVertically() {
        return canScrollVertically(1) && canScrollVertically(-1);
    }
*/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (isInTouching) {
            return super.onInterceptTouchEvent(e);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mScrollViewState.setTouchLastXY(e.getX(), e.getY());
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mScrollViewState.setTouchLastXY(e.getX(0), e.getY(0));
            }
            case MotionEvent.ACTION_MOVE: {
                boolean fingerScrollUp;
                boolean fingerScrollDown;

                mScrollViewState.setTouchDXY(e.getX(), e.getY());

                if (mScrollViewState.touchDY > 0) {
                    fingerScrollDown = true;
                    fingerScrollUp = false;
                } else if (mScrollViewState.touchDY < 0) {
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
                    mScrollViewState.notifyScrollStateChanged(ScrollViewState.SCROLL_STATE_OVER_SCROLL_FOOTER);
                } else if (!ScrollUtil.isChildScrollToBottom(this)
                        && ScrollUtil.isChildScrollToTop(this)
                        && !fingerScrollUp
                        && fingerScrollDown) {
                    //Dragging to top
                    mScrollViewState.notifyScrollStateChanged(ScrollViewState.SCROLL_STATE_OVER_SCROLL_HEADER);
                } else if (ScrollUtil.isChildScrollToTop(this)
                        && ScrollUtil.isChildScrollToBottom(this)) {
                    //Dragging in content
                    mScrollViewState.notifyScrollStateChanged(ScrollViewState.SCROLL_STATE_DRAGGING_IN_CONTENT);
                }

                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                int indexOfUpPointer = e.getActionIndex();
                if (indexOfUpPointer == 0) {
                    mScrollViewState.setTouchLastXY(e.getX(1), e.getY(1));
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mScrollViewState.resetTouch();
            }
        }
        return super.onTouchEvent(e);

    }


    @Override
    public ScrollViewState getScrollViewState() {
        return mScrollViewState;
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
    }

    @Override
    public void attachToAnimatorController(IAnimatorController iAnimatorController) {
        mAnimatorController = iAnimatorController;
    }

    @Override
    public void addScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
        mScrollViewState.addScrollDetectorListener(onScrollDetectorListener);
    }

    @Override
    public void removeScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener) {
        mScrollViewState.removeScrollDetectorListener(onScrollDetectorListener);
    }

    @Override
    public void clearScrollDetectorListener() {
        mScrollViewState.clearScrollDetectorListener();
    }

    @Override
    public void setViewTranslationY(float translationY) {
        setTranslationY(translationY);
    }

    @Override
    public float getViewTranslationY() {
        return getTranslationY();
    }

}
