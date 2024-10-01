package com.compastbc.synchronization.transfer;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.compastbc.core.utils.AppLogger;
import com.compastbc.synchronization.bundle.Item;

import java.util.Locale;

/**
 * Manage active transfers
 */
public class TransferManager {

    public static final String TRANSFER_UPDATED = "com.compastbc.synchronization.TRANSFER_UPDATED";
    public static final String EXTRA_STATUS = "com.compastbc.synchronization.STATUS";
    private static final String TAG = "TransferManager";
    private final SparseArray<Transfer> mTransfers = new SparseArray<>();
    private final Context mContext;
    private final TransferNotificationManager mTransferNotificationManager;

    /**
     * Create a new transfer manager
     */
    TransferManager(Context context, TransferNotificationManager transferNotificationManager) {
        mContext = context;
        mTransferNotificationManager = transferNotificationManager;
    }

    /**
     * Broadcast the status of a transfer
     */
    private void broadcastTransferStatus(TransferStatus transferStatus) {
        Intent intent = new Intent();
        intent.setAction(TRANSFER_UPDATED);
        intent.putExtra(EXTRA_STATUS, transferStatus);
        mContext.sendBroadcast(intent);
    }

    /**
     * Add a transfer to the list
     */
    void addTransfer(final Transfer transfer, final Intent intent) {

        // Grab the initial status
        TransferStatus transferStatus = transfer.getStatus();

        AppLogger.i(TAG, String.format(Locale.US, "starting transfer #%d...", transferStatus.getId()));

        // Add a listener for status change events
        transfer.addStatusChangedListener(new Transfer.StatusChangedListener() {
            @Override
            public void onStatusChanged(TransferStatus transferStatus) {

                // Broadcast transfer status
                broadcastTransferStatus(transferStatus);

                // Update the transfer notification manager
                mTransferNotificationManager.updateTransfer(transferStatus, intent);
            }
        });

        // Add a listener for items being received
        transfer.addItemReceivedListener(new Transfer.ItemReceivedListener() {
            @Override
            public void onItemReceived(Item item) {
                AppLogger.e("TransferManger", "FileReceived");
                Intent localIntent = new Intent("FileReceived");
// Send local broadcast
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
               /* if (mMediaScannerConnection.isConnected() &&
                        item instanceof FileItem) {
                    String path = ((FileItem) item).getPath();
                    mMediaScannerConnection.scanFile(path, null);
                } else if (item instanceof UrlItem) {
                    try {
                        mTransferNotificationManager.showUrl(((UrlItem) item).getUrl());
                    } catch (IOException e) {
                        AppLogger.e(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                }*/
            }
        });

        // Add the transfer to the list
        synchronized (mTransfers) {
            mTransfers.append(transferStatus.getId(), transfer);
        }

        // Add the transfer to the notification manager and immediately update it
        mTransferNotificationManager.addTransfer(transferStatus);
        mTransferNotificationManager.updateTransfer(transferStatus, intent);

        // Create a new thread and run the transfer in it
        new Thread(transfer).start();
    }

    /**
     * Stop the transfer with the specified ID
     */
    void stopTransfer(int id) {
        synchronized (mTransfers) {
            Transfer transfer = mTransfers.get(id);
            if (transfer != null) {
                AppLogger.i(TAG, String.format(Locale.US, "stopping transfer #%d...", transfer.getStatus().getId()));
                transfer.stop();
            }
        }
    }

    /**
     * Remove the transfer with the specified ID
     * <p>
     * Transfers that are in progress cannot be removed and a warning is logged
     * if this is attempted.
     */
    void removeTransfer(int id) {
        synchronized (mTransfers) {
            Transfer transfer = mTransfers.get(id);
            if (transfer != null) {
                TransferStatus transferStatus = transfer.getStatus();
                if (!transferStatus.isFinished()) {
                    AppLogger.i(TAG, String.format(Locale.US, "cannot remove ongoing transfer #%d",
                            transferStatus.getId()));
                    return;
                }
                mTransfers.remove(id);
            }
        }
    }

    /**
     * Trigger a broadcast of all transfers
     */
    void broadcastTransfers() {
        for (int i = 0; i < mTransfers.size(); i++) {
            broadcastTransferStatus(mTransfers.valueAt(i).getStatus());
        }
    }
}
