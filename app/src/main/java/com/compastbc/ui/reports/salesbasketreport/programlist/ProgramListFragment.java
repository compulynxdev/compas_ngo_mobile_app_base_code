package com.compastbc.ui.reports.salesbasketreport.programlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesProgramBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseSalesBasketFragment;
import com.compastbc.ui.reports.salesbasketreport.SalesBasketReportCallBack;
import com.compastbc.ui.reports.salesbasketreport.categorylist.CategoryListFragment;

import java.util.ArrayList;
import java.util.List;


public class ProgramListFragment extends BaseSalesBasketFragment implements ProgramFragmentMvpView, ProgramBeanAdapter.ItemClickListener {

    private static SalesBasketReportCallBack callBack;
    private ProgramFragmentMvpPresenter<ProgramFragmentMvpView> mvpPresenter;
    private ProgramBeanAdapter adapter;
    private int offset = 0;
    private List<SalesProgramBean> list;
    private SalesProgramBean programBean;

    public static ProgramListFragment newInstance(SalesBasketReportCallBack salesBasketReportCallBack) {
        ProgramListFragment fragment = new ProgramListFragment();
        Bundle args = new Bundle();
        callBack = salesBasketReportCallBack;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new ProgramFragmentPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        view.findViewById(R.id.frameLayout).setOnClickListener(v -> {
        });
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        list = new ArrayList<>();
        adapter = new ProgramBeanAdapter(list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        mvpPresenter.getProgrammesData(offset);

        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getProgrammesData(offset);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_program_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    public void setData(List<SalesProgramBean> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();

        list.addAll(data);
        adapter.notifyDataSetChanged();

        if (list.size() > 0) {
            if (callBack != null)
                callBack.onReceiveProgrammes(list, programBean);
        }
    }


    @Override
    public void onItemClick(View v, String... ids) {
        setProgrammeId(ids[0]);
        setProductId(ids[1]);
        programBean = mvpPresenter.getProgramBean(list, ids[0]);
        setCurrency(programBean.getCurrency());
        if (callBack != null)
            callBack.onReceiveProgrammes(list, programBean);
        getBaseActivity().addFragment(CategoryListFragment.newInstance(callBack), R.id.frame, true);
    }
}
