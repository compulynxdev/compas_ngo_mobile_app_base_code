package com.compastbc.ui.changepassword;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.ui.base.BaseActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangePasswordActivity extends BaseActivity implements ChangePasswordMvpView, NFCVerifyCallback {
    private ChangePasswordMvpPresenter<ChangePasswordMvpView> changePasswordMvpPresenter;
    private TextView tapCard;
    private TextView tvName;
    private TextView tvIdentification;
    private EditText etNewPass, etConfirmPass;
    private AppCompatButton btnChange;
    private RelativeLayout rl_changePin;
    private NfcAdapter nfcAdapter;
    private boolean update;
    private String newPass, identification;
    private MaterialDialog dialog;
    private NFCReader nfcReader;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, ChangePasswordActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        changePasswordMvpPresenter = new ChangePasswordPresenter<>(getActivity(), getDataManager());
        changePasswordMvpPresenter.onAttach(this);
        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
        setUp();

    }

    @Override
    protected void setUp() {
        findIds();
        tapCard.setVisibility(View.VISIBLE);
        update = false;
        nfcReader = NFCReader.getInstance(this);
        btnChange.setOnClickListener(v -> verifyInputs(etNewPass.getText().toString().trim(), etConfirmPass.getText().toString().trim()));
        nfcReader.onNfcStatusListener("ChangePassword", this);
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

    private void findIds() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvName = findViewById(R.id.name);
        tvIdentification = findViewById(R.id.identification);
        etNewPass = findViewById(R.id.et_newPassword);
        etConfirmPass = findViewById(R.id.et_confirmPassword);
        btnChange = findViewById(R.id.change);
        tapCard = findViewById(R.id.tv_tap_card);
        rl_changePin = findViewById(R.id.rl_changePin);

        setSupportActionBar(toolbar);
        tvTitle.setText(R.string.ChangeCardPin);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
    }


    @Override
    public void showDetails(String name, String idno) {
        if (dialog != null) dialog.dismiss();
        tapCard.setVisibility(View.GONE);
        identification = idno;
        rl_changePin.setVisibility(View.VISIBLE);
        tvName.setText(getString(R.string.cardHolderInfo).concat(" ").concat(name));
        tvIdentification.setText(getString(R.string.IdNoColon).concat(" ").concat(idno));
    }

    @Override
    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public void changePasswordSuccess() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void hideDialog() {
        if (dialog != null) dialog.dismiss();
    }

    @Override
    public void verifyInputs(String newPass, String confirmPass) {
        if (newPass != null && confirmPass != null && !newPass.isEmpty() && !confirmPass.isEmpty()) {
            if (newPass.length() == 4) {
                if (newPass.equalsIgnoreCase(confirmPass)) {
                    dialog = materialDialog(R.string.ChangeCardPin, R.string.card_change_pin);
                    this.newPass = newPass;
                    dialog.show();

                    if (verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        changePasswordMvpPresenter.writeCardDetails(newPass, identification);
                    }
                } else showMessage(R.string.NewPinMatchConfirmPass);

            } else showMessage(R.string.card_pin_size);
        } else showMessage(R.string.PleaseEnterInput);
    }

    @Override
    protected void onDestroy() {
        changePasswordMvpPresenter.onDetach();
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
        changePasswordMvpPresenter.readCardDetails();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        nfcReader.setIntent(intent);
        if (changePasswordMvpPresenter != null) {
            if (update) {
                dialog.dismiss();
                changePasswordMvpPresenter.writeCardDetails(newPass, identification);
            } else {
                dialog = materialDialog(R.string.ChangeCardPin, R.string.fetching_card_data);
                dialog.show();
                changePasswordMvpPresenter.readCardDetails();
            }
        }
    }


}
