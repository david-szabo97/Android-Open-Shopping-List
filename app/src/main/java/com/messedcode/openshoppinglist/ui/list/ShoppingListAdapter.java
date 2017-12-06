package com.messedcode.openshoppinglist.ui.list;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.transition.ChangeBounds;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.messedcode.openshoppinglist.R;
import com.messedcode.openshoppinglist.model.ShoppingListItem;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> implements ItemAnimator.onAnimationEndListener {

    private static final String TAG = "ShoppingListAdapter";

    public static final int VIEW_SIZE_COMPACT = 0;
    public static final int VIEW_SIZE_COZY = 1;

    private final RecyclerView recyclerView;
    private final Context context;

    private ArrayList<ShoppingListItem> data = new ArrayList<>();
    private int viewSize = VIEW_SIZE_COMPACT;

    public ShoppingListAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        return viewSize;
    }

    @Override
    public ShoppingListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = (viewSize == VIEW_SIZE_COMPACT) ? R.layout.shopping_list_item_compact :
                (viewSize == VIEW_SIZE_COZY) ? R.layout.shopping_list_item_cozy : 0;

        ViewGroup vg = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(vg);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ShoppingListItem item = data.get(position);

        // Reset values used by animation
        holder.itemView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        holder.resetBackgroundColor();
        holder.resetTextColor();


        holder.nameTextView.setText(item.name);
        holder.createdByTextView.setText(context.getString(R.string.shopping_list_item_created_by, item.createdBy));
        holder.priceTextView.setVisibility((item.price > 0) ? View.VISIBLE : View.GONE);

        holder.descriptionTextView.setVisibility((item.description.length() > 0) ? View.VISIBLE : View.GONE);
        holder.descriptionTextView.setText(item.description);

        holder.urgentImageView.setVisibility((item.urgent) ? View.VISIBLE : View.GONE);

        holder.data = item;

        String formattedPrice = item.price + " Ft";
        if (viewSize == VIEW_SIZE_COMPACT) {
            holder.detailsView.setVisibility(View.GONE);
            holder.priceTextView.setText(context.getString(R.string.shopping_list_item_price_compact, formattedPrice));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean expand = holder.detailsView.getVisibility() == View.GONE;

                    holder.detailsView.setVisibility((expand) ? View.VISIBLE : View.GONE);

                    ChangeBounds transition = new ChangeBounds();
                    transition.setDuration(200);
                    TransitionManager.beginDelayedTransition(recyclerView, transition);

                    holder.itemView.setActivated(expand);
                }
            });
        } else if (viewSize == VIEW_SIZE_COZY) {
            holder.priceTextView.setText(context.getString(R.string.shopping_list_item_price_cozy, formattedPrice));
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onChangeEnd(final RecyclerView.ViewHolder newHolder) {
        int index = newHolder.getAdapterPosition();

        Log.v(TAG, "onChangeEnd index=" + index);

        if (index >= 0 && index <= data.size() - 1) {
            ViewHolder vh = (ViewHolder) newHolder;
            vh.isSwiped = false;

            data.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void addItem(ShoppingListItem item) {
        if (item.isActive()) {
            if (item.urgent) {
                data.add(0, item);
                notifyItemInserted(0);

                Log.v(TAG, "addItem urgent position=0");
            } else {
                int size = data.size();
                int firstNonUrgent = 0;
                while (firstNonUrgent < size && data.get(firstNonUrgent).urgent) {
                    firstNonUrgent++;
                }

                data.add(firstNonUrgent, item);
                notifyItemInserted(firstNonUrgent);

                Log.v(TAG, "addItem position=" + firstNonUrgent);
            }


        } else {
            Log.v(TAG, "addItem not active");
        }
    }

    public void setItems(Collection<ShoppingListItem> items) {
        data.clear();
        data.addAll(items);

        // Urgent first, createdAt descend
        Collections.sort(data, new Comparator<ShoppingListItem>() {
            @Override
            public int compare(ShoppingListItem a, ShoppingListItem b) {
                if (a.urgent == b.urgent) {
                    return (int) (b.createdAt - a.createdAt);
                }

                return (a.urgent && !b.urgent) ? -1 : 1;
            }
        });

        notifyItemRangeInserted(0, data.size());
    }

    public void updateItem(ShoppingListItem item) {
        int index = data.indexOf(item);
        Log.v(TAG, "updateItem index=" + index);

        if (index != -1) {
            if (item.isActive()) {
                data.set(index, item);
                notifyItemChanged(index);
            } else {
                data.set(index, item);
                notifyItemChanged(index);
            }
        }
    }

    public void removeItem(ShoppingListItem item) {
        int index = data.indexOf(item);
        Log.v(TAG, "removeItem index=" + index);

        if (index != -1) {
            data.remove(index);
            notifyItemRemoved(index);
        }
    }

    public int getViewSize() {
        return viewSize;
    }

    public void setViewSize(int size) {
        if (size != VIEW_SIZE_COZY && size != VIEW_SIZE_COMPACT) {
            size = VIEW_SIZE_COMPACT;
        }

        this.viewSize = size;
        recyclerView.getItemAnimator().endAnimations();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView[] textViews;
        public final int[] defaultTextColors;
        public final int defaultBackgroundColor;

        public TextView nameTextView;
        public TextView createdByTextView;
        public ImageView urgentImageView;
        public View detailsView;
        public TextView descriptionTextView;
        public TextView priceTextView;

        public ShoppingListItem data;

        public boolean isSwiped;

        public ViewHolder(ViewGroup vg) {
            super(vg);

            nameTextView = vg.findViewById(R.id.shopping_list_item_name);
            createdByTextView = vg.findViewById(R.id.shopping_list_item_created_by);
            detailsView = vg.findViewById(R.id.shopping_list_item_details);
            urgentImageView = vg.findViewById(R.id.shopping_list_item_urgent);
            descriptionTextView = vg.findViewById(R.id.shopping_list_item_description);
            priceTextView = vg.findViewById(R.id.shopping_list_item_price);

            textViews = new TextView[]{
                    nameTextView,
                    createdByTextView,
                    descriptionTextView,
                    priceTextView
            };

            defaultTextColors = new int[textViews.length];
            for (int i = 0; i < defaultTextColors.length; i++) {
                defaultTextColors[i] = textViews[i].getCurrentTextColor();
            }

            defaultBackgroundColor = ((ColorDrawable) itemView.getBackground()).getColor();
        }

        public void resetTextColor() {
            for (int i = 0; i < defaultTextColors.length; i++) {
                textViews[i].setTextColor(defaultTextColors[i]);
            }
        }

        public void resetBackgroundColor() {
            itemView.setBackgroundColor(defaultBackgroundColor);
        }

    }

}
