package com.compastbc.ui.transaction.transaction.cart;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.network.model.TransactionReceipt;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.nfcprint.print.OnPrinterInteraction;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.MainActivity;
import com.compastbc.ui.transaction.transaction.services.ServiceActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CartActivity extends BaseActivity implements CartMvpView, OnPrinterInteraction, NFCVerifyCallback {

    private TextView tv_items, total;
    private RecyclerView listItems;
    private NfcAdapter nfcAdapter;
    private boolean submit;
    private NFCReader nfcReader;
    private TransactionReceipt transactionReceipt;
    private boolean vendorReceipt = true;
    private CartMvpPresenter<CartMvpView> mvpPresenter;
    private List<PurchasedProducts> products = new ArrayList<>();
    private MaterialDialog dialog;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CartActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
        submit = false;
        mvpPresenter = new CartPresenter<>(getActivity(), getDataManager());
        mvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        findIds();
        nfcReader = NFCReader.getInstance(this);
        setData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        nfcReader.setIntent(intent);
        if (submit && mvpPresenter != null) {
            submit = false;
            mvpPresenter.readCardDetails();
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
        TextView tvTitle = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvTitle.setText(R.string.cart);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        //back
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_items = findViewById(R.id.tv_items);
        total = findViewById(R.id.total);
        Button btn_submit = findViewById(R.id.submit);
        listItems = findViewById(R.id.items);

        btn_submit.setOnClickListener(v -> {
            btn_submit.setEnabled(false);
            createLog("Cart Activity", "Submit Selected");
            if (products.isEmpty()) {
                showMessage(R.string.alert_cart_empty);
            } else {
                submit = true;
                dialog = materialDialog(R.string.Transactions, R.string.card_tap_purchase);
                dialog.show();
                nfcReader.onNfcStatusListener("Cart", this);
            }

            new Handler().postDelayed(() -> btn_submit.setEnabled(true), 5000);
        });
    }

    @Override
    public void show(String price, String qty) {
        tv_items.setText(getString(R.string.noitems).concat(" ").concat(String.format(Locale.getDefault(), "%d", products.size())));
        total.setText(getString(R.string.totalPrice).concat(" ").concat(getDataManager().getCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(price))));
        CartAdapter adapter = new CartAdapter(getActivity(), products);
        listItems.setAdapter(adapter);
    }

    @Override
    public void Update(String id) {
        createLog("Cart Activity", "Item Removed");
        if (mvpPresenter != null)
            mvpPresenter.Update(Long.parseLong(id));
    }

    @Override
    public void setData() {
        products = getDataManager().getDaoSession().getPurchasedProductsDao().queryBuilder().list();
        mvpPresenter.getData(products);

    }

    @Override
    public void openNextActivity() {
        Intent i = MainActivity.getStartIntent(CartActivity.this);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    public void openServices() {
        Intent i = ServiceActivity.getStartIntent(CartActivity.this);
        startActivity(i);
    }

    @Override
    public void hideDialog() {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void print(TransactionReceipt receipt, boolean vendorReceipt) {
        showLoading();
        this.vendorReceipt = vendorReceipt;
        this.transactionReceipt = receipt;
        getPrintUtil(new ReportPrintCallback() {
            @Override
            public void onNavigateNextController() {
                hideLoading();
                openNextActivity();
            }

            @Override
            public void onSuccess(PrintServices printServices) {
                printReceipt(printServices, receipt, vendorReceipt);
            }

            @Override
            public void onPrintPairError() {
                hideLoading();
                show(sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.PrinterNotPaired).
                        setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            openNextActivity();
                        }));
            }

            @Override
            public void onPrintError(Exception e) {
                hideLoading();
                sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.PrinterError).concat("<br>").concat(e.toString())).
                        setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            openNextActivity();
                        }).show();
            }
        });
    }

    @Override
    public void printReceipt(PrintServices printUtils, TransactionReceipt receipt, boolean vendorReceipt) {
        if (receipt != null) {
            createLog("Cart Activity", "Transaction receipt print");
            //if print vendor receipt or beneficiary receipt
            if (vendorReceipt)
                printUtils.printVendorTransactionReceipt(receipt, this);
            else printUtils.printBeneficiaryTransactionReceipt(receipt, this);
        } else {
            sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.NoDataToPrint).
                    setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        openNextActivity();
                    }).show();
        }
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
        if (mvpPresenter != null) {
            mvpPresenter.readCardDetails();
        }
    }

    @Override
    public void onSuccess(String TAG) {
        switch (TAG) {
            case PrintServices.VENDOR_RECEIPT_PRINTED:
                hideLoading();
                sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.VendorReceiptPrinted).
                        setConfirmButton(R.string.yes, sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            print(transactionReceipt, false);
                        }).setCancelButton(R.string.no, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    openNextActivity();
                }).show();
                break;

            case PrintServices.PRINT_SUCCESSFULLY:
                hideLoading();
                sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.PrintedSuccessfully).
                        setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            openNextActivity();
                        }).show();
                break;
        }
    }

    @Override
    public void onFail(String TAG) {
        hideLoading();
        sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.PrinterError).
                setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    openNextActivity();
                }).show();
    }

    @Override
    public void onPrintStatusBusy() {
        hideLoading();
        sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.print_status_busy).
                setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    openNextActivity();
                }).show();
    }

    @Override
    public void onPrintStatusHighTemp() {
        hideLoading();
        sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.print_status_high_temp).
                setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    openNextActivity();
                }).show();
    }

    @Override
    public void onPrintStatusPaperLack() {
        hideLoading();
        sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.print_status_paper_lack).
                setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    print(transactionReceipt, vendorReceipt);
                }).show();
    }

    @Override
    public void onPrintStatusNoBattery() {
        hideLoading();
        sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.print_status_no_battery).
                setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    openNextActivity();
                }).show();
    }
}
