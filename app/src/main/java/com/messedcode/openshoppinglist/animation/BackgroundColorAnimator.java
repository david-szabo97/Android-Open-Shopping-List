package com.messedcode.openshoppinglist.animation;

import android.animation.ValueAnimator;
import android.view.View;

public class BackgroundColorAnimator extends ColorAnimator {

    public BackgroundColorAnimator(final View target, int... values) {
        super(values);

        addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                target.setBackgroundColor(value);
            }
        });

    }
}
