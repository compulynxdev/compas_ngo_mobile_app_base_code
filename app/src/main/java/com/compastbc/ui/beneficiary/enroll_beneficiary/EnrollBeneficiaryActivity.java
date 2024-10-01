package com.compastbc.ui.beneficiary.enroll_beneficiary;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.beneficiary.enroll_beneficiary.show_beneficiary_fragment.ShowBeneficiaryDetails;
import com.compastbc.ui.login.fingerprint.fingercapture.FingerCaptureFragment;
import com.compastbc.ui.login.fingerprint.imagecapture.ImageCaptureFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class EnrollBeneficiaryActivity extends BaseActivity implements EnrollBeneficiaryMvpView, View.OnClickListener {
    private EnrollBeneficiaryMvpPresenter<EnrollBeneficiaryMvpView> enrollBeneficiaryMvpPresenter;
    private EditText et_input;
    private TextView tv_name, tv_idno, tv_location;
    private Button btn_search, btn_save;
    private ViewGroup beneficiaryDetails, fragments;
    private RadioButton rb_identification, rb_cardNumber;
    private RelativeLayout bDetails;
    private String searchCriteria, identification, memberId;
    private MemberInfo memberInfo;
    private Bitmap bitmap;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, EnrollBeneficiaryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_beneficiary);
        enrollBeneficiaryMvpPresenter = new EnrollBeneficiaryPresenter<>(getActivity(), getDataManager());
        enrollBeneficiaryMvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        findIds();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        btn_search.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        rb_cardNumber.setOnClickListener(this);
        rb_identification.setOnClickListener(this);
    }

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tv_title = findViewById(R.id.tvTitle);
        tv_title.setText(R.string.EnrollBeneficiary);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        et_input = findViewById(R.id.et_input);
        tv_name = findViewById(R.id.name);
        tv_idno = findViewById(R.id.identificationo);
        tv_location = findViewById(R.id.district);
        btn_save = findViewById(R.id.btn_save);
        btn_search = findViewById(R.id.btn_search);
        beneficiaryDetails = findViewById(R.id.beneficiary_details);
        fragments = findViewById(R.id.fragments);
        rb_cardNumber = findViewById(R.id.cardno);
        rb_identification = findViewById(R.id.rationNo);
        bDetails = findViewById(R.id.bdetails);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                openNextActivity();
                break;

            case R.id.cardno:
                searchCriteria = "cardNo";
                et_input.setHint(R.string.CardNo);
                break;

            case R.id.rationNo:
                searchCriteria = "rationNo";
                et_input.setHint(R.string.IdentificationNo);
                break;

            case R.id.btn_save:
                createLog("Enroll Beneficiary", "Save");
                enrollBeneficiaryMvpPresenter.saveBeneficiaryBiometric(this.identification, this.memberId, bitmap, memberInfo);
                break;

            case R.id.btn_search:
                createLog("Enroll Beneficiary", "Search");
                enrollBeneficiaryMvpPresenter.searchBeneficiary(searchCriteria, et_input.getText().toString().trim());
                break;
        }
    }

    @Override
    public void showBeneficiaryDetails(JSONObject object) {
        beneficiaryDetails.setVisibility(View.VISIBLE);
        et_input.setEnabled(false);
        rb_identification.setEnabled(false);
        rb_cardNumber.setEnabled(false);
        btn_search.setEnabled(false);
        replaceFragment(ShowBeneficiaryDetails.newInstance(object, () -> {
            beneficiaryDetails.setVisibility(View.GONE);
            fragments.setVisibility(View.VISIBLE);
            try {
                this.identification = object.getString("identityNo");
                this.memberId = object.getString("memberId");
                String name = getString(R.string.name) + object.getString("firstName");
                String idno = getString(R.string.IdNoColon) + this.identification;
                AppConstants.beneficiary = true;
                String location = getString(R.string.location) + getDataManager().getUserDetail().getLocationName();
                tv_name.setText(name);
                tv_idno.setText(idno);
                tv_location.setText(location);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            bDetails.setVisibility(View.VISIBLE);
            addFragment(ImageCaptureFragment.newInstance(bitmap -> this.bitmap = bitmap), R.id.frame_image, false);

            addFragment(FingerCaptureFragment.newInstance((MemberInfo memberInfo) -> this.memberInfo = memberInfo), R.id.frame_fp, false);
        }), R.id.beneficiary_details);
    }

    @Override
    public void openNextActivity() {
        createLog("Enroll Beneficiary", "Back");
        finish();
    }
}
