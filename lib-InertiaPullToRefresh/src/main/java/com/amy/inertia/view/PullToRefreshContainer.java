package com.amy.inertia.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.IAnimatorController;
import com.amy.inertia.interfaces.IFooterView;
import com.amy.inertia.interfaces.IHeaderView;
import com.amy.inertia.interfaces.IPullToRefreshContainer;
import com.amy.inertia.interfaces.IPullToRefreshListener;
import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import static com.amy.inertia.util.LogUtil.d;
import static com.amy.inertia.util.LogUtil.e;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_IDLE;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_FLING_FOOTER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_FLING_HEADER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_SCROLL_FOOTER;
import static com.amy.inertia.view.AViewState.SCROLL_STATE_OVER_SCROLL_HEADER;


public class PullToRefreshContainer extends FrameLayout implements IPullToRefreshContainer {

    private Context mContext;
    private int mTouchSlop;

    private FrameLayout mHeaderContainer;

    private FrameLayout mFooterContainer;

    private IHeaderView mHeaderView;

    private IFooterView mFooterView;

    private IAView mAView;

    private float mTouchX;
    private float mTouchY;
    private boolean isInTouching = false;

    private boolean isEnableInertiaOverScroll = true;

    private boolean isEnableInertiaHeaderOverScroll = true;
    private boolean isEnableInertiaFooterOverScroll = true;

    private boolean isEnableInertiaOverScrollHeaderShow = false;
    private boolean isEnableInertiaOverScrollFooterShow = false;
    private boolean isEnableHeaderPullOverScroll = true;
    private boolean isEnableFooterPullOverScroll = true;

    private boolean isEnableHeaderPullToRefresh = false;
    private boolean isEnableFooterPullToRefresh = false;

    private boolean isEnablePullOverScrollHeaderShow = false;
    private boolean isEnablePullOverScrollFooterShow = false;

    //RefreshingState
    private boolean isHeaderRefreshing = false;
    private boolean isFooterRefreshing = false;

    /**
     * Bigger you pull harder.
     */
    private float mPullDamp = 0.3f;
    /**
     * Default is your childView Height
     */
    private float mHeaderPullMaxHeight = 10000;

    /**
     * Default is your childView Height
     */
    private float mFooterPullMaxHeight = 10000;

    /**
     * Default is 1/4 size of your childView Height, also back to this height.
     */
    private float mHeaderTriggerRefreshHeight = 500;

    /**
     * Default is 1/4 size of your childView Height, also back to this height.
     */
    private float mFooterTriggerRefreshHeight = 500;

    //Animator
    private IAnimatorController mAnimatorController = null;

    //PullListeners
    private final List<IPullToRefreshListener> mPullToRefreshListeners = new ArrayList<>();

    private AnimatorBuilder mAnimatorBuilder = AnimatorBuilder.getInstance();

    public PullToRefreshContainer(Context context) {
        this(context, null, 0);
    }

    public PullToRefreshContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshContainer(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        initChildView();

        initFooterAndHeader();

        initDefaultPullListener();
    }

    private void initDefaultPullListener() {
        mPullToRefreshListeners.add(new IPullToRefreshListener() {
            @Override
            public void onPullingHeader(float fraction) {
            }

            @Override
            public void onPullingFooter(float fraction) {
            }

            @Override
            public void onPullHeaderReleasing(float fraction) {
                mAView.setViewTranslationY(mAView.getViewTranslationY() * fraction);
            }

            @Override
            public void onPullFooterReleasing(float fraction) {
                mAView.setViewTranslationY(mAView.getViewTranslationY() * fraction);
            }

            @Override
            public void onHeaderRefresh() {
            }

            @Override
            public void onFooterRefresh() {
            }

            @Override
            public void onFinishHeaderRefresh() {
            }

            @Override
            public void onFinishFooterRefresh() {
            }
        });
    }

    private void initChildView() {
        if (getChildCount() != 1) {
            throw new RuntimeException("Child count : " + getChildCount());
        } else {
            if (getChildAt(0) instanceof IAView) {
                mAView = (IAView) getChildAt(0);
            } else {
                throw new IllegalArgumentException("Child view must be IAView");
            }
        }

        if (mAView != null) {
            mAnimatorController = new AnimatorController(mAView);

            mAView.attachToAnimatorController(mAnimatorController);
            mAView.attachToParent(this);

            initChildScrollDetector();
        } else {
            throw new NullPointerException("ChildView cannot be null.");
        }
    }

    private void handleAViewState(int newState) {
        AViewState aViewState = mAView.getAViewState();

        switch (newState) {
            case SCROLL_STATE_IDLE: {
                mAView.setInTouching(true);
                break;
            }
            case AViewState.SCROLL_STATE_DRAGGING_IN_CONTENT: {
                mAView.setInTouching(true);
                break;
            }
            case AViewState.SCROLL_STATE_SETTLING_IN_CONTENT: {
                mAView.setInTouching(true);
                break;
            }
            case SCROLL_STATE_OVER_SCROLL_HEADER: {
                setInTouching(true);
                break;
            }
            case SCROLL_STATE_OVER_SCROLL_FOOTER: {
                setInTouching(true);
                break;
            }
            case SCROLL_STATE_OVER_FLING_HEADER: {
                setInTouching(true);
                d("Over fling header VY : " + aViewState.getVy());
                break;
            }
            case SCROLL_STATE_OVER_FLING_FOOTER: {
                setInTouching(true);
                d("Over fling footer VY : " + aViewState.getVy());
                break;
            }
        }
    }

    private void initChildScrollDetector() {
        mAView.addScrollDetectorListener(new OnScrollDetectorListener() {

            @Override
            public void onScrollStateChanged(int newState) {
                handleAViewState(newState);
            }

            @Override
            public void onNotFullVisible() {

            }

            @Override
            public void onFullVisible() {

            }

        });
    }

    private void processChildViewTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                d("Child View Action Down");
                mAnimatorController.pauseAllAnim();
                return;
            }
            case MotionEvent.ACTION_UP: {
                d("Child View Action Up");
                mAnimatorController.resumeAllAnim();
                return;
            }
            case MotionEvent.ACTION_MOVE: {
                d("Child View Action Move");
                return;
            }
        }
    }

    private void initFooterAndHeader() {
        //Init Header container
        mHeaderContainer = new FrameLayout(getContext());
        LayoutParams headerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        headerLayoutParams.gravity = Gravity.TOP;
        mHeaderContainer.setLayoutParams(headerLayoutParams);
        addView(mHeaderContainer, 0);

        //Init Footer container
        mFooterContainer = new FrameLayout(getContext());
        LayoutParams footerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        footerLayoutParams.gravity = Gravity.BOTTOM;
        mFooterContainer.setLayoutParams(footerLayoutParams);
        addView(mFooterContainer, 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result;
        if (isInTouching) {
            result = onTouchEvent(ev);
        } else {
            if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                result = mAView.aViewDispatchTouch(ev);
            } else {
                result = super.dispatchTouchEvent(ev);
            }
        }

        //LogUtil.e("TouchingTest Container dispatching inTouching : " + isInTouching + " result : " + result);
        //LogUtil.w("TouchingTest Container dispatching : y : " + ev.getY() + " action : " + ev.getActionMasked());

        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result;

        if (isInTouching) {
            result = true;
        } else {
            result = false;
        }

        //e("TouchingTest Container intercepting inTouching : " + isInTouching + " result : " + result);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean result;
        result = onHandleTouchEvent(e);
        //e("TouchingTest Container onTouching inTouching : " + isInTouching + " result : " + result);
        return result;
    }

    private void handleHeaderRefresh() {
  /*      float translationY = mChildView.getTranslationY();
        int duration =
                Math.min(
                        mAnimatorController.mScrollToAnimMaxDuration,
                        Math.max(mAnimatorController.mScrollToAnimMinDuration,
                                (int) (Math.abs(translationY) - mHeaderTriggerRefreshHeight)));
        animChildViewScrollTo(translationY, mHeaderTriggerRefreshHeight, duration);
        mAnimatorController.getAnimator(ANIM_SCROLL_TO).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isHeaderRefreshing = true;
                for (IPullListener iPullListener : mPullToRefreshListeners.values()) {
                    iPullListener.onHeaderRefresh(PullToRefreshContainer.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isHeaderRefreshing = false;
                animChildViewScrollBack();
            }
        });*/
    }

    private void handleFooterRefresh() {
    /*    float translationY = mChildView.getTranslationY();
        int duration = (int) (Math.abs(translationY) - mFooterTriggerRefreshHeight);
        animChildViewScrollTo(translationY, mFooterTriggerRefreshHeight, duration);
        mAnimatorController.getAnimator(ANIM_SCROLL_TO).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isHeaderRefreshing = true;
                for (IPullListener iPullListener : mPullToRefreshListeners.values()) {
                    iPullListener.onFooterRefresh(PullToRefreshContainer.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isFooterRefreshing = false;
                animChildViewScrollBack();
            }
        });*/
    }

    @Override
    public void attachToAView(IAView iaView) {
        mAView = iaView;
    }

    public void setInTouching(boolean inTouching) {
        if (isInTouching == inTouching) {
            return;
        }
        LogUtil.d("Container inTouching : " + inTouching);

        isInTouching = inTouching;
        if (isInTouching) {
            mAView.setInTouching(false);
        }
    }

    private boolean onHandleTouchEvent(MotionEvent e) {
        AViewState aViewState = mAView.getAViewState();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {

                mAnimatorController.pauseAllAnim();

                aViewState.setTouchLastXY(e);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                aViewState.setTouchLastXY(e);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                aViewState.setTouchDXY(e);

                int dY = (int) aViewState.touchDY;
                int transY = Math.abs(mAView.getViewTranslationY());

                //LogUtil.d("TranslationY : " + transY + " height : " + mAView.getViewHeight());

                if (transY < mAView.getViewHeight() * 0.5f) {
                    dY *= mPullDamp;
                } else if (transY > mAView.getViewHeight() * 0.5f && transY < mAView.getViewHeight() * 0.8f) {
                    dY *= mPullDamp * mPullDamp;
                } else {
                    dY *= mPullDamp * mPullDamp * mPullDamp;
                }

                mAView.setViewTranslationY(mAView.getViewTranslationY() + dY);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int indexOfUpPointer = e.getActionIndex();
                if (indexOfUpPointer == 0) {
                    aViewState.setTouchLastXY(e);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                aViewState.resetTouch();
                mAnimatorController.cancelAllAnim();
                animScrollBack(mAView.getViewTranslationY());
                break;
            }
        }
        return isInTouching;
    }

    private void animScrollBack(float start) {
        //Todo: to handle if has other anim running.

        d("animChildViewScrollBack : " + "translationY : " + start);

        if (start < 1f && start > -1f) {
            e("anim no start.");
            mAView.setViewTranslationY(0f);
            return;
        }

        int duration = (int) Math.abs(start);

        ValueAnimator scrollBack =
                mAnimatorBuilder.buildScrollBackAnimator(mPullToRefreshListeners,
                        start,
                        duration,
                        null);
        scrollBack.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                AViewState aViewState = mAView.getAViewState();
                if (mAView.getViewTranslationY() > 0) {
                    aViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_OVER_SCROLL_FOOTER);
                } else if (mAView.getViewTranslationY() < 0) {
                    aViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_OVER_SCROLL_HEADER);
                } else {
                    aViewState.notifyScrollStateChanged(AViewState.SCROLL_STATE_IDLE);
                }
            }
        });
        mAnimatorController.addAnimator(AnimatorBuilder.ANIM_SCROLL_BACK, scrollBack);
        mAnimatorController.startAnimator(AnimatorBuilder.ANIM_SCROLL_BACK);
    }

    private void animScrollTo() {
    }

    private void animOverFling() {
    }
}