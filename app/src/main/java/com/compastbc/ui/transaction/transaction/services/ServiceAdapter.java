package com.compastbc.ui.transaction.transaction.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.compastbc.R;
import com.compastbc.core.data.db.model.Services;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ServiceAdapter extends RecyclerView.Adapter {
    private final List<Services> list;
    private ItemClickListener itemClickListener;

    ServiceAdapter(List<Services> servicesList) {
        list = servicesList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_programmes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ServiceAdapter.ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.serviceTitle.setText(list.get(position).getServiceName());
        viewHolder.cardView.setTag(R.id.holder_id, list.get(0).getServiceId());

        try {
            Bitmap decodedByte;
            byte[] decodedString = Base64.decode(list.get(position).getServiceImage(), Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            viewHolder.imageView.setImageBitmap(decodedByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void setClickListener(ItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    public interface ItemClickListener {
        void onClick(View v, Services services);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView serviceTitle;
        private final ImageView imageView;
        private final CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceTitle = itemView.findViewById(R.id.program_title);
            imageView = itemView.findViewById(R.id.program_image);
            cardView = itemView.findViewById(R.id.cardview);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cardview) {
                if (itemClickListener != null) {
                    itemClickListener.onClick(v, list.get(getAdapterPosition()));
                }
            }
        }
    }
}
