package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesBasketReportModel;
import com.compastbc.ui.base.BaseFragment;
import com.compastbc.ui.beneficiary.list_beneficiary.detail.BeneficiaryDetailActivity;
import com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist.BeneficiaryUomAdapter;
import com.compastbc.ui.reports.salesbasketreport.categorylist.CategoryListAdapter;
import com.compastbc.ui.reports.salesbasketreport.commoditylist.CommodityListAdapter;
import com.compastbc.ui.reports.salesbasketreport.programlist.ProgramBeanAdapter;
import com.compastbc.ui.reports.salesbasketreport.uoms.UomListAdapter;

public class DisplayFilterData extends BaseFragment {
    private static SalesBasketReportModel basketReportModel;
    private TextView title, tv_col1, tv_col2, tv_col3;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;

    public static DisplayFilterData newInstance(SalesBasketReportModel salesBasketReportModel) {
        DisplayFilterData fragment = new DisplayFilterData();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        basketReportModel = salesBasketReportModel;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_filter_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        findIds(view);
        if (basketReportModel != null) {
            if (basketReportModel.salesBeneficiaries != null)
                showData(5);
            else if (basketReportModel.uomList != null)
                showData(4);
            else if (basketReportModel.salesCommodityBeans != null)
                showData(3);
            else if (basketReportModel.salesCategoryBeans != null)
                showData(2);
            else showData(1);
        }
    }

    private void findIds(View view) {
        linearLayout = view.findViewById(R.id.linear);
        recyclerView = view.findViewById(R.id.recycler_view);
        title = view.findViewById(R.id.titleName);
        tv_col1 = view.findViewById(R.id.col1);
        tv_col2 = view.findViewById(R.id.col2);
        tv_col3 = view.findViewById(R.id.col3);
    }

    private void showData(int a) {

        switch (a) {
            case 1:
                linearLayout.setVisibility(View.GONE);
                tv_col1.setText(R.string.ProgrammeName);
                tv_col2.setText(R.string.totalAmount);
                tv_col3.setText(R.string.benfCount);
                ProgramBeanAdapter adapter = new ProgramBeanAdapter(basketReportModel.salesProgramBeans);
                adapter.setClickListener(null);
                recyclerView.setAdapter(adapter);
                break;


            case 2:
                linearLayout.setVisibility(View.GONE);
                tv_col1.setText(R.string.CategoryName);
                tv_col2.setText(R.string.totalAmount);
                tv_col3.setText(R.string.benfCount);
                CategoryListAdapter categoryListAdapter = new CategoryListAdapter(basketReportModel.salesCategoryBeans);
                categoryListAdapter.setClickListener(null);
                recyclerView.setAdapter(categoryListAdapter);
                break;

            case 3:
                linearLayout.setVisibility(View.GONE);
                tv_col1.setText(R.string.ProductName);
                tv_col2.setText(R.string.totalAmount);
                tv_col3.setText(R.string.benfCount);
                CommodityListAdapter commodityListAdapter = new CommodityListAdapter(basketReportModel.salesCommodityBeans);
                commodityListAdapter.setClickListener(null);
                recyclerView.setAdapter(commodityListAdapter);
                break;


            case 4:
                linearLayout.setVisibility(View.VISIBLE);
                title.setText(basketReportModel.commodityBean.getCommodityName());
                tv_col1.setText(R.string.Uom);
                tv_col2.setText(R.string.maxPrice);
                tv_col3.setText(R.string.benfCount);
                UomListAdapter uomListAdapter = new UomListAdapter(basketReportModel.uomList);
                uomListAdapter.setClickListener(null);
                recyclerView.setAdapter(uomListAdapter);
                break;

            case 5:
                linearLayout.setVisibility(View.VISIBLE);
                String string = basketReportModel.commodityBean.getCommodityName().concat(" (").concat(basketReportModel.uom.getUom()).concat(")");
                title.setText(string);
                tv_col1.setText(R.string.benfCount);
                tv_col2.setText(R.string.Quantity);
                tv_col3.setText(R.string.Value);
                BeneficiaryUomAdapter beneficiaryUomAdapter = new BeneficiaryUomAdapter(basketReportModel.salesBeneficiaries, pos -> {
                    createLog("Sales Basket Report", "Click on details");
                    Intent intent = BeneficiaryDetailActivity.getStartIntent(getActivity());
                    intent.putExtra("EditMode", false);
                    intent.putExtra("IdentityNo", basketReportModel.salesBeneficiaries.get(pos).getIdentityNo());
                    intent.putExtra("pos", pos);
                    startActivity(intent);
                });
                recyclerView.setAdapter(beneficiaryUomAdapter);
                break;

        }
    }
}
