package com.amy.library.inertia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.amy.library.interfaces.IFooterView;
import com.amy.library.interfaces.IHeaderView;

import java.util.HashMap;

public class InertiaPullToRefreshView extends FrameLayout {

    private Context mContext;
    private int mChildHeight;

    private boolean enableDebug = false;
    private String TAG = "AMY";

    private void log(String msg) {
        if (enableDebug) {
            Log.e(TAG, msg);
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
    private boolean isEnableInertiaOverScroll;

    //EnableOverScrollViewShow
    private boolean isEnableHeaderInertiaScrollViewShow;
    private boolean isEnableFooterInertiaScrollViewShow;

    //EnablePullToRefresh
    private boolean isEnableHeaderPullToRefresh;
    private boolean isEnableFooterPullToRefresh;

    //RefreshingState
    private boolean isHeaderRefreshing;
    private boolean isFooterRefreshing;

    //PullOverScrollHeaderMaxHeight
    private float mPullOverScrollHeaderMaxHeight;
    private float mPullOverScrollFooterMaxHeight;

    //TriggerRefreshHeight
    private float mTriggerHeaderRefreshHeight;
    private float mTriggerFooterRefreshHeight;

    //Animators
    private static final String ANIM_SCROLL_BACK = "ANIM_SCROLL_BACK";
    private static final String ANIM_INERTIA_OVER_SCROLL = "ANIM_INERTIA_OVER_SCROLL_RECYCLER_VIEW";
    private final HashMap<String, Animator> mAnimators = new HashMap<String, Animator>();

    //InertiaAnim
    private int mInertiaOverScrollAnimDuration;
    private float mInertiaOverScrollVyMax;
    private float mInertiaOverScrollVyDamp;
    private Interpolator mInertiaOverScrollInterpolator;

    //ScrollBackAnim
    private int mScrollBackAnimMinDuration;
    private int mScrollBackAnimMaxDuration;
    private float mScrollBackAnimDamp;
    private Interpolator mScrollBackAnimInterpolator;

    //KeyOfDefaultListener
    public static final String LISTENER_DEFAULT = "LISTENER_DEFAULT";
    private PullListenerAdapter mDefaultListener;

    //PullListeners
    private final HashMap<String, IPullListener> mPullListeners = new HashMap<String, IPullListener>();

    public InertiaPullToRefreshView(Context context) {
        this(context, null, 0);
    }

    public InertiaPullToRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InertiaPullToRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        mContext = context;

    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        initChildView();

        initFooterAndHeader();

        initPullListeners();

        initBooleanParams();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        initDefaultParams();
    }

    private void initBooleanParams() {
        //Booleans
        log("<------ Boolean Params------>");
        isEnableInertiaOverScroll = true;
        isHeaderRefreshing = false;
        isFooterRefreshing = false;
        isEnableFooterInertiaScrollViewShow = false;
        isEnableHeaderInertiaScrollViewShow = false;
        isEnableFooterPullToRefresh = true;
        isEnableHeaderPullToRefresh = true;

        log("IsEnableInertiaOverScroll : " + isEnableInertiaOverScroll);
        log("IsHeaderRefreshing : " + isHeaderRefreshing);
        log("IsFooterRefreshing : " + isFooterRefreshing);
        log("IsEnableFooterInertiaScrollViewShow : " + isEnableFooterInertiaScrollViewShow);
        log("IsEnableHeaderInertiaScrollViewShow : " + isEnableHeaderInertiaScrollViewShow);
        log("IsEnableFooterPullToRefresh : " + isEnableFooterPullToRefresh);
        log("IsEnableHeaderPullToRefresh : " + isEnableHeaderPullToRefresh);
    }

    private void initDefaultParams() {
        log("<------ Default Params ------>");
        mChildHeight = mChildView.getHeight();
        log("Child Height : " + mChildHeight);

        //PullToRefresh
        mPullOverScrollFooterMaxHeight = mChildHeight * 1f / 4f;
        mPullOverScrollHeaderMaxHeight = mChildHeight * 1f / 4f;
        mTriggerFooterRefreshHeight = mPullOverScrollFooterMaxHeight * 1f / 2f;
        mTriggerHeaderRefreshHeight = mPullOverScrollHeaderMaxHeight * 1f / 2f;
        log("====== PullToRefresh ======");
        log("PullOverScrollFooterMaxHeight : " + mPullOverScrollFooterMaxHeight);
        log("PullOverScrollHeaderMaxHeight : " + mPullOverScrollHeaderMaxHeight);
        log("TriggerFooterRefreshHeight : " + mTriggerFooterRefreshHeight);
        log("TriggerHeaderRefreshHeight : " + mTriggerHeaderRefreshHeight);

        //ScrollBack
        mScrollBackAnimMinDuration = 600;
        mScrollBackAnimMaxDuration = (int) (mChildHeight * 3f / 5f);
        mScrollBackAnimDamp = 7f / 10f;
        mScrollBackAnimInterpolator = new DecelerateInterpolator();
        log("====== ScrollBackAnim ======");
        log("ScrollBackMinDuration : " + mScrollBackAnimMinDuration);
        log("ScrollBackMaxDuration : " + mScrollBackAnimMaxDuration);
        log("ScrollBackDamp : " + mScrollBackAnimDamp);
        log("ScrollBackInterpolator : " + mScrollBackAnimInterpolator.getClass().getName());

        //InertiaOverScroll
        mInertiaOverScrollAnimDuration = 100;
        mInertiaOverScrollVyDamp = 0.13f;
        mInertiaOverScrollVyMax = mChildHeight * mInertiaOverScrollVyDamp;
        mInertiaOverScrollInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        };
        log("====== InertiaOverScrollAnim ======");
        log("InertiaOverScrollAnimDuration : " + mInertiaOverScrollAnimDuration);
        log("InertiaOverScrollVyDamp : " + mInertiaOverScrollVyDamp);
        log("InertiaOverScrollMaxVy : " + mInertiaOverScrollVyMax);
        log("InertiaOverScrollInterpolator : " + mInertiaOverScrollInterpolator.getClass().getName());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        init();
    }

    private void initChildView() {
        if (getChildCount() != 1) {
            throw new RuntimeException("Child count : " + getChildCount());
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

    private ObjectAnimator buildScrollBackAnimator(float translationY) {
        ObjectAnimator overScrollBackAnimator = ObjectAnimator.ofFloat(mChildView, "translationY", translationY, 0);
        int duration = Math.min(
                mScrollBackAnimMaxDuration,
                Math.max(Math.abs((int) (translationY * mScrollBackAnimDamp)),
                        mScrollBackAnimMinDuration)
        );
        log("scroll back duration : " + duration);
        overScrollBackAnimator.setDuration(duration);
        overScrollBackAnimator.setInterpolator(mScrollBackAnimInterpolator);
        overScrollBackAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        return overScrollBackAnimator;
    }

    private Animator buildInertiaOverScrollAnimator(float vY) {
        if (vY > 0 && mInertiaOverScrollVyMax < 0) {
            mInertiaOverScrollVyMax = -mInertiaOverScrollVyMax;
        } else if (vY < 0 && mInertiaOverScrollVyMax > 0) {
            mInertiaOverScrollVyMax = -mInertiaOverScrollVyMax;
        }
        final float finalVY = Math.abs(vY) < Math.abs(mInertiaOverScrollVyMax) ? vY : mInertiaOverScrollVyMax;
        log("finalVY : " + finalVY);
        ValueAnimator overScrollAnimator = ValueAnimator.ofFloat(finalVY, 0);
        overScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float dY = (float) animation.getAnimatedValue();
                mChildView.setTranslationY(mChildView.getTranslationY() + dY);
            }
        });
        overScrollAnimator.setDuration(mInertiaOverScrollAnimDuration);
        overScrollAnimator.setInterpolator(mInertiaOverScrollInterpolator);
        overScrollAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                float translationY = mChildView.getTranslationY();
                log("ChildView  TranslationY : " + translationY + " Size: " + Math.abs(translationY / mChildHeight));
                Animator scrollBackAnimator = buildScrollBackAnimator(mChildView.getTranslationY());
                mAnimators.put(ANIM_SCROLL_BACK, scrollBackAnimator);
                scrollBackAnimator.start();
            }

        });
        return overScrollAnimator;
    }

    private void initRecyclerViewScrollListener() {
        RecyclerView recyclerView = (RecyclerView) mChildView;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final int DY_SIZE = 2;
            int currentIndex = 0;
            int[] dyArray = new int[DY_SIZE];

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int childCount = recyclerView.getChildCount();
                boolean state = newState == RecyclerView.SCROLL_STATE_IDLE;
                boolean firstItemPosition = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0)) == 0;
                boolean firstItemTop = recyclerView.getChildAt(0).getTop() == 0;
                boolean lastItemPosition = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(childCount - 1))
                        == recyclerView.getAdapter().getItemCount() - 1;
                boolean lastItemBottom = recyclerView.getChildAt(childCount - 1).getBottom() == mChildView.getHeight();
                if (state &&
                        ((firstItemPosition && firstItemTop) || lastItemBottom && lastItemPosition)) {
                    int vY = 0;
                    for (int i = 0; i < DY_SIZE; i++) {
                        if (Math.abs(dyArray[i]) > Math.abs(vY)) {
                            vY = dyArray[i];
                        }
                    }

                    Animator inertiaOverScrollAnimator = buildInertiaOverScrollAnimator(-vY);
                    mAnimators.put(ANIM_INERTIA_OVER_SCROLL, inertiaOverScrollAnimator);
                    inertiaOverScrollAnimator.start();
                } else {

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (currentIndex >= DY_SIZE) {
                    currentIndex = 0;
                }

                dyArray[currentIndex++] = dy;
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
    public void enableDebug(boolean enable, String TAG) {
        enableDebug = enable;
        this.TAG = TAG;
    }

    /**
     *
     */
    public void clearOnPullListener() {
        mPullListeners.clear();
    }
}