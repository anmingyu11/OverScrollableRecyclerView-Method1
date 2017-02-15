package com.amy.library.interfaces;

import android.view.View;

public interface OnScrollDetectorListener<T extends View> {

    void onScrolled(T t, int dx, int dy);

    void onScrollToBottom();

    void onScrollToTop();

    void onScrollInContent();
}
