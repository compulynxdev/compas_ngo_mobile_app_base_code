package com.compastbc.nfcprint.nfc.nfc_saral;

import androidx.annotation.StringRes;

/**
 * Created by Hemant Sharma on 18-03-20.
 * Divergent software labs pvt. ltd
 */
public interface SaralWriteDataListener {
    //flag==0 success else fail
    void onDataReceived(int flag);

    void onUnsupportedCard(@StringRes int msg);
}
