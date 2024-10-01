package com.compastbc.ui.reports.salesbasketreport.commoditylist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesCommodityBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseSalesBasketFragment;
import com.compastbc.ui.reports.salesbasketreport.SalesBasketReportCallBack;
import com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist.BeneficiaryUomFragment;
import com.compastbc.ui.reports.salesbasketreport.uoms.UomListFragment;

import java.util.ArrayList;
import java.util.List;

public class CommodityListFragment extends BaseSalesBasketFragment implements CommodityListMvpView, CommodityListAdapter.ItemClickListener {

    private static SalesBasketReportCallBack callBack;
    private CommodityListAdapter adapter;
    private CommodityListMvpPresenter<CommodityListMvpView> mvpPresenter;
    private List<SalesCommodityBean> list;
    private SalesCommodityBean commodityBean;
    private int offset = 0;

    public static CommodityListFragment newInstance(SalesBasketReportCallBack reportCallBack) {
        CommodityListFragment fragment = new CommodityListFragment();
        Bundle args = new Bundle();
        callBack = reportCallBack;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_commodity_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new CommodityListPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        view.findViewById(R.id.frameLayout).setOnClickListener(v -> {
        });
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        list = new ArrayList<>();
        adapter = new CommodityListAdapter(list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        mvpPresenter.getSaleCommodities(getProgrammeId(), getProductId(), getCategoryId(), offset);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getSaleCommodities(getProgrammeId(), getProductId(), getCategoryId(), offset);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public void setData(List<SalesCommodityBean> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();

        list.addAll(data);
        adapter.notifyDataSetChanged();

        if (callBack != null) {
            callBack.onReceiveCommodities(list, commodityBean);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callBack != null)
            callBack.onReceiveCommodities(null, null);
    }

    @Override
    public void onItemClick(String id) {
        if (id != null) {
            String[] split = id.split(",");
            setCommodityId(split[0]);
            setCommodityName(split[1]);

            commodityBean = mvpPresenter.getCommodity(list, split[0]);
            if (callBack != null)
                callBack.onReceiveCommodities(list, commodityBean);

            if (split[2].equalsIgnoreCase("Cash")) {
                setUom(getCurrency());
                getBaseActivity().addFragment(BeneficiaryUomFragment.newInstance(callBack), R.id.frame, true);
            } else
                getBaseActivity().addFragment(UomListFragment.newInstance(callBack), R.id.frame, true);
        }
    }
}
