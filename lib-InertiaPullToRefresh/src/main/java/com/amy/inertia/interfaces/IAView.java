package com.amy.inertia.interfaces;

import com.amy.inertia.view.AViewState;

public interface IAView {

    AViewState getAViewState();

    /**
     * Attach to inertia pull to refresh view.
     *
     * @param iPullToRefresh
     */
    void attachToParent(IPullToRefreshContainer iPullToRefresh);

    /**
     * Add a Scroll Detector listener.
     * <p>
     * Todo: ScrollDetectorListener will be implement in InertiaPullToRefreshLayout.
     *
     * @param onScrollDetectorListener
     */
    void addScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener);

    void removeScrollDetectorListener(OnScrollDetectorListener onScrollDetectorListener);

    void clearScrollDetectorListener();

    /**
     * Encapsulate the view.setTranslationY(float translationY);
     *
     * @param translationY
     */
    int setViewTranslationY(float translationY);

    /**
     * Encapsulate the view.getTranslationY();
     *
     * @return
     */
    int getViewTranslationY();

}