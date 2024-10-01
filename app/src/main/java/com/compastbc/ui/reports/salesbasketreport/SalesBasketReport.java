package com.compastbc.ui.reports.salesbasketreport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesBasketReportModel;
import com.compastbc.core.data.network.model.SalesBeneficiary;
import com.compastbc.core.data.network.model.SalesCategoryBean;
import com.compastbc.core.data.network.model.SalesCommodityBean;
import com.compastbc.core.data.network.model.SalesProgramBean;
import com.compastbc.core.data.network.model.Uom;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.reports.salesbasketreport.programlist.ProgramListFragment;
import com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.DisplayFilterData;
import com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.FilterDialogFragment;

import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SalesBasketReport extends BaseActivity implements SalesBasketMvpView, View.OnClickListener, OnPrinterInteraction, SalesBasketReportCallBack {
    private TextView setDate;
    private SalesBasketReportModel salesBasketReportModel;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SalesBasketReport.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_basket_report);
        SalesBasketMvpPresenter<SalesBasketMvpView> mvpPresenter = new SalesBasketPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        findIds();
        setDate.setText(CalenderUtils.formatByLocale(CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT), CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));
        replaceFragment(ProgramListFragment.newInstance(this), R.id.frame, false);
    }

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle;
        tvTitle = findViewById(R.id.tvTitle);
        setDate = findViewById(R.id.date);
        tvTitle.setText(R.string.sales);
        ImageView filterImage = findViewById(R.id.img_right2);
        ImageView printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);
        filterImage.setVisibility(View.VISIBLE);

        filterImage.setOnClickListener(this);
        printImage.setOnClickListener(this);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;

            //print
            case R.id.img_right:
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
                break;

            //filter
            case R.id.img_right2:
                FilterDialogFragment.newInstance(new FilterDialogFragment.OnFilterInteraction() {
                    @Override
                    public void onInteraction(SalesBasketReportModel model, String startDate, String endDate) {
                        salesBasketReportModel = new SalesBasketReportModel();
                        salesBasketReportModel = model;
                        String date = CalenderUtils.formatByLocale(startDate, CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()).concat("-").concat(CalenderUtils.formatByLocale(endDate, CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));
                        setDate.setText(date);
                        if (salesBasketReportModel != null) {
                            replaceFragment(DisplayFilterData.newInstance(salesBasketReportModel), R.id.frame, false);
                        }
                    }

                    @Override
                    public void onReset() {
                        setUp();
                    }
                }).show(getSupportFragmentManager(), "filter");
                break;
        }

    }

    @Override
    public void onReceiveProgrammes(List<SalesProgramBean> programBeans, SalesProgramBean programBean) {
        if (programBeans != null) {
            if (programBeans.size() > 0) {
                salesBasketReportModel = new SalesBasketReportModel();
                salesBasketReportModel.salesProgramBeans = programBeans;
            }
            salesBasketReportModel.programBean = programBean;
        } else {
            salesBasketReportModel.salesProgramBeans = null;
            salesBasketReportModel.programBean = null;
        }
    }

    @Override
    public void onReceiveCategories(List<SalesCategoryBean> categoryBeans, SalesCategoryBean categoryBean) {
        if (categoryBeans != null) {
            if (categoryBeans.size() > 0) {
                salesBasketReportModel.salesCategoryBeans = categoryBeans;
            }
            salesBasketReportModel.categoryBean = categoryBean;
        } else {
            salesBasketReportModel.salesCategoryBeans = null;
            salesBasketReportModel.categoryBean = null;
        }
    }

    @Override
    public void onReceiveCommodities(List<SalesCommodityBean> salesCommodityBeans, SalesCommodityBean commodityBean) {
        if (salesCommodityBeans != null) {
            if (salesCommodityBeans.size() > 0) {
                salesBasketReportModel.salesCommodityBeans = salesCommodityBeans;
            }
            salesBasketReportModel.commodityBean = commodityBean;
        } else {
            salesBasketReportModel.salesCommodityBeans = null;
            salesBasketReportModel.commodityBean = null;
        }
    }

    @Override
    public void onReceiveUoms(List<Uom> uomList, Uom uom) {
        if (uomList != null) {
            if (uomList.size() > 0) {
                salesBasketReportModel.uomList = uomList;
            }
            salesBasketReportModel.uom = uom;
        } else {
            salesBasketReportModel.uomList = null;
            salesBasketReportModel.uom = null;
        }
    }

    @Override
    public void onReceiveBeneficiaries(List<SalesBeneficiary> salesBeneficiaryList) {
        if (salesBeneficiaryList != null) {
            if (salesBeneficiaryList.size() > 0) {
                salesBasketReportModel.salesBeneficiaries = salesBeneficiaryList;
            }
        } else salesBasketReportModel.salesBeneficiary = null;
    }

    @Override
    public void doPrintOperation(PrintServices printUtils) {
        if (salesBasketReportModel != null)
            printUtils.printSalesBasketReport(salesBasketReportModel, this);

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
