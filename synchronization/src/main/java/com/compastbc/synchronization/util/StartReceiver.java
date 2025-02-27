package com.compastbc.synchronization.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.compastbc.synchronization.transfer.TransferService;

/**
 * Starts the transfer service
 */
public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (new Settings(context).getBoolean(Settings.Key.BEHAVIOR_RECEIVE)) {
            TransferService.startStopService(context, true);
        }
    }
}
