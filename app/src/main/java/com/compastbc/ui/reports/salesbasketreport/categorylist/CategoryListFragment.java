package com.compastbc.ui.reports.salesbasketreport.categorylist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesCategoryBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseSalesBasketFragment;
import com.compastbc.ui.reports.salesbasketreport.SalesBasketReportCallBack;
import com.compastbc.ui.reports.salesbasketreport.commoditylist.CommodityListFragment;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends BaseSalesBasketFragment implements CategoryMvpView, CategoryListAdapter.ItemClickListener {

    private static SalesBasketReportCallBack callBack;
    private List<SalesCategoryBean> list;
    private CategoryListAdapter adapter;
    private CategoryMvpPresenter<CategoryMvpView> mvpPresenter;
    private int offset = 0;
    private SalesCategoryBean categoryBean;

    public static CategoryListFragment newInstance(SalesBasketReportCallBack reportCallBack) {
        CategoryListFragment fragment = new CategoryListFragment();
        Bundle args = new Bundle();
        callBack = reportCallBack;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new CategoryPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        view.findViewById(R.id.frameLayout).setOnClickListener(v -> {
        });
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        list = new ArrayList<>();
        adapter = new CategoryListAdapter(list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        mvpPresenter.getSaleCatrgories(getProgrammeId(), getProductId(), offset);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getSaleCatrgories(getProgrammeId(), getProductId(), offset);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_list, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callBack != null)
            callBack.onReceiveCategories(null, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    public void setData(List<SalesCategoryBean> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();

        list.addAll(data);
        adapter.notifyDataSetChanged();

        if (list.size() > 0)
            callBack.onReceiveCategories(list, categoryBean);

    }

    @Override
    public void onItemClick(String id) {
        setCategoryId(id);
        categoryBean = mvpPresenter.getCategory(list, id);
        if (callBack != null)
            callBack.onReceiveCategories(list, categoryBean);
        getBaseActivity().addFragment(CommodityListFragment.newInstance(callBack), R.id.frame, true);
    }

}
