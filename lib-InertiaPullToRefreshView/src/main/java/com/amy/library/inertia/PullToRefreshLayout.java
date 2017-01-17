package com.amy.library.inertia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.amy.library.LogUtil;
import com.amy.library.Util;
import com.amy.library.interfaces.IFooterView;
import com.amy.library.interfaces.IHeaderView;
import com.amy.scrolldetector.OnScrollDetectorListenerAdapter;
import com.amy.scrolldetector.ScrollDetector;
import com.amy.scrolldetector.ScrollUtil;

import java.util.HashMap;


public class PullToRefreshLayout extends FrameLayout {

    private Context mContext;
    private int mTouchSlop;

    //Pull state
    private static final int PULLING_HEADER = 1;
    private static final int PULLING_FOOTER = 2;
    private static final int FLINGING = 3;
    private int mState = 0;

    /**
     * HeaderViews container
     */
    private FrameLayout mHeaderContainer;

    /**
     * FooterViews container
     */
    private FrameLayout mFooterContainer;

    /**
     * Header View
     */
    private IHeaderView mHeaderView;

    /**
     * Bottom View
     */
    private IFooterView mFooterView;

    private View mChildView;

    private GestureDetector mGestureDetector;

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
    public static final String ANIM_SCROLL_BACK = "ANIM_SCROLL_BACK";
    public static final String ANIM_SCROLL_TO = "ANIM_SCROLL_TO";
    public static final String ANIM_OVER_SCROLL = "ANIM_OVER_SCROLL";
    private final AnimatorController mAnimatorController = new AnimatorController(mChildView);

    //PullListeners
    private final HashMap<String, IPullListener> mPullListeners = new HashMap<String, IPullListener>();

    public PullToRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {

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

        initGestureDetector();

        initFooterAndHeader();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    private void initChildView() {
        if (getChildCount() != 1) {
            throw new RuntimeException("Child count : " + getChildCount());
        } else {
            mChildView = getChildAt(0);
        }

        if (mChildView == null) {
            throw new NullPointerException("ChildView cannot be null.");
        } else if (mChildView instanceof RecyclerView) {
            initRecyclerViewScrollListener();
        } else if (mChildView instanceof ScrollView) {
            //Todo do this in future
            throw new IllegalArgumentException("ChildView is : " + mChildView.getClass().getName() + " not the right view");
        } else if (mChildView instanceof WebView) {
            //Todo do this in future
            throw new IllegalArgumentException("ChildView is : " + mChildView.getClass().getName() + " not the right view");
        } else if (mChildView instanceof AbsListView) {
            //Todo do this in future
            throw new IllegalArgumentException("ChildView is : " + mChildView.getClass().getName() + " not the right view");
        } else {
            throw new IllegalArgumentException("ChildView is : " + mChildView.getClass().getName() + " not the right view");
        }
    }

    private void initGestureDetector() {
    }

    private void initRecyclerViewScrollListener() {
        ScrollDetector.detectScroll((RecyclerView) mChildView, new OnScrollDetectorListenerAdapter() {
            final int DY_SIZE = 2;
            int currentIndexOfdYArray = 0;
            int[] dYArray = new int[DY_SIZE];

            @Override
            public void onScrolled(View view, int dx, int dy) {
                if (currentIndexOfdYArray > 1) {
                    currentIndexOfdYArray = 0;
                }

                dYArray[currentIndexOfdYArray++] = dy;
            }

            @Override
            public void onScrollToBottom() {
                if (!isInTouching) {
                    LogUtil.d(Util.spellArray(dYArray));

                    int max = Util.getMaxAbs(dYArray);

                    animChildViewOverScroll(-max);
                }
            }

            @Override
            public void onScrollToTop() {
                if (!isInTouching) {
                    LogUtil.d(Util.spellArray(dYArray));

                    int max = Util.getMaxAbs(dYArray);

                    animChildViewOverScroll(-max);
                }
            }
        });
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
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchX = ev.getX();
                mTouchY = ev.getY();
                //mAnimatorController.pauseAllAnim();
                //mAnimatorController.cancelAllAnim();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float dX = ev.getX() - mTouchX;
                float dY = ev.getY() - mTouchY;
                if (Math.abs(dX) <= Math.abs(dY)) {
                    if (dY > 0
                            && !ScrollUtil.isChildScrollToBottom(mChildView)
                            && (isEnableHeaderPullOverScroll || isEnableHeaderPullToRefresh)) {
                        LogUtil.d("Intercepted state is pulling header ");
                        mState = PULLING_HEADER;
                        return true;
                    } else if (dY < 0
                            && !ScrollUtil.isChildScrollToTop(mChildView)
                            && (isEnableFooterPullOverScroll || isEnableFooterPullToRefresh)) {
                        LogUtil.d("Intercepted state is pulling footer ");
                        mState = PULLING_FOOTER;
                        return true;
                    }
                }
                break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isHeaderRefreshing | isFooterRefreshing) {
            return super.onTouchEvent(event);
        }

        isInTouching = true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                float pullOverScrollDamp = 1f / mPullOverScrollDamp;
                float dY = event.getY() - mTouchY;

                if (mState == PULLING_HEADER
                        && (isEnableHeaderPullOverScroll || isEnableHeaderPullToRefresh)) {

                    mChildView.setTranslationY(dY * pullOverScrollDamp);
                    mHeaderContainer.getLayoutParams().height = (int) Math.abs(dY * pullOverScrollDamp);
                    //LogUtil.d("pulling header : " + "dy : " + dY + " headerContainerHeight : " + dY * pullOverScrollDamp);
                    mHeaderContainer.requestLayout();

                    handlePullingHeader(dY / mHeaderTriggerRefreshHeight);
                } else if (mState == PULLING_FOOTER
                        && (isEnableFooterPullOverScroll || isEnableFooterPullToRefresh)) {

                    mChildView.setTranslationY(dY * pullOverScrollDamp);
                    mHeaderContainer.getLayoutParams().height = (int) Math.abs(dY * pullOverScrollDamp);
                    //LogUtil.d("pulling footer : " + "dy : " + dY + " headerContainerHeight : " + dY * pullOverScrollDamp);
                    mHeaderContainer.requestLayout();

                    handlePullingFooter(dY / mFooterTriggerRefreshHeight);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mState == PULLING_HEADER) {
                    if (isEnableHeaderPullToRefresh
                            && Math.abs(mChildView.getTranslationY()) > mHeaderTriggerRefreshHeight) {
                        LogUtil.d("handleHeaderRefresh");
                        handleHeaderRefresh();
                    } else if (isEnableHeaderPullToRefresh || isEnableHeaderPullOverScroll) {
                        animChildViewScrollBack();
                    }
                } else if (mState == PULLING_FOOTER) {
                    if (isEnableFooterPullToRefresh
                            && Math.abs(mChildView.getTranslationY()) > mFooterTriggerRefreshHeight) {
                        handleFooterRefresh();
                    } else if (isEnableFooterPullOverScroll || isEnableFooterPullToRefresh) {
                        animChildViewScrollBack();
                    }
                } else if (mState == FLINGING) {
                }
                break;
            }
        }

        isInTouching = false;
        return super.onTouchEvent(event);
    }

    private void animChildViewScrollBack() {
        float translationY = mChildView.getTranslationY();
        LogUtil.d("animChildViewScrollBack : " + "translationY : " + translationY);
        int duration = (int) Math.abs(translationY);
        mAnimatorController.cancelAllAnim();
        mAnimatorController.buildScrollBackAnimator(translationY, duration);
        mAnimatorController.startAnimator(ANIM_SCROLL_BACK);
    }

    private void animChildViewScrollTo(float start, float to, int duration) {
        LogUtil.d("animChildViewScrollTo : " + "start :" + start + " to : " + to + " duration : " + duration);
        mAnimatorController.cancelAllAnim();
        mAnimatorController.buildScrollToAnimator(start, to, duration);
        mAnimatorController.startAnimator(ANIM_SCROLL_TO);
    }

    private void animChildViewOverScroll(float distanceY) {
        LogUtil.d("animChildViewOverScroll : " + "dY : " + distanceY);
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
        float translationY = mChildView.getTranslationY();
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
                    iPullListener.onHeaderRefresh(PullToRefreshLayout.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isHeaderRefreshing = false;
                animChildViewScrollBack();
            }
        });
    }

    private void handleFooterRefresh() {
        float translationY = mChildView.getTranslationY();
        int duration = (int) (Math.abs(translationY) - mFooterTriggerRefreshHeight);
        animChildViewScrollTo(translationY, mFooterTriggerRefreshHeight, duration);
        mAnimatorController.getAnimator(ANIM_SCROLL_TO).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isHeaderRefreshing = true;
                for (IPullListener iPullListener : mPullListeners.values()) {
                    iPullListener.onFooterRefresh(PullToRefreshLayout.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isFooterRefreshing = false;
                animChildViewScrollBack();
            }
        });
    }

    interface IPullListener {

        void onPullingHeader(PullToRefreshLayout pullToRefreshLayout, float fraction);

        void onPullingFooter(PullToRefreshLayout pullToRefreshLayout, float fraction);

        void onPullHeaderReleasing(PullToRefreshLayout pullToRefreshLayout, float fraction);

        void onPullFooterReleasing(PullToRefreshLayout pullToRefreshLayout, float fraction);

        void onHeaderRefresh(PullToRefreshLayout pullToRefreshLayout);

        void onFooterRefresh(PullToRefreshLayout pullToRefreshLayout);

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
    public void setHeaderView(final IHeaderView headerView) {
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
    public void setFooterView(final IFooterView footerView) {
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