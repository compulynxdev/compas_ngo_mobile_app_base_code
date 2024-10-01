package com.compastbc.synchronization.transfer;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.synchronization.R;
import com.compastbc.synchronization.discovery.Device;
import com.compastbc.synchronization.util.Settings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Locale;
import java.util.Objects;

/**
 * Listen for new connections and create Transfers for them
 */
class TransferServer implements Runnable {

    private static final String TAG = "TransferServer";
    private final Thread mThread = new Thread(this);
    private boolean mStop;
    private final Context mContext;
    private final Listener mListener;
    private final TransferNotificationManager mTransferNotificationManager;
    private final Settings mSettings;
    private final Selector mSelector = Selector.open();
    private final NsdManager.RegistrationListener mRegistrationListener =
            new NsdManager.RegistrationListener() {
                @Override
                public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                    AppLogger.i(TAG, "service registered");
                }

                @Override
                public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                    AppLogger.i(TAG, "service unregistered");
                }

                @Override
                public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    AppLogger.e(TAG, String.format(Locale.US, "registration failed: %d", errorCode));
                }

                @Override
                public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    AppLogger.e(TAG, String.format(Locale.US, "unregistration failed: %d", errorCode));
                }
            };

    /**
     * Create a new transfer server
     *
     * @param context                     context for retrieving string resources
     * @param transferNotificationManager notification manager
     * @param listener                    callback for new transfers
     */
    TransferServer(Context context, TransferNotificationManager transferNotificationManager, Listener listener) throws IOException {
        mContext = context;
        mTransferNotificationManager = transferNotificationManager;
        mListener = listener;
        mSettings = new Settings(context);
    }

    /**
     * Start the server if it is not already running
     */
    void start() {
        if (!mThread.isAlive()) {
            mStop = false;
            mThread.start();
        }
    }

    /**
     * Stop the transfer server if it is running and wait for it to finish
     */
    void stop() {
        if (mThread.isAlive()) {
            mStop = true;
            mSelector.wakeup();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                AppLogger.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        AppLogger.i(TAG, "starting server...");

        // Inform the notification manager that the server has started
        mTransferNotificationManager.startListening();

        NsdManager nsdManager = null;

        try {
            // Create a server and attempt to bind to a port
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().setReuseAddress(true);
            serverSocketChannel.socket().bind(new InetSocketAddress(AppConstants.PORT));
            serverSocketChannel.configureBlocking(false);

            AppLogger.i(TAG, String.format(Locale.US, "server bound to port %d",
                    serverSocketChannel.socket().getLocalPort()));

            // Register the service
            nsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
            nsdManager.registerService(new Device(
                            mSettings.getString(Settings.Key.DEVICE_NAME),
                            mSettings.getString(Settings.Key.DEVICE_UUID),
                            null,
                            AppConstants.PORT
                    ).toServiceInfo(),
                    NsdManager.PROTOCOL_DNS_SD,
                    mRegistrationListener);

            // Register the server with the selector
            SelectionKey selectionKey = serverSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);

            // Create Transfers as new connections come in
            while (true) {
                mSelector.select();
                if (mStop) {
                    break;
                }
                if (selectionKey.isAcceptable()) {
                    AppLogger.i(TAG, "accepting incoming connection");
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    String unknownDeviceName = mContext.getString(
                            R.string.service_transfer_unknown_device);
                    mListener.onNewTransfer(
                            new Transfer(
                                    socketChannel,
                                    mSettings.getString(Settings.Key.TRANSFER_DIRECTORY),
                                    true/*mSettings.getBoolean(Settings.Key.BEHAVIOR_OVERWRITE)*/,
                                    unknownDeviceName
                            )
                    );
                }
            }

            // Close the server socket
            serverSocketChannel.close();

        } catch (IOException e) {
            AppLogger.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

        // Unregister the service
        if (nsdManager != null) {
            nsdManager.unregisterService(mRegistrationListener);
        }

        // Inform the notification manager that the server has stopped
        mTransferNotificationManager.stopListening();

        AppLogger.i(TAG, "server stopped");
    }

    interface Listener {
        void onNewTransfer(Transfer transfer);
    }
}
