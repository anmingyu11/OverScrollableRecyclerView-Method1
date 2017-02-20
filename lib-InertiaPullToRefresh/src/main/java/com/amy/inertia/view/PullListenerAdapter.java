package com.amy.inertia.view;

public abstract class PullListenerAdapter implements PullToRefreshContainer.IPullListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPullingHeader(PullToRefreshContainer pullToRefreshContainer, float fraction) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPullingFooter(PullToRefreshContainer pullToRefreshContainer, float fraction) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPullHeaderReleasing(PullToRefreshContainer pullToRefreshContainer, float fraction) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPullFooterReleasing(PullToRefreshContainer pullToRefreshContainer, float fraction) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHeaderRefresh(PullToRefreshContainer pullToRefreshContainer) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFooterRefresh(PullToRefreshContainer pullToRefreshContainer) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishHeaderRefresh() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFooterRefresh() {

    }
}
