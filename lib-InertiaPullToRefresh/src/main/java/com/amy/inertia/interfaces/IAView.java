package com.amy.inertia.interfaces;

import android.os.Message;

import com.amy.inertia.view.AScrollerController;
import com.amy.inertia.view.AViewParams;
import com.amy.inertia.view.AViewState;

public interface IAView {

    int FINISH_HEADER_REFRESH = 0;

    int FINISH_FOOTER_REFRESH = 1;

    AViewState getAViewState();

    void sendMessage(Message message);

    /**
     * Attach to inertia pull to refresh view.
     *
     * @param iPullToRefresh
     */
    void attachToParent(IPullToRefreshContainer iPullToRefresh, AViewParams aViewParams, AScrollerController aScrollerController);

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