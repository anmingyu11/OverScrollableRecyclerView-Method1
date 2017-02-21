package com.amy.inertia.interfaces;

import android.animation.Animator;

public interface IAnimatorController {

    boolean isAnimatorCurrentlyRunning(String key);

    boolean isAnimatorCurrentlyPaused(String key);

    void pauseAllAnim();

    void resumeAllAnim();

    void cancelAllAnim();

    void pauseAnim(String name);

    void resumeAnim(String name);

    void cancelAnim(String name);

    Animator buildScrollBackAnimator(final float start, int duration);

    Animator buildScrollToAnimator(final float start, final float to, int duration);

    Animator buildOverScrollAnimator(float vY);

    void addAnimator(String name, Animator animator);

    void removeAnimator(String name);

    void clearAllAnimator();

    void startAnimator(String name);

}
