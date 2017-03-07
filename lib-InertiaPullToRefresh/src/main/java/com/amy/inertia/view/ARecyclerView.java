package com.amy.inertia.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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

import static com.amy.inertia.view.AViewState.STATE_IDLE;
import static com.amy.inertia.view.AViewState.STATE_FOOTER_REFRESHING;
import static com.amy.inertia.view.AViewState.STATE_HEADER_REFRESHING;
import static com.amy.inertia.view.AViewState.STATE_OVER_FLING_FOOTER;
import static com.amy.inertia.view.AViewState.STATE_OVER_FLING_HEADER;
import static com.amy.inertia.view.AViewState.STATE_OVER_SCROLL_FOOTER;
import static com.amy.inertia.view.AViewState.STATE_OVER_SCROLL_HEADER;
import static com.amy.inertia.view.AViewState.STATE_SETTLING_IN_CONTENT;
import static com.amy.inertia.view.AnimatorBuilder.ANIM_SCROLL_TO;

public class ARecyclerView extends RecyclerView implements IAView {

    private IPullToRefreshContainer mPullToRefreshContainer = null;

    private IAnimatorController mAnimatorController = new AnimatorController(this);

    private final AViewState mAViewState = new AViewState(this, getContext());

    private AViewParams mAViewParams = null;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FINISH_HEADER_REFRESH: {
                    animScrollBack(STATE_OVER_FLING_HEADER);
                    break;
                }
                case FINISH_FOOTER_REFRESH: {
                    animScrollBack(STATE_OVER_FLING_FOOTER);
                    break;
                }
            }
        }
    };

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
            int currentScrollState = mAViewState.CurrentState;
            if (currentScrollState == AViewState.STATE_DRAGGING_IN_CONTENT
                    || currentScrollState == AViewState.STATE_SETTLING_IN_CONTENT) {
                mAViewState.notifyScrollStateChanged(state);//Scroll state to idle
            }
        }

    }

    @Override
    public void onScrolled(int dx, final int dy) {
        super.onScrolled(dx, dy);

        //LogUtil.printTraceStack("onScrolled");
        //LogUtil.d("onScrolled dy : " + dy + " state : " + mAViewState.CurrentState);
        mAViewState.storeVy(dy);

        if (getScrollState() == SCROLL_STATE_SETTLING) {
            boolean isScrollToTop = ScrollUtil.isChildScrollToTop(this) && !ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollToBottom = !ScrollUtil.isChildScrollToTop(this) && ScrollUtil.isChildScrollToBottom(this);
            boolean isScrollInContent = ScrollUtil.isChildScrollToBottom(this) && ScrollUtil.isChildScrollToTop(this);

            if (isScrollInContent) {
                mAViewState.notifyScrollStateChanged(STATE_SETTLING_IN_CONTENT);
            } else if (isScrollToBottom) {
                //d("IsScrollToBottom : " + Util.spellArray(mAViewState.VYArray));
                if (mAViewParams.isEnableOverFling) {
                    animOverFling(mAViewState.getVy(), STATE_OVER_FLING_FOOTER);
                }
            } else if (isScrollToTop) {
                //d("IsScrollToTop : " + Util.spellArray(mAViewState.VYArray));
                if (mAViewParams.isEnableOverFling) {
                    animOverFling(mAViewState.getVy(), STATE_OVER_FLING_HEADER);
                }
            }
        }

    }

    public AViewState getAViewState() {
        return mAViewState;
    }

    @Override
    public void sendMessage(Message message) {
        mHandler.sendMessage(message);
    }

    @Override
    public void attachToParent(IPullToRefreshContainer iPullToRefresh, AViewParams aViewParams) {
        mPullToRefreshContainer = iPullToRefresh;
        mAViewParams = aViewParams;
        mPullToRefreshContainer.attachToAView(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        final float height = getHeight();
        final float transY = getTranslationY();
        final float scrollDamp = mAViewParams.mOverScrollDamp * Math.abs(height - Math.abs(transY)) / height;

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mAViewState.setTouchLastXY(e);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mAViewState.setTouchDXY(e, scrollDamp);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mAViewState.getLastAction() == MotionEvent.ACTION_POINTER_UP) {
                    mAViewState.setTouchLastXY(e);
                } else {
                    mAViewState.setTouchDXY(e, scrollDamp);
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
                break;
            }
        }
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mAViewParams.isFooterRefreshing || mAViewParams.isHeaderRefreshing) {
            return false;
        }
        if (mAnimatorController.hasAnimatorCurrentlyRunning()) {
            return true;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //LogUtil.d("touchAction : " + e.getActionMasked());
        final MotionEvent finalE = MotionEvent.obtain(e);
        mAViewState.storeMotionEvent(finalE);
        boolean result = handleTouchEvent(finalE);
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
            case MotionEvent.ACTION_CANCEL: {
                return onTouchCancel(e);
            }
            default: {
                return super.onTouchEvent(e);
            }
        }
    }

    private boolean onTouchCancel(MotionEvent e) {
        mAViewState.resetTouch();
        mAnimatorController.cancelAllAnim();
        setViewTranslationY(0f);
        mAViewState.notifyScrollStateChanged(SCROLL_STATE_IDLE);
        return super.onTouchEvent(e);
    }

    private boolean onTouchDown(MotionEvent e, int pointerId) {
        //LogUtil.e("----------onTouchDown----------");
        //Util.printTouchInfo(e);
        //mAViewState.setTouchLastXY(e);
        //LogUtil.e("onTouchDown : ");
        int state = mAViewState.CurrentState;

        mAnimatorController.cancelAllAnim();

        mPullToRefreshContainer.changeHeaderOrFooterVisibility(mAViewParams.isEnableOverScrollHeaderShow, mAViewParams.isEnableOverScrollFooterShow);

        /*if (mAViewState.CurrentState == STATE_IDLE ||
                mAViewState.CurrentState == STATE_DRAGGING_IN_CONTENT ||
                mAViewState.CurrentState == STATE_SETTLING_IN_CONTENT) {
            mPullToRefreshContainer.changeHeaderOrFooterVisibility(mAViewParams.isEnableOverScrollHeaderShow,
                    mAViewParams.isEnableOverScrollFooterShow);
        }*/

        switch (state) {
            case STATE_OVER_FLING_FOOTER: {
                mAViewState.notifyScrollStateChanged(STATE_OVER_SCROLL_FOOTER);
                return true;
            }
            case STATE_OVER_FLING_HEADER: {
                mAViewState.notifyScrollStateChanged(STATE_OVER_SCROLL_HEADER);
                return true;
            }
            case STATE_FOOTER_REFRESHING: {
                mAViewState.notifyScrollStateChanged(STATE_OVER_SCROLL_FOOTER);
                return true;
            }
            case STATE_HEADER_REFRESHING: {
                mAViewState.notifyScrollStateChanged(STATE_OVER_SCROLL_HEADER);
                return true;
            }
            case STATE_OVER_SCROLL_FOOTER:
            case STATE_OVER_SCROLL_HEADER: {
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
        return super.onTouchEvent(e);
    }

    private boolean onTouchMove(MotionEvent e, int pointerId) {
        //LogUtil.e("----------onTouchMove----------");
        //Util.printTouchInfo(e);
        boolean fingerScrollUp;
        boolean fingerScrollDown;


        if (mAViewState.touchDY > 0) {
            fingerScrollDown = true;
            fingerScrollUp = false;
        } else if (mAViewState.touchDY < 0) {
            fingerScrollDown = false;
            fingerScrollUp = true;
        } else {
            fingerScrollDown = fingerScrollUp = false;
        }

        if (mAViewState.CurrentState == STATE_OVER_SCROLL_FOOTER
                || mAViewState.CurrentState == STATE_OVER_SCROLL_HEADER
                || mAViewState.CurrentState == STATE_FOOTER_REFRESHING
                || mAViewState.CurrentState == STATE_HEADER_REFRESHING) {

            int beforeOverScrollY = getViewTranslationY();
            int y = (int) (getTranslationY() + mAViewState.touchDY);

            boolean result = false;

            if (mAViewParams.isEnableOverScroll) {
                result = overScroll(mAViewState.touchDY);
            } else {
                return super.onTouchEvent(e);
            }

            if (result) {
                return true;
            } else {
                //LogUtil.e("event  : " + e.getActionMasked());
                //Todo : this is the best i can do
                MotionEvent[] es = mAViewState.getMotionEvents();
                for (int i = 0; i < es.length; i++) {
                    if (i == es.length - 1) {
                        super.onTouchEvent(Util.fakeAUpMotionEvent(es[es.length - 1]));
                    } else {
                        super.onTouchEvent(es[i]);
                    }
                }
                LogUtil.d("state : " + AViewState.SCROLL_STATES[getScrollState()]);
                return true;
                /*
                int dY = (y - beforeOverScrollY);
                LogUtil.d("v= " + dY);
                smoothScrollBy(0,dY);*/
            }
        }

        if (ScrollUtil.isChildScrollToBottom(this)
                && !ScrollUtil.isChildScrollToTop(this)
                && fingerScrollUp
                && !fingerScrollDown) {
            //Dragging to bottom
            mAViewState.notifyScrollStateChanged(STATE_OVER_SCROLL_FOOTER);
            return true;

        } else if (!ScrollUtil.isChildScrollToBottom(this)
                && ScrollUtil.isChildScrollToTop(this)
                && !fingerScrollUp
                && fingerScrollDown) {
            //Dragging to top
            mAViewState.notifyScrollStateChanged(STATE_OVER_SCROLL_HEADER);
            return true;
        } else if (ScrollUtil.isChildScrollToTop(this)
                && ScrollUtil.isChildScrollToBottom(this)) {
            //Dragging in content
            mAViewState.notifyScrollStateChanged(AViewState.STATE_DRAGGING_IN_CONTENT);
            return super.onTouchEvent(e);
        }

        return super.onTouchEvent(e);

    }

    private boolean onTouchPointerUp(MotionEvent e, int pointerId) {
        //LogUtil.e("----------onTouchPointerUp----------");
        //Util.printTouchInfo(e);
        return super.onTouchEvent(e);
    }

    private boolean onTouchUp(MotionEvent e) {
        //LogUtil.e("----------onTouchUp----------");
        //Util.printTouchInfo(e);
        int state = mAViewState.CurrentState;

        //LogUtil.d("transY : " + getTranslationY());
        int transY = getViewTranslationY();


        switch (state) {
            case STATE_OVER_SCROLL_HEADER: {
                if (mAViewParams.isEnableFooterPullToRefresh
                        && Math.abs(transY) > mAViewParams.mHeaderTriggerRefreshHeight) {
                    //Todo scroll to
                    //animScrollTo(mAViewParams.mHeaderTriggerRefreshHeight);
                    animScrollBack(STATE_OVER_FLING_HEADER);
                } else {
                    animScrollBack(STATE_OVER_FLING_HEADER);
                }
                return false;
            }
            case STATE_OVER_SCROLL_FOOTER: {
                if (mAViewParams.isEnableFooterPullToRefresh
                        && Math.abs(transY) > mAViewParams.mHeaderTriggerRefreshHeight) {
                    //Todo scroll to
                    //animScrollTo(mAViewParams.mFooterTriggerRefreshHeight);
                    animScrollBack(STATE_OVER_FLING_FOOTER);
                } else {
                    animScrollBack(STATE_OVER_FLING_FOOTER);
                }
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
                mPullToRefreshContainer.changeHeaderOrFooterVisibility(mAViewParams.isEnableOverFlingHeaderShow,
                        mAViewParams.isEnableOverFlingFooterShow);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (getTranslationY() > 0) {
                    animScrollBack(STATE_OVER_FLING_HEADER);
                } else if (getTranslationY() < 0) {
                    animScrollBack(STATE_OVER_FLING_FOOTER);
                } else {
                    mAViewState.notifyScrollStateChanged(SCROLL_STATE_IDLE);
                }
            }
        });

        mAnimatorController.addAnimator(AnimatorBuilder.ANIM_OVER_FLING, animator);
        mAnimatorController.startAnimator(AnimatorBuilder.ANIM_OVER_FLING);
    }

    private void animScrollTo(float to) {
        mAnimatorController.cancelAllAnim();

        ValueAnimator animator = (ValueAnimator) mPullToRefreshContainer.buildScrollToTriggerAnim();

        if (animator == null) {
            return;
        }

        //int currentHeight = getViewTranslationY();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                int transY = getViewTranslationY();

                if (transY == -mAViewParams.mFooterTriggerRefreshHeight) {
                    mPullToRefreshContainer.footerRefresh();
                }
                if (transY == mAViewParams.mHeaderTriggerRefreshHeight) {
                    mPullToRefreshContainer.headerRefresh();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                int transY = getViewTranslationY();
                if (transY > 0) {
                    mAViewState.notifyScrollStateChanged(STATE_HEADER_REFRESHING);
                } else {
                    mAViewState.notifyScrollStateChanged(STATE_FOOTER_REFRESHING);
                }
            }
        });

        mAnimatorController.addAnimator(ANIM_SCROLL_TO, animator);
        mAnimatorController.startAnimator(ANIM_SCROLL_TO);
    }

    //Todo scroll back anim
    private void animScrollBack(final int notify) {
        mAnimatorController.cancelAllAnim();

        ValueAnimator animator = (ValueAnimator) mPullToRefreshContainer.buildScrollBackAnim(getTranslationY(),
                Util.getDuration(getViewTranslationY()));

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
                int transY = getViewTranslationY();
                transY = adjustTransY(transY);
                //LogUtil.d("transY : " + transY);
                if (transY > 0) {
                    mAViewState.notifyScrollStateChanged(AViewState.STATE_OVER_SCROLL_HEADER);
                } else if (transY < 0) {
                    mAViewState.notifyScrollStateChanged(AViewState.STATE_OVER_SCROLL_FOOTER);
                } else {
                    mAViewState.notifyScrollStateChanged(STATE_IDLE);
                }
            }
        });

        mAnimatorController.addAnimator(AnimatorBuilder.ANIM_SCROLL_BACK, animator);
        mAnimatorController.startAnimator(AnimatorBuilder.ANIM_SCROLL_BACK);
    }


    private int adjustTransY(int transY) {
        if (transY > 0 && transY < 3) {
            return 0;
        } else if (transY < 0 && transY > -3) {
            return 0;
        } else {
            return transY;
        }
    }

    private boolean overScroll(int dY) {
        int y = (int) (getTranslationY() + dY);
        //LogUtil.d("y : " + y);

        if (y == 0) {
            return false;
        }

        if (y > 0 && y > mPullToRefreshContainer.getHeaderPullMaxHeight()) {
            return true;
        } else if (y < 0 && y < -mPullToRefreshContainer.getFooterPullMaxHeight()) {
            return true;
        }

        mPullToRefreshContainer.changeHeaderOrFooterVisibility(true, true);

        if (y > 0 && mAViewParams.isEnableHeaderOverScroll) {
            y = setViewTranslationY(getTranslationY() + dY);
            mPullToRefreshContainer.pullingHeader(y);
        } else if (y < 0 && mAViewParams.isEnableFooterOverScroll) {
            y = setViewTranslationY(getTranslationY() + dY);
            mPullToRefreshContainer.pullingFooter(y);
        }

        return true;
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
        final float overScrollDamp = mAViewParams.mOverScrollDamp;
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
