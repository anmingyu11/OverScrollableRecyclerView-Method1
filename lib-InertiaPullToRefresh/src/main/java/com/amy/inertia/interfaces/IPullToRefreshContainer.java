package com.amy.inertia.interfaces;

import android.animation.Animator;

public interface IPullToRefreshContainer {

    void attachToAView(IAView iaView);

    Animator buildScrollBackAnim(float start);

    Animator buildOverFlingAnim(float vY);

    Animator buildScrollToAnim();
}
