package com.compastbc.ui.beneficiary.create_beneficiary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.ui.base.BaseActivity;

import java.util.Arrays;
import java.util.Locale;

public class CreateBeneficiary extends BaseActivity implements CreateBeneficiaryMvpView, View.OnClickListener {
    private EditText etFirstName, etLastName, etAddress, etIdentificationNumber, et_mobile;
    private CheckBox male, female;
    private TextView dateOfBirth, title, tv_locationName, idType;
    private Toolbar toolbar;
    private Button btnClear, btnSave, btnSignature;
    private String date, signatureData;
    private ImageView img_signature;
    private CreateBeneficiaryMvpPresenter<CreateBeneficiaryMvpView> createBeneficiaryMvpPresenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CreateBeneficiary.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_beneficiary);
        createBeneficiaryMvpPresenter = new CreateBeneficiaryPresenter<>(getDataManager(), this);
        createBeneficiaryMvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        findIds();
        setSupportActionBar(toolbar);
        title.setText(R.string.CreateBeneficiary);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        tv_locationName.setText(getDataManager().getUserDetail().getLocationName());
        idType.setText(getDataManager().getUserDetail().getBenIdLevel());
        setClickListeners(img_back, male, female, btnClear, dateOfBirth, btnSave, btnSignature);
    }

    private void setClickListeners(View... views) {
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }

    private void findIds() {
        title = findViewById(R.id.tvTitle);
        toolbar = findViewById(R.id.toolbar);
        etFirstName = findViewById(R.id.first_name);
        etLastName = findViewById(R.id.last_name);
        etAddress = findViewById(R.id.address);
        etIdentificationNumber = findViewById(R.id.identificationo);
        //etIdentificationNumber.setFilters(new InputFilter[] {new InputFilter.LengthFilter(getDataManager().getConfigurableParameterDetail().getIdLength())});
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        dateOfBirth = findViewById(R.id.tv_dob);
        btnClear = findViewById(R.id.btClear);
        btnSave = findViewById(R.id.btRegister);
        img_signature = findViewById(R.id.img_signature);
        btnSignature = findViewById(R.id.btnSign);
        tv_locationName = findViewById(R.id.locationName);
        idType = findViewById(R.id.idType);
        et_mobile = findViewById(R.id.et_mobile);
    }

    @Override
    public void setDate(String date) {
        this.date = date;
        dateOfBirth.setText(CalenderUtils.formatByLocale(date, CalenderUtils.DATE_FORMAT, Locale.getDefault()));
    }

    @Override
    public void openNextActivity() {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;

            case R.id.male:
                female.setChecked(false);
                break;

            case R.id.female:
                male.setChecked(false);
                break;

            case R.id.tv_dob:
                createBeneficiaryMvpPresenter.onSelectDate();
                break;

            case R.id.btClear:
                createLog("Create Beneficiary", "Clear");
                clearData();
                break;

            case R.id.btRegister:
                createLog("Create Beneficiary", "Save");
                btnSave.setClickable(false);
                saveBeneficiaryData();
                new Handler().postDelayed(() -> btnSave.setClickable(true), 1500);
                break;

            case R.id.btnSign:
                createLog("Create Beneficiary", "Get signature");
                Intent intent = new Intent(this, BeneficiarySignatureActivity.class);
                startActivityForResult(intent, 100);
                break;
        }
    }

    private void clearData() {
        etFirstName.setText("");
        etLastName.setText("");
        etAddress.setText("");
        dateOfBirth.setText("");
        male.setChecked(false);
        female.setChecked(false);
        et_mobile.setText("");
        etIdentificationNumber.setText("");
        img_signature.setImageResource(R.color.transparent);
        date = null;
        signatureData = null;
    }


    private void saveBeneficiaryData() {
        createBeneficiaryMvpPresenter.verifyInputs(etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), male.isChecked() ? "M" : "F", etAddress.getText().toString().trim()
                , date, signatureData, etIdentificationNumber.getText().toString().trim(), et_mobile.getText().toString().trim());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        createBeneficiaryMvpPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                byte[] imageData = data.getByteArrayExtra("BitmapArrayImage");
                Bitmap bitmap = AppUtils.getBitmap(imageData);
                img_signature.setImageBitmap(bitmap);
                signatureData = Arrays.toString(imageData);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                AppLogger.e("CreateBeneficiary", "Activity.RESULT_CANCELED");
            }
        }
    }
}
