package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_program;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesProgramBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseDialog;

import java.util.ArrayList;
import java.util.List;


public class SelectProgram extends BaseDialog implements SelectProgramMvpView, SelectProgramAdapter.ItemClickListener {

    private static String startDate, endDate;
    private static onFragmentInteraction onFragmentInteraction;
    private LinearLayout linearLayout;
    private int offset = 0;
    private List<SalesProgramBean> list;
    private SelectProgramMvpPresenter<SelectProgramMvpView> mvpPresenter;
    private SelectProgramAdapter adapter;

    public static SelectProgram newInstance(onFragmentInteraction interaction, String stDate, String eDate) {
        SelectProgram fragment = new SelectProgram();
        Bundle args = new Bundle();
        onFragmentInteraction = interaction;
        startDate = stDate;
        endDate = eDate;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_program, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);

    }

    @Override
    protected void setUp(View view) {
        mvpPresenter = new SelectProgramPresenter<>(getBaseActivity(), getDataManager());
        mvpPresenter.onAttach(this);
        showLoading();
        list = new ArrayList<>();

        adapter = new SelectProgramAdapter(list);
        adapter.setClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        linearLayout = view.findViewById(R.id.linear);
        mvpPresenter.getPrograms(offset, startDate, endDate);
        recyclerView.setAdapter(adapter);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                adapter.notifyDataSetChanged();
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                mvpPresenter.getPrograms(offset, startDate, endDate);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        ViewGroup frame = view.findViewById(R.id.frame);
        frame.setOnClickListener(v -> dismissDialog("Select Program"));
    }


    @Override
    public void setData(List<SalesProgramBean> data) {
        adapter.showLoading(false);
        adapter.notifyDataSetChanged();
        list.addAll(data);
        if (list.size() > 0) {
            adapter.notifyDataSetChanged();
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            sweetAlert(1, R.string.error, R.string.NoPrograms).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                dismissDialog("Select Program");
            }).show();
        }
    }

    @Override
    public void dismissDialogView() {
        dismissDialog("Select Program");
    }

    @Override
    public void onItemClick(String... ids) {
        if (onFragmentInteraction != null) {
            showLoading();
            SalesProgramBean bean = mvpPresenter.getProgramBean(list, ids[0]);
            hideLoading();
            dismissDialog("Select Program");
            onFragmentInteraction.onItemSelect(list, ids, bean);
        }
    }

    public interface onFragmentInteraction {
        void onItemSelect(List<SalesProgramBean> list, String[] ids, SalesProgramBean bean);
    }
}
