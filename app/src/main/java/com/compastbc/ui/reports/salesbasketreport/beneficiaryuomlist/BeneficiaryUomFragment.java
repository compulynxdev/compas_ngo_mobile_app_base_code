package com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesBeneficiary;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseSalesBasketFragment;
import com.compastbc.ui.beneficiary.list_beneficiary.detail.BeneficiaryDetailActivity;
import com.compastbc.ui.reports.salesbasketreport.SalesBasketReportCallBack;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryUomFragment extends BaseSalesBasketFragment implements BeneficiaryUomMvpView {

    private static SalesBasketReportCallBack callBack;
    private List<SalesBeneficiary> list;
    private BeneficiaryUomAdapter adapter;
    private int offset = 0;
    private BeneficiaryUomMvpPresenter<BeneficiaryUomMvpView> mvpPresenter;
    private boolean isDetailClick = false;

    public static BeneficiaryUomFragment newInstance(SalesBasketReportCallBack reportCallBack) {
        BeneficiaryUomFragment fragment = new BeneficiaryUomFragment();
        Bundle args = new Bundle();
        callBack = reportCallBack;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_beneficiary_uom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callBack != null && !isDetailClick)
            callBack.onReceiveBeneficiaries(null);
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new BeneficiaryUomPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        view.findViewById(R.id.frameLayout).setOnClickListener(v -> {
        });

        TextView tvName = view.findViewById(R.id.productName);
        String name = getCommodityName().concat(" (").concat(getUom()).concat(")");
        tvName.setText(name);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        list = new ArrayList<>();
        adapter = new BeneficiaryUomAdapter(list, pos -> {
            createLog("Sales Basket Report", "Click on details");
            Intent intent = BeneficiaryDetailActivity.getStartIntent(getActivity());
            intent.putExtra("EditMode", false);
            isDetailClick = true;
            intent.putExtra("IdentityNo", list.get(pos).getIdentityNo());
            intent.putExtra("pos", pos);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getBeneficiaryDetails(getProgrammeId(), getCommodityId(), getUom(), offset);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        mvpPresenter.getBeneficiaryDetails(getProgrammeId(), getCommodityId(), getUom(), offset);
    }

    @Override
    public void setData(List<SalesBeneficiary> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();

        list.addAll(data);
        adapter.notifyDataSetChanged();

        if (callBack != null)
            callBack.onReceiveBeneficiaries(list);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            isDetailClick = false;
        }
    }
}
