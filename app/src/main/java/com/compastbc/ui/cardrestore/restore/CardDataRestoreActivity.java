package com.compastbc.ui.cardrestore.restore;

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
import com.compastbc.core.data.db.model.NFCCardDataDao;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.data.db.model.NFCCardData;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.MainActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CardDataRestoreActivity extends BaseActivity implements CardDataRestoreMvpView, NFCVerifyCallback {

    private static final String TAG = "CardDataRestoreActivity";
    private CardDataRestorePresenter<CardDataRestoreMvpView> mvpPresenter;
    private NfcAdapter nfcAdapter;
    private NFCReader nfcReader;
    private NFCCardData nfcCardDataBean;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CardDataRestoreActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_tap);
        mvpPresenter = new CardDataRestorePresenter<>(this, getDataManager());
        mvpPresenter.onAttach(this);
        setUp();
        setUpNFC();
    }

    private void setUpNFC() {
        long primaryId = getIntent().getLongExtra("NFCDataPrimaryKey", -1);
        if (primaryId == -1) {
            showToast(getString(R.string.somethingWentWrong));
            onBackPressed();
        } else {
            nfcCardDataBean = getDataManager().getDaoSession().getNFCCardDataDao().queryBuilder().where(NFCCardDataDao.Properties.Id.eq(primaryId)).unique();
        }
        nfcReader = NFCReader.getInstance(this);
        nfcReader.onNfcStatusListener("", this);

        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
    }

    @Override
    protected void setUp() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(R.string.title_card_restore);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        TextView tv_tap_card = findViewById(R.id.tv_tap_card);
        tv_tap_card.setText(R.string.label_card_tap_restore);

        img_back.setOnClickListener(v -> {
            createLog(TAG, "Back");
            onBackPressed();
        });
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
        if (nfcCardDataBean != null) {
            nfcReader.setIntent(intent);
            mvpPresenter.doCardDataRestore(nfcCardDataBean);
        }
    }

    @Override
    protected void onDestroy() {
        mvpPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public void onNfcNotSupported() {
        sweetAlert(R.string.alert, R.string.error_nfc_not_support).setConfirmClickListener(sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            onBackPressed();
        }).show();
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
        if (nfcCardDataBean != null) {
            mvpPresenter.doCardDataRestore(nfcCardDataBean);
        }
    }

    @Override
    public void onCardRestoreSuccess() {
        createLog(TAG, "Card Data Restored");
        Intent i = MainActivity.getStartIntent(this);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
