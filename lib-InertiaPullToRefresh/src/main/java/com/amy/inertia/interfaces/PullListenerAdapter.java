package com.amy.inertia.interfaces;

public abstract class PullListenerAdapter implements IPullToRefreshListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPullingHeader(float fraction, float currentHeight) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPullingFooter(float fraction, float currentHeight) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHeaderReleasing(float fraction, float currentHeight) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFooterReleasing(float fraction, float currentHeight) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHeaderRefresh() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFooterRefresh() {

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
