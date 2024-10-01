package com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.base.ItemClickListener;
import com.compastbc.core.data.network.model.SalesBeneficiary;
import com.compastbc.core.utils.pagination.FooterLoader;

import java.util.List;
import java.util.Locale;

public class BeneficiaryUomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private final List<SalesBeneficiary> list;
    private boolean showLoader;
    private final ItemClickListener itemClickListener;


    public BeneficiaryUomAdapter(List<SalesBeneficiary> uoms, ItemClickListener itemClickListener) {
        this.list = uoms;
        this.itemClickListener = itemClickListener;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sales_basket_adapter, parent, false);
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
        String name = list.get(position).getName().concat(" (").concat(list.get(position).getIdentityNo().concat(")"));
        viewHolder.pname.setText(name);
        viewHolder.count.setText(list.get(position).getCurrency().concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(list.get(position).getValue()))));
        viewHolder.amount.setText(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(list.get(position).getQuantity())));

        viewHolder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == list.size() - 1) {
            return showLoader ? VIEWTYPE_LOADER : VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView pname;
        private final TextView amount;
        private final TextView count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            pname = itemView.findViewById(R.id.name);
            amount = itemView.findViewById(R.id.amount);
            count = itemView.findViewById(R.id.count);
        }
    }

}
