package com.compastbc.fingerprint;

import java.util.List;

/**
 * Created by Hemant Sharma on 29-01-20.
 * Divergent software labs pvt. ltd
 */
interface FingerprintCallback {
    void setMatchPercentage(int matchPercentage);

    void captureFingerPrint(FingerprintReaderInit.FingerPrintCallback callback);

    void captureFingerPrintWithEncodeData(FingerprintReaderInit.FingerPrintDataCallback callback);

    void captureFingerPrintWithEncodeData(String TAG, FingerprintReaderInit.FingerPrintDataTagCallback callback);

    void verifyCaptureFingerPrint(String oldFingerPrint, FingerprintReaderInit.FingerPrintVerifyCallback callback);

    boolean verifyFingerPrints(String oldFp, String newFp);

    boolean verifyFingerPrints(String newFp, List<String> oldFpList);

    void close();
}
