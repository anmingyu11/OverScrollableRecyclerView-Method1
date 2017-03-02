package com.amy.inertia.view;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.IFooterView;
import com.amy.inertia.interfaces.IHeaderView;
import com.amy.inertia.interfaces.IPullToRefreshContainer;
import com.amy.inertia.interfaces.IPullToRefreshListener;
import com.amy.inertia.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import static com.amy.inertia.util.LogUtil.d;
import static com.amy.inertia.util.LogUtil.e;
import static com.amy.inertia.view.AnimatorBuilder.ANIM_OVER_FLING;
import static com.amy.inertia.view.AnimatorBuilder.ANIM_SCROLL_BACK;
import static com.amy.inertia.view.AnimatorBuilder.ANIM_SCROLL_TO;


public class PullToRefreshContainer extends FrameLayout implements IPullToRefreshContainer {

    private Context mContext;
    private int mTouchSlop;

    private FrameLayout mHeaderContainer;

    private FrameLayout mFooterContainer;

    //Header and Footer view.
    private IHeaderView mHeaderView;
    private IFooterView mFooterView;

    //ChildView
    private IAView mAView;

    //OverFling switch
    private boolean isEnableOverFling = true;
    private boolean isEnableOverFlingHeaderShow = false;
    private boolean isEnableOverFlingFooterShow = false;

    //OverFlingParams
    private int mOverFlingDuration = 100;
    private int mOverFlingMaxVY = 300;
    private Interpolator mOverFlingInterpolator = new LinearInterpolator();

    //OverScroll
    private boolean isEnableOverScroll = true;
    private boolean isEnableHeaderOverScroll = true;
    private boolean isEnableFooterOverScroll = true;
    private boolean isEnableHeaderOverScrollShow = false;
    private boolean isEnableFooterOverScrollShow = false;

    //OverScrollParams
    private float mOverScrollDamp = 0.3f;

    //Refresh
    private boolean isEnableHeaderPullToRefresh = true;
    private boolean isEnableFooterPullToRefresh = true;
    private boolean isHeaderRefreshing = false;
    private boolean isFooterRefreshing = false;

    //ScrollBackParams
    private int mScrollBackAnimMinDuration = 600;
    private int mScrollBackAnimMaxDuration = 1200;
    private float mScrollBackDamp = 7f / 10f;
    private Interpolator mScrollBackAnimInterpolator = new DecelerateInterpolator();

    //ScrollToParams
    private int mScrollToAnimMinDuration = 300;
    private int mScrollToAnimMaxDuration = 600;

    //Trigger and Max params
    private int mHeaderPullMaxHeight = 1000;
    private int mFooterPullMaxHeight = 1000;
    private int mHeaderTriggerRefreshHeight = 500;
    private int mFooterTriggerRefreshHeight = 500;

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

        if (isInEditMode()) {
            return;
        }

        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        init();
    }

    private void init() {

        initChildView();

        initFooterAndHeader();

        initDefaultPullListener();

    }

    private void initDefaultPullListener() {
        return;
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
            mAView.attachToParent(this);
        } else {
            throw new NullPointerException("ChildView cannot be null.");
        }
    }

    private void initFooterAndHeader() {
        //Init Header container
        mHeaderContainer = new FrameLayout(getContext());
        mHeaderContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                View hv = mHeaderView.getView();
            }
        });
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
    public void setHeaderView(final IHeaderView iHeaderView) {
        post(new Runnable() {
            @Override
            public void run() {
                mHeaderContainer.removeAllViewsInLayout();
                mHeaderContainer.addView(mHeaderView.getView());
            }
        });
        mHeaderView = iHeaderView;
    }

    @Override
    public void setFooterView(final IFooterView iFooterView) {
        post(new Runnable() {
            @Override
            public void run() {
                mHeaderContainer.removeAllViewsInLayout();
                mHeaderContainer.addView(iFooterView.getView());
            }
        });
        mFooterView = iFooterView;
    }

    @Override
    public void enableOverFling(boolean enable) {
        isEnableOverFling = enable;
    }

    @Override
    public void enableOverFlingHeaderShow(boolean enable) {
        isEnableOverFlingHeaderShow = enable;
    }

    @Override
    public void enableOverFlingFooterShow(boolean enable) {
        isEnableOverFlingFooterShow = enable;
    }

    @Override
    public void setOverFlingMaxVy(int maxVY) {
        mOverFlingMaxVY = maxVY;
    }

    @Override
    public void setOverFlingDuration(int duration) {
        mOverFlingDuration = duration;
    }

    @Override
    public void setOverFlingInterpolator(Interpolator interpolator) {
        mOverFlingInterpolator = interpolator;
    }

    @Override
    public void setScrollBackMinDuration(int duration) {
        mScrollBackAnimMinDuration = duration;
    }

    @Override
    public void setScrollBackMaxDuration(int duration) {
        mScrollBackAnimMaxDuration = duration;
    }

    @Override
    public void setScrollToMinDuration(int duration) {
        mScrollToAnimMinDuration = duration;
    }

    @Override
    public void setScrollToMaxDuration(int duration) {
        mScrollToAnimMaxDuration = duration;
    }

    @Override
    public void setScrollBackDamp(float damp) {
        mScrollBackDamp = damp;
    }

    @Override
    public void setScrollBackInterpolator(Interpolator interpolator) {
        mScrollBackAnimInterpolator = interpolator;
    }

    @Override
    public void enableOverScroll(boolean enable) {
        isEnableOverScroll = enable;
    }

    @Override
    public void enableHeaderOverScroll(boolean enable) {
        isEnableHeaderOverScroll = enable;
    }

    @Override
    public void enableFooterOverScroll(boolean enable) {
        isEnableFooterOverScroll = enable;
    }

    @Override
    public void enableHeaderOverScrollShow(boolean enable) {
        isEnableHeaderOverScrollShow = enable;
    }

    @Override
    public void enableFooterOverScrollShow(boolean enable) {
        isEnableFooterOverScrollShow = enable;
    }

    @Override
    public void setOverScrollPullDamp(float damp) {
        mOverScrollDamp = damp;
    }

    @Override
    public void finishHeaderRefresh() {

    }

    @Override
    public void finishFooterRefresh() {

    }

    @Override
    public void enableHeaderPullToRefresh(boolean enable) {
        isEnableHeaderPullToRefresh = enable;
    }

    @Override
    public void enableFooterPullToRefresh(boolean enable) {
        isEnableFooterPullToRefresh = enable;
    }

    @Override
    public boolean isHeaderRefreshing() {
        return isHeaderRefreshing;
    }

    @Override
    public boolean isFooterRefreshing() {
        return isFooterRefreshing;
    }

    @Override
    public boolean addIPullListener(IPullToRefreshListener iPullToRefreshListener) {
        return mPullToRefreshListeners.add(iPullToRefreshListener);
    }

    @Override
    public boolean removeIPullListener(IPullToRefreshListener iPullToRefreshListener) {
        return mPullToRefreshListeners.remove(iPullToRefreshListener);
    }

    @Override
    public void pullingHeader(final float currentHeight) {
        mHeaderContainer.getLayoutParams().height = (int) currentHeight;
        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingHeader(currentHeight / mHeaderTriggerRefreshHeight);
        }
        mHeaderContainer.requestLayout();
    }

    @Override
    public void pullingFooter(float currentHeight) {
        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingFooter(currentHeight / mFooterPullMaxHeight);
        }
    }

    @Override
    public void headerReleasing(float currentHeight, float headerHeight) {
        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onHeaderReleasing(currentHeight / headerHeight);
        }
    }

    @Override
    public void footerReleasing(float currentHeight, float footerHeight) {
        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onFooterReleasing(currentHeight / footerHeight);
        }
    }

    @Override
    public void startHeaderAnim(float currentHeight) {
        mHeaderView.startAnim(mHeaderPullMaxHeight, currentHeight);
    }

    @Override
    public void startFooterAnim(float currentHeight) {
        mFooterView.startAnim(mFooterPullMaxHeight, currentHeight);
    }

    @Override
    public void setHeaderTriggerRefreshHeight(int triggerHeight) {
        mHeaderTriggerRefreshHeight = triggerHeight;
    }

    @Override
    public void setFooterTriggerRefreshHeight(int triggerHeight) {
        mFooterTriggerRefreshHeight = triggerHeight;
    }

    @Override
    public void setHeaderPullMaxHeight(int maxHeight) {
        mHeaderPullMaxHeight = maxHeight;
    }

    @Override
    public void setFooterPullMaxHeight(int maxHeight) {
        mFooterPullMaxHeight = maxHeight;
    }

    @Override
    public int getHeaderTriggerRefreshHeight() {
        return mHeaderTriggerRefreshHeight;
    }

    @Override
    public int getFooterTriggerRefreshHeight() {
        return mFooterTriggerRefreshHeight;
    }

    @Override
    public int getHeaderPullMaxHeight() {
        return mHeaderPullMaxHeight;
    }

    @Override
    public int getFooterPullMaxHeight() {
        return mFooterPullMaxHeight;
    }

    @Override
    public void attachToAView(IAView iaView) {
        mAView = iaView;
    }

    @Override
    public List<IPullToRefreshListener> getPullListenerGroup() {
        return null;
    }

    @Override
    public Animator buildScrollBackAnim(float start) {
        final String key = ANIM_SCROLL_BACK;
        d(key + " : " + " start : " + start);

        if (start < 1f && start > -1f) {
            e(key + " cannot be built.");
            mAView.setViewTranslationY(0f);
            return null;
        }

        int duration = Math.min(
                mScrollBackAnimMaxDuration,
                Math.max(Math.abs((int) (start * mScrollBackDamp)),
                        mScrollBackAnimMinDuration)
        );

        LogUtil.d("duration : " + duration);

        Animator scrollBack =
                mAnimatorBuilder.buildScrollBackAnimator(mPullToRefreshListeners,
                        mAView,
                        start,
                        duration,
                        mScrollBackAnimInterpolator);

        return scrollBack;
    }

    @Override
    public Animator buildOverFlingAnim(float vY) {
        final String key = ANIM_OVER_FLING;

        if (vY < 1f && vY > -1f) {
            e(key + " cannot be built.");
            mAView.setViewTranslationY(0f);
            return null;
        }

        if (vY > 0 && mOverFlingMaxVY < 0) {
            mOverFlingMaxVY = -mOverFlingMaxVY;
        } else if (vY < 0 && mOverFlingMaxVY > 0) {
            mOverFlingMaxVY = -mOverFlingMaxVY;
        }

        final float finalVy = Math.abs(vY) < Math.abs(mOverFlingMaxVY) ? vY : mOverFlingMaxVY;
        d(key + " finalVY : " + finalVy);

        Animator overFling =
                mAnimatorBuilder.buildOverFlingAnimator(mAView,
                        finalVy,
                        mOverFlingDuration,
                        mOverFlingInterpolator);

        return overFling;
    }

    @Override
    public Animator buildScrollToTriggerAnim() {
        if (mAView.getViewTranslationY() > 0) {
            return buildScrollToAnim(mAView.getViewTranslationY(), mHeaderTriggerRefreshHeight);
        } else if (mAView.getViewTranslationY() < 0) {
            return buildScrollToAnim(mAView.getViewTranslationY(), -mHeaderTriggerRefreshHeight);
        } else {
            return null;
        }
    }

    private Animator buildScrollToAnim(float start, float to) {
        final String key = ANIM_SCROLL_TO;
        d(key + " : " + " start : " + start + " to : " + to);

        int duration = Math.min(
                mScrollToAnimMaxDuration,
                Math.max(Math.abs((int) ((start - to) * mScrollBackDamp)),
                        mScrollToAnimMinDuration)
        );

        Animator scrollTo = mAnimatorBuilder.buildScrollToAnim(mPullToRefreshListeners,
                mAView,
                to,
                duration,
                mScrollBackAnimInterpolator);

        return scrollTo;
    }

}