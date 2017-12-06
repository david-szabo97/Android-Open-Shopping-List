package com.messedcode.openshoppinglist.ui.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.messedcode.openshoppinglist.R;
import com.messedcode.openshoppinglist.animation.BackgroundColorAnimator;
import com.messedcode.openshoppinglist.animation.ColorAnimator;
import com.messedcode.openshoppinglist.animation.LayoutParamHeightAnimator;
import com.messedcode.openshoppinglist.model.ShoppingListItem;

public class ItemAnimator extends DefaultItemAnimator {

    public static final Interpolator COLLAPSE_INTERPOLATOR = new AccelerateInterpolator(2f);
    public static final Interpolator BACKGROUND_COLOR_INTERPOLATOR = COLLAPSE_INTERPOLATOR;
    public static final Interpolator TEXT_COLOR_INTERPOLATOR = COLLAPSE_INTERPOLATOR;
    public static final Interpolator MOVE_INTERPOLATOR = COLLAPSE_INTERPOLATOR;
    public static final Interpolator APPEAR_INTERPOLATOR = COLLAPSE_INTERPOLATOR;

    public static final int COLLAPSE_ANIM_DURATION = 600;
    public static final int BACKGROUND_COLOR_ANIM_DURATION = 400;
    public static final int TEXT_COLOR_ANIM_DURATION = 400;
    public static final int MOVE_ANIM_DURATION = COLLAPSE_ANIM_DURATION;
    public static final int APPEAR_ANIM_DURATION = COLLAPSE_ANIM_DURATION;
    public static final int APPEAR_ANIM_DELAY = COLLAPSE_ANIM_DURATION / 4;

    private final int deletedBgColor;
    private final int archivedBgColor;
    private final int targetTextColor;

    private final onAnimationEndListener listener;

    private final HashMap<RecyclerView.ViewHolder, Integer> pendingMoveAnims = new HashMap<>();
    private final ArrayList<RecyclerView.ViewHolder> pendingAddAnims = new ArrayList<>();
    private final ArrayList<RecyclerView.ViewHolder> pendingChangeAnims = new ArrayList<>();

    private final HashMap<RecyclerView.ViewHolder, ViewPropertyAnimator> moveAnims = new HashMap<>();
    private final HashMap<RecyclerView.ViewHolder, ViewPropertyAnimator> addAnims = new HashMap<>();
    private final HashMap<RecyclerView.ViewHolder, Animator> changeAnims = new HashMap<>();

    public ItemAnimator(Context context, onAnimationEndListener listener) {
        this.deletedBgColor = context.getResources().getColor(R.color.red);
        this.archivedBgColor = context.getResources().getColor(R.color.green);
        this.targetTextColor = context.getResources().getColor(R.color.white);

        this.listener = listener;
    }

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        // We are already animating our view to disappear in the "animateChange" method
        dispatchRemoveFinished(holder);
        return false;
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        int distance = fromY - toY;

        pendingMoveAnims.put(holder, distance);

        return true;
    }

    @Override
    public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        pendingAddAnims.add(viewHolder);

        return true;
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        final ShoppingListAdapter.ViewHolder vh = (ShoppingListAdapter.ViewHolder) newHolder;

        if (!vh.data.isActive()) {
            pendingChangeAnims.add(newHolder);

            return true;
        } else {
            return false;
        }
    }

    public void animateMoveImpl(final RecyclerView.ViewHolder holder, int distance) {
        int minimumDistance = holder.itemView.getHeight() / 4;

        // Add minimum distance to fix late transition after a view is swiped
        if (Math.abs(distance) > minimumDistance) {
            holder.itemView.setTranslationY(distance);
            ViewPropertyAnimator anim = holder.itemView.animate()
                    .setInterpolator(MOVE_INTERPOLATOR)
                    .setDuration(MOVE_ANIM_DURATION)
                    .translationY(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            dispatchMoveStarting(holder);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            dispatchMoveFinished(holder);
                            moveAnims.remove(holder);
                            dispatchFinishedWhenDone();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                            dispatchMoveFinished(holder);
                            moveAnims.remove(holder);
                            dispatchFinishedWhenDone();
                        }
                    });

            moveAnims.put(holder, anim);
            anim.start();
        } else {
            dispatchMoveFinished(holder);
            moveAnims.remove(holder);
        }
    }

    public void animateAppearanceImpl(@NonNull final RecyclerView.ViewHolder holder) {
        holder.itemView.setTranslationX(-holder.itemView.getWidth());
        ViewPropertyAnimator anim = holder.itemView.animate()
                .setStartDelay(APPEAR_ANIM_DELAY)
                .setInterpolator(APPEAR_INTERPOLATOR)
                .setDuration(APPEAR_ANIM_DURATION)
                .translationX(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        dispatchAddStarting(holder);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        dispatchAddFinished(holder);
                        addAnims.remove(holder);
                        dispatchFinishedWhenDone();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        dispatchAddFinished(holder);
                        addAnims.remove(holder);
                        dispatchFinishedWhenDone();
                    }
                });

        addAnims.put(holder, anim);
        anim.start();
    }

    public void animateChangeImpl(@NonNull final RecyclerView.ViewHolder newHolder) {
        final ShoppingListAdapter.ViewHolder vh = (ShoppingListAdapter.ViewHolder) newHolder;

        final View itemView = vh.itemView;
        AnimatorSet set = new AnimatorSet();

        LayoutParamHeightAnimator animHeight = LayoutParamHeightAnimator.collapse(itemView);
        animHeight.setDuration(COLLAPSE_ANIM_DURATION).setInterpolator(COLLAPSE_INTERPOLATOR);

        if (vh.isSwiped) {
            set.play(animHeight);
        } else {
            int toColor = (vh.data.status == ShoppingListItem.Status.ARCHIVED) ? archivedBgColor : deletedBgColor;

            BackgroundColorAnimator animBackground = new BackgroundColorAnimator(itemView, vh.defaultBackgroundColor, toColor);
            animBackground.setInterpolator(BACKGROUND_COLOR_INTERPOLATOR);
            animBackground.setDuration(BACKGROUND_COLOR_ANIM_DURATION);

            AnimatorSet.Builder builder = set.play(animBackground);

            for (TextView v : vh.textViews) {
                builder.with(createTextColorAnimator(v, targetTextColor));
            }

            builder.before(animHeight);
        }

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchChangeStarting(newHolder, false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                dispatchChangeFinished(newHolder, false);
                listener.onChangeEnd(newHolder);
                changeAnims.remove(newHolder);
                dispatchFinishedWhenDone();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                dispatchChangeFinished(newHolder, false);
                listener.onChangeEnd(newHolder);
                changeAnims.remove(newHolder);
                dispatchFinishedWhenDone();
            }
        });

        changeAnims.put(newHolder, set);
        set.start();
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        pendingMoveAnims.remove(item);
        pendingAddAnims.remove(item);
        pendingChangeAnims.remove(item);

        if (moveAnims.containsKey(item)) {
            moveAnims.get(item).cancel();
            moveAnims.remove(item);
        }

        if (addAnims.containsKey(item)) {
            addAnims.get(item).cancel();
            addAnims.remove(item);
        }

        if (changeAnims.containsKey(item)) {
            changeAnims.get(item).end();
            changeAnims.remove(item);
        }

        super.endAnimation(item);
    }

    @Override
    public void endAnimations() {
        pendingMoveAnims.clear();
        pendingAddAnims.clear();
        pendingChangeAnims.clear();

        for (ViewPropertyAnimator anim : moveAnims.values()) {
            anim.cancel();
        }
        moveAnims.clear();

        for (ViewPropertyAnimator anim : addAnims.values()) {
            anim.cancel();
        }
        addAnims.clear();

        for (Animator anim : changeAnims.values()) {
            anim.end();
        }
        changeAnims.clear();

        super.endAnimations();
    }

    @Override
    public boolean isRunning() {
        return !pendingMoveAnims.isEmpty() ||
                !pendingAddAnims.isEmpty() ||
                !pendingChangeAnims.isEmpty() ||
                !moveAnims.isEmpty() ||
                !addAnims.isEmpty() ||
                !changeAnims.isEmpty() ||
                super.isRunning();
    }

    @Override
    public void runPendingAnimations() {
        for (RecyclerView.ViewHolder vh : pendingAddAnims) {
            animateAppearanceImpl(vh);
        }
        pendingAddAnims.clear();

        for (Map.Entry<RecyclerView.ViewHolder, Integer> entry : pendingMoveAnims.entrySet()) {
            animateMoveImpl(entry.getKey(), entry.getValue());
        }
        pendingMoveAnims.clear();

        for (RecyclerView.ViewHolder vh : pendingChangeAnims) {
            animateChangeImpl(vh);
        }
        pendingChangeAnims.clear();

        super.runPendingAnimations();
    }

    private void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    private ColorAnimator createTextColorAnimator(final TextView view, int targetColor) {
        ColorAnimator anim = new ColorAnimator(view.getCurrentTextColor(), targetColor);
        anim.setInterpolator(TEXT_COLOR_INTERPOLATOR);
        anim.setDuration(TEXT_COLOR_ANIM_DURATION);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setTextColor((int) valueAnimator.getAnimatedValue());
            }
        });

        return anim;
    }

    interface onAnimationEndListener {
        void onChangeEnd(RecyclerView.ViewHolder newHolder);
    }

}
