package com.amy.inertia.view;

import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.amy.inertia.interfaces.IPullToRefreshListener;

import java.util.List;

final class AnimatorBuilder {
    static final String ANIM_SCROLL_BACK = "ANIM_SCROLL_BACK";
    static final String ANIM_SCROLL_TO = "ANIM_SCROLL_TO";
    static final String ANIM_OVER_FLING = "ANIM_OVER_FLING";

    int mOverFlingDuration = 70;
    float mOverFlingMaxVy = 1000;
    final Interpolator mOverFlingInterpolator = new LinearInterpolator();

    //ScrollBackAnim
    int mScrollBackAnimMinDuration = 750;
    int mScrollBackAnimMaxDuration = 1000;
    float mScrollBackAnimDamp = 1f;
    final Interpolator mScrollBackInterpolator = new DecelerateInterpolator(mScrollBackAnimDamp);

    //ScrollToAnim
    int mScrollToAnimMinDuration = 200;
    int mScrollToAnimMaxDuration = 400;
    float mScrollToAnimDamp = 1f;
    final Interpolator mScrollToInterpolator = new DecelerateInterpolator(mScrollToAnimDamp);

    private static AnimatorBuilder sInstance;

    static AnimatorBuilder getInstance() {
        if (sInstance == null) {
            return sInstance = new AnimatorBuilder();
        }
        return sInstance;
    }

    private AnimatorBuilder() {
    }

    ValueAnimator buildScrollBackAnimator(final List<IPullToRefreshListener> iPullToRefreshListeners,
                                          final float start,
                                          int duration,
                                          Interpolator interpolator) {

        //Todo : check if start =0.0f.
        ValueAnimator scrollBackAnimator = null;

        if (interpolator == null) {
            interpolator = mScrollBackInterpolator;
        }
        //Todo : param init.

        scrollBackAnimator = ValueAnimator.ofFloat(start, 0);
        scrollBackAnimator.setDuration(duration);
        scrollBackAnimator.setInterpolator(interpolator);
        scrollBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float fraction = value / start;
                //LogUtil.d("fraction : " + value / start);
                if (start < 0f) {
                    //Scroll back to top
                    for (IPullToRefreshListener iPullToRefreshListener : iPullToRefreshListeners) {
                        iPullToRefreshListener.onPullHeaderReleasing(fraction);
                    }
                } else {
                    //Scroll back to bottom
                    for (IPullToRefreshListener iPullToRefreshListener : iPullToRefreshListeners) {
                        iPullToRefreshListener.onPullFooterReleasing(fraction);
                    }
                }
            }
        });

        return scrollBackAnimator;
    }

    /*

    public Animator buildScrollToAnimator(final float start, final float to, int duration) {
        int realDuration = Math.min(
                Math.max(mScrollToAnimMinDuration, duration),
                mScrollToAnimMaxDuration
        );

        scrollToAnimator = ObjectAnimator.ofFloat(mAView, "viewTranslationY", start, to);
        scrollToAnimator.setDuration(realDuration);
        scrollToAnimator.setInterpolator(mScrollToInterpolator);
        scrollToAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAView.setViewTranslationY(start);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                float translationY = mAView.getViewTranslationY();
                boolean isPaused = animation.isPaused();
                LogUtil.d(ANIM_SCROLL_TO + " onAnimationCancel " + " isPaused: " + isPaused + " translationY : " + translationY);
                //if (!isPaused) {
                mAView.setViewTranslationY(to);
                //}
            }
        });
        addAnimator(ANIM_SCROLL_TO, scrollToAnimator);
        return scrollToAnimator;
    }

    public Animator buildOverScrollAnimator(float vY) {
        LogUtil.d("over scroll animator start value : " + vY);
        float dY = Math.min(vY, mInertiaOverScrollVyMax);
        overScrollAnimator = ValueAnimator.ofFloat(dY, 0);
        overScrollAnimator.setDuration(mInertiaOverScrollAnimDuration);
        overScrollAnimator.setInterpolator(mOverScrollInterpolator);
        overScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mAView.setViewTranslationY(mAView.getViewTranslationY() + value);
            }
        });
        overScrollAnimator.addListener(new AnimatorListenerAdapter() {
            boolean isCancel = false;

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAView.setViewTranslationY(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isCancel) {
                    isCancel = false;
                    return;
                }
                cancelAnim(ANIM_SCROLL_TO);
                cancelAnim(ANIM_SCROLL_BACK);
                float translationY = mAView.getViewTranslationY();
                int duration = Math.min(
                        Math.max((int) translationY, mScrollBackAnimMinDuration),
                        mScrollBackAnimMaxDuration);
                buildScrollBackAnimator(translationY, duration);
                scrollBackAnimator.start();
                isCancel = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                float translationY = mAView.getViewTranslationY();
                boolean isPaused = animation.isPaused();
                LogUtil.d(ANIM_OVER_SCROLL + " onAnimationCancel " + " isPaused: " + isPaused + " translationY : " + translationY);
                //if (!isPaused) {
                mAView.setViewTranslationY(0);
                //}
                isCancel = true;
            }

        });

        addAnimator(ANIM_OVER_SCROLL, overScrollAnimator);
        return overScrollAnimator;
    }

    */
}
