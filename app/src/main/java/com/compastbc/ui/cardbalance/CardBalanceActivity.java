package com.compastbc.ui.cardbalance;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.network.model.CardBalanceBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.cardbalance.carddialog.CardBalanceDialog;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CardBalanceActivity extends BaseActivity implements CardBalanceMvpView, NFCVerifyCallback {

    private CardBalancePresenter<CardBalanceMvpView> cardBalancePresenter;
    private NfcAdapter nfcAdapter;
    private NFCReader nfcReader;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CardBalanceActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_tap);
        cardBalancePresenter = new CardBalancePresenter<>(this, getDataManager());
        cardBalancePresenter.onAttach(this);
        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
        setUp();
    }

    @Override
    protected void setUp() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(R.string.CardBalance);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        TextView tv_tap_card = findViewById(R.id.tv_tap_card);
        tv_tap_card.setText(R.string.card_tap_check_bal);
        nfcReader = NFCReader.getInstance(this);
        nfcReader.onNfcStatusListener("", this);
    }

    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            //nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{},  new String[][] { new String[] { NfcF.class.getName() } });
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{}, null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        nfcReader.setIntent(intent);
        cardBalancePresenter.readCardBalance();
    }

    @Override
    protected void onDestroy() {
        cardBalancePresenter.onDetach();
        super.onDestroy();
    }


    @Override
    public void showBalance(String identityNo, String name, String cardNo, List<CardBalanceBean> cardBalanceBeans) {
        hideLoading();
        CardBalanceDialog.newInstance(identityNo, cardNo, name, cardBalanceBeans, () -> {
            hideLoading();
            finish();
        }).show(getSupportFragmentManager(), "Card Balance");
    }

    @Override
    public void onNfcNotSupported() {
        sweetAlert(R.string.alert, R.string.error_nfc_not_support).show();
    }

    @Override
    public void onNFcDisable() {
        sweetAlert(R.string.alert, R.string.error_nfc_disable)
                .setConfirmText(getString(R.string.msg_enable_nfc))
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                    startActivity(intent);
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();

    }

    @Override
    public void onNfcEnable(String TAG) {
        cardBalancePresenter.readCardBalance();
    }
}
