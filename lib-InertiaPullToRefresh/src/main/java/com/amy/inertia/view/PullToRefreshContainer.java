package com.amy.inertia.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.IFooterView;
import com.amy.inertia.interfaces.IHeaderView;
import com.amy.inertia.interfaces.IPullToRefreshContainer;
import com.amy.inertia.interfaces.IPullToRefreshListener;

import java.util.ArrayList;
import java.util.List;

import static com.amy.inertia.util.LogUtil.d;
import static com.amy.inertia.util.LogUtil.e;


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
    public void animScrollBack(float start) {
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
        //mAnimatorController.addAnimator(AnimatorBuilder.ANIM_SCROLL_BACK, scrollBack);
        // mAnimatorController.startAnimator(AnimatorBuilder.ANIM_SCROLL_BACK);
    }

    @Override
    public void animScrollTo() {
    }

    @Override
    public void animOverFling() {
    }
}