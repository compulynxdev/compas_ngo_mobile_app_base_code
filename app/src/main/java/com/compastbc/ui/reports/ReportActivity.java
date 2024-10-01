package com.compastbc.ui.reports;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.HomeBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.HomeAdapter;
import com.compastbc.ui.reports.commodityreport.CommodityReportActivity;
import com.compastbc.ui.reports.sales_transaction_history.SalesTransactionHistoryReportActivity;
import com.compastbc.ui.reports.salesbasketreport.SalesBasketReport;
import com.compastbc.ui.reports.submit_report.SubmitTransactionReportActivity;
import com.compastbc.ui.reports.summary.SummaryReportActivity;
import com.compastbc.ui.reports.sync_report.SyncReportActivity;
import com.compastbc.ui.reports.vendor_summary.VendorSummaryReport;
import com.compastbc.ui.reports.void_transaction_report.VoidReportActivity;
import com.compastbc.ui.reports.xreport.XReportActivity;

import java.util.List;

public class ReportActivity extends BaseActivity implements ReportsMvpView {
    private ReportsPresenter<ReportsMvpView> mvpPresenter;
    public static Intent getStartIntent(Context context) {
        return new Intent(context, ReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        mvpPresenter= new ReportsPresenter<>(getDataManager(),this);
        mvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        findIds();
        List<HomeBean> homeList = mvpPresenter.getHomeOptions();
        HomeAdapter homeAdapter = new HomeAdapter(homeList, pos -> {
            switch (homeList.get(pos).getPos()){
                case AppConstants.SUMMARY_VIEW:
                    createLog("Report Activity", "Summary Selected");
                    startActivity(SummaryReportActivity.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.VENDOR_SUMMARY_VIEW:
                    createLog("Report Activity", "Vendor Summary Selected");
                    startActivity(VendorSummaryReport.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.X_VIEW:
                    createLog("Report Activity", "X report Selected");
                    startActivity(XReportActivity.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.COMMODITY_VIEW:
                    createLog("Report Activity", "Commodity Report Selected");
                    startActivity(CommodityReportActivity.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.SUBMIT_VIEW:
                    createLog("Report Activity", "Submit report Selected");
                    startActivity(SubmitTransactionReportActivity.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.SYNC_REPORT_VIEW:
                    createLog("Report Activity", "Sync report Selected");
                    startActivity(SyncReportActivity.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.SALES_BASKET_VIEW:
                    createLog("Report Activity", "Sales Basket report Selected");
                    startActivity(SalesBasketReport.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.SALES_TXN_VIEW:
                    createLog("Report Activity", "Sales/transaction history Selected");
                    startActivity(SalesTransactionHistoryReportActivity.getStartIntent(ReportActivity.this));
                    break;

                case AppConstants.VOID_REPORT_VIEW:
                    createLog("Report Activity", "Void transaction Selected");
                    startActivity(VoidReportActivity.getStartIntent(ReportActivity.this));
                    break;
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(homeAdapter);
    }
    private void findIds() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(R.string.reports);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(v -> onBackPressed());
    }
}
