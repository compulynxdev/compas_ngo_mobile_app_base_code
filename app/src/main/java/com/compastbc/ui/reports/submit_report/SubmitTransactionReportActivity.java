package com.compastbc.ui.reports.submit_report;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SubmitTransactionBean;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SubmitTransactionReportActivity extends BaseActivity implements SubmitMvpView, OnPrinterInteraction {
    private RecyclerView recyclerView;
    private TextView text;
    private List<SubmitTransactionBean> list;
    private String totalamount;
    private ImageView printImage;
    private Button btn_submit;
    private RelativeLayout relativeLayout;
    private SubmitMvpPresenter<SubmitMvpView> presenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SubmitTransactionReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_transaction_report);
        presenter = new SubmitPresenter<>(getDataManager());
        presenter.onAttach(this);
        setUp();

        printImage.setOnClickListener((View v) -> {
            showLoading();
            if (getDataManager().getConfigurableParameterDetail().isActivityLog())
                createLog("Submit Report", "Print");
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

        btn_submit.setOnClickListener(view -> {
            if (getDataManager().getConfigurableParameterDetail().isActivityLog())
                createLog("Submit Report", "Submit Transaction");
            presenter.updateTransactions();
        });

    }

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.submitreports);
        printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recycler_view);
        text = findViewById(R.id.text);
        relativeLayout = findViewById(R.id.relative);
        btn_submit = findViewById(R.id.submittxn);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
    }

    @Override
    protected void setUp() {
        findIds();
        presenter.getData();
    }

    @Override
    public void setData(List<SubmitTransactionBean> transactionHistories, String amount) {
        list = new ArrayList<>();
        if (transactionHistories.size() > 0) {
            list.clear();
            list.addAll(transactionHistories);
            text.setVisibility(View.GONE);
            TextView totalAmount = findViewById(R.id.total);
            relativeLayout.setVisibility(View.VISIBLE);
            totalAmount.setText(getString(R.string.tAmt).concat(" ".concat(amount)));
            totalamount = amount;
        } else {
            relativeLayout.setVisibility(View.GONE);
            text.setVisibility(View.VISIBLE);
        }
        SubmitTxnAdapter adapter = new SubmitTxnAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void openNextActivity() {
        startActivity(MainActivity.getStartIntent(SubmitTransactionReportActivity.this));
        finish();
    }

    @Override
    public void doPrintOperation(PrintServices printUtils) {
        showLoading();
        if (list.size() > 0)
            printUtils.printSubmitTransactionReport(list, totalamount, this);
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
