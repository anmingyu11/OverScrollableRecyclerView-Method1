package com.amy.library.inertia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.amy.library.LogUtil;

import java.util.HashMap;

import static com.amy.library.Util.checkNotNull;
import static com.amy.library.inertia.InertiaPullToRefreshLayout.ANIM_OVER_SCROLL;
import static com.amy.library.inertia.InertiaPullToRefreshLayout.ANIM_SCROLL_BACK;
import static com.amy.library.inertia.InertiaPullToRefreshLayout.ANIM_SCROLL_TO;

final class AnimatorController {

    private View mChildView;

    //InertiaAnim
    int mInertiaOverScrollAnimDuration = 70;
    float mInertiaOverScrollVyMax = 1000;
    final Interpolator mOverScrollInterpolator = new LinearInterpolator();

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

    final HashMap<String, Animator> mAnimators = new HashMap<String, Animator>();
    //Default animators
    ValueAnimator overScrollAnimator;
    ObjectAnimator scrollBackAnimator;
    ObjectAnimator scrollToAnimator;

    AnimatorController(View childView) {
        mChildView = childView;
    }


    boolean isAnimatorCurrentlyRunning(Animator animator) {
        if (animator != null
                && animator.isStarted()
                && animator.isRunning()
                && !animator.isPaused()) {
            return true;
        } else {
            return false;
        }
    }

    boolean isAnimatorCurrentlyPaused(Animator animator) {
        if (animator != null
                && animator.isStarted()
                && animator.isRunning()
                && animator.isPaused()) {
            return true;
        } else {
            return false;
        }
    }

    void pauseAllAnim() {
        for (String name : mAnimators.keySet()) {
            pauseAnim(name);
        }
    }

    void resumeAllAnim() {
        for (String name : mAnimators.keySet()) {
            resumeAnim(name);
        }
    }

    void cancelAllAnim() {
        for (String name : mAnimators.keySet()) {
            cancelAnim(name);
        }
    }

    void pauseAnim(String name) {
        Animator animator = getAnimator(name);
        if (animator == null) {
            LogUtil.d("pause anim : name you have given have animator is null or not exists");
            return;
        }
        boolean pause = isAnimatorCurrentlyRunning(animator);
        LogUtil.d("pausing animator : " + name + " canPause: " + pause);
        if (pause) {
            animator.pause();
        }
    }

    void resumeAnim(String name) {
        Animator animator = getAnimator(name);
        if (animator == null) {
            LogUtil.d("resume anim : name you have given have animator is null or not exists");
            return;
        }

        boolean resume = isAnimatorCurrentlyPaused(animator);
        LogUtil.d("resuming animator : " + name + " canResume :" + resume);
        if (resume) {
            animator.resume();
        }
    }

    void cancelAnim(String name) {
        Animator animator = getAnimator(name);
        if (animator == null) {
            LogUtil.d("cancel anim : name you have given have animator is null or not exists");
            return;
        }

        boolean cancel = isAnimatorCurrentlyRunning(animator) || isAnimatorCurrentlyPaused(animator);
        LogUtil.d("canceling animator : " + name + " canCancel : " + cancel);
        if (cancel) {
            animator.cancel();
        }
    }

    Animator buildScrollBackAnimator(final float start, int duration) {
        scrollBackAnimator = ObjectAnimator.ofFloat(mChildView, "translationY", start, 0);
        scrollBackAnimator.setDuration(duration);
        scrollBackAnimator.setInterpolator(mScrollBackInterpolator);
        scrollBackAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mChildView.setTranslationY(start);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                float translationY = mChildView.getTranslationY();
                boolean isPaused = animation.isPaused();
                LogUtil.d(ANIM_SCROLL_BACK + " onAnimationCancel " + " isPaused: " + isPaused + " translationY : " + translationY);
                //if (!isPaused) {
                mChildView.setTranslationY(0);
                //}
            }
        });
        addAnimator(ANIM_SCROLL_BACK, scrollBackAnimator);
        return scrollBackAnimator;
    }

    Animator buildScrollToAnimator(final float start, final float to, int duration) {
        int realDuration = Math.min(
                Math.max(mScrollToAnimMinDuration, duration),
                mScrollToAnimMaxDuration
        );

        scrollToAnimator = ObjectAnimator.ofFloat(mChildView, "translationY", start, to);
        scrollToAnimator.setDuration(realDuration);
        scrollToAnimator.setInterpolator(mScrollToInterpolator);
        scrollToAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mChildView.setTranslationY(start);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                float translationY = mChildView.getTranslationY();
                boolean isPaused = animation.isPaused();
                LogUtil.d(ANIM_SCROLL_TO + " onAnimationCancel " + " isPaused: " + isPaused + " translationY : " + translationY);
                //if (!isPaused) {
                mChildView.setTranslationY(to);
                //}
            }
        });
        addAnimator(ANIM_SCROLL_TO, scrollToAnimator);
        return scrollToAnimator;
    }

    Animator buildOverScrollAnimator(float vY) {
        LogUtil.d("over scroll animator start value : " + vY);
        float dY = Math.min(vY, mInertiaOverScrollVyMax);
        overScrollAnimator = ValueAnimator.ofFloat(dY, 0);
        overScrollAnimator.setDuration(mInertiaOverScrollAnimDuration);
        overScrollAnimator.setInterpolator(mOverScrollInterpolator);
        overScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mChildView.setTranslationY(mChildView.getTranslationY() + value);
            }
        });
        overScrollAnimator.addListener(new AnimatorListenerAdapter() {
            boolean isCancel = false;

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mChildView.setTranslationY(0f);
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
                float translationY = mChildView.getTranslationY();
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
                float translationY = mChildView.getTranslationY();
                boolean isPaused = animation.isPaused();
                LogUtil.d(ANIM_OVER_SCROLL + " onAnimationCancel " + " isPaused: " + isPaused + " translationY : " + translationY);
                //if (!isPaused) {
                mChildView.setTranslationY(0);
                //}
                isCancel = true;
            }

        });

        addAnimator(ANIM_OVER_SCROLL, overScrollAnimator);
        return overScrollAnimator;
    }

    void addAnimator(String name, Animator animator) {
        mAnimators.put(name, animator);
    }

    void removeAnimator(String name) {
        mAnimators.remove(name);
    }

    void clearAllAnimator() {
        mAnimators.clear();
    }

    void startAnimator(String name) {
        checkNotNull(getAnimator(name)).start();
    }

    Animator getAnimator(String name) {
        if (mAnimators.containsKey(name)) {
            return mAnimators.get(name);
        } else {
            return null;
        }
    }

}
