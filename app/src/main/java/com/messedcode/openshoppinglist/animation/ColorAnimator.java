package com.messedcode.openshoppinglist.animation;

import android.animation.ArgbEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;

public class ColorAnimator extends ValueAnimator {

    private static TypeEvaluator EVALUATOR = new ArgbEvaluator();

    public ColorAnimator(int... values) {
        setIntValues(values);
        setEvaluator(EVALUATOR);
    }

}
