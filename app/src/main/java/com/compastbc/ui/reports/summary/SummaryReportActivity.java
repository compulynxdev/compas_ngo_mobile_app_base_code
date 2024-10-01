package com.compastbc.ui.reports.summary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SummaryReportBean;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;

import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SummaryReportActivity extends BaseActivity implements SummaryMvpView, OnPrinterInteraction {

    private SummaryMvpPresenter<SummaryMvpView> presenter;
    private TextView tvTotalCards, tvTotalTopups, tvTotalTrans, tvTotalCommodity, tvtopuplogs, tvBlock;
    private ImageView printImage;
    private SummaryReportBean summaryReportBean;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SummaryReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_report);
        presenter = new SummaryPresenter<>(getDataManager());
        presenter.onAttach(this);
        setUp();

        printImage.setOnClickListener((View v) -> {
            showLoading();
            createLog("Summary", "Print");
            getPrintUtil(new ReportPrintCallback() {
                @Override
                public void onSuccess(PrintServices printUtils) {
                    doPrintOperation(printUtils);
                }

                @Override
                public void onPrintPairError() {
                    hideLoading();
                    showMessage(R.string.PrinterNotPaired);
                }

                @Override
                public void onPrintError(Exception e) {
                    hideLoading();
                    showMessage(getString(R.string.PrinterError).concat("<br>").concat(e.toString()));
                }

                @Override
                public void onNavigateNextController() {
                    hideLoading();
                    //no need to navigate
                }
            });
        });
    }

    @Override
    protected void setUp() {
        findIds();

        summaryReportBean = new SummaryReportBean();
        presenter.getAllDetails();
    }

    private void findIds() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);
        tvBlock = findViewById(R.id.tv_TotalBlock);
        tvTotalCards = findViewById(R.id.tv_totalCards);
        tvTotalTopups = findViewById(R.id.tv_totalTopups);
        tvTotalTrans = findViewById(R.id.tv_Totaltxn);
        tvTotalCommodity = findViewById(R.id.tv_TotalCommodity);
        tvtopuplogs = findViewById(R.id.tv_TotalTopupLog);

        setSupportActionBar(toolbar);
        tvTitle.setText(R.string.SummaryReports);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void showData(SummaryReportBean summaryReportBean) {
        this.summaryReportBean = summaryReportBean;
        tvTotalCards.setText(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlCardHolder));
        tvTotalTopups.setText(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTopup));
        tvTotalTrans.setText(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTransactions));
        tvTotalCommodity.setText(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlCommodities));
        tvtopuplogs.setText(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTopupLog));
        tvBlock.setText(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlBlockCards));
    }

    @Override
    public void doPrintOperation(PrintServices printUtils) {
        printUtils.printSummaryReport(summaryReportBean, this);
    }

    @Override
    public void onSuccess(String TAG) {
        hideLoading();
        sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.PrintedSuccessfully).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();
    }

    @Override
    public void onFail(String TAG) {
        hideLoading();
        showMessage(R.string.PrinterError);
    }

    @Override
    public void onPrintStatusBusy() {
        hideLoading();
        showMessage(R.string.print_status_busy);
    }

    @Override
    public void onPrintStatusHighTemp() {
        hideLoading();
        showMessage(R.string.print_status_high_temp);
    }

    @Override
    public void onPrintStatusPaperLack() {
        hideLoading();
        showMessage(R.string.print_status_paper_lack);
    }

    @Override
    public void onPrintStatusNoBattery() {
        hideLoading();
        showMessage(R.string.print_status_no_battery);
    }
}
