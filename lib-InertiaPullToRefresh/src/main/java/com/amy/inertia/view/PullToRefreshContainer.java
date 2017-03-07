package com.amy.inertia.view;

import android.animation.Animator;
import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
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

    private final AViewParams mAViewParams = new AViewParams();

    //PullListeners
    private final List<IPullToRefreshListener> mPullToRefreshListeners = new ArrayList<>();

    private final AnimatorBuilder mAnimatorBuilder = AnimatorBuilder.getInstance(mAViewParams);

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
            mAView.attachToParent(this, mAViewParams);
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
        mAViewParams.isEnableOverFling = enable;
    }

    @Override
    public void enableOverFlingHeaderShow(boolean enable) {
        mAViewParams.isEnableOverFlingHeaderShow = enable;
    }

    @Override
    public void enableOverFlingFooterShow(boolean enable) {
        mAViewParams.isEnableOverFlingFooterShow = enable;
    }

    @Override
    public void setOverFlingMaxVy(int maxVY) {
        mAViewParams.mOverFlingMaxVY = maxVY;
    }

    @Override
    public void setOverFlingDuration(int duration) {
        mAViewParams.mOverFlingDuration = duration;
    }

    @Override
    public void setOverFlingInterpolator(Interpolator interpolator) {
        mAViewParams.mOverFlingInterpolator = interpolator;
    }

    @Override
    public void setScrollBackMinDuration(int duration) {
        mAViewParams.mScrollBackAnimMinDuration = duration;
    }

    @Override
    public void setScrollBackMaxDuration(int duration) {
        mAViewParams.mScrollBackAnimMaxDuration = duration;
    }

    @Override
    public void setScrollToMinDuration(int duration) {
        mAViewParams.mScrollToAnimMinDuration = duration;
    }

    @Override
    public void setScrollToMaxDuration(int duration) {
        mAViewParams.mScrollToAnimMaxDuration = duration;
    }

    @Override
    public void setScrollBackDamp(float damp) {
        mAViewParams.mScrollBackDamp = damp;
    }

    @Override
    public void setScrollBackInterpolator(Interpolator interpolator) {
        mAViewParams.mScrollBackAnimInterpolator = interpolator;
    }

    @Override
    public void enableOverScroll(boolean enable) {
        mAViewParams.isEnableOverScroll = enable;
    }

    @Override
    public void enableHeaderOverScroll(boolean enable) {
        mAViewParams.isEnableHeaderOverScroll = enable;
    }

    @Override
    public void enableFooterOverScroll(boolean enable) {
        mAViewParams.isEnableFooterOverScroll = enable;
    }

    @Override
    public void enableOverScrollHeaderShow(boolean enable) {
        mAViewParams.isEnableOverScrollHeaderShow = enable;
    }

    @Override
    public void enableOverScrollFooterShow(boolean enable) {
        mAViewParams.isEnableOverScrollFooterShow = enable;
    }

    @Override
    public void setOverScrollPullDamp(float damp) {
        mAViewParams.mOverScrollDamp = damp;
    }

    @Override
    public void enableHeaderPullToRefresh(boolean enable) {
        mAViewParams.isEnableHeaderPullToRefresh = enable;
    }

    @Override
    public void enableFooterPullToRefresh(boolean enable) {
        mAViewParams.isEnableFooterPullToRefresh = enable;
    }

    @Override
    public boolean isHeaderRefreshing() {
        return mAViewParams.isHeaderRefreshing;
    }

    @Override
    public boolean isFooterRefreshing() {
        return mAViewParams.isFooterRefreshing;
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
        float fraction = currentHeight / mAViewParams.mHeaderTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mHeaderContainer.getLayoutParams().height = (int) currentHeight;
        mHeaderContainer.requestLayout();

        if (mHeaderView != null) {
            mHeaderView.onPulling(fraction);
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingHeader(fraction, currentHeight);
        }
    }

    @Override
    public void pullingFooter(float currentHeight) {
        float fraction = currentHeight / mAViewParams.mFooterTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mFooterContainer.getLayoutParams().height = (int) currentHeight;
        mFooterContainer.requestLayout();

        if (mFooterView != null) {
            mFooterView.onPulling(fraction);
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onPullingFooter(fraction, currentHeight);
        }
    }

    @Override
    public void headerReleasing(float currentHeight) {
        float fraction = currentHeight / mAViewParams.mHeaderTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mHeaderContainer.getLayoutParams().height = (int) currentHeight;
        mHeaderContainer.requestLayout();

        if (mHeaderView != null) {
            mHeaderView.onReleasing(fraction);
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onHeaderReleasing(fraction, currentHeight);
        }
    }

    @Override
    public void footerReleasing(float currentHeight) {
        float fraction = currentHeight / mAViewParams.mFooterTriggerRefreshHeight;
        fraction = Math.abs(fraction);
        mFooterContainer.getLayoutParams().height = (int) currentHeight;
        mFooterContainer.requestLayout();

        if (mFooterView != null) {
            mFooterView.onReleasing(fraction);
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onFooterReleasing(fraction, currentHeight);
        }
    }

    @Override
    public void headerRefresh() {
        if (mHeaderView != null) {
            mHeaderView.onRefresh(mAViewParams.mHeaderPullMaxHeight, mAView.getViewTranslationY());
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onHeaderRefresh();
        }
    }

    @Override
    public void footerRefresh() {
        if (mFooterView != null) {
            mFooterView.onRefresh(mAViewParams.mFooterPullMaxHeight, mAView.getViewTranslationY());
        }

        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onFooterRefresh();
        }
    }

    @Override
    public void finishHeaderRefresh() {
        Message msg = new Message();
        msg.what = IAView.FINISH_HEADER_REFRESH;
        mAView.sendMessage(msg);
        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onFinishHeaderRefresh();
        }
    }

    @Override
    public void finishFooterRefresh() {
        Message msg = new Message();
        msg.what = IAView.FINISH_FOOTER_REFRESH;
        mAView.sendMessage(msg);
        for (IPullToRefreshListener iPullToRefreshListener : mPullToRefreshListeners) {
            iPullToRefreshListener.onFinishFooterRefresh();
        }
    }

    public void setHeaderTriggerRefreshHeight(int triggerHeight) {
        mAViewParams.mHeaderTriggerRefreshHeight = triggerHeight;
    }

    @Override
    public void setFooterTriggerRefreshHeight(int triggerHeight) {
        mAViewParams.mFooterTriggerRefreshHeight = triggerHeight;
    }

    @Override
    public void setHeaderPullMaxHeight(int maxHeight) {
        mAViewParams.mHeaderPullMaxHeight = maxHeight;
    }

    @Override
    public void setFooterPullMaxHeight(int maxHeight) {
        mAViewParams.mFooterPullMaxHeight = maxHeight;
    }

    @Override
    public int getHeaderTriggerRefreshHeight() {
        return mAViewParams.mHeaderTriggerRefreshHeight;
    }

    @Override
    public int getFooterTriggerRefreshHeight() {
        return mAViewParams.mFooterTriggerRefreshHeight;
    }

    @Override
    public int getHeaderPullMaxHeight() {
        return mAViewParams.mHeaderPullMaxHeight;
    }

    @Override
    public int getFooterPullMaxHeight() {
        return mAViewParams.mFooterPullMaxHeight;
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
    public Animator buildScrollBackAnim(float start, int duration) {
        final String key = ANIM_SCROLL_BACK;

        if (start < 1f && start > -1f) {
            e(key + " cannot be built.");
            mAView.setViewTranslationY(0f);
            return null;
        }

        /*int duration = Math.min(
                mAViewParams.mScrollBackAnimMaxDuration,
                Math.max(Math.abs((int) (start * mAViewParams.mScrollBackDamp)),
                        mAViewParams.mScrollBackAnimMinDuration)
        );*/

        d("key" + " : " + " start : " + start + " duration : " + duration);

        Animator scrollBack =
                mAnimatorBuilder.buildScrollBackAnimator(this,
                        mAView,
                        start,
                        duration,
                        mAViewParams.mScrollBackAnimInterpolator);

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

        if (vY > 0 && mAViewParams.mOverFlingMaxVY < 0) {
            mAViewParams.mOverFlingMaxVY = -mAViewParams.mOverFlingMaxVY;
        } else if (vY < 0 && mAViewParams.mOverFlingMaxVY > 0) {
            mAViewParams.mOverFlingMaxVY = -mAViewParams.mOverFlingMaxVY;
        }

        final float finalVy = Math.abs(vY) < Math.abs(mAViewParams.mOverFlingMaxVY) ? vY : mAViewParams.mOverFlingMaxVY;
        d(key + " finalVY : " + finalVy);

        Animator overFling =
                mAnimatorBuilder.buildOverFlingAnimator(
                        this,
                        mAView,
                        vY > 0 ? mAViewParams.mHeaderTriggerRefreshHeight : mAViewParams.mFooterTriggerRefreshHeight,
                        finalVy,
                        mAViewParams.mOverFlingDuration,
                        mAViewParams.mOverFlingInterpolator);

        return overFling;
    }

    @Override
    public void changeHeaderOrFooterVisibility(boolean headerShow, boolean footerShow) {
        //LogUtil.d("headerShow : " + headerShow + " footerShow : " + footerShow);
        if (mHeaderView != null) {
            mHeaderView.setVisible(headerShow);
        }

        if (mFooterView != null) {
            mFooterView.setVisible(footerShow);
        }
    }

    @Override
    public Animator buildScrollToTriggerAnim() {
        Animator animator = null;

        int currentHeight = mAView.getViewTranslationY();

        if (currentHeight > 0) {
            animator = buildScrollToAnim(mAView.getViewTranslationY(), mAViewParams.mHeaderTriggerRefreshHeight);
        } else if (currentHeight < 0) {
            animator = buildScrollToAnim(mAView.getViewTranslationY(), -mAViewParams.mHeaderTriggerRefreshHeight);
        }

        return animator;
    }

    private Animator buildScrollToAnim(float start, float to) {
        final String key = ANIM_SCROLL_TO;

        int duration = Math.min(
                mAViewParams.mScrollToAnimMaxDuration,
                Math.max(Math.abs((int) ((start - to) * mAViewParams.mScrollBackDamp)),
                        mAViewParams.mScrollToAnimMinDuration)
        );

        Animator scrollTo = mAnimatorBuilder.buildScrollToAnim(this,
                mAView,
                to,
                duration,
                mAViewParams.mScrollBackAnimInterpolator);

        return scrollTo;
    }

}