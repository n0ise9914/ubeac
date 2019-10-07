package io.ubeac.app.features.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.ubeac.app.R;

import java.util.ArrayList;
import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ItemHolder> {
    private Context context;
    private List<DrawerItem> items;
    private DrawerClickListener drawerClickListener;

    public void setDrawerClickListener(DrawerClickListener drawerClickListener) {
        this.drawerClickListener = drawerClickListener;
    }

    public void setItems(List<DrawerItem> items) {
        this.items = items;
    }

    DrawerAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        final DrawerItem item = items.get(position);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerClickListener != null)
                    drawerClickListener.OnClick(item.getType());
            }
        });

        Drawable icon = context.getResources().getDrawable(item.getIcon());
        holder.name.setText(item.getName());
        holder.icon.setImageDrawable(icon);
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.main_item_drawer;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class ItemHolder extends RecyclerView.ViewHolder {
        public View container;
        public LinearLayout item;
        public ImageView icon;
        public TextView name;

        public ItemHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            item = (LinearLayout) itemView.findViewById(R.id.item);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            container = itemView;
        }
    }
}
