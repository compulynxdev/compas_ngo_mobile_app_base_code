package com.compastbc.nfcprint.nfc;

public interface NFCReadStatus {

    void cardNotActivated();

    void cardAuthenticated();

    void cardReadFail();
}
