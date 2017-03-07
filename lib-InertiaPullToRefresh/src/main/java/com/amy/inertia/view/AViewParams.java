package com.amy.inertia.view;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public final class AViewParams {

    //OverFling switch
    boolean isEnableOverFling = true;
    boolean isEnableOverFlingHeaderShow = false;
    boolean isEnableOverFlingFooterShow = false;

    //OverFlingParams
    int mOverFlingDuration = 100;
    int mOverFlingMaxVY = 300;
    Interpolator mOverFlingInterpolator = new LinearInterpolator();

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
    Interpolator mScrollBackAnimInterpolator = new DecelerateInterpolator();

    //ScrollToParams
    int mScrollToAnimMinDuration = 300;
    int mScrollToAnimMaxDuration = 600;

    //Trigger and Max params
    int mHeaderPullMaxHeight = 2240;
    int mFooterPullMaxHeight = 2240;
    int mHeaderTriggerRefreshHeight = 500;
    int mFooterTriggerRefreshHeight = 500;

}
