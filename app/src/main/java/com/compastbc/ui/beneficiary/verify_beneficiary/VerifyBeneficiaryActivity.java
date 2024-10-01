package com.compastbc.ui.beneficiary.verify_beneficiary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.beneficiary.makerchecker.NetworkSetupActivity;
import com.compastbc.ui.login.fingerprint.fingercapture.FingerCaptureFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class VerifyBeneficiaryActivity extends BaseActivity implements VerifyBeneficiaryMvpView, View.OnClickListener {

    private VerifyBeneficiaryPresenter<VerifyBeneficiaryMvpView> verifyBeneficiaryPresenter;

    //view declaration
    private EditText et_input;
    private TextView tv_name;
    private TextView tv_idno;
    private TextView tv_location;
    private RadioButton rb_identification, rb_cardNumber;
    private String searchCriteria;
    private MemberInfo memberInfo;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, VerifyBeneficiaryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_beneficiary);
        verifyBeneficiaryPresenter = new VerifyBeneficiaryPresenter<>(getActivity(), getDataManager());
        verifyBeneficiaryPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        findIds();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        rb_cardNumber.setOnClickListener(this);
        rb_identification.setOnClickListener(this);
    }

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tv_title = findViewById(R.id.tvTitle);
        et_input = findViewById(R.id.et_input);
        tv_name = findViewById(R.id.name);
        tv_idno = findViewById(R.id.identificationo);
        tv_location = findViewById(R.id.district);
        rb_cardNumber = findViewById(R.id.cardno);
        rb_identification = findViewById(R.id.rationNo);
        tv_title.setText(R.string.VerifyBeneficiary);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        verifyBeneficiaryPresenter.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                openPreviousActivity();
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
                createLog("Verify Beneficiary", "Verify");
                if (getDataManager().getConfigurableParameterDetail().getOnline())
                    showMessage(R.string.under_development);
                else if (memberInfo == null)
                    showMessage(R.string.PleaseCaptureFingerPrints);
                else {
                    verifyBeneficiaryPresenter.doVerifyBenfFp(memberInfo);
                }
                break;

            case R.id.btn_search:
                createLog("Verify Beneficiary", "Search Beneficiary");
                verifyBeneficiaryPresenter.searchBeneficiary(searchCriteria, et_input.getText().toString().trim());
                break;
        }
    }

    @Override
    public void reEnrollFingerPrint(JSONObject object) {
        et_input.setEnabled(false);
        rb_identification.setEnabled(false);
        rb_cardNumber.setEnabled(false);
        findViewById(R.id.btn_search).setEnabled(false);
        try {
            String identification = object.getString("identityNo");
            //String memberId = object.getString("memberId");
            String name = getString(R.string.name) + object.getString("firstName");
            String idno = getString(R.string.IdNoColon) + identification;
            String location = getString(R.string.location) + getDataManager().getUserDetail().getLocationName();
            tv_name.setText(name);
            tv_idno.setText(idno);
            tv_location.setText(location);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        findViewById(R.id.rl_details).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_fragment_view).setVisibility(View.VISIBLE);

        replaceFragment(FingerCaptureFragment.newInstance((MemberInfo memberInfo) -> this.memberInfo = memberInfo), R.id.frame_fp, false);
    }

    @Override
    public void openNextActivity() {
        Intent i = new Intent(getActivity(), NetworkSetupActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void openPreviousActivity() {
        createLog("Verify Beneficiary", "Back");
        onBackPressed();
    }
}
