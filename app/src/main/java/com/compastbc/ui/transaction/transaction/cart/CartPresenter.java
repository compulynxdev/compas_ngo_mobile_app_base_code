package com.compastbc.ui.transaction.transaction.cart;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.compastbc.Compas;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.NFCCardData;
import com.compastbc.core.data.db.model.NFCCardDataDao;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.db.model.PurchasedProductsDao;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.db.model.TransactionListProducts;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TxnCount;
import com.compastbc.core.data.db.model.TxnCountDao;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.core.data.network.model.TransactionReceipt;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.nfcprint.nfc.NFCListener;
import com.compastbc.nfcprint.nfc.NFCReadDataListener;
import com.compastbc.nfcprint.nfc.NFCReader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartPresenter<V extends CartMvpView> extends BasePresenter<V>
        implements CartMvpPresenter<V> {

    private final NFCReader nfcReader;
    private final Activity activity;
    private static final String TAG = "CartPresenter";

    CartPresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        this.activity = activity;
        nfcReader = NFCReader.getInstance(activity);
    }

    @Override
    public void getData(List<PurchasedProducts> purchasedProducts) {
        double amount = 0.0;
        double quantity = 0.0;
        for (int i = 0; i < purchasedProducts.size(); i++) {
            amount = amount + Double.parseDouble(purchasedProducts.get(i).getTotalPrice());
            quantity = quantity + Double.parseDouble(purchasedProducts.get(i).getQuantity());
        }
        getMvpView().show(String.valueOf(amount), String.valueOf(quantity));
    }

    @Override
    public void Update(Long id) {
        PurchasedProducts products = getDataManager().getDaoSession().getPurchasedProductsDao().queryBuilder().where(PurchasedProductsDao.Properties.Id.eq(id)).unique();
        Topups topups = getDataManager().getTopupDetails();
        topups.setVouchervalue(String.valueOf(Double.parseDouble(topups.getVouchervalue()) + Double.parseDouble(products.getTotalPrice())));
        getDataManager().getDaoSession().getPurchasedProductsDao().delete(products);
        getDataManager().setTopupDetails(topups);
        getMvpView().setData();
    }

    @Override
    public void readCardDetails() {
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            if (getMvpView().isNetworkConnected()) {
                readData();
            }
        } else {
            readData();
        }
    }


    private void readData() {
        nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                getMvpView().hideDialog();
                doNextProcess(data);
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideDialog();
            }
        }, true);

    }

    private void doNextProcess(String response) {
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject object = new JSONObject(response);
                String cardNo = object.getString("cardNo");
                if (cardNo.equalsIgnoreCase(getDataManager().getTopupDetails().getCardnumber())) {
                    if (getDataManager().getConfigurableParameterDetail().isOnline()) {
                        saveData();
                    } else {
                        nfcReader.doReadCardData(NFCReader.CARD_DATA, new NFCReadDataListener() {
                            @Override
                            public void onSuccess(String data) {
                                try {
                                    JSONObject object1 = new JSONObject(data);
                                    if (object1.length() > 0) {
                                        Topups topups = getDataManager().getTopupDetails();
                                        JSONArray array = object1.getJSONArray("programs");
                                        if (array.length() > 0) {
                                            JSONObject programObject = array.getJSONObject(topups.getIndex());
                                            programObject.put("vouchervalue", topups.getVouchervalue());
                                            JSONArray array1 = new JSONArray(topups.getPurchasedIds());
                                            programObject.put("purchasedItemId", array1);
                                            array.put(topups.getIndex(), programObject);
                                            object1.put("programs", array);

                                            doValidateWriteCardData(object1, false);
                                        }
                                    }
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().uploadExceptionData(object.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                                    getMvpView().showMessage(R.string.unableWrite);
                                }

                            }

                            @Override
                            public void onFail(String TAG, String msg) {
                                getMvpView().hideLoading();
                            }
                        }, false);
                    }

                } else {
                    getMvpView().hideLoading();
                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, activity.getString(R.string.invalid_card_txn, getDataManager().getTopupDetails().getBeneficiaryName())).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                            readCardDetails();
                        }
                    }));
                }
            } catch (Exception e) {
                e.printStackTrace();
                getMvpView().uploadExceptionData("", Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                getMvpView().hideLoading();
                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.error), activity.getString(R.string.card_error_read_data)).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardDetails();
                    }
                }));
            }
        } else {
            getMvpView().hideLoading();
            getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_read_fail).setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                    readCardDetails();
                }
            }));
        }
    }

    private void doWriteCardData(JSONObject object1) {
        nfcReader.doWriteCardData(NFCReader.CARD_DATA, object1.toString(), new NFCListener() {
            @Override
            public void onSuccess(int flag) {
                if (flag == 0) {
                    deleteCardDataAfterSuccess();
                    saveData();
                } else {
                    getMvpView().hideLoading();
                    saveCardDataForRestore(object1);
                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.alert_rewrite).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        doValidateWriteCardData(object1, true);
                    }));
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, false);
    }

    private void doValidateWriteCardData(JSONObject object1, boolean beep) {
        getMvpView().showLoading(activity.getString(R.string.title_card_tap));
        nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                if (data != null && !data.isEmpty()) {
                    try {
                        JSONObject object = new JSONObject(data);
                        String cardNo = object.getString("cardNo");
                        if (cardNo.equalsIgnoreCase(getDataManager().getTopupDetails().getCardnumber())) {
                            doWriteCardData(object1);
                        } else {
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, activity.getString(R.string.invalid_card_txn, getDataManager().getTopupDetails().getBeneficiaryName())).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    doValidateWriteCardData(object1, true);
                                }
                            }));
                        }
                    } catch (Exception e) {
                        getMvpView().hideLoading();
                        getMvpView().uploadExceptionData(object1.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                        //getMvpView().showMessage(e.toString());
                        getMvpView().showMessage(R.string.card_error_write_data);
                    }
                } else {
                    getMvpView().hideLoading();
                    getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_read_fail).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                            doValidateWriteCardData(object1, true);
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

    private void deleteCardDataAfterSuccess() {
        NFCCardData nfcCardBean = getDataManager().getDaoSession().getNFCCardDataDao().queryBuilder().where(NFCCardDataDao.Properties.CardNumber.eq(getDataManager().getTopupDetails().getCardnumber())).unique();
        if (nfcCardBean != null) {
            getDataManager().getDaoSession().getNFCCardDataDao().delete(nfcCardBean);
        }
    }

    private void saveCardDataForRestore(JSONObject object1) {
        NFCCardData nfcCardData = new NFCCardData();
        nfcCardData.setBeneficiaryName(getDataManager().getTopupDetails().getBeneficiaryName());
        nfcCardData.setCardID(getDataManager().getTopupDetails().getIdentificationNumber());
        nfcCardData.setCardNumber(getDataManager().getTopupDetails().getCardnumber());
        nfcCardData.setCreatedDate(new Date());
        nfcCardData.setCardJsonObjData(object1.toString());
        getDataManager().getDaoSession().getNFCCardDataDao().save(nfcCardData);
    }

    @Override
    public void saveData() {
        TransactionReceipt receipt = new TransactionReceipt();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            try {
                JSONObject transaction = new JSONObject();
                JSONArray commodities = new JSONArray();
                JSONArray transactionListProduct = new JSONArray();
                JSONObject transListObject = new JSONObject();
                transListObject.put("masterDeviceId", getDataManager().getDeviceId());
                Topups topups = getDataManager().getTopupDetails();
                transaction.put("voucher", topups.getVocheridno());
                transaction.put("transaction_type", "0");
                transaction.put("cancelled_transaction", 0);
                transaction.put("rationNo", topups.getIdentificationNumber());
                String id = getDataManager().getUserDetail().getAgentId();
                String strNo;
                if (id == null || id.isEmpty()) {
                    int randomNo = new Random().nextInt(1000);
                    strNo = String.valueOf(new Date().getTime()).concat(String.valueOf(randomNo));
                } else {
                    strNo = String.valueOf(new Date().getTime()).concat(id);
                }
                Long receiptNo = Long.parseLong(strNo);
                transaction.put("receipt_number", receiptNo);
                transaction.put("value_remaining", topups.getVouchervalue());
                transaction.put("user", getDataManager().getUserDetail().getUser());
                transaction.put("locationId", getDataManager().getUserDetail().getLocationId());
                transaction.put("cardNumber", topups.getCardnumber());
                transaction.put("timestamp_transaction_created", CalenderUtils.getDateTime(CalenderUtils.DB_TIMESTAMP_FORMAT, Locale.US));
                transaction.put("programId", topups.getProgrammeid());
                transaction.put("agentId", getDataManager().getUserDetail().getAgentId());
                transaction.put("latitude", getDataManager().getUserDetail().getLatitude());
                transaction.put("startDate", Long.parseLong(topups.getStartDate()));
                transaction.put("endDate", Long.parseLong(topups.getEndDate()));
                transaction.put("programCurrency", topups.getProgramCurrency());
                transaction.put("longitude", getDataManager().getUserDetail().getLongitude());
                transaction.put("authentication_type", 0);
                transaction.put("pos_terminal", getDataManager().getDeviceId());
                List<PurchasedProducts> products = getDataManager().getDaoSession().getPurchasedProductsDao().queryBuilder().list();
                double amount = 0.0;
                for (int i = 0; i < products.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("pos_commodity", products.get(i).getServiceId());
                    object.put("uom", products.get(i).getUom());
                    object.put("maxPrice",products.get(i).getMaxPrice());
                    String uniqueId;
                    if (id == null || id.isEmpty()) {
                        int randomNo = new Random().nextInt(100);
                        uniqueId = String.valueOf(new Date().getTime()).concat(String.valueOf(randomNo)).concat(String.valueOf(i));
                    } else {
                        uniqueId = String.valueOf(new Date().getTime()).concat(id).concat(String.valueOf(i));
                    }
                    object.put("uniqueId", Long.parseLong(uniqueId));
                    amount = amount + Double.parseDouble(products.get(i).getTotalPrice());
                    object.put("transactionNo", receiptNo.toString());
                    object.put("quantity_remaining", "0");
                    object.put("amount_charged_by_retailer", products.get(i).getTotalPrice());
                    object.put("deducted_quantity", products.get(i).getQuantity());
                    commodities.put(object);
                    JSONObject obj = new JSONObject();
                    obj.put("serviceId", Integer.parseInt(products.get(i).getServiceId()));
                    obj.put("uom", products.get(i).getUom());
                    obj.put("quantity", products.get(i).getQuantity());
                    obj.put("value", products.get(i).getTotalPrice());
                    obj.put("transactiono", receiptNo.toString());
                    obj.put("deviceId", getDataManager().getDeviceId());
                    obj.put("programmeid", Integer.parseInt(topups.getProgrammeid()));
                    obj.put("transactionDate", CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US));
                    transactionListProduct.put(obj);
                }
                transaction.put("total_amount_charged_by_retailer", String.valueOf(amount));
                transaction.put("commodities", commodities);
                transListObject.put("transList", transactionListProduct);
                JSONObject totalObject = new JSONObject();
                totalObject.put("transaction", transaction);
                totalObject.put("transactionListProduct", transListObject);

                //printReceiptData
                receipt.setRation(topups.getIdentificationNumber());
                receipt.setReceiptNo(String.valueOf(receiptNo));
                receipt.setCurrentBalance(topups.getVouchervalue());
                receipt.setProgramCurrency(topups.getProgramCurrency());
                receipt.setTxnValue(String.valueOf(amount));
                receipt.setOpeningBal(String.valueOf(Double.parseDouble(topups.getVouchervalue()) + amount));
                receipt.setProductsList(products);

                RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, totalObject.toString());
                getDataManager().uploadTransactions("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            alertSuccess(receipt);
                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            try {
                                getMvpView().hideLoading();
                                assert response.errorBody() != null;
                                JSONObject object = new JSONObject(response.errorBody().string());
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.error), object.getString("message")).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }
                                }));
                            } catch (Exception e) {
                                getMvpView().uploadExceptionData("", Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.error), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }
                                }));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        getMvpView().hideLoading();
                        getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.error), t.getMessage() != null && t.getMessage().isEmpty() ? activity.getString(R.string.ServerError) : t.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                readCardDetails();
                            }
                        }));
                    }
                });

            } catch (Exception e) {
                getMvpView().uploadExceptionData("", Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                getMvpView().hideLoading();
                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.error), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardDetails();
                    }
                }));
                e.printStackTrace();
            }
        } else {
            Topups topups = getDataManager().getTopupDetails();

          /*  com.compastbc.data.db.model.Topups tps = getDataManager().getDaoSession().getTopupsDao().queryBuilder().where(TopupsDao.Properties.ProgrammeId.eq(topups.getProgrammeid()),
                    TopupsDao.Properties.CardNumber.eq(topups.getCardnumber())).list().get(0);*/
            com.compastbc.core.data.db.model.Topups tps = getDataManager().getDaoSession().getTopupsDao().queryBuilder()
                    .where(TopupsDao.Properties.CardNumber.eq(topups.getCardnumber()),
                            TopupsDao.Properties.ProgrammeId.eq(topups.getProgrammeid()))
                    .whereOr(TopupsDao.Properties.StartDate.lt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                            TopupsDao.Properties.StartDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)))
                    .whereOr(TopupsDao.Properties.EndDate.gt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                            TopupsDao.Properties.EndDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT))).limit(1).unique();

            Beneficiary beneficiary = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IdentityNo.eq(topups.getIdentificationNumber())).limit(1).unique();

            Transactions transactions = new Transactions();
            transactions.setAgentId(getDataManager().getUserDetail().getAgentId());
            transactions.setIdentityNo(topups.getIdentificationNumber());
            transactions.setCardNo(topups.getCardnumber());
            transactions.setVoucherId(topups.getVoucherid());
            transactions.setTotalValueRemaining(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(topups.getVouchervalue())));
            transactions.setUser(getDataManager().getUserDetail().getUser());
            transactions.setLocationId(getDataManager().getUserDetail().getLocationId());
            transactions.setProgramId(topups.getProgrammeid());
            Programs programs = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(topups.getProgrammeid())).unique();
            transactions.setProgramName(programs.getProgramName());
            transactions.setProgramCurrency(programs.getProgramCurrency());
            transactions.setDate(CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US));
            transactions.setTimeStamp(CalenderUtils.getTimestamp(CalenderUtils.DB_TIMESTAMP_FORMAT));
            transactions.setTransactionType("0");
            transactions.setIsUploaded("0");
            transactions.setVoucherIdNo(topups.getVocheridno());
            transactions.setDeviceId(getDataManager().getDeviceId());
            String id = getDataManager().getUserDetail().getAgentId();
            String strNo;
            if (id == null || id.isEmpty()) {
                int randomNo = new Random().nextInt(1000);
                strNo = String.valueOf(new Date().getTime()).concat(String.valueOf(randomNo));
            } else {
                strNo = String.valueOf(new Date().getTime()).concat(id);
            }
            Long receiptNo = Long.parseLong(strNo);
            transactions.setReceiptNo(receiptNo);
            if (tps != null) {
                transactions.setTopupEndDate(tps.getEndDate());
                transactions.setTopupStartDate(tps.getStartDate());
            } else {
                transactions.setTopupEndDate(new Date());
                transactions.setTopupStartDate(new Date());
            }
            transactions.setBeneficiaryName(topups.getBeneficiaryName());
            transactions.setLatitude(Compas.LATITUDE);
            transactions.setLongitude(Compas.LONGITUDE);
            double amount = 0.0;
            List<PurchasedProducts> productsList = getDataManager().getDaoSession().getPurchasedProductsDao().queryBuilder().list();
            for (int i = 0; i < productsList.size(); i++) {
                //save commodities
                amount = amount + Double.parseDouble(productsList.get(i).getTotalPrice());
                Services services = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.ServiceId.eq(productsList.get(i).getServiceId())).unique();
                Commodities commodities = new Commodities();
                commodities.setDate(CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT));
                commodities.setTransactionNo(transactions.getReceiptNo().toString());
                String uniqueId;
                if (id == null || id.isEmpty()) {
                    int randomNo = new Random().nextInt(100);
                    uniqueId = String.valueOf(new Date().getTime()).concat(String.valueOf(randomNo)).concat(String.valueOf(i));
                } else {
                    uniqueId = String.valueOf(new Date().getTime()).concat(id).concat(String.valueOf(i));
                }
                commodities.setUniqueId(Long.parseLong(uniqueId));
                commodities.setProductName(services.getServiceName());
                commodities.setProgramId(topups.getProgrammeid());
                commodities.setCategoryId(services.getCategoryId());
                commodities.setIdentificationNum(topups.getIdentificationNumber());
                commodities.setProductId(productsList.get(i).getServiceId());
                commodities.setMaxPrice(productsList.get(i).getMaxPrice());
                commodities.setBeneficiaryName(topups.getBeneficiaryName());
                commodities.setQuantityDeducted(productsList.get(i).getQuantity());
                commodities.setTotalAmountChargedByRetailer(Double.parseDouble(productsList.get(i).getTotalPrice()));
                commodities.setUom(productsList.get(i).getUom());
                commodities.setVoidTransaction("0");
                getDataManager().getDaoSession().getCommoditiesDao().insert(commodities);

                //save archive txn
                TransactionListProducts transactionListProducts = new TransactionListProducts();
                transactionListProducts.setTransactionDate(CalenderUtils.getTimestamp());
                transactionListProducts.setTransactionNo(transactions.getReceiptNo().toString());
                transactionListProducts.setUniqueid(UUID.randomUUID().toString());
                transactionListProducts.setProductName(services.getServiceName());
                transactionListProducts.setProductId(productsList.get(i).getServiceId());
                transactionListProducts.setBeneficiaryName(topups.getBeneficiaryName());
                transactionListProducts.setDeviceId(getDataManager().getDeviceId());
                transactionListProducts.setProgramId(topups.getProgrammeid());
                transactionListProducts.setQuantity(productsList.get(i).getQuantity());
                transactionListProducts.setVal(productsList.get(i).getTotalPrice());
                transactionListProducts.setUnitOfMeasure(productsList.get(i).getUom());
                transactionListProducts.setVoidTransaction("0");

                getDataManager().getDaoSession().getTransactionListProductsDao().insert(transactionListProducts);
                Services serviceBean = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.ServiceId.eq(productsList.get(i).getServiceId())).unique();
                if (serviceBean != null) {
                    double maxQty = serviceBean.getMaxQuantity() - Double.parseDouble(productsList.get(i).getQuantity());
                    serviceBean.setMaxQuantity(maxQty);
                    getDataManager().getDaoSession().getServicesDao().update(serviceBean);
                }
            }
            transactions.setSubmit("0");
            transactions.setTotalAmountChargedByRetail(String.valueOf(amount));
            if (beneficiary!=null) {
                receipt.setCardSerialNumber(beneficiary.getCardSerialNumber());
                transactions.setCardSerialNumber(beneficiary.getCardSerialNumber());
            }
            if (amount > 0) {
                getDataManager().getDaoSession().getTransactionsDao().insert(transactions);
            }

            //update topups voucher value
            com.compastbc.core.data.db.model.Topups topups1 = getDataManager().getDaoSession().getTopupsDao().queryBuilder().where(TopupsDao.Properties.CardNumber.eq(topups.getCardnumber()),
                    TopupsDao.Properties.ProgrammeId.eq(topups.getProgrammeid()), TopupsDao.Properties.VocherIdNo.eq(topups.getVocheridno())).limit(1).unique();
            if (topups1 != null) {
                topups1.setVoucherValue(topups.getVouchervalue());
                getDataManager().getDaoSession().getTopupsDao().update(topups1);
            }

            //update txn count
            if (amount > 0) {
                TxnCount txnCount = getDataManager().getDaoSession().getTxnCountDao().queryBuilder().where(TxnCountDao.Properties.Date.eq(transactions.getDate())).limit(1).unique();
                if (txnCount != null) {
                    long txn_count = txnCount.getCount() + 1;
                    txnCount.setCount(txn_count);
                    getDataManager().getDaoSession().getTxnCountDao().update(txnCount);
                } else {
                    TxnCount txnCountBean = new TxnCount();
                    txnCountBean.setCount(1);
                    txnCountBean.setUniqueId(UUID.randomUUID().toString());
                    txnCountBean.setDate(transactions.getDate());
                    if (tps != null) {
                        txnCountBean.setStartDate(tps.getStartDate());
                        txnCountBean.setEndDate(tps.getEndDate());
                    } else {
                        txnCountBean.setStartDate(new Date());
                        txnCountBean.setEndDate(new Date());
                    }
                    getDataManager().getDaoSession().getTxnCountDao().save(txnCountBean);
                }
            }

            //printReceiptData
            receipt.setRation(topups.getIdentificationNumber());
            receipt.setProgramCurrency(programs.getProgramCurrency());
            receipt.setReceiptNo(String.valueOf(receiptNo));
            receipt.setCurrentBalance(topups.getVouchervalue());
            receipt.setTxnValue(String.valueOf(amount));
            receipt.setOpeningBal(String.valueOf(Double.parseDouble(topups.getVouchervalue()) + amount));
            receipt.setProductsList(productsList);

            alertSuccess(receipt);
        }
    }

    private void alertSuccess(TransactionReceipt receipt) {
        getMvpView().createLog("Cart Activity", "Transaction Success");
        getDataManager().getDaoSession().getPurchasedProductsDao().deleteAll();
        getMvpView().hideLoading();
        getMvpView().show(getMvpView().sweetAlert(2, R.string.success, R.string.transactionSuccess)
                .setCancelButton(R.string.no, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    getMvpView().openNextActivity();
                }).setConfirmButton(R.string.dialog_ok, sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    getMvpView().print(receipt, true);
                }));
    }
}
