package com.compastbc.ui.reports.salesbasketreport.uoms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.Uom;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseSalesBasketFragment;
import com.compastbc.ui.reports.salesbasketreport.SalesBasketReportCallBack;
import com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist.BeneficiaryUomFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UomListFragment extends BaseSalesBasketFragment implements UomListMvpView, UomListAdapter.ItemClickListener {

    private static SalesBasketReportCallBack callBack;
    private UomListAdapter adapter;
    private List<Uom> list;
    private int offset = 0;
    private Uom uom;
    private UomListMvpPresenter<UomListMvpView> mvpPresenter;

    public static UomListFragment newInstance(SalesBasketReportCallBack reportCallBack) {
        UomListFragment fragment = new UomListFragment();
        callBack = reportCallBack;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_uom_list, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new UomListPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        view.findViewById(R.id.frameLayout).setOnClickListener(v -> {
        });
        TextView commodityName = view.findViewById(R.id.productName);
        commodityName.setText(String.format(Locale.getDefault(), "%s", getCommodityName()));
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        list = new ArrayList<>();
        adapter = new UomListAdapter(list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        mvpPresenter.getSaleUom(getProgrammeId(), getCommodityId(), offset);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getSaleUom(getProgrammeId(), getCommodityId(), offset);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public void setData(List<Uom> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();

        list.addAll(data);
        adapter.notifyDataSetChanged();

        if (callBack != null)
            callBack.onReceiveUoms(list, uom);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callBack != null)
            callBack.onReceiveUoms(null, null);
    }

    @Override
    public void onClick(String uom1) {
        setUom(uom1);
        uom = mvpPresenter.getUom(list, uom1);
        if (callBack != null)
            callBack.onReceiveUoms(list, uom);


        getBaseActivity().addFragment(BeneficiaryUomFragment.newInstance(callBack), R.id.frame, true);
    }
}