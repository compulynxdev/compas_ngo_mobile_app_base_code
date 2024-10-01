package com.compastbc.nfcprint.nfc.nfc_pos;

/**
 * Created by Hemant Sharma on 28-11-19.
 * Divergent software labs pvt. ltd
 */
public interface CardReadCallback {
    void onReadSuccess(String data);

    void cardNotActivated();

    void cardReadFail();
}
