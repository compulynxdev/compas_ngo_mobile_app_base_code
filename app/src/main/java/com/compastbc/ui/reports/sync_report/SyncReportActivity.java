package com.compastbc.ui.reports.sync_report;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SyncReportModel;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SyncReportActivity extends BaseActivity implements SyncReportMvpView, OnPrinterInteraction {

    private SyncReportMvpPresenter<SyncReportMvpView> presenter;
    private RelativeLayout relativeLayout;
    private TextView tvNoData;
    private List<SyncReportModel> syncReportModels;
    private RecyclerView syncRecyclerView;
    private String count, amount, txns;
    private ImageView printImage;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SyncReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_report);
        presenter = new SyncReportPresenter<>(getDataManager());
        presenter.onAttach(this);
        setUp();

        printImage.setOnClickListener((View v) -> {
            showLoading();
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

    private void doPrintOperation(PrintServices printUtils) {
        if (syncReportModels.size() > 0)
            printUtils.printSyncReport(syncReportModels, count, txns, amount, this);
        else {
            hideLoading();
            showMessage(R.string.NoDataToPrint);
        }
    }

    @Override
    protected void setUp() {
        findIds();
        syncReportModels = new ArrayList<>();
        presenter.getData();
    }

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.SyncReport);
        printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);
        relativeLayout = findViewById(R.id.relative);
        tvNoData = findViewById(R.id.tv_no_data);
        syncRecyclerView = findViewById(R.id.sync_list);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void setData(List<SyncReportModel> models, String count, String totalTxn, String totalAmount) {
        if (!models.isEmpty()) {
            TextView tvTotalTxn, tvTotalNoDevices, tvTotalAmount, tvDate;
            tvTotalTxn = findViewById(R.id.noOftxn);
            tvTotalNoDevices = findViewById(R.id.noOfDevice);
            tvTotalAmount = findViewById(R.id.amount);
            tvDate = findViewById(R.id.date);

            relativeLayout.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);

            this.count = count;
            amount = totalAmount;
            txns = totalTxn;
            tvTotalTxn.setText(totalTxn);
            tvTotalAmount.setText(totalAmount);
            tvTotalNoDevices.setText(count);
            tvDate.setText(CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US));
            syncReportModels.clear();
            syncReportModels.addAll(models);
            SyncReportAdapter adapter = new SyncReportAdapter(this, syncReportModels);
            syncRecyclerView.setAdapter(adapter);
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }
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
