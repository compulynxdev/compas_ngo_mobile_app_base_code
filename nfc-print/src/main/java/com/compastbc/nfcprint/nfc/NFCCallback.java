package com.compastbc.nfcprint.nfc;

import android.content.Intent;

/**
 * Created by Hemant Sharma on 07-02-20.
 * Divergent software labs pvt. ltd
 */
public interface NFCCallback {
    void setIntent(Intent intent);

    void doActivateCard(String personalData, String cardPin, NFCListener nfcListener, Boolean beep, Boolean isCardDataAlreadyStored);

    void doReadCardDataForActivate(NFCReadListDataOrErrorListener nfcListener, Boolean beep);

    void doReadCardDataForActivate(int fileName, NFCReadDataListener nfcListener, Boolean beep);

    void doReadCardData(int fileName, NFCReadDataListener nfcListener, Boolean beep);

    void doReadCardByList(NFCReadListDataListener nfcListener, Boolean beep, int... fileName);

    void doWriteCardData(int fileName, String data, NFCListener nfcListener, Boolean beep);

    void doDeleteFile(NFCListener nfcListener, Boolean beep, int... fileName);

    void doFormat(NFCListener nfcListener, Boolean beep);

    void closeNfcReader();
}
