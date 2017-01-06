package com.amy.library.inertia;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amy.library.interfaces.IFooterView;
import com.amy.library.interfaces.IHeaderView;

import java.util.HashMap;

public class InertiaPullToRefreshView extends FrameLayout {

    private boolean enableDebug = false;

    private void log(String msg) {
        if (enableDebug) {
            Log.e("AMY", msg);
        }
    }

    interface IPullListener {

        void onPullingDown(InertiaPullToRefreshView refreshLayout, float fraction);

        void onPullingUp(InertiaPullToRefreshView refreshLayout, float fraction);

        void onPullDownReleasing(InertiaPullToRefreshView refreshLayout, float fraction);

        void onPullUpReleasing(InertiaPullToRefreshView refreshLayout, float fraction);

        void onRefresh(InertiaPullToRefreshView refreshLayout);

        void onLoadMore(InertiaPullToRefreshView refreshLayout);

        void onFinishRefresh();

        void onFinishLoadMore();
    }

    /**
     * Header container
     */
    private FrameLayout mHeaderLayout;

    /**
     * Footer container
     */
    private FrameLayout mFooterLayout;

    /**
     * Header View
     */
    private IHeaderView mHeaderView;

    /**
     * Bottom View
     */
    private IFooterView mFooterView;

    /**
     * Child View
     */
    private View mChildView;

    //TouchPosition
    private float mTouchY;
    private float mTouchX;

    //EnableInertiaOverScroll
    private boolean isEnableHeaderInertiaOverScroll = true;
    private boolean isEnableFooterInertiaOverScroll = true;

    //EnableOverScrollViewShow
    private boolean isEnableHeaderInertiaScrollViewShow = false;
    private boolean isEnableFooterInertiaScrollViewShow = false;

    //EnablePullToRefresh
    private boolean isEnableHeaderPullToRefresh = true;
    private boolean isEnableFooterPullToRefresh = true;

    //RefreshingState
    private boolean isHeaderRefreshing = false;
    private boolean isFooterRefreshing = false;

    //InertiaOverScrollMaxHeight
    private float mInertiaOverScrollHeaderMaxHeight;
    private float mInertiaOverScrollFooterMaxHeight;

    //PullOverScrollHeaderMaxHeight
    private float mPullOverScrollHeaderMaxHeight;
    private float mPullOverScrollFooterMaxHeight;

    //TriggerRefreshHeight
    private float mTriggerHeaderRefreshHeight;
    private float mTriggerFooterRefreshHeight;

    private static final String ANIM_SCROLL_BACK = "SCROLL_BACK_ANIM";
    private static final String ANIM_INERTIA_OVER_SCROLL = "INERTIA_OVER_SCROLL_RECYCLER_VIEW";
    private final HashMap<String, Animator> mAnimators = new HashMap<String, Animator>();

    //KeyOfDefaultListener
    public static final String LISTENER_DEFAULT = "LISTENER_DEFAULT";

    //PullListeners
    private final HashMap<String, IPullListener> mPullListeners = new HashMap<String, IPullListener>();
    private PullListenerAdapter mDefaultListener;

    public InertiaPullToRefreshView(Context context) {
        this(context, null, 0);
    }

    public InertiaPullToRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InertiaPullToRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        initChildView();

        initFooterAndHeader();

        initPullListeners();
    }

    private void initChildView() {
        if (getChildCount() != 1) {
            throw new RuntimeException("Child count != 1");
        } else {
            mChildView = getChildAt(0);
        }

        if (mChildView instanceof RecyclerView) {
            initRecyclerViewScrollListener();
        } else {
            //Todo maybe support more views
            throw new IllegalArgumentException("ChildView is : " + mChildView.getClass().getName() + " not the right view");
        }
    }

    private void initRecyclerViewScrollListener() {
        RecyclerView recyclerView = (RecyclerView) mChildView;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                boolean state = (newState == RecyclerView.SCROLL_STATE_IDLE);
                boolean firstItemPosition = (recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0)) == 0);
                boolean firstItemTop = (recyclerView.getChildAt(0).getTop() == 0);
                if (state && firstItemPosition && firstItemTop) {
                    
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initFooterAndHeader() {
        //Init Header container
        mHeaderLayout = new FrameLayout(getContext());
        LayoutParams headerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        headerLayoutParams.gravity = Gravity.TOP;
        mHeaderLayout.setLayoutParams(headerLayoutParams);
        addView(mHeaderLayout, 0);

        //Init Footer container
        mFooterLayout = new FrameLayout(getContext());
        LayoutParams footerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        footerLayoutParams.gravity = Gravity.BOTTOM;
        mFooterLayout.setLayoutParams(footerLayoutParams);
        addView(mFooterLayout, 0);
    }

    private void initPullListeners() {
        mDefaultListener = new PullListenerAdapter() {
            @Override
            public void onPullingDown(InertiaPullToRefreshView refreshLayout, float fraction) {
            }

            @Override
            public void onPullingUp(InertiaPullToRefreshView refreshLayout, float fraction) {
            }

            @Override
            public void onPullDownReleasing(InertiaPullToRefreshView refreshLayout, float fraction) {
            }

            @Override
            public void onPullUpReleasing(InertiaPullToRefreshView refreshLayout, float fraction) {
            }

            @Override
            public void onRefresh(InertiaPullToRefreshView refreshLayout) {
            }

            @Override
            public void onLoadMore(InertiaPullToRefreshView refreshLayout) {
            }

            @Override
            public void onFinishRefresh() {
            }

            @Override
            public void onFinishLoadMore() {
            }

        };

        clearOnPullListener();
        addOnPullListener(LISTENER_DEFAULT, mDefaultListener);
    }

    private void initAnimators() {
        ObjectAnimator scrollBackAnimator = ObjectAnimator.ofFloat(mChildView, "translationY", mChildView.getTranslationY(), 0);
        mAnimators.put(ANIM_SCROLL_BACK, scrollBackAnimator);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /************************************   API   ************************************/

    /**
     * FinishHeaderRefresh
     */
    public void finishHeaderRefresh() {
        isHeaderRefreshing = false;

        for (IPullListener iPullListener : mPullListeners.values()) {
            iPullListener.onFinishRefresh();
        }
    }

    /**
     * FinishFooterRefresh
     */
    public void finishFooterRefresh() {
        isFooterRefreshing = false;

        for (IPullListener iPullListener : mPullListeners.values()) {
            iPullListener.onFinishLoadMore();
        }
    }

    /**
     * Set Header View
     *
     * @param headerView
     */
    public void setHeaderView(final IHeaderView headerView) {
        if (headerView != null) {
            mHeaderLayout.removeAllViewsInLayout();
            mHeaderLayout.addView(headerView.getView());
        } else {
            mHeaderLayout.removeAllViewsInLayout();
        }
    }

    /**
     * Set Footer View
     *
     * @param footerView
     */
    public void setFooterView(final IFooterView footerView) {
        if (footerView != null) {
            mHeaderLayout.removeAllViewsInLayout();
            mHeaderLayout.addView(footerView.getView());
        } else {
            mFooterLayout.removeAllViewsInLayout();
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
     * EnableDebug log default is false.
     *
     * @param enable
     */
    public void enableDebug(boolean enable) {
        enableDebug = enable;
    }

    /**
     *
     */
    public void clearOnPullListener() {
        mPullListeners.clear();
    }
}