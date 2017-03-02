package com.amy.inertia.interfaces;

public interface IPullToRefreshListener {

    void onPullingHeader(float fraction);

    void onPullingFooter(float fraction);

    void onHeaderReleasing(float fraction);

    void onFooterReleasing(float fraction);

    void onHeaderRefresh();

    void onFooterRefresh();

    void onFinishHeaderRefresh();

    void onFinishFooterRefresh();

}
