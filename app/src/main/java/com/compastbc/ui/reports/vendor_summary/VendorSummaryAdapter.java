package com.compastbc.ui.reports.vendor_summary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.db.model.TxnCount;

import java.util.List;
import java.util.Locale;

public class VendorSummaryAdapter extends RecyclerView.Adapter {

    private final List<TxnCount> list;

    VendorSummaryAdapter( List<TxnCount> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor_summary_report, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        TxnCount bean = list.get(position);
        viewHolder.label.setText(bean.getDate());
        viewHolder.count.setText(String.format(Locale.getDefault(),"%d",bean.getCount()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView label;
        private final TextView count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            label = itemView.findViewById(R.id.tv_label);
            count = itemView.findViewById(R.id.tv_count);
        }
    }

}
