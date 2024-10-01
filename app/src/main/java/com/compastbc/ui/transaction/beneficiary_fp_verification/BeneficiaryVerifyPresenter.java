package com.compastbc.ui.transaction.beneficiary_fp_verification;

import android.content.Context;
import android.graphics.Bitmap;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.fingerprint.FingerprintReaderInit;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class BeneficiaryVerifyPresenter<V extends BeneficiaryVerifyMvpView> extends BasePresenter<V>
        implements BeneficiaryVerifyMvpPresenter<V>, FingerprintReaderInit.FingerPrintDataCallback {
    private final List<String> fingers;
    private final Context context;
    private FingerprintReaderInit fpInstance;

    BeneficiaryVerifyPresenter(DataManager dataManager, Context context1, List<String> finger) {
        super(dataManager);
        this.context = context1;
        this.fingers = finger;
    }

    @Override
    public void onViewLoaded() {
        fpInstance = FingerprintReaderInit.getInstance(context);
        fpInstance.setMatchPercentage(getDataManager().getConfigurableParameterDetail().getMatchingPercentage());
    }

    @Override
    public void onClick() {
        if (checkFpInit()) {
            fpInstance.captureFingerPrintWithEncodeData(this);
        } else getMvpView().showMessage(R.string.NotInitialise);
    }

    private boolean checkFpInit() {
        return fpInstance != null;
    }

    @Override
    public void onFingerPrintCapture(Bitmap bitmap, String encodedCaptureData, int captureQuality) {
        getMvpView().updateImage(bitmap);
        if (captureQuality < getDataManager().getConfigurableParameterDetail().getMatchingPercentage()) {
            getMvpView().showMessage(R.string.LowFPQuality);
        } else {
            if (fpInstance.verifyFingerPrints(encodedCaptureData, fingers))
                getMvpView().getPrograms();
            else getMvpView().showMessage(R.string.FingerPrintNotMatched);
        }
    }

    @Override
    public void onFingerPrintQualityError() {
        SweetAlertDialog alert = getMvpView().sweetAlert(R.string.error_quality_title, R.string.msg_recapture)
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
        alert.show();
    }
}
