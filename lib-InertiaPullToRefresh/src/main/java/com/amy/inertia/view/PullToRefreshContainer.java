package com.amy.inertia.view;

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
import com.amy.inertia.interfaces.OnScrollDetectorListener;
import com.amy.inertia.util.LogUtil;

import java.util.HashMap;

import static com.amy.inertia.util.LogUtil.d;
import static com.amy.inertia.view.AnimatorController.ANIM_OVER_SCROLL;
import static com.amy.inertia.view.AnimatorController.ANIM_SCROLL_BACK;
import static com.amy.inertia.view.AnimatorController.ANIM_SCROLL_TO;
import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_IDLE;
import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_OVER_FLING_FOOTER;
import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_OVER_FLING_HEADER;
import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_OVER_SCROLL_FOOTER;
import static com.amy.inertia.view.ScrollViewState.SCROLL_STATE_OVER_SCROLL_HEADER;


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
    private float mPullOverScrollDamp = 3f;

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
    private final HashMap<String, IPullListener> mPullListeners = new HashMap<String, IPullListener>();

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
        ScrollViewState scrollViewState = mAView.getScrollViewState();

        switch (newState) {
            case SCROLL_STATE_IDLE: {
                setInTouching(false);
                break;
            }
            case ScrollViewState.SCROLL_STATE_DRAGGING_IN_CONTENT: {
                setInTouching(false);
                break;
            }
            case ScrollViewState.SCROLL_STATE_SETTLING_IN_CONTENT: {
                setInTouching(false);
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
                LogUtil.d("Over fling header VY : " + scrollViewState.getVy());
                break;
            }
            case SCROLL_STATE_OVER_FLING_FOOTER: {
                setInTouching(true);
                LogUtil.d("Over fling footer VY : " + scrollViewState.getVy());
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
                LogUtil.d("Child View Action Down");
                mAnimatorController.pauseAllAnim();
                return;
            }
            case MotionEvent.ACTION_UP: {
                LogUtil.d("Child View Action Up");
                mAnimatorController.resumeAllAnim();
                return;
            }
            case MotionEvent.ACTION_MOVE: {
                LogUtil.d("Child View Action Move");
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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        LogUtil.d("on intercept : " + ev.getAction());
        if (isInTouching) {
            LogUtil.d("intercepted.");
            LogUtil.d("action : " + ev.getAction());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        ScrollViewState scrollViewState = mAView.getScrollViewState();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                scrollViewState.setTouchLastXY(e.getX(), e.getY());
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                scrollViewState.setTouchLastXY(e.getX(0), e.getY(0));
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                scrollViewState.setTouchDXY(e.getX(), e.getY());
                mAView.setViewTranslationY(mAView.getViewTranslationY() + scrollViewState.touchDY);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int indexOfUpPointer = e.getActionIndex();
                if (indexOfUpPointer == 0) {
                    scrollViewState.setTouchLastXY(e.getX(1), e.getY(1));
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                scrollViewState.resetTouch();
                break;
            }
        }

        return isInTouching;
    }

    public void animChildViewScrollBack() {
        float translationY = mAView.getViewTranslationY();
        d("animChildViewScrollBack : " + "translationY : " + translationY);
        int duration = (int) Math.abs(translationY);
        //mAnimatorController.pauseAllAnim();
        mAnimatorController.buildScrollBackAnimator(translationY, duration);
        mAnimatorController.startAnimator(ANIM_SCROLL_BACK);
    }

    public void animChildViewScrollTo(float start, float to, int duration) {
        d("animChildViewScrollTo : " + "start :" + start + " to : " + to + " duration : " + duration);
        mAnimatorController.pauseAllAnim();
        mAnimatorController.buildScrollToAnimator(start, to, duration);
        mAnimatorController.startAnimator(ANIM_SCROLL_TO);
    }

    public void animChildViewOverScroll(float distanceY) {
        d("animChildViewOverScroll : " + "dY : " + distanceY);
        mAnimatorController.cancelAllAnim();
        mAnimatorController.buildOverScrollAnimator(distanceY);
        mAnimatorController.startAnimator(ANIM_OVER_SCROLL);
    }

    private void handlePullingHeader(float fraction) {
        //LogUtil.d("pulling header fraction : " + fraction);
        for (IPullListener iPullListener : mPullListeners.values()) {
            iPullListener.onPullingHeader(this, fraction);
        }
    }

    private void handlePullingFooter(float fraction) {
        //LogUtil.d("pulling footer fraction : " + fraction);
        for (IPullListener iPullListener : mPullListeners.values()) {
            iPullListener.onPullingFooter(this, fraction);
        }
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
                for (IPullListener iPullListener : mPullListeners.values()) {
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
                for (IPullListener iPullListener : mPullListeners.values()) {
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

    public void setInTouching(boolean inTouching) {
        isInTouching = inTouching;
        if (isInTouching) {
            mAView.setInTouching(false);
        }
    }

    interface IPullListener {

        void onPullingHeader(PullToRefreshContainer pullToRefreshContainer, float fraction);

        void onPullingFooter(PullToRefreshContainer pullToRefreshContainer, float fraction);

        void onPullHeaderReleasing(PullToRefreshContainer pullToRefreshContainer, float fraction);

        void onPullFooterReleasing(PullToRefreshContainer pullToRefreshContainer, float fraction);

        void onHeaderRefresh(PullToRefreshContainer pullToRefreshContainer);

        void onFooterRefresh(PullToRefreshContainer pullToRefreshContainer);

        void onFinishHeaderRefresh();

        void onFinishFooterRefresh();

    }

    /************************************   API   ************************************/

    /**
     * FinishHeaderRefresh
     */
    public void finishHeaderRefresh() {
        isHeaderRefreshing = false;

        for (IPullListener iPullListener : mPullListeners.values()) {
            iPullListener.onFinishHeaderRefresh();
        }

        animChildViewScrollBack();
    }

    /**
     * FinishFooterRefresh
     */
    public void finishFooterRefresh() {
        isFooterRefreshing = false;

        for (IPullListener iPullListener : mPullListeners.values()) {
            iPullListener.onFinishFooterRefresh();
        }
        animChildViewScrollBack();
    }

    /**
     * Set Header View
     *
     * @param headerView
     */
    public void setHeaderView(final com.amy.inertia.interfaces.IHeaderView headerView) {
        if (headerView != null) {
            mHeaderContainer.removeAllViewsInLayout();
            mHeaderContainer.addView(headerView.getView());
        } else {
            mHeaderContainer.removeAllViewsInLayout();
        }
    }

    /**
     * Set Footer View
     *
     * @param footerView
     */
    public void setFooterView(final com.amy.inertia.interfaces.IFooterView footerView) {
        if (footerView != null) {
            mHeaderContainer.removeAllViewsInLayout();
            mHeaderContainer.addView(footerView.getView());
        } else {
            mFooterContainer.removeAllViewsInLayout();
        }
    }

    /**
     * 添加PullListener
     *
     * @param listenerName
     * @param pullListener
     */
    public void addOnPullListener(String listenerName, IPullListener pullListener) {
        mPullListeners.put(listenerName, pullListener);
    }

    /**
     * @param listenerName
     * @return
     */
    public IPullListener removeOnPullListener(String listenerName) {
        if (mPullListeners.containsKey(listenerName)) {
            return mPullListeners.remove(listenerName);
        } else {
            throw new IllegalArgumentException("PullListener : " + listenerName + " not exists.");
        }
    }

    /**
     *
     */
    public void clearOnPullListener() {
        mPullListeners.clear();
    }

    /**
     * Enable inertia over scroll
     * Default is true;
     *
     * @param enableInertiaOverScroll
     */
    public void setEnableInertiaOverScroll(boolean enableInertiaOverScroll) {
        isEnableInertiaOverScroll = enableInertiaOverScroll;
    }

    /**
     * Enable inertia header over scroll
     *
     * @param enableInertiaHeaderOverScroll Default is true, if you want make this work,
     *                                      please call {@link #setEnableInertiaOverScroll(boolean)}
     *                                      make sure {@link #isEnableInertiaOverScroll} is true.
     */
    public void setEnableInertiaHeaderOverScroll(boolean enableInertiaHeaderOverScroll) {
        isEnableInertiaHeaderOverScroll = enableInertiaHeaderOverScroll;
    }

    /**
     * Enable inertia footer over scroll
     *
     * @param enableInertiaFooterOverScroll
     */
    public void setEnableInertiaFooterOverScroll(boolean enableInertiaFooterOverScroll) {
        isEnableInertiaFooterOverScroll = enableInertiaFooterOverScroll;
    }

    /**
     * Enable inertia header views show when inertia over scroll started.
     *
     * @param enableInertiaOverScrollHeaderShow
     */
    public void setEnableInertiaOverScrollHeaderShow(boolean enableInertiaOverScrollHeaderShow) {
        isEnableInertiaOverScrollHeaderShow = enableInertiaOverScrollHeaderShow;
    }

    /**
     * Enable inertia footer views show when inertia over scroll started.
     *
     * @param enableInertiaOverScrollFooterShow
     */
    public void setEnableInertiaOverScrollFooterShow(boolean enableInertiaOverScrollFooterShow) {
        isEnableInertiaOverScrollFooterShow = enableInertiaOverScrollFooterShow;
    }

    /**
     * Enable header pull over scroll
     *
     * @param enableHeaderPullOverScroll
     */
    public void setEnableHeaderPullOverScroll(boolean enableHeaderPullOverScroll) {
        isEnableHeaderPullOverScroll = enableHeaderPullOverScroll;
    }

    /**
     * Enable footer pull over scroll
     *
     * @param enableFooterPullOverScroll
     */
    public void setEnableFooterPullOverScroll(boolean enableFooterPullOverScroll) {
        isEnableFooterPullOverScroll = enableFooterPullOverScroll;
    }

    public void setEnableHeaderPullToRefresh(boolean enableHeaderPullToRefresh) {
        isEnableHeaderPullToRefresh = enableHeaderPullToRefresh;
    }

    public void setEnableFooterPullToRefresh(boolean enableFooterPullToRefresh) {
        isEnableFooterPullToRefresh = enableFooterPullToRefresh;
    }

    public void setEnablePullOverScrollHeaderShow(boolean enablePullOverScrollHeaderShow) {
        isEnablePullOverScrollHeaderShow = enablePullOverScrollHeaderShow;
    }

    public void setEnablePullOverScrollFooterShow(boolean enablePullOverScrollFooterShow) {
        isEnablePullOverScrollFooterShow = enablePullOverScrollFooterShow;
    }

    public void setHeaderRefreshing(boolean headerRefreshing) {
        isHeaderRefreshing = headerRefreshing;
    }

    public void setFooterRefreshing(boolean footerRefreshing) {
        isFooterRefreshing = footerRefreshing;
    }

    public void setPullOverScrollDamp(float pullOverScrollDamp) {
        mPullOverScrollDamp = pullOverScrollDamp;
    }

    public void setHeaderPullMaxHeight(float headerPullMaxHeight) {
        mHeaderPullMaxHeight = headerPullMaxHeight;
    }

    public void setFooterPullMaxHeight(float footerPullMaxHeight) {
        mFooterPullMaxHeight = footerPullMaxHeight;
    }

    public void setHeaderTriggerRefreshHeight(float headerTriggerRefreshHeight) {
        mHeaderTriggerRefreshHeight = headerTriggerRefreshHeight;
    }

    public void setFooterTriggerRefreshHeight(float footerTriggerRefreshHeight) {
        mFooterTriggerRefreshHeight = footerTriggerRefreshHeight;
    }

}