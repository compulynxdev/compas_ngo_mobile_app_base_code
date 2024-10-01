package com.compastbc.ui.reports.void_transaction_report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.core.utils.pagination.FooterLoader;

import java.util.List;
import java.util.Locale;

public class VoidTransactionAdapter extends RecyclerView.Adapter {
    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private boolean showLoader;
    private final List<TransactionHistory> historyList;

    VoidTransactionAdapter(List<TransactionHistory> histories) {
        this.historyList = histories;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pagination_item_loader, parent, false);
                return new FooterLoader(view);

            default:
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.void_row, parent, false);
                return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterLoader) {
            FooterLoader loaderViewHolder = (FooterLoader) holder;
            if (showLoader) {
                loaderViewHolder.mProgressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.mProgressBar.setVisibility(View.GONE);
            }
            return;
        }
        VoidTransactionAdapter.ViewHolder rvHolder = (ViewHolder) holder;

        TransactionHistory mainBean = historyList.get(position);
        rvHolder.transId.setText(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.getReceiptNo())));
        rvHolder.identification.setText(mainBean.getIdentityNo());
        rvHolder.name.setText(mainBean.getBenfName());
        rvHolder.amount.setText(String.format(Locale.getDefault(), "%s %.2f", mainBean.getCurrency(), Double.parseDouble(mainBean.getAmount())));
        rvHolder.date.setText(CalenderUtils.formatByLocale(mainBean.getDate(), CalenderUtils.DATE_FORMAT, Locale.getDefault()));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == historyList.size() - 1) {
            return showLoader ? VIEWTYPE_LOADER : VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView transId, name, identification, date, amount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            transId = itemView.findViewById(R.id.transId);
            name = itemView.findViewById(R.id.name);
            identification = itemView.findViewById(R.id.identification);
            amount = itemView.findViewById(R.id.amt);
            date = itemView.findViewById(R.id.date);
        }
    }
}
