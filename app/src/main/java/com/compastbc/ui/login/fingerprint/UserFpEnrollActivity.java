package com.compastbc.ui.login.fingerprint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.login.LoginActivity;
import com.compastbc.ui.login.fingerprint.fingercapture.FingerCaptureFragment;
import com.compastbc.ui.login.fingerprint.imagecapture.ImageCaptureFragment;
import com.compastbc.ui.main.MainActivity;
import com.compastbc.ui.transaction.beneficiary_fp_verification.TransactionBeneficiaryVerification;

import java.util.List;

public class UserFpEnrollActivity extends BaseActivity implements UserFpEnrollMvpView, View.OnClickListener, TransactionBeneficiaryVerification.BeneficiaryVerificationInteraction {

    private static String view;
    private UserFpEnrollMvpPresenter<UserFpEnrollMvpView> mPresenter;
    private Bitmap bitmap;
    private MemberInfo memberInfo;

    public static Intent getStartIntent(Context context, String enroll) {
        view = enroll;
        return new Intent(context, UserFpEnrollActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_fp_enroll);
        mPresenter = new UserFpEnrollPresenter<>(getDataManager(), this);
        mPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        TextView tv_username = findViewById(R.id.tv_username);
        TextView tv_district = findViewById(R.id.tv_district);
        ScrollView scrollView = findViewById(R.id.scrollable);
        Details bean = getDataManager().getUserDetail();
        tv_username.setText(bean.getUser());
        tv_district.setText(bean.getLocationName());
        findViewById(R.id.btn_save).setOnClickListener(this);
        AppConstants.beneficiary = false;
        if (view.equalsIgnoreCase("Enroll")) {
            scrollView.setVisibility(View.VISIBLE);
            addFragment(ImageCaptureFragment.newInstance(bitmap -> this.bitmap = bitmap), R.id.frame_image, false);
            addFragment(FingerCaptureFragment.newInstance(memberInfo -> this.memberInfo = memberInfo), R.id.frame_fp, false);
        } else {
            mPresenter.getAgentBiometric();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_save) {
            mPresenter.doSaveAgentData(bitmap, memberInfo);
        }
    }

    @Override
    public void onBackPressed() {
        createLog("User Enroll", "Back");
        if (isTaskRoot()) {
            Intent intent = LoginActivity.getStartIntent(this);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public void openNextActivity(int flag) {
        if (flag == 0)
            onBackPressed();
        else {
            Intent intent = UserFpEnrollActivity.getStartIntent(this, "Enroll");
            startActivity(intent);
        }
    }

    @Override
    public void showVerifyView(List<String> fps) {
        ViewGroup frame = findViewById(R.id.verify_frame);
        frame.setVisibility(View.VISIBLE);
        addFragment(TransactionBeneficiaryVerification.newInstance(fps, getDataManager().getUserName(), this), R.id.verify_frame, false);
    }

    @Override
    public void onSuccess() {
        getDataManager().setLoggedIn(true);
        createLog("Agent verify", "Login success");
        createAttendanceLog();
        Intent i = MainActivity.getStartIntent(getActivity());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setUp();
    }
}
