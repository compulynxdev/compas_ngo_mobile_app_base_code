package com.compastbc.ui.beneficiary.list_beneficiary.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.ui.base.BaseActivity;

import java.util.Locale;

public class BeneficiaryDetailActivity extends BaseActivity implements BeneficiaryDetailMvpView, View.OnClickListener {

    private CheckBox cbMale, cbFemale, cbTrue, cbFalse;
    private EditText etName, etId, etCardNo, etAddress, etMobile;
    private TextView tvTitle;

    private BeneficiaryDetailMvpPresenter<BeneficiaryDetailMvpView> beneficiaryDetailMvpPresenter;
    private int pos;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, BeneficiaryDetailActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary_detail);
        beneficiaryDetailMvpPresenter = new BeneficiaryDetailPresenter<>(getDataManager(), this);
        beneficiaryDetailMvpPresenter.onAttach(this);
        setUp();
        pos = getIntent().getIntExtra("pos", 0);
        beneficiaryDetailMvpPresenter.getBnfData(getIntent());
    }

    @Override
    protected void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.beneficiary_detail);

        etName = findViewById(R.id.et_name);
        etId = findViewById(R.id.et_id);
        etCardNo = findViewById(R.id.et_card_no);
        etAddress = findViewById(R.id.et_address);
        etMobile = findViewById(R.id.et_mobile);

        cbMale = findViewById(R.id.cb_male);
        cbFemale = findViewById(R.id.cb_female);
        cbTrue = findViewById(R.id.cb_true);
        cbFalse = findViewById(R.id.cb_false);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        if (getDataManager().getConfigurableParameterDetail().isBiometric())
            findViewById(R.id.ll_bio).setVisibility(View.VISIBLE);
    }

    @Override
    public void openNextActivity(BeneficiaryListResponse.ContentBean data) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", data);
        returnIntent.putExtra("pos", pos);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void openNextActivity(String identityNo) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("IdentityNo", identityNo);
        returnIntent.putExtra("pos", pos);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void updateEditUi(BeneficiaryListResponse.ContentBean data) {
        tvTitle.setText(R.string.beneficiary_edit);
        findViewById(R.id.rl_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_update).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        TextView tvDate = findViewById(R.id.tv_date);
        etName.setEnabled(true);
        etAddress.setEnabled(true);
        etMobile.setEnabled(true);
        cbMale.setEnabled(true);
        cbFemale.setEnabled(true);
        cbMale.setOnClickListener(this);
        cbFemale.setOnClickListener(this);

        etName.setText(data.getFirstName().concat(" ").concat(data.getSurName()));
        etId.setText(data.getIdPassPortNo());
        etCardNo.setText(data.getCardno());
        etAddress.setText(data.getPhysicalAdd().isEmpty() ? getString(R.string.na) : data.getPhysicalAdd());
        etMobile.setText(data.getMobileNo().isEmpty() || data.getMobileNo().equalsIgnoreCase("null") ? getString(R.string.na) : String.format(Locale.getDefault(), "%d", Long.parseLong(data.getMobileNo())));
        tvDate.setText(CalenderUtils.formatByLocale(data.getDateOfBirth(), CalenderUtils.DATE_FORMAT, Locale.getDefault()));
        cbTrue.setChecked(data.isBioStatus());
        cbFalse.setChecked(!data.isBioStatus());
        if (data.getGender().equalsIgnoreCase("M") || data.getGender().equalsIgnoreCase(getString(R.string.m))) {
            cbMale.setChecked(true);
        } else cbFemale.setChecked(false);
    }

    @Override
    public void updateUi(BeneficiaryListResponse.ContentBean data) {
        TextView tvDate = findViewById(R.id.tv_date);

        etName.setText(data.getFirstName().concat(" ").concat(data.getSurName()));
        etId.setText(data.getIdPassPortNo());
        etCardNo.setText(data.getCardno());
        etAddress.setText(data.getPhysicalAdd().isEmpty() ? getString(R.string.na) : data.getPhysicalAdd());
        etMobile.setText(data.getMobileNo().isEmpty() || data.getMobileNo().equalsIgnoreCase("null") ? getString(R.string.na) : String.format(Locale.getDefault(), "%d", Long.parseLong(data.getMobileNo())));
        tvDate.setText(CalenderUtils.formatByLocale(data.getDateOfBirth(), CalenderUtils.DATE_FORMAT, Locale.getDefault()));
        cbTrue.setChecked(data.isBioStatus());
        cbFalse.setChecked(!data.isBioStatus());

        if (data.getGender().equalsIgnoreCase("M") || data.getGender().equalsIgnoreCase(getString(R.string.m))) {
            cbMale.setChecked(true);
        } else cbFemale.setChecked(false);
    }

    @Override
    public void updateEditUiFromDB(Beneficiary data) {
        tvTitle.setText(R.string.beneficiary_edit);
        findViewById(R.id.rl_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_update).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        TextView tvDate = findViewById(R.id.tv_date);
        etName.setEnabled(true);
        etAddress.setEnabled(true);
        etMobile.setEnabled(true);
        cbMale.setEnabled(true);
        cbFemale.setEnabled(true);
        cbMale.setOnClickListener(this);
        cbFemale.setOnClickListener(this);

        etName.setText(data.getFirstName().concat(" ").concat(data.getLastName()));
        etId.setText(data.getIdentityNo());
        etCardNo.setText(data.getCardNumber());
        etAddress.setText(data.getAddress().isEmpty() ? getString(R.string.na) : data.getAddress());
        etMobile.setText(data.getMobile().isEmpty() || data.getMobile().equalsIgnoreCase("null") ? getString(R.string.na) : String.format(Locale.getDefault(), "%d", Long.parseLong(data.getMobile())));
        tvDate.setText(CalenderUtils.formatByLocale(data.getDateOfBirth(), CalenderUtils.DATE_FORMAT, Locale.getDefault()));
        cbTrue.setChecked(data.getBio());
        cbFalse.setChecked(!data.getBio());
        if (data.getGender().equalsIgnoreCase("M") || data.getGender().equalsIgnoreCase(getString(R.string.m))) {
            cbMale.setChecked(true);
        } else cbFemale.setChecked(false);
    }

    @Override
    public void updateUiFromDB(Beneficiary data) {
        TextView tvDate = findViewById(R.id.tv_date);

        etName.setText(data.getFirstName().concat(" ").concat(data.getLastName()));
        etId.setText(data.getIdentityNo());
        etCardNo.setText(data.getCardNumber());
        etAddress.setText(data.getAddress().isEmpty() ? getString(R.string.na) : data.getAddress());
        etMobile.setText(data.getMobile().isEmpty() || data.getMobile().equalsIgnoreCase("null") ? getString(R.string.na) : String.format(Locale.getDefault(), "%d", Long.parseLong(data.getMobile())));
        tvDate.setText(CalenderUtils.formatByLocale(data.getDateOfBirth(), CalenderUtils.DATE_FORMAT, Locale.getDefault()));
        cbTrue.setChecked(data.getBio());
        cbFalse.setChecked(!data.getBio());
        if (data.getGender().equalsIgnoreCase("M") || data.getGender().equalsIgnoreCase(getString(R.string.m))) {
            cbMale.setChecked(true);
        } else cbFemale.setChecked(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
            case R.id.btn_cancel:
                createLog("Beneficiary Details", "Back");
                onBackPressed();
                break;

            case R.id.btn_update:
                createLog("Beneficiary Details", "Update");
                String tmpName = etName.getText().toString().trim();
                String tmpAddress = etAddress.getText().toString().trim();
                String tmpMob = String.format(Locale.US, "%s", etMobile.getText().toString().trim());
                String tmpGender = cbMale.isChecked() ? "M" : cbFemale.isChecked() ? "F" : "";
                beneficiaryDetailMvpPresenter.updateBeneficiary(tmpName, tmpAddress, tmpMob, tmpGender);
                break;

            case R.id.cb_male:
                cbFemale.setChecked(false);
                break;

            case R.id.cb_female:
                cbMale.setChecked(false);
                break;
        }
    }

}
