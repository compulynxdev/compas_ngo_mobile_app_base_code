package com.compastbc.nfcprint.nfc.nfc_pos;

import com.pos.device.picc.MifareDesfire;

/**
 * Created by Hemant Sharma on 28-11-19.
 * Divergent software labs pvt. ltd
 */
public interface CardPreparationListener {
    void onCardReady(MifareDesfire mifareDesfire);

    void onTimeOutError();

    void onUnsupportedCard(String cardName);

    void onCardFail();
}
