package com.compastbc.ui.cardrestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.base.ItemClickListener;
import com.compastbc.core.data.db.model.NFCCardData;
import java.util.List;

public class CardRestoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<NFCCardData> nfcCardDataList;
    private final ItemClickListener listener;

    CardRestoreAdapter(List<NFCCardData> list, ItemClickListener clickListener) {
        this.nfcCardDataList = list;
        this.listener = clickListener;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_restore, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;

        NFCCardData bean = nfcCardDataList.get(position);

        viewHolder.tvId.setText(" ".concat(bean.getCardID()));
        viewHolder.tvName.setText(bean.getBeneficiaryName());
    }

    @Override
    public int getItemCount() {
        return nfcCardDataList.size();
    }

    @Override
    public long getItemId(int position) {
        NFCCardData bean = nfcCardDataList.get(position);
        return bean.getId();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tvId, tvName;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvId = itemView.findViewById(R.id.tv_id);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null && getAdapterPosition() != -1) {
                listener.onItemClick(getAdapterPosition());
            }
        }
    }

}
