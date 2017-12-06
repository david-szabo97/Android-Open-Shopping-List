package com.messedcode.openshoppinglist.ui.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.messedcode.openshoppinglist.R;
import com.messedcode.openshoppinglist.utils.Utils;


public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final Context context;
    private final OnSwipeListener listener;

    private final int leftColor;
    private final Bitmap leftIcon;
    private final int rightColor;
    private final Bitmap rightIcon;

    private final Paint paint = new Paint();

    public ItemTouchHelperCallback(Context context, OnSwipeListener listener) {
        this.context = context;
        this.listener = listener;

        // TODO: Move to builder
        leftColor = context.getResources().getColor(R.color.green);
        leftIcon = Utils.convertDrawableToBitmap(context.getResources().getDrawable(R.drawable.ic_check_white_24dp));
        rightColor = context.getResources().getColor(R.color.red);
        rightIcon = Utils.convertDrawableToBitmap(context.getResources().getDrawable(R.drawable.ic_delete_white_24dp));
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        ShoppingListAdapter.ViewHolder vh = (ShoppingListAdapter.ViewHolder) viewHolder;

        vh.isSwiped = true;

        if (direction == ItemTouchHelper.START) {
            listener.onSwipeLeft(vh);
        } else if (direction == ItemTouchHelper.END) {
            listener.onSwipeRight(vh);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            dX = 0;
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            super.onChildDrawOver(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;

            float width = itemView.getWidth();
            float height = itemView.getHeight();

            if (height > 0) {
                float left = itemView.getLeft();
                float top = itemView.getTop();
                float right = itemView.getRight();
                float bottom = itemView.getBottom();

                float centerY = (top + bottom) / 2;

                float iconSize = height * 0.64f;
                float margin = width * 0.025f;

                if (dX > 0) {
                    paint.setColor(leftColor);
                    RectF background = new RectF(left, top, dX, bottom);
                    c.drawRect(background, paint);

                    float maxLeft = left + margin;
                    float iconLeft = Math.min(dX - iconSize - margin, maxLeft);
                    RectF iconRect = new RectF(iconLeft, centerY - iconSize / 2, iconLeft + iconSize, centerY + iconSize / 2);
                    c.drawBitmap(leftIcon, null, iconRect, paint);
                } else {
                    paint.setColor(rightColor);
                    RectF background = new RectF(right + dX, top, right, bottom);
                    c.drawRect(background, paint);

                    float minRight = right - margin;
                    float iconRight = Math.max(right + dX + iconSize + margin, minRight);
                    RectF iconRect = new RectF(iconRight - iconSize, centerY - iconSize / 2, iconRight, centerY + iconSize / 2);
                    c.drawBitmap(rightIcon, null, iconRect, paint);
                }
            }
        } else {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    }


    public interface OnSwipeListener {

        void onSwipeRight(ShoppingListAdapter.ViewHolder vh);

        void onSwipeLeft(ShoppingListAdapter.ViewHolder vh);

    }

}
