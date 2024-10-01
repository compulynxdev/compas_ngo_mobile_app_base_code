package com.compastbc.fingerprint;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.compastbc.core.utils.AppConstants;

import java.util.List;

public final class FingerprintReaderInit implements FingerprintCallback {

    public static final String ACTION_USB_PERMISSION = "com.compastbc.USB_PERMISSION";
    private static FingerprintReaderInit instance;
    private final String modelName;
    private SaralFingerPrint saralFingerPrintInstance;
    private SecugenFingerPrint secugenFingerPrintInstance;

    private FingerprintReaderInit(Context context) {
        modelName = Build.MODEL;
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance = SaralFingerPrint.getInstance(context);
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance = SecugenFingerPrint.getInstance(context);
                break;
        }
    }

    public static synchronized FingerprintReaderInit getInstance(Context context) {
        if (instance == null) {
            instance = new FingerprintReaderInit(context);
        } else instance.setContext(context);

        return instance;
    }

    private void setContext(Context context) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance.setContext(context);
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance.setContext(context);
                break;
        }
    }

    @Override
    public void setMatchPercentage(int matchPercentage) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance.setMatchPercentage(matchPercentage);
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance.setMatchPercentage(matchPercentage);
                break;
        }
    }

    @Override
    public void captureFingerPrint(FingerPrintCallback callback) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance.captureFingerPrint(callback);
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance.captureFingerPrint(callback);
                break;
        }
    }

    @Override
    public void captureFingerPrintWithEncodeData(FingerPrintDataCallback callback) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance.captureFingerPrintWithEncodeData(callback);
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance.captureFingerPrintWithEncodeData(callback);
                break;
        }
    }

    @Override
    public void captureFingerPrintWithEncodeData(String TAG, FingerPrintDataTagCallback callback) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance.captureFingerPrintWithEncodeData(TAG, callback);
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance.captureFingerPrintWithEncodeData(TAG, callback);
                break;
        }
    }

    @Override
    public void verifyCaptureFingerPrint(String oldFingerPrint, FingerPrintVerifyCallback callback) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance.verifyCaptureFingerPrint(oldFingerPrint, callback);
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance.verifyCaptureFingerPrint(oldFingerPrint, callback);
                break;
        }
    }

    @Override
    public boolean verifyFingerPrints(String oldFp, String newFp) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                return saralFingerPrintInstance.verifyFingerPrints(oldFp, newFp);

            case AppConstants.MODEL_SUNMI:
                return false;

            default:
                return secugenFingerPrintInstance.verifyFingerPrints(oldFp, newFp);
        }
    }

    @Override
    public boolean verifyFingerPrints(String newFp, List<String> oldFpList) {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                return saralFingerPrintInstance.verifyFingerPrints(newFp, oldFpList);

            case AppConstants.MODEL_SUNMI:
                return false;

            default:
                return secugenFingerPrintInstance.verifyFingerPrints(newFp, oldFpList);
        }
    }

    @Override
    public void close() {
        switch (modelName) {
            case AppConstants.MODEL_SARAL:
                saralFingerPrintInstance.close();
                break;

            case AppConstants.MODEL_SUNMI:
                break;

            default:
                secugenFingerPrintInstance.close();
                break;
        }
    }

    public interface FingerPrintCallback {
        void onFingerPrintCapture(Bitmap bitmap);
    }

    public interface FingerPrintDataCallback {
        void onFingerPrintCapture(Bitmap bitmap, String encodedCaptureData, int captureQuality);

        void onFingerPrintQualityError();
    }

    public interface FingerPrintDataTagCallback {
        void onFingerPrintCapture(String TAG, Bitmap bitmap, String encodedCaptureData, int captureQuality);

        void onFingerPrintQualityError(String TAG);
    }

    public interface FingerPrintVerifyCallback {
        void onFingerPrintVerify(Bitmap bitmap, boolean isVerify);

        void onFingerPrintQualityError();
    }
}
