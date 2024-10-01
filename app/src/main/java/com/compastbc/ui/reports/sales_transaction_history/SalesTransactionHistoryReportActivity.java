package com.compastbc.ui.reports.sales_transaction_history;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SalesTransactionHistoryReportActivity extends BaseActivity implements SalesTransactionMvpView, View.OnClickListener, OnPrinterInteraction {

    private SalesTransactionMvpPresenter<SalesTransactionMvpView> presenter;
    private TextView tvFromDate;
    private TextView tvToDate;
    private TextView tv_no_record;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private LinearLayout llSearchView;
    private ImageView printImage, downloadImage;
    private List<TransactionHistory> list;
    private boolean isFromSelect = false;
    private int offset = 0;
    private SalesTransactionHistoryAdapter adapter;
    private RecyclerViewScrollListener scrollListener;
    private StringBuilder fromDate;
    private StringBuilder toDate = new StringBuilder();
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
        return new Intent(context, SalesTransactionHistoryReportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_transaction_history_report);
        presenter = new SalesTransactionPresenter<>(getDataManager());
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

        downloadImage.setOnClickListener(v -> {
            if (list.size() > 0) {
                createPdf();
            } else showMessage(R.string.NoDataToDownload);
        });
    }

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.sales_txn_history_title);
        printImage = findViewById(R.id.img_right);
        printImage.setVisibility(View.VISIBLE);
        downloadImage = findViewById(R.id.img_right2);
        downloadImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_cloud_computing));
        downloadImage.setVisibility(View.VISIBLE);
        tvFromDate = findViewById(R.id.startDate);
        recyclerView = findViewById(R.id.recycler_view);
        tvToDate = findViewById(R.id.endDate);
        tv_no_record = findViewById(R.id.tv_no_record);
        TextView search = findViewById(R.id.search);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        etSearch = findViewById(R.id.et_search);
        llSearchView = findViewById(R.id.llSearchView);

        tvFromDate.setOnClickListener(this);
        tvToDate.setOnClickListener(this);
        search.setOnClickListener(this);
        img_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;

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
                presenter.getTransactionDetails(etSearch.getText().toString().trim(), fromDate.toString(), toDate.toString(), offset);
                break;

        }

    }

    @Override
    protected void setUp() {
        findIds();
        list = new ArrayList<>();
        String displayDate = CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault());
        tvFromDate.setText(displayDate);
        tvToDate.setText(displayDate);
        adapter = new SalesTransactionHistoryAdapter(list, this);
        recyclerView.setAdapter(adapter);

        String date = CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT);
        fromDate = new StringBuilder(date);
        toDate = new StringBuilder(date);
        presenter.getTransactionDetails(etSearch.getText().toString().trim(), fromDate.toString(), toDate.toString(), offset);
        if (!getDataManager().getConfigurableParameterDetail().isOnline()) {
            llSearchView.setVisibility(View.VISIBLE);
            presenter.setupSearch(etSearch);
            scrollListener = new RecyclerViewScrollListener() {
                @Override
                public void onLoadMore() {
                    adapter.showLoading(true);
                    offset += AppConstants.LIMIT;
                    presenter.getTransactionDetails(etSearch.getText().toString().trim(), fromDate.toString(), toDate.toString(), offset);
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
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
    public void doSearch(int page, String search) {
        list.clear();
        scrollListener.onDataCleared();
        this.offset = page;
        presenter.getTransactionDetails(search, fromDate.toString(), toDate.toString(), offset);
    }

    @Override
    public void setData(List<TransactionHistory> transactionHistories) {
        adapter.showLoading(false);
        if (offset == 0)
            list.clear();
        list.addAll(transactionHistories);
        if (list.size() > 0) {
            tv_no_record.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            tv_no_record.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public void doPrintOperation(PrintServices printUtils) {
        showLoading();
        if (list.size() > 0)
            printUtils.printSalesTransactionHistoryReport(list, this);
        else {
            hideLoading();
            showMessage(R.string.NoDataToPrint);
        }
    }

    @Override
    public void createPdf() {
        showLoading();
        Document document = new Document();
        PdfPTable table = new PdfPTable(new float[]{1, 3, 3, 5, 3, 4, 3, 4});
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell("S.No.");
        table.addCell("Trans Id");
        table.addCell("Ration No.");
        table.addCell("Commodity Name");
        table.addCell("Quantity");
        table.addCell("Total Amount");
        table.addCell("Void");
        table.addCell("Date");
        table.setHeaderRows(1);
        PdfPCell[] cells = table.getRow(0).getCells();
        for (PdfPCell cell : cells) {
            cell.setBackgroundColor(BaseColor.GRAY);
        }
        for (int i = 0; i < list.size(); i++) {
            table.addCell("" + (i + 1));
            table.addCell(list.get(i).getReceiptNo());
            table.addCell(list.get(i).getIdentityNo());
            table.addCell(list.get(i).getCommodityName());
            table.addCell(list.get(i).getQuantity().concat(" (").concat(list.get(i).getUom()).concat(")"));
            table.addCell(list.get(i).getAmount());
            if (list.get(i).getTransactionType().equalsIgnoreCase("-1")) {
                table.addCell("Void");
            } else {
                table.addCell("");
            }
            table.addCell(list.get(i).getDate());
        }

        try {
            String directory_path = Environment.getExternalStorageDirectory().getPath() + "/mypdf/";
            File file = new File(directory_path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String targetPdf = directory_path + "TransactionHistory.pdf";
            File filePath = new File(targetPdf);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(table);
            document.close();


            hideLoading();
            sweetAlert(2, R.string.success, R.string.generatedPdf).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();

        } catch (Exception e) {
            hideLoading();
            showMessage(R.string.unablepdf);
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
