//Todo copyRight
package com.amy.library.inertia;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.amy.library.LogUtil;
import com.amy.library.ScrollUtil;

/**
 * If only use this view , only inertia over scroll can be used.
 */
public class InertiaRecyclerView extends RecyclerView {

    private InertiaPullToRefreshLayout mFather;

    private boolean isEnableInertiaHeaderOverScroll = true;
    private boolean isEnableInertiaFooterOverScroll = true;

    private int go = 0;
    private boolean isGo = false;

    private boolean isScrollToTop;
    private boolean isScrollToBottom;

    private float mLastY = 0;
    private float mLastX = 0;

    private float mLastTouchDy;
    private float mTouchDy;

    private float mPullOverScrollY;
    private float mPullOverScrollDy;

    private int mScrollState;
    private String[] stateString = new String[]{"idle", "dragging", "settling"};

    private int dy;

    public InertiaRecyclerView(Context context) {
        super(context);
    }

    public InertiaRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InertiaRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initFather();
    }

    private void initFather() {
        if (getParent() instanceof InertiaPullToRefreshLayout) {
            mFather = (InertiaPullToRefreshLayout) getParent();
        } else {
            mFather = null;
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if (go < 2) {
            go++;
            if (go == 2) {
                isGo = true;
            }
        }

        isScrollToBottom = ScrollUtil.isChildScrollToBottom(this) && !ScrollUtil.isChildScrollToTop(this);
        isScrollToTop = ScrollUtil.isChildScrollToTop(this) && !ScrollUtil.isChildScrollToBottom(this);

        if (isGo) {
            if (isScrollToTop) {
                LogUtil.d("isScrollToTop : " + isScrollToTop);
            } else if (isScrollToBottom) {
                LogUtil.d("isScrollToBottom : " + isScrollToBottom);
            }
        }

    }

    @Override
    public float getTranslationY() {
        float translationY = super.getTranslationY();
        return translationY;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (Math.abs(getTranslationY()) > 1f) {
            return false;
        }
        return super.canScrollVertically(direction);
    }

    private boolean isCanScrollVertically() {
        return canScrollVertically(1) && canScrollVertically(-1);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        mScrollState = state;
        LogUtil.d("state : " + stateString[state]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        /*
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mLastX = e.getX();
                mLastY = e.getY();
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mLastX = e.getX(0);
                mLastY = e.getY(0);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int indexOfUpPointer = e.getActionIndex();
                if (indexOfUpPointer == 0) {
                    mLastX = e.getX(1);
                    mLastY = e.getY(1);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float dy = e.getY() - mLastY;

                if (isScrollToTop || isScrollToBottom && isGo) {
                    setTranslationY(getTranslationY() + dy);
                } else if (!isCanScrollVertically()) {
                    setTranslationY(getTranslationY() + dy);
                }

                mLastY = e.getY();
                mLastX = e.getX();
                LogUtil.d("touchDy : " + mTouchDy + " overScrollDy : " + mPullOverScrollDy);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                break;
            }
        }
        if (mScrollState == SCROLL_STATE_DRAGGING) {
        }
        */
        return super.onTouchEvent(e);
    }


}
