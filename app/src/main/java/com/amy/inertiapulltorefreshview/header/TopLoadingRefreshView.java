//Todo copyRight
package com.amy.inertiapulltorefreshview.header;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.amy.inertia.interfaces.IHeaderView;
import com.amy.inertia.util.LogUtil;
import com.amy.inertiapulltorefreshview.R;

public class TopLoadingRefreshView extends FrameLayout implements IHeaderView {

    private Context mContext;
    private float mOldFraction = 0.0f;
    //Trigger of icon top
    private ValueAnimator mRotateAnimation;
    private ImageView mLoadingIcon;
    private float mLoadingIconShowPosition;

    public TopLoadingRefreshView(Context context) {
        this(context, null, 0);
    }

    public TopLoadingRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopLoadingRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setBackgroundColor(mContext.getResources().getColor(R.color.color_main_black_ground));
        LayoutInflater.from(mContext).inflate(R.layout.loading_layout, this);
        if (this.getChildCount() > 1) {
            throw new IllegalArgumentException("only one child view available");
        }
        mLoadingIcon = (ImageView) findViewById(R.id.loading_layout_loading_icon);
        mLoadingIconShowPosition = getResources().getDimensionPixelSize(R.dimen.refresh_view_show_position);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        LogUtil.i("\n pivotY : " + mLoadingIcon.getPivotY()
                + " pivotX : " + mLoadingIcon.getPivotX()
                + "\n x : " + mLoadingIcon.getX()
                + " y : " + mLoadingIcon.getY()
                + "\n top : " + mLoadingIcon.getTop()
                + " bottom : " + mLoadingIcon.getBottom()
                + " left : " + mLoadingIcon.getLeft()
                + " right : " + mLoadingIcon.getRight()
        );
        LogUtil.i("onLayout " + " changed : " + changed + " left : " + left + " top : " + top + " right : " + right + " bottom : " + bottom);
        // LogUtil.printTraceStack("::");
    }

    /*
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LogUtil.d("onFinishInflate");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogUtil.d("onAttachedToWindow");
    }
*/

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onPullingHeader(float fraction) {
        float baseRotation = 180f;
        float loadingIconY = mLoadingIcon.getY();

        LogUtil.d("loadingIconY : " + loadingIconY
                + " showLocation : " + mLoadingIconShowPosition
                + " fraction : " + fraction);

        if (loadingIconY < mLoadingIconShowPosition) {
            if (mLoadingIcon.getAlpha() > 0.01f) {
                mLoadingIcon.setAlpha(0.0f);
            }
        } else if (loadingIconY > mLoadingIconShowPosition) {
            mLoadingIcon.setAlpha(fraction > 0.99f ? 1f : fraction);
        }

        float deltaFraction = fraction - mOldFraction;
        /*
        LogUtil.d("amy" +
                "down "
                + " headHeight : " + headHeight
                + " maxHeadHeight : " + maxHeadHeight);
                */
        LogUtil.d("\n pivotY : " + mLoadingIcon.getPivotY()
                + " pivotX : " + mLoadingIcon.getPivotX()
                + "\n x : " + mLoadingIcon.getX()
                + " y : " + mLoadingIcon.getY()
                + "\n top : " + mLoadingIcon.getTop()
                + " bottom : " + mLoadingIcon.getBottom()
                + " left : " + mLoadingIcon.getLeft()
                + " right : " + mLoadingIcon.getRight()
        );

        LogUtil.i(" height : " + mLoadingIcon.getLayoutParams().height + " width : " + mLoadingIcon.getLayoutParams().width);
        LogUtil.i(" rotation : " + mLoadingIcon.getRotation());
        LogUtil.i(" alpha : " + mLoadingIcon.getAlpha());

        if (fraction < 0f) {
            throw new IllegalArgumentException("fraction can not < 0");
        }
        if (fraction < 1f) {
            setLoadingIconScale(fraction);
        } else if (fraction > 1f) {
            setLoadingIconScale(1f);
        }
        setLoadingIconRotate(mLoadingIcon.getRotation() + deltaFraction * baseRotation);

        LogUtil.d("amy" + "Pulling releasing " + " oldFraction : " + mOldFraction);
        mOldFraction = fraction;
        LogUtil.d("amy" + "Pulling down " + " newFraction : " + fraction);
        LogUtil.d("amy" + "Pulling down " + " deltaFraction : " + deltaFraction);
    }

    @Override
    public void onPullReleasing(float fraction) {
        float baseRotation = 180f;
        float loadingIconY = mLoadingIcon.getY();
        /*
        LogUtil.d("amy" +
                "releasing "
                        + "loadingIconY : " + loadingIconY
                        + " fraction : " + fraction);
                        */
        if (loadingIconY < mLoadingIconShowPosition) {
            if (mLoadingIcon.getAlpha() > 0.01f) {
                mLoadingIcon.setAlpha(0.0f);
            }
        } else if (loadingIconY > mLoadingIconShowPosition) {
            mLoadingIcon.setAlpha(fraction > 0.99f ? 1f : fraction);
        }

        /*
        LogUtil.d("amy" + "releasing " + " headHeight : "
                + headHeight + " maxHeadHeight : " + maxHeadHeight);
        LogUtil.d("amy" + "releasing "
                + "\n pivotY : " + mLoadingIcon.getPivotY()
                + " pivotX : " + mLoadingIcon.getPivotX()
                + "\n x : " + mLoadingIcon.getX()
                + " y : " + mLoadingIcon.getY()
                + "\n top : " + mLoadingIcon.getTop()
                + " bottom : " + mLoadingIcon.getBottom()
                + " left : " + mLoadingIcon.getLeft()
                + " right : " + mLoadingIcon.getRight()
        );
        */
        float deltaFraction = fraction - mOldFraction;

        if (fraction < 0f) {
            throw new IllegalArgumentException("fraction can not < 0");
        }
        if (fraction < 1f) {
            setLoadingIconScale(fraction);
        } else if (fraction > 1f) {
            setLoadingIconScale(1f);
        }

        setLoadingIconRotate(mLoadingIcon.getRotation() + -deltaFraction * baseRotation);

        //LogUtil.d("amy" +  "releasing " + " oldFraction : " + mOldFraction);
        mOldFraction = fraction;
        //LogUtil.d("amy" + "releasing " + " newFraction : " + fraction);
        //LogUtil.d("amy" + "releasing " + " deltaFraction : " + deltaFraction);
    }

    @Override
    public void startAnim(float maxHeadHeight, float headHeight) {
        if (mRotateAnimation == null) {
            mRotateAnimation = getLoadingRotateAnimation();
        }
        /*
        LogUtil.d("amy" + "start "
                + "\n pivotY : " + mLoadingIcon.getPivotY()
                + " pivotX : " + mLoadingIcon.getPivotX()
                + "\n x : " + mLoadingIcon.getX()
                + " y : " + mLoadingIcon.getY()
                + "\n top : " + mLoadingIcon.getTop()
                + " bottom : " + mLoadingIcon.getBottom()
                + " left : " + mLoadingIcon.getLeft()
                + " right : " + mLoadingIcon.getRight()
        );
        LogUtil.d("amy" + "start "
                + " headHeight : " + headHeight
                + " maxHeadHeight : " + maxHeadHeight);
        */
        mRotateAnimation.start();
    }

    @Override
    public void onFinish() {
        if (mRotateAnimation != null) {
            mRotateAnimation.cancel();
        }
        mRotateAnimation = null;
    }

    private ValueAnimator getLoadingRotateAnimation() {
        int duration = 1200;
        float rotationAngle = 360f;
        float fromRotation = mLoadingIcon.getRotation();
        float toRotation = mLoadingIcon.getRotation() + rotationAngle;


        ValueAnimator animator = ValueAnimator.ofFloat(fromRotation, toRotation);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setLoadingIconRotate((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        return animator;
    }

    private void setLoadingIconRotate(float rotation) {
        float baseRotation = 360f;
        mLoadingIcon.setRotation(rotation % baseRotation);
    }

    private void setLoadingIconScale(float scale) {
        int loadingIconWidth = getResources().getDimensionPixelSize(R.dimen.refresh_view_loading_size);
        int loadingIconHeight = getResources().getDimensionPixelSize(R.dimen.refresh_view_loading_size);
        final ViewGroup.LayoutParams layoutParams = mLoadingIcon.getLayoutParams();
        layoutParams.width = (int) (loadingIconWidth * scale);
        layoutParams.height = (int) (loadingIconHeight * scale);
        mLoadingIcon.setLayoutParams(layoutParams);
    }

}
