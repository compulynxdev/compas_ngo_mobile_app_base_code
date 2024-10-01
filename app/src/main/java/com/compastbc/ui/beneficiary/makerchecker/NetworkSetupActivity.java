package com.compastbc.ui.beneficiary.makerchecker;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.compastbc.R;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.ui.base.BaseActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NetworkSetupActivity extends BaseActivity implements View.OnClickListener {

    //--------------hotspot and join network
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final String networkSSID = "Compass Local Network";
    private final String networkPass = "pass";
    private WifiManager wifiManager;

    private CardView cv_network;
    //private ActivityLog al = new ActivityLog();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_setup);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);//Call Wi-Fi service

        setUp();
    }

    @Override
    protected void setUp() {
        setToolbar();
        initView();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTiltle = findViewById(R.id.tvTitle);
        tvTiltle.setText(getString(R.string.network_setup));

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        img_back.setOnClickListener(this);
    }

    private void initView() {
        findViewById(R.id.cv_create_nw).setOnClickListener(this);
        findViewById(R.id.cv_join_nw).setOnClickListener(this);
        cv_network = findViewById(R.id.cv_network);
        cv_network.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cv_create_nw:
                createLog("Network Setup", "Create Network");
                createWifiAccessPoint();
                break;

            case R.id.cv_join_nw:
                createLog("Network Setup", "Join Network");
                joinWifiNetwork();
                break;

            case R.id.cv_network:
                createLog("Network Setup", "Already Connected");
                startActivity(new Intent(this, SyncFPWithNetworkActivity.class));
                break;

            case R.id.img_back:
                createLog("Network setup", "Back");
                onBackPressed();
                break;
        }
    }

    /*Wifi setup start here*/
    private void createWifiAccessPoint() {
        new Thread(() -> {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();

            for (Method method : wmMethods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    WifiConfiguration netConfig = new WifiConfiguration();
                    netConfig.SSID = networkSSID;
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    try {
                        final boolean apStatus = (Boolean) method.invoke(wifiManager, netConfig, true);
                      /*  for (Method isWifiApEnabledMethod : wmMethods)
                            if (isWifiApEnabledMethod.getName().equals("isWifiApEnabled")) {
                                while (!(Boolean) isWifiApEnabledMethod.invoke(wifiManager)) {
                                }
                                for (Method method1 : wmMethods) {
                                    if (method1.getName().equals("getWifiApState")) {
                                    }
                                }
                            }*/

                        handler.post(() -> {
                            if (apStatus) {
                                System.out.println("SUCCESS ");
                                // hotspot=true;
                                CommonUtils.showToast(getActivity(), getString(R.string.WifiHotspotCreated), Toast.LENGTH_SHORT);
                                cv_network.callOnClick();
                            } else {
                                System.out.println("FAILED");
                                //hotspot=false;
                                CommonUtils.showToast(getActivity(), getString(R.string.WifiHotspotCreationFail), Toast.LENGTH_SHORT);
                            }
                        });

                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void joinWifiNetwork() {
        new Thread(() -> {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            wifiManager.addNetwork(conf);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                wifiManager.startScan();
            }

            int netId = wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

            handler.post(() -> {
                CommonUtils.showToast(this, getString(R.string.joined_to) + " " + networkSSID, Toast.LENGTH_SHORT);
                System.out.println("SUCCESS ");
                //wifi=true;
                cv_network.callOnClick();
            });
        }).start();
    }
    /*Wifi setup end here*/
}
