package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_program;

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

public class SelectProgramAdapter extends RecyclerView.Adapter {

    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private ItemClickListener listener;
    private final List<SalesProgramBean> list;
    private boolean showLoader;

    SelectProgramAdapter(List<SalesProgramBean> programBeans) {
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_program, parent, false);
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
        viewHolder.pname.setText(list.get(position).getProgramName());
        String id = list.get(position).getProgramId().concat(",").concat(list.get(position).getProductId().concat(",").concat(list.get(position).getProgramName()));
        viewHolder.cardView.setTag(R.id.holder_id, id);
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void setClickListener(ItemClickListener clickListener) {
        listener = clickListener;
    }

    public interface ItemClickListener {
        void onItemClick(String... ids);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView pname;
        private final CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            pname = itemView.findViewById(R.id.pname);
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cardView) {
                if (listener != null) {
                    showLoading(true);
                    String[] ids = v.getTag(R.id.holder_id).toString().split(",");
                    listener.onItemClick(ids);
                }
            }
        }
    }
}
