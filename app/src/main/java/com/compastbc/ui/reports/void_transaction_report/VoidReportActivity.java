package com.compastbc.ui.reports.void_transaction_report;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class VoidReportActivity extends BaseActivity implements VoidReportMvpView, View.OnClickListener, OnPrinterInteraction {
    private VoidReportMvpPresenter<VoidReportMvpView> presenter;
    private TextView tvFromDate;
    private TextView tvToDate;
    private TextView text;
    private RecyclerView recyclerView;
    private int offset = 0;
    private List<TransactionHistory> historyList;
    private ImageView printImage;
    private VoidTransactionAdapter adapter;
    private boolean isFromSelect = false;
    private StringBuilder fromDate;
    private StringBuilder toDate;
    @NonNull
    private final DatePickerDialog.OnDateSetListener datePicker = (view, selectedYear, selectedMonth, selectedDay) -> {
        selectedMonth += 1;

        if (isFromSelect) {
            fromDate = new StringBuilder();
            fromDate.append(selectedDay).append("/").append(selectedMonth).append("/").append(selectedYear);

            tvFromDate.setText(CalenderUtils.formatDate(String.valueOf(fromDate), CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));

        } else {
            toDate = new StringBuilder();
            toDate.append(selectedDay).append("/").append(selectedMonth).append("/").append(selectedYear);
            tvToDate.setText(CalenderUtils.formatDate(String.valueOf(toDate), CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));
        }
    };

    public static Intent getStartIntent(Context context) {
        return new Intent(context, VoidReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_report);
        presenter = new VoidReportPresenter<>(getDataManager());
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

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.voidReport);
        tvFromDate = findViewById(R.id.startDate);
        printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recycler_view);
        tvToDate = findViewById(R.id.endDate);
        text = findViewById(R.id.text);
        TextView search = findViewById(R.id.search);

        tvFromDate.setOnClickListener(this);
        tvToDate.setOnClickListener(this);
        search.setOnClickListener(this);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void setData(List<TransactionHistory> transactionHistories) {
        historyList.addAll(transactionHistories);
        if (historyList.size() > 0) {
            text.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            text.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void doPrintOperation(PrintServices printUtils) {

        if (historyList.size() > 0)
            printUtils.printVoidTransactionReport(historyList, this);
        else {
            hideLoading();
            showMessage(R.string.NoDataToPrint);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startDate:
                isFromSelect = true;
                getDate(tvFromDate.getText().toString().trim());
                break;

            case R.id.endDate:
                isFromSelect = false;
                getDate(tvToDate.getText().toString().trim());
                break;

            case R.id.search:
                offset = 0;
                historyList.clear();
                presenter.getTransactionDetails(fromDate.toString(), toDate.toString(), offset);
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private void getTransactionDetails(int offset) {
        adapter.showLoading(false);
        if (offset == 0)
            historyList.clear();
        presenter.getTransactionDetails(fromDate.toString(), toDate.toString(), offset);
    }

    @Override
    protected void setUp() {
        findIds();
        historyList = new ArrayList<>();
        String displayDate = CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault());
        tvFromDate.setText(displayDate);
        tvToDate.setText(displayDate);

        String date = CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT);
        fromDate = new StringBuilder(date);
        toDate = new StringBuilder(date);

        adapter = new VoidTransactionAdapter(historyList);
        recyclerView.setAdapter(adapter);
        RecyclerViewScrollListener scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                adapter.showLoading(true);
                if (getDataManager().getConfigurableParameterDetail().isOnline())
                    offset += 1;
                else offset += AppConstants.LIMIT;
                getTransactionDetails(offset);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        getTransactionDetails(offset);
    }

    private void getDate(String selectedDate) {
        Calendar newCalender = Calendar.getInstance();

        int day, month, year;

        if (selectedDate.isEmpty()) {
            day = newCalender.get(Calendar.DAY_OF_MONTH);
            month = newCalender.get(Calendar.MONTH);
            year = newCalender.get(Calendar.YEAR);
        } else {
            String[] date = selectedDate.split("/");

            day = Integer.parseInt(date[0]);
            month = Integer.parseInt(date[1]) - 1;
            year = Integer.parseInt(date[2]);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, datePicker, year, month, day);
        Date maxDate = CalenderUtils.getDateFormat(CalenderUtils.getCurrentDate(), CalenderUtils.TIMESTAMP_FORMAT);
        assert maxDate != null;
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
        if (!isFromSelect && !tvFromDate.getText().toString().isEmpty() && tvToDate.getText().toString().isEmpty()) {
            Date cDate = CalenderUtils.getDateFormat(tvFromDate.getText().toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDate != null;
            datePickerDialog.getDatePicker().setMinDate(cDate.getTime());
        } else if (isFromSelect && tvFromDate.getText().toString().isEmpty() && !tvToDate.getText().toString().isEmpty()) {
            Date cDate = CalenderUtils.getDateFormat(tvToDate.getText().toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDate != null;
            datePickerDialog.getDatePicker().setMaxDate(cDate.getTime());
        } else if (isFromSelect && !tvFromDate.getText().toString().isEmpty() && !tvToDate.getText().toString().isEmpty()) {
            Date cDateMax = CalenderUtils.getDateFormat(tvToDate.getText().toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDateMax != null;
            datePickerDialog.getDatePicker().setMaxDate(cDateMax.getTime());
        } else if (!isFromSelect && !tvFromDate.getText().toString().isEmpty() && !tvToDate.getText().toString().isEmpty()) {
            Date cDateMin = CalenderUtils.getDateFormat(tvFromDate.getText().toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDateMin != null;
            datePickerDialog.getDatePicker().setMinDate(cDateMin.getTime());
        }
        datePickerDialog.show();
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
