package com.amy.inertia.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.IPullToRefreshListener;
import com.amy.inertia.util.LogUtil;

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

     */

    Animator buildScrollBackAnimator(final List<IPullToRefreshListener> iPullToRefreshListeners,
                                     final IAView iaView,
                                     final float start,
                                     int duration,
                                     Interpolator interpolator) {

        //Todo : check if start =0.0f.
        ValueAnimator scrollBackAnimator = null;

        scrollBackAnimator = ValueAnimator.ofFloat(start, 0);
        scrollBackAnimator.setDuration(duration);
        scrollBackAnimator.setInterpolator(interpolator);
        scrollBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                iaView.setViewTranslationY(value);

                float fraction = value / start;
                //LogUtil.d("fraction : " + value / start);
                if (start < 0f) {
                    //Scroll back to top
                    for (IPullToRefreshListener iPullToRefreshListener : iPullToRefreshListeners) {
                        iPullToRefreshListener.onHeaderReleasing(fraction);
                    }
                } else {
                    //Scroll back to bottom
                    for (IPullToRefreshListener iPullToRefreshListener : iPullToRefreshListeners) {
                        iPullToRefreshListener.onFooterReleasing(fraction);
                    }
                }
            }
        });

        return scrollBackAnimator;
    }


    public Animator buildOverFlingAnimator(final IAView iaView,
                                           final float vY,
                                           int duration,
                                           Interpolator interpolator) {
        LogUtil.d("over scroll animator start value : " + vY);

        ValueAnimator overScrollAnimator = null;

        overScrollAnimator = ValueAnimator.ofFloat(-vY, 0);
        overScrollAnimator.setDuration(Math.abs(duration));
        overScrollAnimator.setInterpolator(interpolator);
        overScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                iaView.setViewTranslationY(iaView.getViewTranslationY() + value);
            }
        });

        return overScrollAnimator;
    }

    public Animator buildScrollToAnim(final List<IPullToRefreshListener> iPullToRefreshListeners,
                                      final IAView iaView,
                                      final float to,
                                      int duration,
                                      Interpolator interpolator) {
        LogUtil.d("scroll to " + to);

        ValueAnimator scrollToAnimator = null;

        scrollToAnimator = ValueAnimator.ofFloat(to);
        scrollToAnimator.setDuration(duration);
        scrollToAnimator.setInterpolator(interpolator);
        scrollToAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                iaView.setViewTranslationY(value);

                float fraction = value / to;
                //LogUtil.d("fraction : " + value / start);
                if (to < 0f) {
                    //Scroll to top
                    for (IPullToRefreshListener iPullToRefreshListener : iPullToRefreshListeners) {
                        iPullToRefreshListener.onHeaderReleasing(fraction);
                    }
                } else {
                    //Scroll to bottom
                    for (IPullToRefreshListener iPullToRefreshListener : iPullToRefreshListeners) {
                        iPullToRefreshListener.onFooterReleasing(fraction);
                    }
                }
            }
        });

        return scrollToAnimator;
    }
}
