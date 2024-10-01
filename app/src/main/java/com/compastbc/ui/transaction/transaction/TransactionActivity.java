package com.compastbc.ui.transaction.transaction;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.compastbc.R;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.beneficiary.BeneficiaryActivity;
import com.compastbc.ui.reports.submit_report.SubmitTransactionReportActivity;
import com.compastbc.ui.transaction.beneficiary_fp_verification.TransactionBeneficiaryVerification;
import com.compastbc.ui.transaction.transaction.cart.CartActivity;
import com.compastbc.ui.transaction.pin.TransactionPinFragment;
import com.compastbc.ui.transaction.transaction.vouchers.VouchersActivity;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class TransactionActivity extends BaseActivity implements TransactionMvpView, TransactionProgramAdapter.ItemClickListener, NFCVerifyCallback {

    private static final String TAG = "TransactionActivity";
    private TextView tvtapCard;
    private ViewGroup frameView;
    private NfcAdapter nfcAdapter;
    private RecyclerView recyclerView;
    private JSONObject object;
    private NFCReader nfcReader;
    private boolean nfcFlag = true;
    private TransactionMvpPresenter<TransactionMvpView> transactionMvpPresenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, TransactionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        transactionMvpPresenter = new TransactionPresenter<>(getActivity(), getDataManager());
        transactionMvpPresenter.onAttach(this);
        if (verifyDeviceModel(AppConstants.listOfSupportedDevices)) {
            NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
            assert manager != null;
            nfcAdapter = manager.getDefaultAdapter();
        }
        setUp();
    }


    @Override
    protected void setUp() {
        getDataManager().getDaoSession().getPurchasedProductsDao().deleteAll();
        nfcReader = NFCReader.getInstance(this);
        nfcReader.onNfcStatusListener("Transaction", this);
        findIds();
        tvtapCard.setVisibility(View.VISIBLE);

        if (!getDataManager().getConfigurableParameterDetail().isOnline()) {
            long count = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(
                    TransactionsDao.Properties.Submit.eq("0"),
                    TransactionsDao.Properties.Date.lt(CalenderUtils.getTimestamp(CalenderUtils.DATE_FORMAT))).count();
            if (count != 0) {
                sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.txnShouldSubmit).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    startActivity(SubmitTransactionReportActivity.getStartIntent(TransactionActivity.this));
                    finish();
                }).show();
            }
        }
    }

    private void findIds() {
        TextView tvTiltle = findViewById(R.id.tvTitle);
        tvtapCard = findViewById(R.id.tapCard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        frameView = findViewById(R.id.pinFragment);
        recyclerView = findViewById(R.id.txnRecyclerView);

        setSupportActionBar(toolbar);
        tvTiltle.setText(R.string.Transactions);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> {
            createLog("Transaction Activity", "Back");
            finish();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (nfcFlag) {
            nfcReader.setIntent(intent);
            if (transactionMvpPresenter != null)
                transactionMvpPresenter.readCardDetails();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcFlag) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{}, null);
        }
    }

    //show input pin layout
    @Override
    public void showPinView(String cardPin, List<Programs> programmesList, JSONObject programData) {
        hideLoading();
        object = programData;
        tvtapCard.setVisibility(View.GONE);
        frameView.setVisibility(View.VISIBLE);
        addFragment(TransactionPinFragment.newInstance(new TransactionPinFragment.OnTransactionPinListener() {
            @Override
            public void onSuccess() {
                createLog("Transaction Activity ", "Pin Success");
                frameView.setVisibility(View.GONE);
                showProgramList(programmesList);
            }

            @Override
            public void onFailure() {
                showMessage(R.string.InvalidPassword);
            }
        }, cardPin), R.id.pinFragment, false);
        nfcFlag = false;
    }

    @Override
    public void showBiometricView(List<Programs> programmesList, List<String> fps, JSONObject programData) {
        hideLoading();
        object = programData;
        tvtapCard.setVisibility(View.GONE);
        frameView.setVisibility(View.VISIBLE);
        addFragment(TransactionBeneficiaryVerification.newInstance(fps, getDataManager().getTopupDetails().getBeneficiaryName(), () -> {
            frameView.setVisibility(View.GONE);
            createLog("Transaction Activity", " Verify Biometric");
            showProgramList(programmesList);
        }), R.id.pinFragment, false);
        nfcFlag = false;
    }

    //show list of programs
    @Override
    public void showProgramList(List<Programs> programmesList) {
        frameView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        TransactionProgramAdapter adapter = new TransactionProgramAdapter(programmesList,this);
        adapter.setClickListener(TransactionActivity.this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void openBeneficiaryActivity() {
        startActivity(BeneficiaryActivity.getStartIntent(this));
        finish();
    }

    @Override
    public void openCartActivity() {
        startActivity(new Intent(TransactionActivity.this, CartActivity.class));
        finish();
    }

    @Override
    public void onClick(List<Integer> purchasedItemIds, int programId) {
        transactionMvpPresenter.setProgramId(programId);
        createLog("Transaction Activity", "Commodity Selected");
        if (!getDataManager().getConfigurableParameterDetail().isOnline()) {
                try {
                    JSONArray programs = object.getJSONArray("programs");
                    for (int i = 0; i < programs.length(); i++) {
                        if (programs.getJSONObject(i).getString("programmeid").equalsIgnoreCase(String.valueOf(programId))) {
                            List<com.compastbc.core.data.db.model.Topups> topups = getDataManager().getDaoSession().getTopupsDao().queryBuilder()
                                    .where(TopupsDao.Properties.CardNumber.eq(object.getString("cardno")),
                                            TopupsDao.Properties.ProgrammeId.eq(programId))
                                    .whereOr(TopupsDao.Properties.StartDate.lt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                                            TopupsDao.Properties.StartDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)))
                                    .whereOr(TopupsDao.Properties.EndDate.gt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                                            TopupsDao.Properties.EndDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT))).list();
                            if (!topups.isEmpty()) {
                                try {
                                    createLog("Purchase Fragment", "Program Selected");
                                    Programs programs1 = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(String.valueOf(programId))).unique();
                                    if (programs1 != null)
                                        getDataManager().setCurrency(programs1.getProgramCurrency());
                                    Topups topup = getDataManager().getTopupDetails();
                                    topup.setProgrammeid(String.valueOf(programId));
                                    topup.setVouchervalue(programs.getJSONObject(i).getString("vouchervalue"));
                                    topup.setVocheridno(programs.getJSONObject(i).getString("voucherno"));
                                    topup.setVoucherid(programs.getJSONObject(i).getString("voucherid"));

                                    Type listType = new TypeToken<List<Integer>>() {
                                    }.getType();
                                    topup.setPurchasedIds(getDataManager().getGson().fromJson(programs.getJSONObject(i).getJSONArray("purchasedItemId").toString(), listType));
                                    topup.setIndex(i);
                                    getDataManager().setTopupDetails(topup);
                                } catch (Exception e) {
                                    uploadExceptionData(programs.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                                    AppLogger.i(TAG, e.toString());
                                }
                            startActivity(new Intent(TransactionActivity.this, VouchersActivity.class).putExtra("VOUCHERVALUE", programs.getJSONObject(i).getString("vouchervalue")));
                            finish();
                               // transactionMvpPresenter.setProducts(String.valueOf(programId), purchasedItemIds);
                            } else {
                                try {
                                    String progName = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(String.valueOf(programId))).unique().getProgramName();
                                    List<com.compastbc.core.data.db.model.Topups> tps = getDataManager().getDaoSession().getTopupsDao().queryBuilder().where(TopupsDao.Properties.ProgrammeId.eq(String.valueOf(programId))).list();
                                    if (!tps.isEmpty()) {
                                        showMessage(getString(R.string.alert), getString(R.string.youCantDoTxn).concat(" ").concat(progName).concat(" ").concat(getString(R.string.programNotEnable)).concat(" ")
                                                .concat(CalenderUtils.formatDateUTC(tps.get(0).getStartDate().toString(), CalenderUtils.SERVER_DATE_FORMAT, CalenderUtils.DATE_FORMAT, Locale.getDefault()))
                                                .concat(" ")
                                                .concat(getString(R.string.to))
                                                .concat(" ")
                                                .concat(CalenderUtils.formatDateUTC(tps.get(0).getEndDate().toString(), CalenderUtils.SERVER_DATE_FORMAT, CalenderUtils.DATE_FORMAT, Locale.getDefault())));
                                    } else {
                                        showMessage(getString(R.string.alert), getString(R.string.noTopupForThisProgram));
                                    }
                                } catch (Exception e) {
                                    uploadExceptionData("", Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                                    showMessage(getString(R.string.alert), e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    uploadExceptionData("", Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                    AppLogger.i(TAG, e.toString());
                }
        } else {
            Intent i = VouchersActivity.getStartIntent(TransactionActivity.this);
            startActivity(i);
            finish();
        }

    }

/*
    private boolean validateTxn(List<Integer> purchasedItemIds,int programId) {
        double ttlCapacity = 0;
        List<ServiceDetails> serviceDetail = getDataManager().getDaoSession().getServiceDetailsDao().queryBuilder().where(ServiceDetailsDao.Properties.ProgramId.eq(programId)).list();
        for (ServiceDetails details : serviceDetail ){
            if (!purchasedItemIds.contains(Integer.parseInt(details.getServiceId()))) {
                Services services = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.ServiceId.eq(details.getServiceId())).limit(1).unique();
                Services bean = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.ServiceId.eq(details.getServiceId()), ServicesDao.Properties.MaxQuantity.lt(services.getMaxQuantityBenf())).limit(1).unique();
                if (bean == null) {
                    return true;
                } else ttlCapacity += services.getMaxQuantity();
            }
        }

        if (ttlCapacity <= 0 && purchasedItemIds.size() == 0) {
            showMessage(getString(R.string.alert), getString(R.string.alert_txn_capacity));
            return false;
        } else {
            showMessage(getString(R.string.alert), getString(R.string.txn_already_done));
            return false;
        }
    }
*/

    @Override
    protected void onDestroy() {
        transactionMvpPresenter.onDetach();
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
        transactionMvpPresenter.readCardDetails();
    }
}
