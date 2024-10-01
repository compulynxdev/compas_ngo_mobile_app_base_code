package com.compastbc.nfcprint.nfc;

/**
 * Created by Hemant Sharma on 07-02-20.
 * Divergent software labs pvt. ltd
 */
public interface NFCReadDataListener {
    void onSuccess(String data);

    void onFail(String TAG, String msg);
}
