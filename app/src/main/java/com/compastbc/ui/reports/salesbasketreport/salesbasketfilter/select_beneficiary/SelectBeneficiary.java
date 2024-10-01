package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_beneficiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesBeneficiary;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseDialog;
import com.compastbc.ui.base.BaseFilterSalesReport;

import java.util.ArrayList;
import java.util.List;

public class SelectBeneficiary extends BaseDialog implements SelectBeneficiaryMvpView, SelectBeneficiaryAdapter.ItemClickListener {
    private static OnFragmentInteractionListener mListener;
    private static String startDate, endDate;
    private int offset = 0;
    private LinearLayout linearLayout;
    private SelectBeneficiaryAdapter adapter;
    private List<SalesBeneficiary> list;
    private SelectBeneficiaryMvpPresenter<SelectBeneficiaryMvpView> mvpPresenter;

    public static SelectBeneficiary newInstance(OnFragmentInteractionListener listener, String stDate, String eDate) {
        SelectBeneficiary fragment = new SelectBeneficiary();
        Bundle args = new Bundle();
        startDate = stDate;
        endDate = eDate;
        fragment.setArguments(args);
        mListener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_beneficiary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new SelectBeneficiaryPresenter<>(getDataManager(), getBaseActivity());
        mvpPresenter.onAttach(this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        linearLayout = view.findViewById(R.id.linear);
        list = new ArrayList<>();
        adapter = new SelectBeneficiaryAdapter(list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        mvpPresenter.getBeneficiaryDetails(BaseFilterSalesReport.getProgrammeId(), BaseFilterSalesReport.getCommodityId(), BaseFilterSalesReport.getUom(), offset, startDate, endDate);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getBeneficiaryDetails(BaseFilterSalesReport.getProgrammeId(), BaseFilterSalesReport.getCommodityId(), BaseFilterSalesReport.getUom(), offset, startDate, endDate);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        ViewGroup frame = view.findViewById(R.id.frame);
        frame.setOnClickListener(v -> dismissDialog("Select Benf"));
    }

    @Override
    public void setData(List<SalesBeneficiary> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();
        list.addAll(data);
        if (list.size() > 0) {
            adapter.notifyDataSetChanged();
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            sweetAlert(1, R.string.error, R.string.NoBeneficiaries).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                dismissDialog("Select Benf");
            }).show();

        }
    }

    @Override
    public void dismissDialogView() {
        dismissDialog("Select Benf");
    }

    @Override
    public void onItemClick(String name) {

        if (mListener != null) {
            dismissDialog("Select Benf");
            mListener.onFragmentInteraction(list, name);
        }
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(List<SalesBeneficiary> list, String name);
    }
}
