package com.compastbc.nfcprint.nfc.nfc_saral;

import androidx.annotation.StringRes;

/**
 * Created by Hemant Sharma on 18-03-20.
 * Divergent software labs pvt. ltd
 */
public interface SaralReadCardDataListener {
    void onDataReceived(String data);

    void onUnsupportedCard(@StringRes int msg);

    void cardNotActivated();
}
