package com.amy.inertia.view;

import android.animation.Animator;

import com.amy.inertia.interfaces.IAView;
import com.amy.inertia.interfaces.IAnimatorController;
import com.amy.inertia.util.LogUtil;

import java.util.HashMap;

import static android.R.attr.name;
import static com.amy.inertia.util.Util.checkNotNull;

final class AnimatorController implements IAnimatorController {

    IAView mAView;

    final HashMap<String, Animator> mAnimators = new HashMap<String, Animator>();

    AnimatorController(IAView iaView) {
        attachAView(iaView);
    }

    @Override
    public void attachAView(IAView iAview) {
        mAView = iAview;
    }

    @Override
    public boolean isAnimatorCurrentlyRunning(String key) {
        Animator animator = getAnimator(key);
        return isAnimatorCurrentlyRunning(animator);
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

    @Override
    public boolean isAnimatorCurrentlyPaused(String key) {
        Animator animator = getAnimator(key);
        return isAnimatorCurrentlyPaused(animator);
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

    @Override
    public void pauseAllAnim() {
        for (String name : mAnimators.keySet()) {
            pauseAnim(name);
        }
    }

    public void resumeAllAnim() {
        for (String name : mAnimators.keySet()) {
            resumeAnim(name);
        }
    }

    public void cancelAllAnim() {
        for (String name : mAnimators.keySet()) {
            cancelAnim(name);
        }
    }

    public void pauseAnim(String name) {
        Animator animator = getAnimator(name);
        pauseAnim(animator);
    }

    void pauseAnim(Animator animator) {
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

    public void resumeAnim(String name) {
        Animator animator = getAnimator(name);
        resumeAnim(animator);
    }

    void resumeAnim(Animator animator) {
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

    public void cancelAnim(String name) {
        Animator animator = getAnimator(name);
        cancelAnim(animator);
    }

    void cancelAnim(Animator animator) {
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

    public void addAnimator(String name, Animator animator) {
        mAnimators.put(name, animator);
    }

    public void removeAnimator(String name) {
        mAnimators.remove(name);
    }

    public void clearAllAnimator() {
        mAnimators.clear();
    }

    public void startAnimator(String name) {
        checkNotNull(getAnimator(name)).start();
    }

    Animator getAnimator(String name) {
        if (mAnimators.containsKey(name)) {
            return mAnimators.get(name);
        } else {
            LogUtil.d("Name : " + name + " animator you get not exist. ");
            return null;
        }
    }

}
