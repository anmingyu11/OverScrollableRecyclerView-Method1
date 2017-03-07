package com.amy.inertia.interfaces;

import android.animation.Animator;
import android.view.animation.Interpolator;

import java.util.List;

public interface IPullToRefreshContainer {

    void setHeaderView(IHeaderView iHeaderView);

    void setFooterView(IFooterView iFooterView);

    void changeHeaderOrFooterVisibility(boolean headerShow, boolean footerShow);

    //Trigger and Max.
    void setHeaderTriggerRefreshHeight(int triggerHeight);

    void setFooterTriggerRefreshHeight(int triggerHeight);

    void setHeaderPullMaxHeight(int maxHeight);

    void setFooterPullMaxHeight(int maxHeight);

    int getHeaderTriggerRefreshHeight();

    int getFooterTriggerRefreshHeight();

    int getHeaderPullMaxHeight();

    int getFooterPullMaxHeight();

    //OverFling
    void enableOverFling(boolean enable);

    void enableOverFlingHeaderShow(boolean enable);

    void enableOverFlingFooterShow(boolean enable);

    void setOverFlingMaxVy(int maxVY);

    void setOverFlingDuration(int duration);

    void setOverFlingInterpolator(Interpolator interPolator);

    //ScrollBackAnim
    void setScrollBackMinDuration(int duration);

    void setScrollBackMaxDuration(int duration);

    void setScrollToMinDuration(int duration);

    void setScrollToMaxDuration(int duration);

    void setScrollBackDamp(float damp);

    void setScrollBackInterpolator(Interpolator interpolator);

    //OverScroll
    void enableOverScroll(boolean enable);

    void enableHeaderOverScroll(boolean enable);

    void enableFooterOverScroll(boolean enable);

    void enableOverScrollHeaderShow(boolean enable);

    void enableOverScrollFooterShow(boolean enable);

    void setOverScrollPullDamp(float damp);

    //Refresh

    void enableHeaderPullToRefresh(boolean enable);

    void enableFooterPullToRefresh(boolean enable);

    boolean isHeaderRefreshing();

    boolean isFooterRefreshing();

    //Pull listener
    boolean addIPullListener(IPullToRefreshListener iPullToRefreshListener);

    boolean removeIPullListener(IPullToRefreshListener iPullToRefreshListener);

    //pulling releasing refreshing view changed
    void pullingHeader(float currentHeight);

    void pullingFooter(float currentHeight);

    void headerReleasing(float currentHeight);

    void footerReleasing(float currentHeight);

    void headerRefresh();

    void footerRefresh();

    void finishHeaderRefresh();

    void finishFooterRefresh();

    //Inside functions
    void attachToAView(IAView iaView);

    List<IPullToRefreshListener> getPullListenerGroup();

    Animator buildScrollBackAnim(float start,int duration);

    Animator buildOverFlingAnim(float vY);

    Animator buildScrollToTriggerAnim();

}
