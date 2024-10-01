package com.compastbc.ui.reports.salesbasketreport.programlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesProgramBean;
import com.compastbc.core.utils.pagination.FooterLoader;

import java.util.List;
import java.util.Locale;

public class ProgramBeanAdapter extends RecyclerView.Adapter {

    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private ItemClickListener listener;
    private final List<SalesProgramBean> list;
    private boolean showLoader;

    public ProgramBeanAdapter(List<SalesProgramBean> programBeans) {
        list = programBeans;
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
        SalesProgramBean bean = list.get(position);
        viewHolder.pname.setText(bean.getProgramName());
        viewHolder.amount.setText(bean.getCurrency().concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(bean.getTotalAmount()))));
        viewHolder.count.setText(String.format(Locale.getDefault(), "%d", Integer.parseInt(bean.getBeneficiaryCount())));
        String id = bean.getProgramId().concat(",").concat(bean.getProductId());
        viewHolder.cardView.setTag(R.id.holder_id, id);
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setClickListener(ItemClickListener clickListener) {
        listener = clickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View v, String... ids);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView pname;
        private final TextView amount;
        private final TextView count;
        private final CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            pname = itemView.findViewById(R.id.name);
            amount = itemView.findViewById(R.id.amount);
            count = itemView.findViewById(R.id.count);
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cardView) {
                if (listener != null) {
                    String[] ids = v.getTag(R.id.holder_id).toString().split(",");
                    listener.onItemClick(v, ids);
                }
            }
        }
    }
}
