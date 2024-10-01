package com.compastbc.nfcprint.print;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Hemant Sharma on 05-03-20.
 * Divergent software labs pvt. ltd
 */
public interface BlueToothCallback {
    void onBlueToothNotSupported();

    void onBlueToothConnected(BluetoothDevice bluetoothDevice);

    void onBlueToothDisable();
}
