package com.amy.inertia.util;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

public class ScrollerUtil {

    public static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 0.95f;
            return t * t * t * t * t + 0.8f;
        }
    };

    private static float sViscousFluidScale;
    private static float sViscousFluidNormalize;

    static {
        // This controls the viscous fluid effect (how much of it)
        sViscousFluidScale = 8.0f;
        // must be set to 1.0 (used in viscousFluid())
        sViscousFluidNormalize = 1.0f;
        sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
    }

    /**
     * Replace interpolator viscousInterpolator.
     *
     * @param x
     * @return
     */
    public static float viscousFluid(float x) {
        x *= sViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float) Math.exp(-x));
        } else {
            float start = 0.36787944117f;   // 1/e == exp(-1)
            x = 1.0f - (float) Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        x *= sViscousFluidNormalize;
        return x;
    }

    public static boolean isChildCanScroll(View v) {
        return ViewCompat.canScrollVertically(v, 1) && ViewCompat.canScrollVertically(v, -1);
    }

    public static boolean isChildScrollToBottom(View v) {
        return !scrollToTop(v) && scrollToBottom(v);
    }

    public static boolean isChildScrollToTop(View v) {
        return scrollToTop(v) && !scrollToBottom(v);
    }

    public static boolean isChildScrollInContent(View v) {
        return scrollToBottom(v) && scrollToTop(v);
    }

    private static boolean scrollToBottom(View view) {
        if (view == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(view, -1) || view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, -1);
        }
    }

    private static boolean scrollToTop(View view) {
        if (Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(view, 1) || view.getScrollY() < 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, 1);
        }
    }
}
