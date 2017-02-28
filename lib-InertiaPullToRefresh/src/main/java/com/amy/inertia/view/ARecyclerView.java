package com.amy.inertia.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
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

import static com.amy.inertia.util.LogUtil.d;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_FLING_FOOTER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_FLING_HEADER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_SCROLL_FOOTER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_SCROLL_HEADER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_SETTLING_IN_CONTENT;

public class ARecyclerView extends RecyclerView implements IAView {

    private IPullToRefreshContainer mPullToRefreshContainer = null;

    private IAnimatorController mAnimatorController = new AnimatorController(this);

    private AnimatorBuilder mAnimatorBuilder = AnimatorBuilder.getInstance();

    private final AViewState mAViewState = new AViewState(this, getContext());

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
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mPullToRefreshContainer == null) {
            //TOdo : maybe
            // throw new RuntimeException("Must have a Container");
        }
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
    public void onScrolled(int dx, final int dy) {
        super.onScrolled(dx, dy);

        //LogUtil.printTraceStack("onScrolled");
        //LogUtil.d("onScrolled dy : " + dy + " state : " + mAViewState.CurrentScrollState);
        mAViewState.storeVy(dy);

        if (getScrollState() == SCROLL_STATE_SETTLING) {
            boolean isScrollToTop = ScrollUtil.isChildScrollToTop(this) && !ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollToBottom = !ScrollUtil.isChildScrollToTop(this) && ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollInContent = ScrollUtil.isChildScrollToBottom(this) && ScrollUtil.isChildScrollToTop(this);

            if (isScrollInContent) {
                mAViewState.notifyScrollStateChanged(SCROLL_STATE_SETTLING_IN_CONTENT);
            } else if (isScrollToBottom) {
                d("IsScrollToBottom : " + Util.spellArray(mAViewState.VYArray));
                animOverFling(mAViewState.getVy(), SCROLL_STATE_OVER_FLING_FOOTER);
            } else if (isScrollToTop) {
                d("IsScrollToTop : " + Util.spellArray(mAViewState.VYArray));
                animOverFling(mAViewState.getVy(), SCROLL_STATE_OVER_FLING_HEADER);
            }
        }

    }

    public AViewState getAViewState() {
        return mAViewState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        LogUtil.d("touchAction : " + e.getActionMasked());
        final MotionEvent finalE = MotionEvent.obtain(e);
        mAViewState.currentMotionEvent = finalE;
        boolean result = handleTouchEvent(finalE);
        mAViewState.lastMotionEvent = finalE;
        mAViewState.lastAction = MotionEventCompat.getActionMasked(finalE);
        return result;
    }

    private boolean handleTouchEvent(MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        final int pointerId = e.getPointerId(actionIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                return onTouchDown(e, 0);
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                return onTouchPointerDown(e, 0);
            }
            case MotionEvent.ACTION_MOVE: {
                return onTouchMove(e, pointerId);
            }
            case MotionEvent.ACTION_POINTER_UP: {
                return onTouchPointerUp(e, pointerId);
            }
            case MotionEvent.ACTION_UP: {
                return onTouchUp(e);
            }
            default: {
                return super.onTouchEvent(e);
            }
        }
    }

    private boolean onTouchDown(MotionEvent e, int pointerId) {
        //LogUtil.e("----------onTouchDown----------");
        //Util.printTouchInfo(e);
        mAViewState.setTouchLastXY(e);
        int state = mAViewState.CurrentScrollState;

        switch (state) {
            case SCROLL_STATE_OVER_FLING_FOOTER: {
                mAnimatorController.cancelAllAnim();
                mAViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_SCROLL_FOOTER);
                return true;
            }
            case SCROLL_STATE_OVER_FLING_HEADER: {
                mAnimatorController.cancelAllAnim();
                mAViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_SCROLL_HEADER);
                return true;
            }
            case SCROLL_STATE_OVER_SCROLL_FOOTER:
            case SCROLL_STATE_OVER_SCROLL_HEADER: {
                //Todo : over scroll logic
                throw new RuntimeException();
            }
            default: {
                return super.onTouchEvent(e);
            }
        }
    }

    private boolean onTouchPointerDown(MotionEvent e, int pointerId) {
        //LogUtil.e("----------onTouchPointerDown----------");
        //Util.printTouchInfo(e);
        mAViewState.setTouchDXY(e);
        return super.onTouchEvent(e);
    }

    private boolean onTouchMove(MotionEvent e, int pointerId) {
        //LogUtil.e("----------onTouchMove----------");
        //Util.printTouchInfo(e);
        boolean fingerScrollUp;
        boolean fingerScrollDown;

        if (mAViewState.lastAction == MotionEvent.ACTION_POINTER_UP) {
            mAViewState.setTouchLastXY(e);
            return true;
        } else {
            mAViewState.setTouchDXY(e);
        }

        if (mAViewState.touchDY > 0) {
            fingerScrollDown = true;
            fingerScrollUp = false;
        } else if (mAViewState.touchDY < 0) {
            fingerScrollDown = false;
            fingerScrollUp = true;
        } else {
            fingerScrollDown = fingerScrollUp = false;
        }

        if (mAViewState.CurrentScrollState == SCROLL_STATE_OVER_SCROLL_FOOTER
                || mAViewState.CurrentScrollState == SCROLL_STATE_OVER_SCROLL_HEADER) {

            boolean result = overScroll(mAViewState.touchDY);
            if (result) {
                return true;
            } else {
                LogUtil.e("event  : " + e.getActionMasked());
                return super.onTouchEvent(e);
            }
        }

        if (ScrollUtil.isChildScrollToBottom(this)
                && !ScrollUtil.isChildScrollToTop(this)
                && fingerScrollUp
                && !fingerScrollDown) {
            //Dragging to bottom
            mAViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_SCROLL_FOOTER);
            return true;

        } else if (!ScrollUtil.isChildScrollToBottom(this)
                && ScrollUtil.isChildScrollToTop(this)
                && !fingerScrollUp
                && fingerScrollDown) {
            //Dragging to top
            mAViewState.notifyScrollStateChanged(SCROLL_STATE_OVER_SCROLL_HEADER);
            return true;
        } else if (ScrollUtil.isChildScrollToTop(this)
                && ScrollUtil.isChildScrollToBottom(this)) {
            //Dragging in content
            mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_DRAGGING_IN_CONTENT);
            return super.onTouchEvent(e);
        }

        return super.onTouchEvent(e);

    }

    private boolean onTouchPointerUp(MotionEvent e, int pointerId) {
        //LogUtil.e("----------onTouchPointerUp----------");
        //Util.printTouchInfo(e);
        int indexOfUpPointer = e.getActionIndex();
        if (indexOfUpPointer == 0) {
            mAViewState.setTouchLastXY(e);
        }

        return super.onTouchEvent(e);
    }

    private boolean onTouchUp(MotionEvent e) {
        //LogUtil.e("----------onTouchUp----------");
        //Util.printTouchInfo(e);
        mAViewState.resetTouch();
        int state = mAViewState.CurrentScrollState;

        LogUtil.d("transY : " + getTranslationY());
        switch (state) {
            case SCROLL_STATE_OVER_SCROLL_HEADER: {
                animScrollBack(SCROLL_STATE_OVER_FLING_HEADER);
                return false;
            }
            case SCROLL_STATE_OVER_SCROLL_FOOTER: {
                animScrollBack(SCROLL_STATE_OVER_FLING_FOOTER);
                return false;
            }
            default: {
                return super.onTouchEvent(e);
            }
        }
    }

    private void animOverFling(float vY, final int notify) {
        mAnimatorController.cancelAllAnim();

        ValueAnimator animator = (ValueAnimator) mPullToRefreshContainer.buildOverFlingAnim(vY);

        if (animator == null) {
            return;
        }

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAViewState.notifyScrollStateChanged(notify);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (getTranslationY() > 0) {
                    animScrollBack(SCROLL_STATE_OVER_FLING_HEADER);
                } else if (getTranslationY() < 0) {
                    animScrollBack(SCROLL_STATE_OVER_FLING_FOOTER);
                } else {
                    mAViewState.notifyScrollStateChanged(SCROLL_STATE_IDLE);
                }
            }
        });

        mAnimatorController.addAnimator(AnimatorBuilder.ANIM_OVER_FLING, animator);
        mAnimatorController.startAnimator(AnimatorBuilder.ANIM_OVER_FLING);
    }

    private void animScrollBack(final int notify) {
        mAnimatorController.cancelAllAnim();

        ValueAnimator animator = (ValueAnimator) mPullToRefreshContainer.buildScrollBackAnim(getTranslationY());

        if (animator == null) {
            return;
        }

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAViewState.notifyScrollStateChanged(notify);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (getViewTranslationY() > 0) {
                    mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_OVER_SCROLL_HEADER);
                } else if (getViewTranslationY() < 0) {
                    mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_OVER_SCROLL_FOOTER);
                } else {
                    mAViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_IDLE);
                }
            }
        });

        mAnimatorController.addAnimator(AnimatorBuilder.ANIM_SCROLL_BACK, animator);
        mAnimatorController.startAnimator(AnimatorBuilder.ANIM_SCROLL_BACK);
    }

    private boolean overScroll(int dY) {
        int y = (int) (getTranslationY() + dY);
        LogUtil.d("y : " + y);
        y = setViewTranslationY(getTranslationY() + dY);
        if (y == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void attachToParent(IPullToRefreshContainer iPullToRefresh) {
        mPullToRefreshContainer = iPullToRefresh;
        mPullToRefreshContainer.attachToAView(this);
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
    public int setViewTranslationY(float translationY) {
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
            mAnimatorController.cancelAllAnim();
            setTranslationY(transY);
            mAViewState.notifyScrollStateChanged(SCROLL_STATE_IDLE);
        } else {
            setTranslationY(transY);
        }

        return transY;
    }

    @Override
    public int getViewTranslationY() {
        return (int) getTranslationY();
    }

}
