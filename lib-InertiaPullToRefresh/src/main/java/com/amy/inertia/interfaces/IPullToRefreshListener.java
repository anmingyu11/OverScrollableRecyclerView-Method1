package com.amy.inertia.interfaces;

public interface IPullToRefreshListener {

    void onPullingHeader(float fraction, float currentHeight);

    void onPullingFooter(float fraction, float currentHeight);

    void onHeaderReleasing(float fraction, float currentHeight);

    void onFooterReleasing(float fraction, float currentHeight);

    void onHeaderRefresh();

    void onFooterRefresh();

    void onFinishHeaderRefresh();

    void onFinishFooterRefresh();

}
