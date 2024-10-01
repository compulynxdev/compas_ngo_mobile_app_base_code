package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_uom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.Uom;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseDialog;
import com.compastbc.ui.base.BaseFilterSalesReport;

import java.util.ArrayList;
import java.util.List;


public class SelectUom extends BaseDialog implements SelectUomMvpView, SelectUomAdapter.ItemClickListener {

    private static OnFragmentInteractionListener interactionListener;
    private static String startDate, endDate;
    private LinearLayout linearLayout;
    private int offset = 0;
    private SelectUomMvpPresenter<SelectUomMvpView> mvpPresenter;
    private SelectUomAdapter adapter;
    private List<Uom> list;

    public static SelectUom newInstance(OnFragmentInteractionListener listener, String stDate, String eDate) {
        SelectUom fragment = new SelectUom();
        Bundle args = new Bundle();
        startDate = stDate;
        endDate = eDate;
        fragment.setArguments(args);
        interactionListener = listener;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_uom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new SelectUomPresenter<>(getDataManager(), getBaseActivity());
        mvpPresenter.onAttach(this);
        showLoading();
        list = new ArrayList<>();
        adapter = new SelectUomAdapter(list);
        adapter.setClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        linearLayout = view.findViewById(R.id.linear);
        mvpPresenter.getSaleUom(BaseFilterSalesReport.getProgrammeId(), BaseFilterSalesReport.getCommodityId(), offset, startDate, endDate);
        recyclerView.setAdapter(adapter);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getSaleUom(BaseFilterSalesReport.getProgrammeId(), BaseFilterSalesReport.getCommodityId(), offset, startDate, endDate);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        ViewGroup frame = view.findViewById(R.id.frame);
        frame.setOnClickListener(v -> dismissDialog("Select Uom"));
    }

    @Override
    public void setData(List<Uom> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();
        list.addAll(data);
        if (list.size() > 0) {
            adapter.notifyDataSetChanged();
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            sweetAlert(1, R.string.error, R.string.NoUoms).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                dismissDialog("Select Uom");
            }).show();

        }
    }

    @Override
    public void dismissDialogView() {
        dismissDialog("Select Uom");
    }

    @Override
    public void onClick(String uom) {
        if (interactionListener != null) {
            Uom uomBean = mvpPresenter.getUom(list, uom);
            dismissDialog("Select Uom");
            interactionListener.onFragmentInteraction(list, uomBean, uom);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(List<Uom> list, Uom uomBean, String uri);
    }
}
