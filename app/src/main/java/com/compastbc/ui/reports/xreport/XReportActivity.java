package com.compastbc.ui.reports.xreport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.XReportBean;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;

import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class XReportActivity extends BaseActivity implements XReportMvpView, OnPrinterInteraction {

    private XReportMvpPresenter<XReportMvpView> mvpPresenter;
    private TextView tvDate, tvNoItems;
    private ImageView printImage;
    private RecyclerView recyclerView;
    private List<XReportBean> xReportBeanList;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, XReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xreport);
        mvpPresenter = new XReportPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
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

    @Override
    protected void setUp() {
        findIds();
        String date = CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault());
        tvDate.setText(date);
        //   btn_print.setOnClickListener(v -> showMessage("Under development"));
        mvpPresenter.getXreportData();
    }

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.Xreports);

        printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.GONE);
        tvDate = findViewById(R.id.date);
        tvNoItems = findViewById(R.id.no);


        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void showData(List<XReportBean> xReportBeanList) {
        try {
            showLoading();
            this.xReportBeanList = xReportBeanList;
            /*if(xReportBean==null || (xReportBean.voidCount.equalsIgnoreCase("0") && xReportBean.transactionCount.equalsIgnoreCase("0"))){
                tvNoItems.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                hideLoading();
            }else {
                tvNoItems.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                txnCount.setText(xReportBean.transactionCount);
                vCount.setText(xReportBean.voidCount);
                vAmount.setText(xReportBean.voidAmount);
                txnAmount.setText(xReportBean.transactionAmount);
                nSales.setText(xReportBean.transactionAmount);
                hideLoading();
            }*/
            if (xReportBeanList == null || xReportBeanList.isEmpty()) {
                tvNoItems.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                hideLoading();
            } else {
                tvNoItems.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                XReportAdapter adapter = new XReportAdapter(xReportBeanList, this);
                recyclerView.setAdapter(adapter);
                hideLoading();
            }
        } catch (Exception e) {
            showMessage(e.getMessage());
        }
    }

    @Override
    public void doPrintOperation(PrintServices printUtils) {
        if (xReportBeanList != null && !xReportBeanList.isEmpty())
            printUtils.printXReport(xReportBeanList, this);
        else {
            hideLoading();
            showMessage(R.string.NoDataToPrint);
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
