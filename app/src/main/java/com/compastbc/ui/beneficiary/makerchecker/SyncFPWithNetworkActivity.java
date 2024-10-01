package com.compastbc.ui.beneficiary.makerchecker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.beneficiary.makerchecker.wifimanager.ClientScanResult;
import com.compastbc.ui.beneficiary.makerchecker.wifimanager.WifiApiManager;
import com.compastbc.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class SyncFPWithNetworkActivity extends BaseActivity implements View.OnClickListener, BaseActivity.SyncReceiverCallback {

    private TextView tv_no_device;

    //-------------- Client
    private WifiApiManager wifiApManager;
    private ArrayList<String> values;
    private ListView lv_devices;
    private ArrayAdapter<String> adapter;

    private IntentFilter mIntentFilter;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Button btnRefresh;
    private final List<WifiP2pDevice> peers = new ArrayList<>();
    //private String[] deviceNameArray;
    //private WifiP2pDevice[] deviceArray;
    private ArrayList<ClientScanResult> clients;
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                //  deviceNameArray=new String[peerList.getDeviceList().size()];
                //  deviceArray=new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                if (clients == null) return;

                updateUI();
            }
        }
    };
    //put it in base activity for again init data when wifi on or off
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                /*if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context,"Wifi is ON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,"Wifi is OFF", Toast.LENGTH_SHORT).show();
                }*/
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                //do something
                if (mManager != null) {
                    if (ActivityCompat.checkSelfPermission(SyncFPWithNetworkActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mManager.requestPeers(mChannel, peerListListener);
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                updateThisDevice(intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            }
        }
    };

    private static String getDeviceStatus(int deviceStatus) {
        AppLogger.d("SyncFPNetworkActivity", "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_fpwith_network);

        wifiApManager = new WifiApiManager(this);
        values = new ArrayList<>();
        //createLog();
        setUp();

        if (getIp() == null)
            initDeviceConnection(false, null, null);
    }

    @Override
    protected void setUp() {
        initView();
        initWifiDiscovery();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTiltle = findViewById(R.id.tvTitle);
        tvTiltle.setText(getString(R.string.SYNC));

        tv_no_device = findViewById(R.id.tv_no_device);
        lv_devices = findViewById(R.id.lv_devices);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        img_back.setOnClickListener(this);
        findViewById(R.id.btn_synchronise).setOnClickListener(this);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(this);
    }

    private void initWifiDiscovery() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        discoverConnectedDevices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private synchronized void updateUI() {
        values.clear();

        if (!peers.isEmpty()) {
            for (ClientScanResult tmp : clients) {
                for (WifiP2pDevice device : peers) {
                    if (tmp.getHWAddr().equals(device.deviceAddress)) {
                        values.add(device.deviceName);
                    }
                }
            }
        } else {
            for (ClientScanResult device : clients) {
                values.add(device.getIpAddress());
            }
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            adapter = new ArrayAdapter<>(getActivity(), R.layout.item_connected_devices, R.id.list_content, values);
            lv_devices.setAdapter(adapter);

            tv_no_device.setVisibility(values.isEmpty() ? View.VISIBLE : View.GONE);
            //    btn_synchronise.setVisibility(values.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    public void updateThisDevice(WifiP2pDevice device) {
        TextView view = findViewById(R.id.my_name);
        view.setText(device.deviceName);

        view = findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.img_back) {
            if (getDataManager().getConfigurableParameterDetail().isActivityLog())
                createLog("Sync Fp", "Back");
            onBackPressed();
        } else if (view.getId() == R.id.btn_synchronise) {
            createLog("Sync Fp", "Synchronise");
            if (getIp() == null)
                initDeviceConnection(true, this, values);
            else {
                doSendBenfData(this, values);
            }
        } else if (view.getId() == R.id.btn_refresh) {
            createLog("Sync Fp", "Refresh");
            btnRefresh.setClickable(false);
            final MaterialDialog pDialog = new MaterialDialog.Builder(this)
                    .title(getString(R.string.scan_device))
                    .content(getString(R.string.please_wait))
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .cancelable(false).build();
            pDialog.show();

            discoverConnectedDevices();
            initDeviceConnection(false, null, null);

            new Handler().postDelayed(() -> {
                btnRefresh.setClickable(true);
                pDialog.dismiss();
            }, 3000);
        }
    }

    /*Wifi setup start here*/
    private void discoverConnectedDevices() {
        new Thread(() -> {
            clients = wifiApManager.getClientList(false);

            updateUI();
        }).start();
    }
    /*Wifi setup end here*/

    @Override
    public void onSyncSuccess() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
