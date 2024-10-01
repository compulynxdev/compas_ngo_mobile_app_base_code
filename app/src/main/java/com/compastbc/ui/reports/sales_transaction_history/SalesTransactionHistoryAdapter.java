package com.compastbc.ui.reports.sales_transaction_history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.utils.pagination.FooterLoader;

import java.util.List;
import java.util.Locale;

public class SalesTransactionHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private final List<TransactionHistory> historyList;
    private final Context context;
    private boolean showLoader;

    SalesTransactionHistoryAdapter(List<TransactionHistory> histories, Context context) {
        this.historyList = histories;
        this.context = context;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_report_adapter, parent, false);
                return new ViewHolder(view);

        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == historyList.size() - 1) {
            return showLoader ? VIEWTYPE_LOADER : VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }


    public void showLoading(boolean status) {
        showLoader = status;
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
        ViewHolder viewHolder = (ViewHolder) holder;
        TransactionHistory mainBean = historyList.get(position);
        viewHolder.transId.setText(context.getString(R.string.transId).concat(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.getReceiptNo()))));
        viewHolder.cname.setText(mainBean.getCommodityName());
        viewHolder.uom.setText(mainBean.getUom());
        viewHolder.quantity.setText(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(mainBean.getQuantity())));
        viewHolder.value.setText(String.format(Locale.getDefault(), "%s %.2f", mainBean.getCurrency(), Double.parseDouble(mainBean.getAmount())));
        viewHolder.identityNo.setText(mainBean.getIdentityNo());
        if (mainBean.getTransactionType().equalsIgnoreCase("-1"))
            viewHolder.void_.setText(R.string.VoidTransaction);
        else viewHolder.void_.setText("");

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView transId, cname, uom, quantity, value, identityNo, void_;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            transId = itemView.findViewById(R.id.transId);
            cname = itemView.findViewById(R.id.cname);
            uom = itemView.findViewById(R.id.uom);
            quantity = itemView.findViewById(R.id.quantity);
            value = itemView.findViewById(R.id.value);
            identityNo = itemView.findViewById(R.id.identityNo);
            void_ = itemView.findViewById(R.id.void_);
        }
    }
}
