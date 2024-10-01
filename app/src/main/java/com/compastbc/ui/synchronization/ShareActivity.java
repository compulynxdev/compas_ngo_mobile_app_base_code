package com.compastbc.ui.synchronization;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.PermissionUtils;
import com.compastbc.synchronization.discovery.Device;
import com.compastbc.synchronization.transfer.TransferService;
import com.compastbc.synchronization.util.Settings;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.synchronization.receive.ReceiveActivity;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Display a list of devices available for receiving a transfer
 * <p>
 * mDNS (multicast DNS) is used to find other peers capable of receiving the transfer. Once a
 * device is selected, the transfer service is provided with the device information and the file.
 */
public class ShareActivity extends BaseActivity {

    private static final String TAG = "ShareActivity";
    private Activity activity;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ProgressDialog progressDialog;
    private DeviceAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_share);
        setUp();
    }

    @Override
    protected void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tv_title = findViewById(R.id.tvTitle);
        tv_title.setText(R.string.SYNC);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        // Attempt to obtain permission if it is somehow missing
        if (PermissionUtils.haveStoragePermission(this)) {
            finishInit();
        } else {
            PermissionUtils.requestStoragePermission(this);
        }
    }

    /**
     * Finish initializing the activity
     */
    private void finishInit() {
        //createTestFile();

        mDeviceAdapter = new DeviceAdapter();
        mDeviceAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
        mDeviceAdapter.start();

        final ListView listView = findViewById(R.id.selectList);
        listView.setAdapter(mDeviceAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Ensure valid data is present in the intent
            progressDialog = ProgressDialog.show(activity, getString(R.string.populatingData), getString(R.string.please_wait), true,
                    false, dialog -> {

                    });
            progressDialog.setCancelable(false);

            doSynchroniseData(position);
        });
    }

    private void doSynchroniseData(final int position) {
        new Thread(() -> {
            final ArrayList<Uri> uriList = new ArrayList<>();//buildUriList();
            new SynchronizationData(getDataManager(), activity, new SynchronizationData.SynchronizationDataCallback() {
                @Override
                public void onDataPathReceive(final File filePath) {
                    handler.post(() -> {
                        progressDialog.dismiss();
                        uriList.add(Uri.fromFile(filePath));

                        if (!uriList.isEmpty()) {
                            Device device = mDeviceAdapter.getDevice(position);

                            if (device != null) {
                                Intent startTransfer = new Intent(ShareActivity.this, TransferService.class);
                                startTransfer.setAction(TransferService.ACTION_START_TRANSFER);
                                startTransfer.putExtra(TransferService.EXTRA_DEVICE, device);
                                startTransfer.putParcelableArrayListExtra(TransferService.EXTRA_URIS, uriList);
                                startService(startTransfer);

                                navigateToNextScreen();
                            } else {
                                alertError(getString(R.string.error), getString(R.string.somethingWentWrong));
                            }
                        } else {
                            showDialog();
                        }
                    });
                }

                @Override
                public void onNoDataFound() {
                    handler.post(() -> {
                        progressDialog.dismiss();
                        alertError(getString(R.string.error), getString(R.string.no_data_found));
                    });
                }

                @Override
                public void onException() {
                    handler.post(() -> {
                        progressDialog.dismiss();
                        alertError(getString(R.string.error), getString(R.string.process_error));
                    });
                }
            });
        }).start();
    }

    void alertError(String title, String msg) {
        new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText(getString(R.string.Ok))
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void navigateToNextScreen() {
        Intent intent = new Intent(this, ReceiveActivity.class);
        intent.putExtra("sender", true);
        startActivity(intent);
        finish();
    }

    private void showDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.activity_share_intent)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    public Uri load() {
        File downloads = new File(Environment.getExternalStorageDirectory(), "Download");
        File folder = new File(downloads +
                File.separator + AppConstants.FOLDER_NAME);
        File myExternalFile = new File(folder.getPath() + "/test" + ".txt");
        return Uri.fromFile(myExternalFile);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionUtils.obtainedStoragePermission(requestCode, grantResults)) {
            finishInit();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.activity_share_permissions)
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.dismiss()).create()
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mDeviceAdapter != null) {
            mDeviceAdapter.stop();
        }
        super.onDestroy();
    }

    /**
     * Adapter that discovers other devices on the network
     */
    private class DeviceAdapter extends ArrayAdapter<String> {

        /**
         * Maintain a mapping of device IDs to discovered devices
         */
        private final Map<String, Device> mDevices = new HashMap<>();

        /**
         * Maintain a queue of devices to resolve
         */
        private final List<NsdServiceInfo> mQueue = new ArrayList<>();

        private NsdManager mNsdManager;
        /**
         * Listener for discovery events
         */
        private final NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
              /*  if (serviceInfo.getServiceName().equals(mThisDeviceName)) {
                    return;
                }*/
                AppLogger.d(TAG, String.format(Locale.US, "found \"%s\"; queued for resolving", serviceInfo.getServiceName()));
                boolean resolve;
                synchronized (mQueue) {
                    resolve = mQueue.size() == 0;
                    mQueue.add(serviceInfo);
                }
                if (resolve) {
                    resolveNextService();
                }
            }

            @Override
            public void onServiceLost(final NsdServiceInfo serviceInfo) {
                AppLogger.d(TAG, String.format(Locale.US, "lost \"%s\"", serviceInfo.getServiceName()));
                runOnUiThread(() -> {
                    remove(serviceInfo.getServiceName());
                    mDevices.remove(serviceInfo.getServiceName());
                });
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                AppLogger.d(TAG, "service discovery started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                AppLogger.d(TAG, "service discovery stopped");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                AppLogger.e(TAG, "unable to start service discovery");
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                AppLogger.e(TAG, "unable to stop service discovery");
            }
        };

        DeviceAdapter() {
            super(ShareActivity.this, R.layout.view_simple_list_item, android.R.id.text1);
        }

        /**
         * Prepare to resolve the next service
         * <p>
         * For some inexplicable reason, Android chokes miserably when
         * resolving more than one service at a time. The queue performs each
         * resolution sequentially.
         */
        private void prepareNextService() {
            synchronized (mQueue) {
                mQueue.remove(0);
                if (mQueue.size() == 0) {
                    return;
                }
            }
            resolveNextService();
        }

        /**
         * Resolve the next service in the queue
         */
        private void resolveNextService() {
            NsdServiceInfo serviceInfo;
            synchronized (mQueue) {
                serviceInfo = mQueue.get(0);
            }
            AppLogger.d(TAG, String.format(Locale.US, "resolving \"%s\"", serviceInfo.getServiceName()));
            mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                @Override
                public void onServiceResolved(final NsdServiceInfo serviceInfo) {
                    AppLogger.d(TAG, String.format(Locale.US, "resolved \"%s\"", serviceInfo.getServiceName()));
                    final Device device = new Device(
                            serviceInfo.getServiceName(),
                            "",
                            serviceInfo.getHost(),
                            serviceInfo.getPort()
                    );
                    runOnUiThread(() -> {
                        //name: S700, type: ._nitroshare._tcp, host: /192.168.137.35, port: 40818
                        String ip = serviceInfo.getHost().toString().replace("/", "");
                        InetAddress inetAddress = Settings.getInetAddress();
                        if (!ip.equals(inetAddress == null ? "" : inetAddress.getHostAddress())) {
                            mDevices.put(serviceInfo.getServiceName(), device);
                            add(serviceInfo.getServiceName());
                        }
                    });
                    prepareNextService();
                }

                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    AppLogger.e(TAG, String.format(Locale.US, "unable to resolve \"%s\": %d",
                            serviceInfo.getServiceName(), errorCode));
                    prepareNextService();
                }
            });
        }

        void start() {
            mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            assert mNsdManager != null;
            mNsdManager.discoverServices(Device.SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

            // String mThisDeviceName = new Settings(getContext()).getString(Settings.Key.DEVICE_NAME);
        }

        void stop() {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }

        /**
         * Retrieve the specified device
         *
         * @param position device index
         * @return device at the specified position
         */
        Device getDevice(int position) {
            if (mDevices.isEmpty()) return null;
            return mDevices.get(getItem(position));
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(R.layout.view_simple_list_item, parent, false);
            }
            Device device = mDevices.get(getItem(position));
            assert device != null;
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(device.getName());
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(device.getHost().getHostAddress());
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(R.drawable.ic_device);
            return convertView;
        }
    }
}
