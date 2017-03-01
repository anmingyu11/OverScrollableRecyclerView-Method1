package com.amy.inertia.interfaces;

import android.animation.Animator;

public interface IAnimatorController {

    void attachAView(IAView iAview);

    boolean hasAnimatorCurrentlyRunning();

    boolean isAnimatorCurrentlyRunning(String name);

    boolean isAnimatorCurrentlyPaused(String name);

    void pauseAnim(String name);

    void resumeAnim(String name);

    void cancelAnim(String name);

    void pauseAllAnim();

    void resumeAllAnim();

    void cancelAllAnim();

    void addAnimator(String name, Animator animator);

    void removeAnimator(String name);

    void clearAllAnimator();

    void startAnimator(String name);

}
