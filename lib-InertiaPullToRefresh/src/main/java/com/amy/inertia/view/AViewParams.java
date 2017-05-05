package com.amy.inertia.view;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import com.amy.inertia.ViscousInterpolator;
import com.amy.inertia.util.ScrollerUtil;

public final class AViewParams {

    private Context mContext;

    int maxOverFlingDistance;
    int maxOverScrollDistance;
    int maxVelocity;
    int minVelocity;

    //OverFling switch
    boolean isEnableOverFling = true;
    boolean isEnableOverFlingHeaderShow = false;
    boolean isEnableOverFlingFooterShow = false;

    //OverFlingParams
    int mOverFlingDuration = 100;
    int mOverFlingMaxVY = 300;
    //Interpolator mOverFlingInterpolator = ScrollerUtil.sQuinticInterpolator;
    Interpolator mOverFlingInterpolator = ScrollerUtil.sQuinticInterpolator;

    //OverScroll
    boolean isEnableOverScroll = true;
    boolean isEnableHeaderOverScroll = true;
    boolean isEnableFooterOverScroll = true;
    boolean isEnableOverScrollHeaderShow = true;
    boolean isEnableOverScrollFooterShow = true;

    //OverScrollParams
    float mOverScrollDamp = 0.5f;

    //Refresh
    boolean isEnableHeaderPullToRefresh = true;
    boolean isEnableFooterPullToRefresh = true;
    boolean isHeaderRefreshing = false;
    boolean isFooterRefreshing = false;

    //ScrollBackParams
    int mScrollBackAnimMinDuration = 600;
    int mScrollBackAnimMaxDuration = 1200;
    float mScrollBackDamp = 7f / 10f;
    Interpolator mScrollBackAnimInterpolator = new ViscousInterpolator();

    //ScrollToParams
    int mScrollToAnimMinDuration = 300;
    int mScrollToAnimMaxDuration = 600;

    //Trigger and Max params
    int mHeaderPullMaxHeight = 2240;
    int mFooterPullMaxHeight = 2240;
    int mHeaderTriggerRefreshHeight = 500;
    int mFooterTriggerRefreshHeight = 500;

    public AViewParams(Context context) {
        mContext = context;
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
        minVelocity = configuration.getScaledMinimumFlingVelocity();
    }
}
