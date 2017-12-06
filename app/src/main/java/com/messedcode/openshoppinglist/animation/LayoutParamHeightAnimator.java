package com.messedcode.openshoppinglist.animation;

import android.animation.ValueAnimator;
import android.view.View;

public class LayoutParamHeightAnimator extends ValueAnimator {

    public LayoutParamHeightAnimator(final View target, int... values) {
        setIntValues(values);

        addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                target.getLayoutParams().height = value;
                target.requestLayout();
            }
        });
    }

    public static LayoutParamHeightAnimator collapse(View target) {
        return new LayoutParamHeightAnimator(target, target.getHeight(), 0);
    }

}
