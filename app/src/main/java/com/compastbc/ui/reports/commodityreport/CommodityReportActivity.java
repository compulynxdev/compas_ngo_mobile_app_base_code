package com.compastbc.ui.reports.commodityreport;

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
import com.compastbc.core.data.network.model.CommodityReportBean;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CommodityReportActivity extends BaseActivity implements CommodityReportMvpView, OnPrinterInteraction {

    private CommodityReportMvpPresenter<CommodityReportMvpView> mvpPresenter;
    private RecyclerView recyclerView;
    private TextView text, setDate;
    private ImageView printImage;
    private String selectedDate;
    private RelativeLayout relativeLayout;
    private List<CommodityReportBean> list;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CommodityReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_report);
        mvpPresenter = new CommodityReportPresenter<>(getDataManager(), getActivity());
        mvpPresenter.onAttach(this);
        list = new ArrayList<>();
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
        selectedDate = CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT);
        setDate.setText(CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));
        text.setVisibility(View.VISIBLE);
        relativeLayout.setVisibility(View.GONE);
        mvpPresenter.getData(selectedDate);
        setDate.setOnClickListener(v -> mvpPresenter.onSelectDate());
    }

    private void findIds() {
        recyclerView = findViewById(R.id.recycler_view);
        text = findViewById(R.id.text);
        setDate = findViewById(R.id.date);
        Toolbar toolbar = findViewById(R.id.toolbar);
        printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);
        relativeLayout = findViewById(R.id.relative);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.commodity_report);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void setData(List<CommodityReportBean> beanList) {
        if (!beanList.isEmpty()) {
            list.clear();
            list.addAll(beanList);
            text.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
            CommodityAdapter adapter = new CommodityAdapter(this, list);
            recyclerView.setAdapter(adapter);
        } else {
            relativeLayout.setVisibility(View.GONE);
            text.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setDate(String date, String displayDate) {
        setDate.setText(displayDate);
        this.selectedDate = date;
        mvpPresenter.getData(selectedDate);
    }

    @Override
    public void doPrintOperation(PrintServices printUtils) {
        if (list.size() > 0)
            printUtils.printDailyCommodityReport(list, this);
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
