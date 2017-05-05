package com.amy.inertia.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.Interpolator;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.IPullToRefreshContainer;
import com.amy.inertia.util.LogUtil;

final class AnimatorBuilder {
    static final String ANIM_SCROLL_BACK = "ANIM_SCROLL_BACK";
    static final String ANIM_SCROLL_TO = "ANIM_SCROLL_TO";
    static final String ANIM_OVER_FLING = "ANIM_OVER_FLING";

    private static AnimatorBuilder sInstance;
    private AViewParams mParams = null;

    static AnimatorBuilder getInstance(AViewParams aViewParams) {
        if (sInstance == null) {
            return sInstance = new AnimatorBuilder(aViewParams);
        }
        return sInstance;
    }

    private AnimatorBuilder(AViewParams aViewParams) {
        mParams = aViewParams;
    }

    Animator buildScrollBackAnimator(final IPullToRefreshContainer iPullToRefreshContainer,
                                     final IAView iaView,
                                     final float start,
                                     int duration,
                                     Interpolator interpolator) {

        LogUtil.d("scroll back start at : " + start + " duration : " + duration);

        ValueAnimator scrollBackAnimator = null;

        scrollBackAnimator = ValueAnimator.ofFloat(start, 0);
        scrollBackAnimator.setDuration(duration);
        scrollBackAnimator.setInterpolator(interpolator);
        scrollBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                iaView.setViewTranslationY(value);

                //float fraction = value / start;
                //LogUtil.d("fraction : " + value / start);
                if (start > 0f) {
                    //Scroll back to top
                    iPullToRefreshContainer.headerReleasing(value);
                } else {
                    //Scroll back to bottom
                    iPullToRefreshContainer.footerReleasing(value);
                }
            }
        });

        return scrollBackAnimator;
    }


    public Animator buildOverFlingAnimator(final IPullToRefreshContainer iPullToRefreshContainer,
                                           final IAView iaView,
                                           final float triggerHeight,
                                           final int distance,
                                           int duration,
                                           Interpolator interpolator) {
        LogUtil.d("over fling animator start value : " + distance + " duration : " + duration);

        ValueAnimator overScrollAnimator = null;

        overScrollAnimator = ValueAnimator.ofFloat(1, distance);
        overScrollAnimator.setDuration(Math.abs(duration));
        overScrollAnimator.setInterpolator(interpolator);
        overScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentHeight = (float) animation.getAnimatedValue();

                iaView.setViewTranslationY(currentHeight);
                LogUtil.d("currentHeight : " + currentHeight);
                //float fraction = currentHeight / triggerHeight;
                //LogUtil.d("fraction : " + value / start);
                if (currentHeight > 0f) {
                    //Scroll to top
                    iPullToRefreshContainer.pullingHeader(currentHeight);
                } else {
                    //Scroll to bottom
                    iPullToRefreshContainer.pullingFooter(currentHeight);
                }
            }
        });

        return overScrollAnimator;
    }

    public Animator buildScrollToAnim(final IPullToRefreshContainer iPullToRefreshContainer,
                                      final IAView iaView,
                                      final float to,
                                      int duration,
                                      Interpolator interpolator) {
        LogUtil.d("scroll to " + to + " duration : " + duration + " transY : " + iaView.getViewTranslationY());

        ValueAnimator scrollToAnimator = null;

        float start = iaView.getViewTranslationY();

        scrollToAnimator = ValueAnimator.ofFloat(start, to);
        scrollToAnimator.setDuration(duration);
        scrollToAnimator.setInterpolator(interpolator);
        scrollToAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                iaView.setViewTranslationY(value);

                //float fraction = value / to;
                //LogUtil.d("fraction : " + value / start);
                if (to > 0f) {
                    //Scroll to top
                    iPullToRefreshContainer.headerReleasing(value);
                } else {
                    //Scroll to bottom
                    iPullToRefreshContainer.footerReleasing(value);
                }
            }
        });

        return scrollToAnimator;
    }
}
