package com.compastbc.ui.beneficiary.list_beneficiary.dialog;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.core.base.DialogMvpView;
import com.compastbc.core.data.network.model.BeneficiaryFilterBean;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.ui.base.BaseDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BeneficiaryFilterDialog extends BaseDialog implements View.OnClickListener, DialogMvpView {

    private EditText etId, etName;
    private TextView tvDob;
    private String date;
    private CheckBox cbMale, cbFemale, cbTrue, cbFalse;

    private BeneficiaryFilterCallback filterCallback;
    private BeneficiaryFilterBean filterBean;
    // the callback received when the user "sets" the Date in the
// DatePickerDialog
    @NonNull
    private final DatePickerDialog.OnDateSetListener datePicker = (view, selectedYear, selectedMonth, selectedDay) -> {
        selectedMonth += 1;

        date = CalenderUtils.formatDate(selectedDay + "/" + selectedMonth + "/" + selectedYear, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT);
        tvDob.setText(CalenderUtils.formatByLocale(date, CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));
    };

    private BeneficiaryFilterDialog() {
        // Required empty public constructor
    }

    public static BeneficiaryFilterDialog newInstance(BeneficiaryFilterBean filterBean, BeneficiaryFilterCallback filterCallback) {
        BeneficiaryFilterDialog filterDialog = new BeneficiaryFilterDialog();
        filterDialog.setCallback(filterBean, filterCallback);
        return filterDialog;
    }

    private void setCallback(BeneficiaryFilterBean filterBean, BeneficiaryFilterCallback filterCallback) {
        this.filterBean = filterBean;
        this.filterCallback = filterCallback;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_beneficiary_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_male:
                cbFemale.setChecked(false);
                break;

            case R.id.cb_female:
                cbMale.setChecked(false);
                break;

            case R.id.tv_dob:
                hideKeyboard();
                getDate(date);
                break;

            case R.id.cb_true:
                cbFalse.setChecked(false);
                break;

            case R.id.cb_false:
                cbTrue.setChecked(false);
                break;

            case R.id.frame_cancel:
                createLog("Beneficiary Filter", "Cancel");
                dismissDialog("BeneficiaryFilterDialog");
                break;

            case R.id.btn_cancel:
                createLog("Beneficiary Filter", "Reset");
                if (filterCallback != null)
                    filterCallback.reset();
                dismissDialog("BeneficiaryFilterDialog");
                break;

            case R.id.btn_filter:
                createLog("Beneficiary Filter", "Filter");
                String tmpId = etId.getText().toString().trim();
                String tmpName = etName.getText().toString().trim();
                String tmpDob = date;
                String tmpGender = cbMale.isChecked() ? "M" : cbFemale.isChecked() ? "F" : "";
                String tmpBioStatus = cbTrue.isChecked() ? "TRUE" : cbFalse.isChecked() ? "FALSE" : "";
                if (verifyInputs(tmpId, tmpName, tmpDob, tmpGender, tmpBioStatus)) {
                    if (filterCallback != null) {
                        BeneficiaryFilterBean filterBean = new BeneficiaryFilterBean();
                        filterBean.setTmpId(tmpId);
                        filterBean.setTmpName(tmpName);
                        filterBean.setTmpDob(tmpDob == null || tmpDob.isEmpty() ? "" : CalenderUtils.formatDate(tmpDob, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT));
                        filterBean.setTmpGender(tmpGender);
                        filterBean.setTmpBioStatus(tmpBioStatus);
                        filterCallback.filterData(filterBean);
                    }
                    dismissDialog("BeneficiaryFilterDialog");
                }
                break;
        }
    }

    @Override
    protected void setUp(View view) {
        etId = view.findViewById(R.id.et_id);
        etName = view.findViewById(R.id.et_name);
        tvDob = view.findViewById(R.id.tv_dob);
        cbMale = view.findViewById(R.id.cb_male);
        cbFemale = view.findViewById(R.id.cb_female);
        cbTrue = view.findViewById(R.id.cb_true);
        cbFalse = view.findViewById(R.id.cb_false);

        tvDob.setOnClickListener(this);
        cbMale.setOnClickListener(this);
        cbFemale.setOnClickListener(this);
        cbTrue.setOnClickListener(this);
        cbFalse.setOnClickListener(this);

        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_filter).setOnClickListener(this);
        view.findViewById(R.id.frame_cancel).setOnClickListener(this);
        if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
            view.findViewById(R.id.bio_linear).setVisibility(View.VISIBLE);
        }
        if (filterBean != null) {
            etId.setText(filterBean.getTmpId());
            etName.setText(filterBean.getTmpName());
            tvDob.setText(filterBean.getTmpDob().contains("-") ? CalenderUtils.formatByLocale(CalenderUtils.formatDate(filterBean.getTmpDob(), CalenderUtils.DATE_FORMAT, CalenderUtils.TIMESTAMP_FORMAT)
                    , CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()) : CalenderUtils.formatByLocale(filterBean.getTmpDob(), CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));
            if (filterBean.getTmpDob() != null && !filterBean.getTmpDob().isEmpty())
                date = CalenderUtils.formatByLocale(filterBean.getTmpDob(), CalenderUtils.TIMESTAMP_FORMAT, Locale.US);
            if (filterBean.getTmpGender().isEmpty()) {
                cbMale.setChecked(false);
                cbFemale.setChecked(false);
            } else if (filterBean.getTmpGender().equalsIgnoreCase("M")) {
                cbMale.setChecked(true);
                cbFemale.setChecked(false);
            } else {
                cbMale.setChecked(false);
                cbFemale.setChecked(true);
            }

            if (filterBean.getTmpBioStatus().isEmpty()) {
                cbTrue.setChecked(false);
                cbFalse.setChecked(false);
            } else if (filterBean.getTmpBioStatus().equalsIgnoreCase("true")) {
                cbTrue.setChecked(true);
                cbFalse.setChecked(false);
            } else {
                cbTrue.setChecked(false);
                cbFalse.setChecked(true);
            }
        }
    }

    private boolean verifyInputs(String id, String name, String dob, String gender, String bioStatus) {
        if (name.isEmpty() && gender != null && gender.isEmpty() && dob != null && dob.isEmpty() && bioStatus != null && id.isEmpty()) {
            showMessage(R.string.EmptyInputs);
            return false;
        } else {
            return true;
        }
    }

    private void getDate(String selectedDate) {
        //date 26/06/2018

        Calendar newCalender = Calendar.getInstance();

        int day, month, year;

        if (selectedDate == null || selectedDate.isEmpty()) {
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

        datePickerDialog.show();
    }

    public interface BeneficiaryFilterCallback {
        void reset();

        void filterData(BeneficiaryFilterBean filterBean);
    }


}


