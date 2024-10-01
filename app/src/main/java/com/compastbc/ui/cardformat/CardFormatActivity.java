package com.compastbc.ui.cardformat;

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
import com.compastbc.core.utils.AppConstants;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.ui.base.BaseActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CardFormatActivity extends BaseActivity implements CardFormatMvpView, NFCVerifyCallback {

    private CardFormatPresenter<CardFormatMvpView> cardFormatPresenter;
    private NfcAdapter nfcAdapter;
    private NFCReader nfcReader;
    private boolean isCleanFormat = false;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CardFormatActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_tap);
        cardFormatPresenter = new CardFormatPresenter<>(this, getDataManager());
        cardFormatPresenter.onAttach(this);

        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
        checkFormatStatus(getIntent());
        setUp();
    }


    @Override
    protected void setUp() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(R.string.FormatCard);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        TextView tv_tap_card = findViewById(R.id.tv_tap_card);
        tv_tap_card.setText(R.string.card_tap_format);
        nfcReader = NFCReader.getInstance(this);
        nfcReader.onNfcStatusListener("format", this);
    }

    private void checkFormatStatus(Intent intent) {
        if (intent.hasExtra("CleanFormat")) {
            isCleanFormat = intent.getBooleanExtra("CleanFormat", false);
        }
        //AppLogger.d("FormatTest", ""+isCleanFormat);
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
        checkFormatStatus(intent);
        cardFormatPresenter.formatCard(intent, isCleanFormat);
    }

    @Override
    public void onFormatSuccess() {
        createLog("Format Activity", "Card Formatted");
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        cardFormatPresenter.onDetach();
        nfcReader.closeNfcReader();
        super.onDestroy();
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
        cardFormatPresenter.formatCard(null, isCleanFormat);
    }

}
