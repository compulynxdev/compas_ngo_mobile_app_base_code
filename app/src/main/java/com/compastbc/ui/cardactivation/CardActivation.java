package com.compastbc.ui.cardactivation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.cardactivation.inputdialog.IdNumberInputDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class CardActivation extends BaseActivity implements CardActivationMvpView, View.OnClickListener {

    private Button btn_scanqr, btn_manually, btn_activate;
    private TextView tv_name;
    private TextView tv_dob;
    private TextView tv_gender;
    private TextView tv_idno;
    private TextView tv_cardno;
    private ScrollView sc_details;
    private LinearLayout ll_options;
    private CardActivationMvpPresenter<CardActivationMvpView> cardActivationMvpPresenter;
    private MaterialDialog dialog;
    private NfcAdapter nfcAdapter;
    private NFCReader nfcReader;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CardActivation.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_activation);
        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
        cardActivationMvpPresenter = new CardActivationPresenter<>(this, getDataManager());
        cardActivationMvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        findIds();
        addClickListeners();
        ll_options.setVisibility(View.VISIBLE);
        sc_details.setVisibility(View.GONE);
        nfcReader = NFCReader.getInstance(this);
    }

    private void addClickListeners() {
        btn_activate.setOnClickListener(this);
        btn_scanqr.setOnClickListener(this);
        btn_manually.setOnClickListener(this);
    }

    private void findIds() {
        btn_scanqr = findViewById(R.id.btn_sacnqr);
        btn_manually = findViewById(R.id.btn_manuallyactivate);
        btn_activate = findViewById(R.id.btn_activate_card);
        tv_name = findViewById(R.id.tv_name);
        tv_cardno = findViewById(R.id.tv_cardno);
        tv_dob = findViewById(R.id.tv_dob);
        tv_gender = findViewById(R.id.tv_gender);
        tv_idno = findViewById(R.id.tv_idno);
        ll_options = findViewById(R.id.ll_activate_options);
        sc_details = findViewById(R.id.sc_benf_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tv_title = findViewById(R.id.tvTitle);

        setSupportActionBar(toolbar);
        tv_title.setText(R.string.CardActivation);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.img_back:
                onBackPressed();
                break;

            case R.id.btn_sacnqr:
                createLog("Card Activation", "scan qr");
                scanQr();
                break;

            case R.id.btn_manuallyactivate:
                createLog("Card Activation", "Manually");
                IdNumberInputDialog.newInstance(input -> cardActivationMvpPresenter.verifyInput(input)).show(getSupportFragmentManager(), "Card Manually Activate");
                break;

            case R.id.btn_activate_card:
                dialog = materialDialog(R.string.CardActivation, R.string.card_tap_activate);
                dialog.show();

                createLog("Card Activation", "Activate");
                if (verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                    cardActivationMvpPresenter.doActivateCard();
                }
                break;
        }
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
    public void showBeneficiaryDetails(JSONObject bnfJsonObject) {
        sc_details.setVisibility(View.VISIBLE);
        ll_options.setVisibility(View.GONE);
        try {
            String name = getString(R.string.name) .concat(" ").concat( bnfJsonObject.getString("firstName"));
            String identityNo = getString(R.string.IdNoColon) .concat(" ").concat( bnfJsonObject.getString("identityNo"));
            String gender = getString(R.string.gender) + " : " + (bnfJsonObject.getString("gender").equalsIgnoreCase("M") ? getString(R.string.male) : getString(R.string.female));
            String cardNumber = getString(R.string.CardNumber) + " : " + bnfJsonObject.getString("cardNumber");
            String dateOfBirth = getString(R.string.date_of_birth) + " : " + CalenderUtils.formatByLocale(bnfJsonObject.getString("dateOfBirth"), CalenderUtils.DATE_FORMAT, Locale.getDefault());
            tv_name.setText(name);
            tv_idno.setText(identityNo);
            tv_gender.setText(gender);
            tv_dob.setText(dateOfBirth);
            tv_cardno.setText(cardNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void scanQr() {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setPrompt("Scan QRcode");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    public void cardActivationSuccess() {
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //createLog("QR code scanned");

        if (result != null && result.getContents() != null) {
            cardActivationMvpPresenter.findBeneficiary(result.getContents());
        } else {
            showMessage(R.string.qrerror);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (dialog != null && nfcAdapter != null) {
            dialog.dismiss();
            dialog = null;
            nfcReader.setIntent(intent);
            cardActivationMvpPresenter.doActivateCard();
        }
    }

    @Override
    protected void onDestroy() {
        cardActivationMvpPresenter.onDetach();
        nfcReader.closeNfcReader();
        super.onDestroy();
    }
}
