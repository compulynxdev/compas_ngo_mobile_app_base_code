package com.compastbc.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.base.BaseViewHolder;
import com.compastbc.core.base.ItemClickListener;
import com.compastbc.core.data.network.model.HomeBean;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final List<HomeBean> list;
    private final ItemClickListener itemClickCallback;

    public HomeAdapter(List<HomeBean> list, ItemClickListener itemClickCallback) {
        this.list = list;
        this.itemClickCallback = itemClickCallback;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends BaseViewHolder {

        ImageView img_icon;
        TextView tv_title;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_icon = itemView.findViewById(R.id.img_icon);
            tv_title = itemView.findViewById(R.id.tv_title);

            itemView.setOnClickListener(v -> {
                if (itemClickCallback != null && getAdapterPosition() != -1) {
                    itemClickCallback.onItemClick(getAdapterPosition());
                }
            });
        }

        @Override
        public void onBind(int position) {
            HomeBean mainBean = list.get(position);

            tv_title.setText(mainBean.getTitle());
            img_icon.setImageDrawable(img_icon.getContext().getResources().getDrawable(mainBean.getIcon()));
        }
    }
}
