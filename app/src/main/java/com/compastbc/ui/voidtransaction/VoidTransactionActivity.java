package com.compastbc.ui.voidtransaction;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.ui.base.BaseActivity;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class VoidTransactionActivity extends BaseActivity implements VoidTransactionMvpView, NFCVerifyCallback, View.OnClickListener {

    private Toolbar toolbar;
    private CardView cardView;
    private boolean isVoid = false;
    private NfcAdapter nfcAdapter;
    private EditText etReceipt;
    private TextView tvTitle, identityNo, receiptNo, date, value;
    //private TextView tapCard;
    private TextInputLayout input_layout;
    private Button btnSearch;
    private Button btn_void, btnCancel;
    private String streceiptNo;
    private MaterialDialog dialog;
    private NFCReader nfcReader;
    //private boolean isDataVisible = false;
    private VoidTransactionMvpPresenter<VoidTransactionMvpView> presenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, VoidTransactionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_transaction);
        presenter = new VoidTransactionPresenter<>(getActivity(), getDataManager());
        presenter.onAttach(this);
        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
        setUp();
    }

    @Override
    protected void setUp() {
        nfcReader = NFCReader.getInstance(this);
        nfcReader.onNfcStatusListener("VoidTransaction", this);
        findIds();
        setSupportActionBar(toolbar);
        tvTitle.setText(R.string.VoidTransaction);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
        btnSearch.setOnClickListener(this);
        btn_void.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        nfcReader.setIntent(intent);
        doProcessCardData();
    }

    private void doProcessCardData() {
        if (presenter != null) {
            /*if (firstTimeTap && !isDataVisible)
                presenter.readCardDetails();
            else if (!firstTimeTap) {
                dialog.dismiss();
                presenter.setLastTransaction(streceiptNo);
            }*/
            if (isVoid) {
                if (dialog != null)
                    dialog.dismiss();
                presenter.setLastTransaction(streceiptNo);
            }
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
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{}, null);
        }
    }

    private void findIds() {
        toolbar = findViewById(R.id.toolbar);
        cardView = findViewById(R.id.cardView);
        tvTitle = findViewById(R.id.tvTitle);
        identityNo = findViewById(R.id.identityNo);
        receiptNo = findViewById(R.id.receiptNo);
        date = findViewById(R.id.date);
        value = findViewById(R.id.value);
        //tapCard=findViewById(R.id.tapCard);
        btnSearch = findViewById(R.id.btn_search);
        etReceipt = findViewById(R.id.et_receipt);
        input_layout = findViewById(R.id.input_layout);
        btnCancel = findViewById(R.id.cancel_button);
        btn_void = findViewById(R.id.void_button);
    }

    @Override
    public void showDetails(JSONObject object) {
        try {
            input_layout.setVisibility(View.GONE);
            btnSearch.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            identityNo.setText(object.getString("rationNo"));
            streceiptNo = object.getString("receiptNo");
            receiptNo.setText(String.format(Locale.getDefault(), "%d", Long.parseLong(streceiptNo)));
            date.setText(CalenderUtils.formatTimestamp(object.getLong("timeStamp"), CalenderUtils.DB_TIMESTAMP_FORMAT));
            value.setText(String.format(Locale.getDefault(), "%S %.2f", object.getString("programCurrency"), Double.parseDouble(object.getString("getAmountCharged"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //isDataVisible = true;
    }

    @Override
    public void showDetails(Transactions transactions) {
        try {
            input_layout.setVisibility(View.GONE);
            btnSearch.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            identityNo.setText(transactions.getIdentityNo());
            streceiptNo = transactions.getReceiptNo().toString();
            receiptNo.setText(String.format(Locale.getDefault(), "%d", Long.parseLong(streceiptNo)));
            value.setText(transactions.getProgramCurrency().concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(transactions.getTotalAmountChargedByRetail()))));
            date.setText(CalenderUtils.formatByLocale(transactions.getTimeStamp(), CalenderUtils.DB_TIMESTAMP_FORMAT, Locale.getDefault()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //isDataVisible = true;
    }

    @Override
    public void openNextActivity() {
        onBackPressed();
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
        doProcessCardData();
    }

    @Override
    public void hideDialog() {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        nfcReader.closeNfcReader();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.void_button:
                isVoid = true;
                dialog = materialDialog(R.string.VoidTransaction, R.string.tapToVoid);
                dialog.show();
                presenter.setLastTransaction(streceiptNo);
                break;


            case R.id.btn_search:
                hideKeyboard();
                presenter.getTransaction(etReceipt.getText().toString().trim());
                break;


            case R.id.cancel_button:
                isVoid = false;
                cardView.setVisibility(View.GONE);
                etReceipt.setText("");
                input_layout.setVisibility(View.VISIBLE);
                btnSearch.setVisibility(View.VISIBLE);
                break;

        }
    }
}
