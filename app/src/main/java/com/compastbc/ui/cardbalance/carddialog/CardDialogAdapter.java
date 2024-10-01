package com.compastbc.ui.cardbalance.carddialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.CardBalanceBean;

import java.util.List;

public class CardDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<CardBalanceBean> cardBalanceBeans;

    CardDialogAdapter(List<CardBalanceBean> beans) {
        this.cardBalanceBeans = beans;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.balance_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.progName.setText(cardBalanceBeans.get(position).getProgramName());
        viewHolder.value.setText(cardBalanceBeans.get(position).getVoucherValue());
    }

    @Override
    public int getItemCount() {
        return cardBalanceBeans.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView progName;
        private final TextView value;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            progName = itemView.findViewById(R.id.tv_progName);
            value = itemView.findViewById(R.id.tv_value);
        }
    }
}
