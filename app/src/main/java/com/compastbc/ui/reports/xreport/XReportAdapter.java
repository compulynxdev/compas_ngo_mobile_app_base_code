package com.compastbc.ui.reports.xreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.XReportBean;
import com.compastbc.core.utils.pagination.FooterLoader;

import java.util.List;
import java.util.Locale;

public class XReportAdapter extends RecyclerView.Adapter {

    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private final List<XReportBean> list;
    private boolean showLoader;
    private final Context context;

    XReportAdapter(List<XReportBean> beans, Context context1) {
        list = beans;
        context = context1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == list.size() - 1) {
            return showLoader ? VIEWTYPE_LOADER : VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_x_report, parent, false);
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

        ViewHolder viewHolder = (ViewHolder) holder;
        XReportBean mainBean = list.get(position);

        viewHolder.purchase.setText(context.getString(R.string.purvalue).concat(" ").concat(mainBean.currencyType));
        viewHolder.txnCount.setText(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.transactionCount)));
        viewHolder.vCount.setText(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.voidCount)));
        viewHolder.vAmount.setText(String.format(Locale.getDefault(), "%s %.2f", mainBean.currencyType, Double.parseDouble(mainBean.voidAmount)));
        viewHolder.txnAmount.setText(String.format(Locale.getDefault(), "%s %.2f", mainBean.currencyType, Double.parseDouble(mainBean.transactionAmount)));
        viewHolder.nSales.setText(String.format(Locale.getDefault(), "%s %.2f", mainBean.currencyType, Double.parseDouble(mainBean.transactionAmount)));
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txnCount, vCount, nSales, txnAmount, vAmount, purchase;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txnCount = itemView.findViewById(R.id.txnCount);
            vCount = itemView.findViewById(R.id.vCount);
            nSales = itemView.findViewById(R.id.nsales);
            vAmount = itemView.findViewById(R.id.vAmt);
            txnAmount = itemView.findViewById(R.id.samt);
            purchase = itemView.findViewById(R.id.pur_value);
        }
    }
}