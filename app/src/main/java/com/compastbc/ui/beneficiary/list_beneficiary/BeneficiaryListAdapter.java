package com.compastbc.ui.beneficiary.list_beneficiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.compastbc.Compas;
import com.compastbc.R;
import com.compastbc.core.base.ClickListener;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;
import com.compastbc.core.utils.pagination.FooterLoader;

import java.util.List;

public class BeneficiaryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private boolean showLoader;

    private List<BeneficiaryListResponse.ContentBean> bnfList;
    private List<Beneficiary> bnfListDB;
    private final boolean isDB;
    private final ClickListener listener;
    private final boolean isBiometric;

    BeneficiaryListAdapter(List<BeneficiaryListResponse.ContentBean> list, ClickListener clickListener) {
        this.isDB = false;
        this.bnfList = list;
        this.listener = clickListener;
        this.isBiometric = Compas.getInstance().getDataManager().getConfigurableParameterDetail().isBiometric();
    }

    BeneficiaryListAdapter(boolean isDB, List<Beneficiary> list, ClickListener clickListener) {
        this.isDB = isDB;
        this.bnfListDB = list;
        this.listener = clickListener;
        this.isBiometric = Compas.getInstance().getDataManager().getConfigurableParameterDetail().isBiometric();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    public void showLoading(boolean status) {
        showLoader = status;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beneficiaries, parent, false);
                return new ViewHolder(view);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (isDB ? bnfListDB.size() : bnfList.size()) - 1) {
            return showLoader ? VIEWTYPE_LOADER : VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterLoader) {
            FooterLoader loaderViewHolder = (FooterLoader) holder;
            loaderViewHolder.mProgressBar.setVisibility(showLoader ? View.VISIBLE : View.GONE);
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;

        if (isDB) {
            Beneficiary bean = bnfListDB.get(position);

            viewHolder.tvId.setText(" ".concat(bean.getIdentityNo()));
            viewHolder.tvName.setText(bean.getFirstName().concat(" ").concat(bean.getLastName()));
            viewHolder.tvGender.setText(" : ".concat((bean.getGender().equalsIgnoreCase("M") || bean.getGender().equalsIgnoreCase(viewHolder.tvId.getContext().getString(R.string.m)))
                    ? viewHolder.tvId.getContext().getString(R.string.male) : viewHolder.tvId.getContext().getString(R.string.female)));
            viewHolder.tvBioStatus.setText(" : ".concat(String.valueOf(bean.getBio())));
        } else {
            BeneficiaryListResponse.ContentBean bean = bnfList.get(position);

            viewHolder.tvId.setText(" ".concat(bean.getIdPassPortNo()));
            viewHolder.tvName.setText(bean.getFirstName().concat(" ").concat(bean.getSurName()));
            viewHolder.tvGender.setText(" : ".concat((bean.getGender().equalsIgnoreCase("M") || bean.getGender().equalsIgnoreCase(viewHolder.tvId.getContext().getString(R.string.m)))
                    ? viewHolder.tvId.getContext().getString(R.string.male) : viewHolder.tvId.getContext().getString(R.string.female)));
            viewHolder.tvBioStatus.setText(" : ".concat(String.valueOf(bean.isBioStatus())));

            if (!bean.getBenfImage().isEmpty()) {
                Glide.with(viewHolder.tvId.getContext())
                        .load(bean.getBenfImage())
                        .apply(new RequestOptions().centerCrop().placeholder(R.drawable.ic_def_user))
                        .into(viewHolder.imgUser);
            }
        }
    }

    @Override
    public int getItemCount() {
        return isDB ? bnfListDB.size() : bnfList.size();
    }

    @Override
    public long getItemId(int position) {
        if (isDB) {
            Beneficiary bean = bnfListDB.get(position);
            return bean.getId();
        } else {
            BeneficiaryListResponse.ContentBean bean = bnfList.get(position);
            return bean.getMemberId();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tvBioStatus;
        private final TextView tvId;
        private final TextView tvGender;
        private final TextView tvName;
        private final ImageView imgUser;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvId = itemView.findViewById(R.id.tv_id);
            tvGender = itemView.findViewById(R.id.tv_gender);
            tvBioStatus = itemView.findViewById(R.id.tv_bio_status);
            imgUser = itemView.findViewById(R.id.img_user);
            itemView.findViewById(R.id.bio_linear).setVisibility(isBiometric ? View.VISIBLE : View.GONE);
            ImageView imgEdit = itemView.findViewById(R.id.img_edit);
            imgEdit.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_edit) {
                if (listener != null && getAdapterPosition() != -1) {
                    listener.onEditClick(getAdapterPosition());
                }
            } else {
                if (listener != null && getAdapterPosition() != -1) {
                    listener.onItemClick(getAdapterPosition());
                }
            }
        }
    }

}
