package com.compastbc.nfcprint.nfc.nfc_saral;

import androidx.annotation.StringRes;

import java.util.List;

/**
 * Created by Hemant Sharma on 18-03-20.
 * Divergent software labs pvt. ltd
 */
public interface SaralReadListDataListener {
    void onDataReceived(List<String> data);

    void onUnsupportedCard(@StringRes int msg);

    void cardNotActivated();
}
