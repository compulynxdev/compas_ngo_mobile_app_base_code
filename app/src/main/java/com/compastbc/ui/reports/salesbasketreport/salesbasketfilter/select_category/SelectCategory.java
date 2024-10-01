package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesCategoryBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseDialog;
import com.compastbc.ui.base.BaseFilterSalesReport;

import java.util.ArrayList;
import java.util.List;

public class SelectCategory extends BaseDialog implements SelectCategoryMvpView, SelectCategoryAdapter.ItemClickListener {

    private static OnFragmentInteractionListener interactionListener;
    private static String startDate, endDate;
    private LinearLayout linearLayout;
    private int offset = 0;
    private SelectCategoryAdapter adapter;
    private List<SalesCategoryBean> list;
    private SelectCategoryMvpPresenter<SelectCategoryMvpView> mvpPresenter;

    public static SelectCategory newInstance(OnFragmentInteractionListener listener, String stDate, String eDate) {
        SelectCategory fragment = new SelectCategory();
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
        return inflater.inflate(R.layout.fragment_select_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new SelectCategoryPresenter<>(getDataManager(), getBaseActivity());
        mvpPresenter.onAttach(this);
        showLoading();
        list = new ArrayList<>();
        adapter = new SelectCategoryAdapter(list);
        adapter.setClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        linearLayout = view.findViewById(R.id.linear);
        mvpPresenter.getSaleCatrgories(BaseFilterSalesReport.getProgrammeId(), BaseFilterSalesReport.getProductId(), offset, startDate, endDate);
        recyclerView.setAdapter(adapter);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getSaleCatrgories(BaseFilterSalesReport.getProgrammeId(), BaseFilterSalesReport.getProductId(), offset, startDate, endDate);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        ViewGroup frame = view.findViewById(R.id.frame);
        frame.setOnClickListener(v -> dismissDialog("Select Category"));
    }

    @Override
    public void setData(List<SalesCategoryBean> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();

        list.addAll(data);

        if (list.size() > 0) {
            linearLayout.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        } else {
            sweetAlert(1, R.string.error, R.string.NoCategory).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                dismissDialog("Select Category");
            }).show();

        }
    }

    @Override
    public void dismissDialogView() {
        dismissDialog("Select Category");
    }

    @Override
    public void onItemClick(String id) {
        if (interactionListener != null) {
            String[] ids = id.split(",");
            SalesCategoryBean bean = mvpPresenter.getCategory(list, ids[0]);
            dismissDialog("Select Category");
            interactionListener.onFragmentInteraction(list, id, bean);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(List<SalesCategoryBean> list, String id, SalesCategoryBean bean);
    }
}
