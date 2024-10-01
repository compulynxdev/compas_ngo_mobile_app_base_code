package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.core.data.network.model.SalesBasketReportModel;
import com.compastbc.core.data.network.model.SalesProgramBean;
import com.compastbc.core.data.network.model.Uom;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.ui.base.BaseDialog;
import com.compastbc.ui.base.BaseFilterSalesReport;
import com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_beneficiary.SelectBeneficiary;
import com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_category.SelectCategory;
import com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_commodity.SelectCommodity;
import com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_program.SelectProgram;
import com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_uom.SelectUom;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilterDialogFragment extends BaseDialog implements View.OnClickListener {
    private static OnFilterInteraction interaction;
    private ViewGroup frameStDate, frameEndDate, frameProgram, frameCategory, frameProduct, frameUom, frameBenf;
    private boolean isFromSelect;
    private TextView tvFromDate, tvToDate, selectProgram, selectCategory, selectProduct, selectUom, selectBenf, tvStart, tvEnd;
    private StringBuilder fromDate = new StringBuilder();
    private StringBuilder toDate = new StringBuilder();
    private SalesBasketReportModel salesBasketReportModel;
    @NonNull
    private final DatePickerDialog.OnDateSetListener datePicker = (view, selectedYear, selectedMonth, selectedDay) -> {
        selectedMonth += 1;

        if (isFromSelect) {
            fromDate = new StringBuilder();
            if (selectedMonth < 10)
                fromDate.append(selectedDay).append("/0").append(selectedMonth).append("/").append(selectedYear);
            else
                fromDate.append(selectedDay).append("/").append(selectedMonth).append("/").append(selectedYear);
            tvFromDate.setTextColor(getResources().getColor(R.color.black));
            tvFromDate.setText(CalenderUtils.formatByLocale(CalenderUtils.formatDate(String.valueOf(fromDate), CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT), CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));

        } else {
            toDate = new StringBuilder();
            if (selectedMonth < 10)
                toDate.append(selectedDay).append("/0").append(selectedMonth).append("/").append(selectedYear);
            else
                toDate.append(selectedDay).append("/").append(selectedMonth).append("/").append(selectedYear);
            tvToDate.setTextColor(getResources().getColor(R.color.black));
            tvToDate.setText(CalenderUtils.formatByLocale(CalenderUtils.formatDate(String.valueOf(toDate), CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT), CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));

        }
    };

    public static FilterDialogFragment newInstance(OnFilterInteraction listener) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        interaction = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        findIds(view);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);

        Button btn_filter = view.findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(this);

        Button btn_reset = view.findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(this);

        frameStDate.setOnClickListener(this);
        frameEndDate.setOnClickListener(this);
        frameProgram.setOnClickListener(this);
        frameCategory.setOnClickListener(this);
        frameProduct.setOnClickListener(this);
        frameUom.setOnClickListener(this);
        frameBenf.setOnClickListener(this);
        frameCategory.setVisibility(View.GONE);
        frameProduct.setVisibility(View.GONE);
        frameUom.setVisibility(View.GONE);
        frameBenf.setVisibility(View.GONE);
        tvFromDate.setText(CalenderUtils.getCurrentDate());
        tvToDate.setText(CalenderUtils.getCurrentDate());
    }

    private void findIds(View view) {

        frameStDate = view.findViewById(R.id.frame_startDate);
        frameEndDate = view.findViewById(R.id.frame_endDate);
        frameProgram = view.findViewById(R.id.frame_selectProgram);
        frameCategory = view.findViewById(R.id.frame_selectCategory);
        frameProduct = view.findViewById(R.id.frame_selectProduct);
        frameUom = view.findViewById(R.id.frame_selectUom);
        tvFromDate = view.findViewById(R.id.startDate);
        tvToDate = view.findViewById(R.id.endDate);
        frameBenf = view.findViewById(R.id.frame_selectBenf);
        selectProgram = view.findViewById(R.id.selectProgram);
        selectCategory = view.findViewById(R.id.selectCategory);
        selectProduct = view.findViewById(R.id.selectProduct);
        selectUom = view.findViewById(R.id.selectUom);
        selectBenf = view.findViewById(R.id.selectBenf);
        tvStart = view.findViewById(R.id.emptyStartDate);
        tvEnd = view.findViewById(R.id.emptyEndDate);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.frame_startDate:
                isFromSelect = true;
                tvStart.setVisibility(View.GONE);
                getDate(fromDate.toString());
                break;

            case R.id.frame_endDate:
                if (fromDate == null || fromDate.toString().isEmpty()) {
                    showMessage(R.string.pleaseSelectStartDate);
                } else {
                    isFromSelect = false;
                    tvEnd.setVisibility(View.GONE);
                    getDate(toDate.toString());
                }
                break;

            case R.id.frame_selectProgram:
                frameCategory.setVisibility(View.GONE);
                frameProduct.setVisibility(View.GONE);
                frameUom.setVisibility(View.GONE);
                frameBenf.setVisibility(View.GONE);

                selectCategory.setText(R.string.selectCategory);
                selectProduct.setText(R.string.selectProduct);
                selectUom.setText(R.string.SelectUom);
                selectBenf.setText(R.string.selectBeneficiary);

                if (fromDate == null || fromDate.toString().isEmpty()) {
                    showMessage(R.string.pleaseSelectStartDate);
                } else if (toDate == null || toDate.toString().isEmpty()) {
                    showMessage(R.string.pleaseSelectEndDate);
                } else {
                    SelectProgram.newInstance((List<SalesProgramBean> list, String[] ids, SalesProgramBean bean) -> {
                        BaseFilterSalesReport.setProgrammeId(ids[0]);
                        BaseFilterSalesReport.setProductId(ids[1]);
                        BaseFilterSalesReport.setCurrency(bean.getCurrency());
                        salesBasketReportModel = new SalesBasketReportModel();
                        salesBasketReportModel.salesProgramBeans = list;
                        salesBasketReportModel.programBean = bean;
                        frameCategory.setVisibility(View.VISIBLE);
                        selectProgram.setText(ids[2]);
                    }, fromDate.toString(), toDate.toString()).show(getChildFragmentManager(), "Select Program");
                }

                break;

            case R.id.frame_selectCategory:
                frameProduct.setVisibility(View.GONE);
                frameUom.setVisibility(View.GONE);
                frameBenf.setVisibility(View.GONE);
                selectProduct.setText(R.string.selectProduct);
                selectUom.setText(R.string.SelectUom);
                selectBenf.setText(R.string.selectBeneficiary);
                SelectCategory.newInstance((list, id, bean) -> {
                    String[] ids = id.split(",");
                    BaseFilterSalesReport.setCategoryId(ids[0]);
                    frameProduct.setVisibility(View.VISIBLE);
                    salesBasketReportModel.salesCategoryBeans = list;
                    salesBasketReportModel.categoryBean = bean;
                    selectCategory.setText(ids[1]);
                }, fromDate.toString(), toDate.toString()).show(getChildFragmentManager(), "Select Category");
                break;

            case R.id.frame_selectProduct:
                frameUom.setVisibility(View.GONE);
                frameBenf.setVisibility(View.GONE);
                selectUom.setText(R.string.SelectUom);
                selectBenf.setText(R.string.selectBeneficiary);
                SelectCommodity.newInstance((list, id, bean, commodityType, cashUom) -> {
                    String[] ids = id.split(",");
                    BaseFilterSalesReport.setCommodityId(ids[0]);
                    selectProduct.setText(ids[1]);
                    salesBasketReportModel.salesCommodityBeans = list;
                    salesBasketReportModel.commodityBean = bean;
                    if (commodityType.equalsIgnoreCase("Commodity"))
                        frameUom.setVisibility(View.VISIBLE);
                    else {
                        Uom uom = new Uom();
                        uom.setUom(BaseFilterSalesReport.getCurrency());
                        salesBasketReportModel.uom = uom;
                        BaseFilterSalesReport.setUom(BaseFilterSalesReport.getCurrency());
                        frameBenf.setVisibility(View.VISIBLE);
                    }
                }, fromDate.toString(), toDate.toString()).show(getChildFragmentManager(), "Select Commodity");
                break;

            case R.id.frame_selectUom:
                frameBenf.setVisibility(View.GONE);
                selectBenf.setText(R.string.selectBeneficiary);
                SelectUom.newInstance((list, uomBean, uri) -> {
                    BaseFilterSalesReport.setUom(uri);
                    selectUom.setText(uri);
                    frameBenf.setVisibility(View.VISIBLE);

                    salesBasketReportModel.uomList = list;
                    salesBasketReportModel.uom = uomBean;
                }, fromDate.toString(), toDate.toString()).show(getChildFragmentManager(), "Select Uom");
                break;

            case R.id.frame_selectBenf:
                SelectBeneficiary.newInstance((list, name) -> {
                            selectBenf.setText(name);
                            salesBasketReportModel.salesBeneficiaries = list;
                            BaseFilterSalesReport.setBenfName(name);
                        }
                        , fromDate.toString(), toDate.toString()).show(getChildFragmentManager(), "Select Benf");
                break;

            case R.id.btn_filter:
                if (fromDate == null) {
                    showMessage(R.string.pleaseSelectStartDate);
                } else if (toDate == null) {
                    showMessage(R.string.pleaseSelectEndDate);
                } else if (selectProgram.getText().toString().equalsIgnoreCase("Select Program")) {
                    showMessage(R.string.pleaseSelectProgram);
                } else {
                    if (interaction != null) {
                        dismissDialog("filter");
                        interaction.onInteraction(salesBasketReportModel, fromDate.toString(), toDate.toString());
                    }
                }
                break;

            case R.id.btn_cancel:
                dismissDialog("filter");
                break;


            case R.id.btn_reset:
                dismissDialog("filter");
                if (interaction != null)
                    interaction.onReset();
                break;

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
        DatePickerDialog datePickerDialog = new DatePickerDialog(getBaseActivity(), datePicker, year, month, day);
        Date maxDate = CalenderUtils.getDateFormat(CalenderUtils.getCurrentDate(), CalenderUtils.TIMESTAMP_FORMAT);
        assert maxDate != null;
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
        if (!isFromSelect && !fromDate.toString().isEmpty() && toDate.toString().isEmpty()) {
            Date cDate = CalenderUtils.getDateFormat(fromDate.toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDate != null;
            datePickerDialog.getDatePicker().setMinDate(cDate.getTime());
        } else if (isFromSelect && fromDate.toString().isEmpty() && !toDate.toString().isEmpty()) {
            Date cDate = CalenderUtils.getDateFormat(toDate.toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDate != null;
            datePickerDialog.getDatePicker().setMaxDate(cDate.getTime());
        } else if (isFromSelect && !fromDate.toString().isEmpty() && !toDate.toString().isEmpty()) {
            Date cDateMax = CalenderUtils.getDateFormat(toDate.toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDateMax != null;
            datePickerDialog.getDatePicker().setMaxDate(cDateMax.getTime());
        } else if (!isFromSelect && !fromDate.toString().isEmpty() && !toDate.toString().isEmpty()) {
            Date cDateMin = CalenderUtils.getDateFormat(fromDate.toString(), CalenderUtils.TIMESTAMP_FORMAT);
            assert cDateMin != null;
            datePickerDialog.getDatePicker().setMinDate(cDateMin.getTime());
        }
        datePickerDialog.show();
    }


    public interface OnFilterInteraction {
        void onInteraction(SalesBasketReportModel model, String startDate, String endDate);

        void onReset();
    }
}
