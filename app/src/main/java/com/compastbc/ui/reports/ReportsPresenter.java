package com.compastbc.ui.reports;

import android.content.Context;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.network.model.HomeBean;
import com.compastbc.core.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

class ReportsPresenter<V extends ReportsMvpView> extends BasePresenter<V>
        implements ReportsMvpPresenter<V> {

    private final Context context;
    ReportsPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public List<HomeBean> getHomeOptions() {
        List<HomeBean> homeList = new ArrayList<>();
        homeList.add(new HomeBean(AppConstants.SUMMARY_VIEW,R.drawable.ic_summary,context.getString(R.string.SummaryReports)));
        homeList.add(new HomeBean(AppConstants.X_VIEW,R.drawable.ic_xreport,context.getString(R.string.Xreports)));
        homeList.add(new HomeBean(AppConstants.COMMODITY_VIEW,R.drawable.ic_daily_commodity,context.getString(R.string.commodity_report)));
        homeList.add(new HomeBean(AppConstants.SALES_TXN_VIEW,R.drawable.ic_sales_transaction,context.getString(R.string.SalesTransactionHistory)));
        if (!getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2"))
            homeList.add(new HomeBean(AppConstants.VENDOR_SUMMARY_VIEW,R.drawable.ic_summary,context.getString(R.string.vendor_summary)));
        if (!getDataManager().getConfigurableParameterDetail().isOnline())
            homeList.add(new HomeBean(AppConstants.SUBMIT_VIEW,R.drawable.ic_submit_txns,context.getString(R.string.submitreports)));
        if (!getDataManager().getConfigurableParameterDetail().isOnline() && getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2"))
            homeList.add(new HomeBean(AppConstants.SYNC_REPORT_VIEW,R.drawable.ic_sync,context.getString(R.string.SyncReport)));

        if (getDataManager().getConfigurableParameterDetail().isVoidTransaction())
            homeList.add(new HomeBean(AppConstants.VOID_REPORT_VIEW,R.drawable.ic_void_report,context.getString(R.string.voidReport)));

        if (getDataManager().getConfigurableParameterDetail().isSalesReport())
            homeList.add(new HomeBean(AppConstants.SALES_BASKET_VIEW,R.drawable.ic_sales_basket,context.getString(R.string.sales)));
        return homeList;
    }
}
