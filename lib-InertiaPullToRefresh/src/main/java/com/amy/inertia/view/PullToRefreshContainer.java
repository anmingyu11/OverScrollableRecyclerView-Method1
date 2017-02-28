package com.amy.inertia.view;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
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


public class PullToRefreshContainer extends FrameLayout implements IPullToRefreshContainer {

    private Context mContext;
    private int mTouchSlop;

    private FrameLayout mHeaderContainer;

    private FrameLayout mFooterContainer;

    private IHeaderView mHeaderView;

    private IFooterView mFooterView;

    private IAView mAView;

    private boolean isEnableOverFling = true;

    private boolean isEnableOverFlingHeaderShow = false;
    private boolean isEnableOverFlingFooterShow = false;
    private boolean isEnableHeaderPullOverScroll = true;
    private boolean isEnableFooterPullOverScroll = true;

    private boolean isEnableHeaderPullToRefresh = false;
    private boolean isEnableFooterPullToRefresh = false;

    private boolean isEnablePullOverScrollHeaderShow = false;
    private boolean isEnablePullOverScrollFooterShow = false;

    //RefreshingState
    private boolean isHeaderRefreshing = false;
    private boolean isFooterRefreshing = false;

    private int mMaxOverFlingVY = 300;

    /**
     * Bigger you pull harder.
     */
    private float mPullDamp = 0.3f;

    //ScrollBack
    private int mScrollBackAnimMinDuration = 600;
    private int mScrollBackAnimMaxDuration = 1200;
    private float mScrollBackAnimDamp = 7f / 10f;
    private Interpolator mScrollBackAnimInterpolator = new DecelerateInterpolator();

    //OverFling
    private int mOverFlingAnimDuration = 100;
    private float mOverScrollVyDamp = 0.13f;
    private float mOverFlingVyMax = 2440 * 0.13f;
    private Interpolator mOverFlingInterpolator = new LinearInterpolator();

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
            }

            @Override
            public void onPullFooterReleasing(float fraction) {
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
            mAView.attachToParent(this);
        } else {
            throw new NullPointerException("ChildView cannot be null.");
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

    public void handleHeaderRefresh() {
    }

    public void handleFooterRefresh() {
    }

    @Override
    public void attachToAView(IAView iaView) {
        mAView = iaView;
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
                Math.max(Math.abs((int) (start * mScrollBackAnimDamp)),
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

        if (vY > 0 && mMaxOverFlingVY < 0) {
            mMaxOverFlingVY = -mMaxOverFlingVY;
        } else if (vY < 0 && mMaxOverFlingVY > 0) {
            mMaxOverFlingVY = -mMaxOverFlingVY;
        }

        final float finalVy = Math.abs(vY) < Math.abs(mMaxOverFlingVY) ? vY : mMaxOverFlingVY;
        d(key + " finalVY : " + finalVy);

        Animator overFling =
                mAnimatorBuilder.buildOverFlingAnimator(mAView,
                        finalVy,
                        mOverFlingAnimDuration,
                        null);

        return overFling;
    }

    @Override
    public Animator buildScrollToAnim() {
        return null;
    }

}