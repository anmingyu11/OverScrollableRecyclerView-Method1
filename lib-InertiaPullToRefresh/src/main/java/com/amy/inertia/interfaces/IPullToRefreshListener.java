package com.amy.inertia.interfaces;

public interface IPullToRefreshListener {

    void onPullingHeader(float fraction);

    void onPullingFooter(float fraction);

    void onPullHeaderReleasing(float fraction);

    void onPullFooterReleasing(float fraction);

    void onHeaderRefresh();

    void onFooterRefresh();

    void onFinishHeaderRefresh();

    void onFinishFooterRefresh();

}
