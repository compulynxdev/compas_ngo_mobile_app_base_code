package com.compastbc.ui.voidtransaction;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.NFCCardData;
import com.compastbc.core.data.db.model.NFCCardDataDao;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;
import com.compastbc.core.data.db.model.TransactionListProducts;
import com.compastbc.core.data.db.model.TransactionListProductsDao;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.nfcprint.nfc.NFCListener;
import com.compastbc.nfcprint.nfc.NFCReadDataListener;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoidTransactionPresenter<V extends VoidTransactionMvpView> extends BasePresenter<V>
        implements VoidTransactionMvpPresenter<V> {

    private final Activity activity;
    private final NFCReader nfcReader;
    private Transactions transactions;
    //private String cardNo = "";
    private String vouchervalue = "0";
    private int index = -1;
    private final String TAG = "Void Transaction Presenter";

    VoidTransactionPresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        this.activity = activity;
        nfcReader = NFCReader.getInstance(activity);
    }

  /*  @Override
    public void readCardDetails() {
        nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                doNextProcess(data);
            }

            @Override
            public void onFail(String TAG, String msg) {
                //not required at moment
            }
        }, true);
    }

    private void doNextProcess(String response) {
        try {
            if (!response.isEmpty()) {
                getMvpView().showLoading();
                JSONObject object = new JSONObject(response);
                String cardNo = object.getString("cardNo");
                if (cardNo != null) {
                    if (getDataManager().getConfigurableParameterDetail().getOnline())
                        getLastTransaction(cardNo);
                    else {
                       // processLocalData();
                    }
                } else {
                    getMvpView().hideLoading();
                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.cardNotActivated).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                            readCardDetails();
                        }
                    }));
                }
            } else {
                getMvpView().hideLoading();
                getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.cardNotActivated).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardDetails();
                    }
                }));
            }
        }catch (Exception e){
            getMvpView().hideLoading();
            getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                    readCardDetails();
                }
            }));
        }
    }*/

/*
    private void processLocalData() {
        nfcReader.doReadCardByList(new NFCReadListDataListener() {

            @Override
            public void onSuccessRead(List<String> listData) {
                getMvpView().hideLoading();
                if (listData != null) {
                    try {
                        JSONObject card_pin = new JSONObject(listData.get(0));
                        jsonData = new JSONObject(listData.get(1));
                        if (jsonData.has("programs")) {
                            JSONArray programs = jsonData.getJSONArray("programs");
                            //old query
                            //List<Transactions> txns = Transactions.find(Transactions.class, "cardno =? and transactiontype=? and submit=?", card_pin.getString("cardNo"), "0", "0");
                            txns = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.CardNo.eq(card_pin.getString("cardNo")),
                                    TransactionsDao.Properties.TransactionType.eq("0"), TransactionsDao.Properties.Submit.eq("0")).list();
                            if (!txns.isEmpty()) {
                                cardNo = card_pin.getString("cardNo");
                                if (txns.get(txns.size() - 1).getDeviceId().equalsIgnoreCase(getDataManager().getDeviceId())) {
                                    for (int i = 0; i < programs.length(); i++) {
                                        if (programs.getJSONObject(i).getString("programmeid").equalsIgnoreCase(txns.get(txns.size() - 1).getProgramId())) {
                                            vouchervalue = programs.getJSONObject(i).getString("vouchervalue");
                                            index = i;
                                        }
                                    }
                                    vouchervalue = String.valueOf(Float.parseFloat(vouchervalue) + Float.parseFloat(txns.get(txns.size() - 1).getTotalAmountChargedByRetail()));
                                    JSONObject object;
                                    object = programs.getJSONObject(index);
                                    object.put("vouchervalue", vouchervalue);
                                    programs.put(index, object);
                                    jsonData.put("programs", programs);

                                    getMvpView().showDetails(txns);
                                } else {
                                    getMvpView().showMessage(R.string.alert, R.string.nallowedtxn);
                                }

                            } else {
                                getMvpView().showMessage(R.string.alert, R.string.no_txns);
                            }
                        } else {
                            getMvpView().showMessage(R.string.alert, R.string.no_txns);
                        }
                    } catch (Exception e) {
                        getMvpView().showMessage(R.string.error, R.string.card_error_data);
                    }
                } else {
                    getMvpView().showMessage(R.string.cardNotActivated);
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                //nothing to do here
            }
        }, true, NFCReader.CARD_PIN, NFCReader.CARD_DATA);
    }
*/

    @Override
    public void getLastTransaction(String receipt) {
        try {
            JSONObject object = new JSONObject();
            object.put("receiptNo", receipt);
            object.put("locationId", getDataManager().getUserDetail().getLocationId());
            object.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
            object.put("macAddress", getDataManager().getDeviceId());
            RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());

            if (getMvpView().isNetworkConnected()) {
                getDataManager().getLastTransaction("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                        if (response.code() == 200) {
                            try {
                                assert response.body() != null;
                                JSONObject object = new JSONObject(response.body().string());
                                getMvpView().hideLoading();
                                getMvpView().showDetails(object);
                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                /* if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }*/
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                            }
                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            try {
                                getMvpView().hideLoading();
                                assert response.errorBody() != null;
                                JSONObject object = new JSONObject(response.errorBody().string());

                                /*if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }*/
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), object.getString("message")).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                /* if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }*/
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                            }
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        getMvpView().hideLoading();
                        /* if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                readCardDetails();
                            }*/
                        getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.error), t.getMessage() != null && t.getMessage().isEmpty() ? activity.getString(R.string.ServerError) : t.getMessage()).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                    }
                });
            }

        } catch (Exception e) {
            getMvpView().hideLoading();
            getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
        }
    }

    @Override
    public void setLastTransaction(String receiptNo) {
        nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                getMvpView().hideDialog();
                getMvpView().showLoading();
                if (getDataManager().getConfigurableParameterDetail().getOnline()) {
                    doProcessLastTxn(receiptNo, data);
                } else{
                    if(data!=null && !data.isEmpty()){
                        try {
                            JSONObject cardDetails = new JSONObject(data);
                            if(transactions.getCardNo().equalsIgnoreCase(cardDetails.getString("cardNo"))){
                                doProcessLastTxn(receiptNo);
                            }else{
                                getMvpView().showMessage(activity.getString(R.string.alert),activity.getString(R.string.invalid_card_txn,transactions.getBeneficiaryName()));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        getMvpView().showMessage(R.string.cardNotActivated);
                    }
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideDialog();
                getMvpView().showLoading();
            }
        }, true);
    }

    @Override
    public void getTransaction(String receipt) {
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            getLastTransaction(String.format(Locale.ENGLISH, "%d", Long.parseLong(receipt)));
        } else {
            if (receipt != null && !receipt.isEmpty()) {
                Transactions transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.ReceiptNo.eq(String.format(Locale.ENGLISH, "%d", Long.parseLong(receipt)))
                        , TransactionsDao.Properties.DeviceId.eq(getDataManager().getDeviceId()), TransactionsDao.Properties.TransactionType.eq("0"), TransactionsDao.Properties.Submit.eq("0")).unique();
                if (transactionsList != null) {
                    transactions = transactionsList;
                    getMvpView().showDetails(transactionsList);
                }
                else getMvpView().showMessage(R.string.alert, R.string.noTxns);
            } else {
                getMvpView().showMessage(R.string.error, R.string.pleaseEnterReceiptNo);
            }
        }
    }

    private void doProcessLastTxn(String receiptNo) {
        nfcReader.doReadCardData(NFCReader.CARD_DATA, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                if (data !=null && !data.isEmpty()) {
                    try {
                        JSONObject jsonData = new JSONObject(data);
                        if (jsonData.has("programs")) {
                            // if (cardNo.equalsIgnoreCase(jsonData.getString("cardno"))) {
                             if (transactions != null) {
                                //topup detail
                                Topups topups = new Topups();
                                topups.setCardnumber(transactions.getCardNo());
                                topups.setBeneficiaryName(transactions.getBeneficiaryName());
                                topups.setIdentificationNumber(transactions.getIdentityNo());
                                getDataManager().setTopupDetails(topups);

                                JSONArray programs = jsonData.getJSONArray("programs");
                                List<Integer> purchasedItemIds = new ArrayList<>();
                                for (int i = 0; i < programs.length(); i++) {
                                    if (programs.getJSONObject(i).getString("programmeid").equalsIgnoreCase(transactions.getProgramId())) {
                                        vouchervalue = programs.getJSONObject(i).getString("vouchervalue");
                                        Type listType = new TypeToken<List<Integer>>() {}.getType();
                                        purchasedItemIds = new Gson().fromJson(programs.getJSONObject(i).getJSONArray("purchasedItemId").toString(), listType);
                                        index = i;
                                    }
                                }
                                List<Commodities> commodities = getDataManager().getDaoSession().getCommoditiesDao().queryBuilder().where(CommoditiesDao.Properties.TransactionNo.eq(transactions.getReceiptNo().toString())).list();
                                for (Commodities commodity : commodities){
                                    if (purchasedItemIds.contains(Integer.parseInt(commodity.getProductId()))) {
                                        purchasedItemIds.remove(Integer.valueOf(commodity.getProductId()));
                                    }
                                }

                                if (index != -1) {
                                    vouchervalue = String.valueOf(Float.parseFloat(vouchervalue) + Float.parseFloat(transactions.getTotalAmountChargedByRetail()));
                                    JSONObject object = programs.getJSONObject(index);
                                    object.put("vouchervalue", vouchervalue);
                                    JSONArray array = new JSONArray(purchasedItemIds);
                                    object.put("purchasedItemId", array);
                                    programs.put(index, object);
                                    jsonData.put("programs", programs);
                                    //before writing validate json formation
                                    new JSONObject(jsonData.toString());
                                    doValidateWriteCardData(jsonData,receiptNo,false);
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.noTopupFoundForReceipt)
                                            .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                                }
                            } else {
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.noTxns)
                                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                            }
                        } else {
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.no_topups)
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                        }

                    } catch (Exception e) {
                        getMvpView().hideLoading();
                        getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.card_error_read_data)
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                    }
                } else {
                    getMvpView().hideLoading();
                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.card_read_fail)
                            .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation));
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, false);

    }

    private void doValidateWriteCardData(JSONObject object1,String receipt, boolean beep) {
        getMvpView().showLoading(activity.getString(R.string.title_card_tap));
        nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                if (data != null && !data.isEmpty()) {
                    try {
                        JSONObject object = new JSONObject(data);
                        String cardNo = object.getString("cardNo");
                        if (cardNo.equalsIgnoreCase(getDataManager().getTopupDetails().getCardnumber())) {
                            doWriteCardData(object1,receipt);
                        } else {
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, activity.getString(R.string.invalid_card_txn, getDataManager().getTopupDetails().getBeneficiaryName())).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    doValidateWriteCardData(object1, receipt,true);
                                }
                            }));
                        }
                    } catch (Exception e) {
                        getMvpView().hideLoading();
                        getMvpView().uploadExceptionData(object1.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                        getMvpView().showMessage(R.string.card_error_write_data);
                    }
                } else {
                    getMvpView().hideLoading();
                    getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_read_fail).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                            doValidateWriteCardData(object1,receipt, true);
                        }
                    }));
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, beep);
    }

    private void doWriteCardData(JSONObject jsonData,String receipt) {
        nfcReader.doWriteCardData(NFCReader.CARD_DATA, jsonData.toString(), new NFCListener() {
            @Override
            public void onSuccess(int flag) {
                if (flag == 0) {
                    deleteCardDataAfterSuccess();
                    saveVoidData(receipt);
                } else {
                    getMvpView().hideLoading();
                    saveCardDataForRestore(jsonData);
                    getMvpView().showMessage(R.string.error, R.string.unableWrite);
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, false);
    }

    private void deleteCardDataAfterSuccess() {
        NFCCardData nfcCardBean = getDataManager().getDaoSession().getNFCCardDataDao().queryBuilder().where(NFCCardDataDao.Properties.CardNumber.eq(getDataManager().getTopupDetails().getCardnumber())).unique();
        if (nfcCardBean != null) {
            getDataManager().getDaoSession().getNFCCardDataDao().delete(nfcCardBean);
        }
    }

    private void saveCardDataForRestore(JSONObject object1) {
        NFCCardData nfcCardData = new NFCCardData();
        nfcCardData.setCardID(getDataManager().getTopupDetails().getIdentificationNumber());
        nfcCardData.setCardNumber(getDataManager().getTopupDetails().getCardnumber());
        nfcCardData.setCreatedDate(new Date());
        nfcCardData.setCardJsonObjData(object1.toString());
        getDataManager().getDaoSession().getNFCCardDataDao().save(nfcCardData);
    }

    //save void data to db
    private void saveVoidData(String receipt) {

        Transactions transactions = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.ReceiptNo.eq(Long.parseLong(receipt))
                , TransactionsDao.Properties.CardNo.eq(getDataManager().getTopupDetails().getCardnumber()), TransactionsDao.Properties.DeviceId.eq(getDataManager().getDeviceId()), TransactionsDao.Properties.TransactionType.eq("0"), TransactionsDao.Properties.Submit.eq("0")).unique();

      if(transactions!=null){
          TransactionListProductsDao transactionListProductsDao = getDataManager().getDaoSession().getTransactionListProductsDao();
          List<TransactionListProducts> transactionsListProducts = transactionListProductsDao.queryBuilder().where(TransactionListProductsDao.Properties.TransactionNo.eq(transactions.getReceiptNo().toString())).list();
          CommoditiesDao commoditiesDao = getDataManager().getDaoSession().getCommoditiesDao();
          ServicesDao servicesDao = getDataManager().getDaoSession().getServicesDao();
          List<Commodities> commodities = commoditiesDao.queryBuilder().where(CommoditiesDao.Properties.TransactionNo.eq(transactions.getReceiptNo().toString())).list();
          for (int i = 0; i < transactionsListProducts.size(); i++) {
              transactionsListProducts.get(i).setVoidTransaction("-1");
              transactionListProductsDao.save(transactionsListProducts.get(i));
          }

          for (int i = 0; i < commodities.size(); i++) {
              Commodities commodityBean = commodities.get(i);
              commodityBean.setVoidTransaction("-1");

              //add void txn capacity to service
              Services serviceBean = servicesDao.queryBuilder().where(ServicesDao.Properties.ServiceId.eq(commodityBean.getProductId())).unique();
              if (serviceBean != null) {
                  double maxQty = serviceBean.getMaxQuantity() + Double.parseDouble(commodityBean.getQuantityDeducted());
                  serviceBean.setMaxQuantity(maxQty);
                  servicesDao.update(serviceBean);
              }
              commoditiesDao.save(commodities.get(i));
          }
          transactions.setTransactionType("-1");
          double addVoidAmount = Double.parseDouble(transactions.getTotalAmountChargedByRetail()) + Double.parseDouble(transactions.getTotalValueRemaining());
          transactions.setTotalValueRemaining(String.format(Locale.ENGLISH, "%.2f", addVoidAmount));
          getDataManager().getDaoSession().getTransactionsDao().save(transactions);
          getMvpView().hideLoading();
          getMvpView().show(getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.SuccessVoid).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
              sweetAlertDialog.dismissWithAnimation();
              getMvpView().openNextActivity();
          }));
      }
    }

    private void doProcessLastTxn(String receiptNo, String res) {
        try {
            if (res != null) {
                JSONObject object = new JSONObject(res);
                JSONObject data = new JSONObject();
                data.put("cardNo", object.getString("cardNo"));
                data.put("receiptNo", receiptNo);
                data.put("transactionType", -1);
                RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, data.toString());
                if (getMvpView().isNetworkConnected()) {
                    getDataManager().setLastTransaction("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                try {
                                    getMvpView().hideLoading();

                                    getMvpView().show(getMvpView().sweetAlert(2, R.string.success, R.string.SuccessVoid).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().openNextActivity();
                                    }));

                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(e.getMessage());
                                }
                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                try {
                                    getMvpView().hideLoading();
                                    assert response.errorBody() != null;
                                    JSONObject object = new JSONObject(response.errorBody().string());
                                    getMvpView().show(getMvpView().sweetAlert(1, activity.getString(R.string.error), object.getString("message")).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().openNextActivity();
                                    }));
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            getMvpView().hideLoading();
                            handleApiFailure(call, t);
                        }
                    });
                }
            } else {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.error, R.string.card_error_read_data);
            }

        } catch (Exception e) {
            e.printStackTrace();
            getMvpView().hideLoading();
            getMvpView().showMessage(e.getMessage());
        }
    }
}
