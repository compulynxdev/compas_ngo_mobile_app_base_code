package com.compastbc.nfcprint.nfc;

/**
 * Created by Hemant Sharma on 19-09-19.
 * Divergent software labs pvt. ltd
 */
public interface NFCVerifyCallback {
    void onNfcNotSupported();

    void onNFcDisable();

    void onNfcEnable(String TAG);
}
