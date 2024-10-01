package com.compastbc.ui.beneficiary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.compastbc.R;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.beneficiary.create_beneficiary.CreateBeneficiary;
import com.compastbc.ui.beneficiary.enroll_beneficiary.EnrollBeneficiaryActivity;
import com.compastbc.ui.beneficiary.list_beneficiary.BeneficiaryListActivity;
import com.compastbc.ui.beneficiary.verify_beneficiary.VerifyBeneficiaryActivity;

public class BeneficiaryActivity extends BaseActivity implements BeneficiaryMvpView, View.OnClickListener {
    public static Intent getStartIntent(Context context) {
        return new Intent(context, BeneficiaryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary);
        BeneficiaryPresenter<BeneficiaryMvpView> mvpPresenter = new BeneficiaryPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(R.string.Beneficiary);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        img_back.setOnClickListener(this);
        findViewById(R.id.cv_create_beneficiary).setOnClickListener(this);
        findViewById(R.id.cv_beneficiary_list).setOnClickListener(this);
        if (!getDataManager().getConfigurableParameterDetail().getOnline() && getDataManager().getConfigurableParameterDetail().isBiometric()) {
            CardView cv_verify_benf = findViewById(R.id.cv_verify_benf);
            cv_verify_benf.setVisibility(View.VISIBLE);
            cv_verify_benf.setOnClickListener(this);
        }

        if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
            CardView cv_verify_benf = findViewById(R.id.cv_enroll);
            cv_verify_benf.setVisibility(View.VISIBLE);
            cv_verify_benf.setOnClickListener(this);
        }

        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2")){
            CardView cv_create_benf = findViewById(R.id.cv_create_beneficiary);
            cv_create_benf.setVisibility(View.VISIBLE);
            cv_create_benf.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                createLog("Beneficiary", "Back");
                onBackPressed();
                break;

            case R.id.cv_create_beneficiary:
                createLog("Beneficiary", "Create");
                startActivity(CreateBeneficiary.getStartIntent(BeneficiaryActivity.this));
                break;

            case R.id.cv_enroll:
                createLog("Beneficiary", "Enroll");
                startActivity(EnrollBeneficiaryActivity.getStartIntent(BeneficiaryActivity.this));
                break;

            case R.id.cv_beneficiary_list:
                createLog("Beneficiary", "Beneficiary List");
                startActivity(BeneficiaryListActivity.getStartIntent(BeneficiaryActivity.this));
                break;

            case R.id.cv_verify_benf:
                createLog("Beneficiary", "Verify");
                startActivity(VerifyBeneficiaryActivity.getStartIntent(BeneficiaryActivity.this));
                break;

        }
    }
}
