package com.compastbc.ui.transaction.transaction;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryBio;
import com.compastbc.core.data.db.model.BeneficiaryBioDao;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.BlockCards;
import com.compastbc.core.data.db.model.BlockCardsDao;
import com.compastbc.core.data.db.model.NFCCardDataDao;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.db.model.ServiceDetails;
import com.compastbc.core.data.db.model.ServiceDetailsDao;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.ServicePricesDao;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;
import com.compastbc.core.data.db.model.TopupLogs;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.core.data.db.model.NFCCardData;
import com.compastbc.nfcprint.nfc.NFCListener;
import com.compastbc.nfcprint.nfc.NFCReadDataListener;
import com.compastbc.nfcprint.nfc.NFCReadListDataListener;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionPresenter<V extends TransactionMvpView> extends BasePresenter<V>
        implements TransactionMvpPresenter<V> {

    private static final String TAG = "TransactionPresenter";
    private String cardNo, cardPin, identityNo, beneficiaryName;
    private List<Programs> programmesList = new ArrayList<>();
    private final Activity activity;
    private String vouchervalue;
    private final NFCReader nfcReader;
    private JSONObject objectCardData;

    TransactionPresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        this.activity = activity;
        nfcReader = NFCReader.getInstance(activity);
    }

    @Override
    public void readCardDetails() {
        nfcReader.doReadCardByList(new NFCReadListDataListener() {

            @Override
            public void onSuccessRead(List<String> data) {
                objectCardData = new JSONObject();
                doProcessData(data);
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, true, NFCReader.PERSONAL_DETAIL, NFCReader.CARD_PIN, NFCReader.CARD_DATA);

    }

    private void doProcessData(List<String> response) {
        try {
            if (response.isEmpty() || response.get(0).isEmpty() || (response.size() > 1 && response.get(1).isEmpty())) {
                getMvpView().hideLoading();
                getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_read_fail).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardDetails();
                    }
                }));
            } else {
                getMvpView().showLoading(activity.getString(R.string.title_card_tap));
                JSONObject personObject = new JSONObject(response.get(0));
                identityNo = personObject.getString("rationalNo");
                beneficiaryName = personObject.getString("name");
                JSONObject pinObject = new JSONObject(response.get(1));
                cardNo = pinObject.getString("cardNo");
                cardPin = pinObject.getString("pin");
                Topups topups = new Topups();
                topups.setIdentificationNumber(identityNo);
                topups.setBeneficiaryName(beneficiaryName);
                topups.setCardnumber(cardNo);
                getDataManager().setTopupDetails(topups);
                if (response.size() > 2) {
                    objectCardData = new JSONObject(response.get(2));
                }
                findBlockCard(cardNo);
            }
        } catch (Exception e) {
            getMvpView().hideLoading();
            String data = "";
            for(int i=0;i<response.size();i++){
                data = data.concat(response.get(i));
            }
            getMvpView().uploadExceptionData(data, Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
            getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_error_read_data).setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                    readCardDetails();
                }
            }));
        }
    }

    @Override
    public void findTopups(String cardNo) {
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            Map<String, String> map = new HashMap<>();
            map.put("cardNo", cardNo);
            map.put("serialNo", getDataManager().getDeviceId());
            map.put("agentId", getDataManager().getUserDetail().getAgentId());
            if (getMvpView().isNetworkConnected()) {
                getDataManager().getBeneficiaryTopups("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        try {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    JSONArray object = new JSONArray(response.body().string());
                                    if (object.length() > 0) {
                                        List<Integer> programId = new ArrayList<>();
                                        Topups topups = new Topups();
                                        topups.setCardnumber(cardNo);
                                        topups.setIdentificationNumber(identityNo);
                                        topups.setBeneficiaryName(beneficiaryName);
                                        getDataManager().setTopupDetails(topups);
                                        for (int i = 0; i < object.length(); i++) {
                                            programId.add(Integer.parseInt(object.getJSONObject(i).getString("programmeId")));
                                        }
                                        if (programId.size() > 0) {
                                            findPrograms(programId,null);
                                        }
                                    } else {
                                        getMvpView().hideLoading();
                                        getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.NoTopups).setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                                readCardDetails();
                                            }
                                        }));
                                    }
                                }
                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                assert response.errorBody() != null;
                                JSONObject object = new JSONObject(response.errorBody().string());
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), object.getString("message")).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }
                                }));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.some_error).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    readCardDetails();
                                }
                            }));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        handleApiFailure(call, t);
                    }
                });

            }
        } else {
            if (cardNo == null || cardNo.isEmpty()) {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.card_read_fail);
                return;
            }
            List<com.compastbc.core.data.db.model.Topups> topupsList = getDataManager().getDaoSession().getTopupsDao().queryBuilder()
                    .where(TopupsDao.Properties.CardNumber.eq(cardNo))
                    .whereOr(TopupsDao.Properties.StartDate.lt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                            TopupsDao.Properties.StartDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)))
                    .whereOr(TopupsDao.Properties.EndDate.gt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                            TopupsDao.Properties.EndDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT))).list();
            if (!topupsList.isEmpty()) {
                Type listType = new TypeToken<List<Integer>>() {}.getType();

                List<List<Integer>> purchasedItemIdList = new ArrayList<>();
                try {
                    //check card has program data or not
                    if (objectCardData.has("programs")) {
                        JSONArray programs = objectCardData.getJSONArray("programs");
                        for (int i = 0; i < topupsList.size(); i++) {
                            if (programs.length() > i) {
                                //if voucher no. matched for device topups and card topup values
                                if (programs.getJSONObject(i).getString("voucherno").equalsIgnoreCase(topupsList.get(i).getVocherIdNo())) {
                                    if (i == topupsList.size() - 1) {
                                        vouchervalue = programs.getJSONObject(i).getString("vouchervalue");
                                        List<Integer> id = new ArrayList<>();
                                        for (int j = 0; j < programs.length(); j++) {
                                            id.add(Integer.parseInt(programs.getJSONObject(j).getString("programmeid")));
                                            List<Integer> purchasedItemIds = getDataManager().getGson().fromJson(programs.getJSONObject(j).getJSONArray("purchasedItemId").toString(), listType);
                                            purchasedItemIdList.add(purchasedItemIds);
                                        }
                                        findPrograms(id, purchasedItemIdList);
                                        return;
                                    }
                                }
                            }
                        }

                        topUpCard(topupsList, programs);
                    } else {
                        //insert new program on blank card
                        topUpCard(topupsList, new JSONArray());
                    }
                } catch (Exception e) {
                    getMvpView().uploadExceptionData("", Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                    AppLogger.i(TAG, e.toString());
                    getMvpView().hideLoading();
                    getMvpView().showMessage(R.string.card_error_data);
                }
            } else {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.no_topups);
            }

        }
    }

    @Override
    public void findBlockCard(String cardNo) {
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            Map<String, String> map = new HashMap<>();
            map.put("cardNo", cardNo);
            if (getMvpView().isNetworkConnected()) {
                getDataManager().getCardIsBlocked("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        try {
                            if (response.code() == 200) {
                                assert response.body() != null;
                                JSONObject object = new JSONObject(response.body().string());
                                if (!object.getBoolean("result")) {
                                    findTopups(cardNo);
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.CardBlocked).setConfirmClickListener(sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                            readCardDetails();
                                        }
                                    }));
                                }
                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                assert response.errorBody() != null;
                                JSONObject object = new JSONObject(response.errorBody().string());
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), object.getString("message")).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }
                                }));
                            }
                        } catch (Exception e) {
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.ServerError).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    readCardDetails();
                                }
                            }));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        handleApiFailure(call, t);
                    }
                });
            }
        } else {
            BlockCards blockCards = getDataManager().getDaoSession().getBlockCardsDao().queryBuilder().where(BlockCardsDao.Properties.CardNo.eq(cardNo)).unique();
            if (blockCards != null) {
                getMvpView().hideLoading();
                getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.CardBlocked).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardDetails();
                    }
                }));
            } else {
                findTopups(cardNo);
            }

        }
    }

    @Override
    public void findPrograms(List<Integer> programId,List<List<Integer>> canDoTxn)  {
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            programmesList = new ArrayList<>();
            RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, programId.toString());
            if (getMvpView().isNetworkConnected()) {
                getDataManager().getProgramsByTopups("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            try {
                                assert response.body() != null;
                                JSONArray object = new JSONArray(response.body().string());
                                if (object.length() > 0) {
                                    for (int i = 0; i < object.length(); i++) {
                                        Programs programmes = new Programs();
                                        programmes.setProgramId(object.getJSONObject(i).getString("programmeId"));
                                        programmes.setProgramName(object.getJSONObject(i).getString("programmeName"));
                                        programmes.setProgramCurrency(object.getJSONObject(i).getString("programCurrency"));
                                        programmes.setProductId(object.getJSONObject(i).getString("productId"));
                                        programmesList.add(programmes);
                                    }
                                    setProgramList(programmesList);
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.NoPrograms).setConfirmClickListener(sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                            readCardDetails();
                                        }
                                    }));
                                }

                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }
                                }));
                            }
                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            try {
                                assert response.errorBody() != null;
                                JSONObject object = new JSONObject(response.errorBody().string());
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), object.getString("message")).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardDetails();
                                    }
                                }));
                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
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
                        handleApiFailure(call, t);
                    }
                });

            }
        } else {
            programmesList = new ArrayList<>();
            for (int i =0;i<programId.size();i++) {
                Programs programs = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(programId.get(i))).unique();
                if (programs != null) {
                    if (canDoTxn!=null && canDoTxn.size()>0)
                        programs.setPuchasedItemIds(canDoTxn.get(i));
                    programmesList.add(programs);
                }
            }
            setProgramList(programmesList);
        }
    }

    private void setProgramList(List<Programs> programmesList) {
        if (programmesList != null && !programmesList.isEmpty()) {
            if (getDataManager().getConfigurableParameterDetail().isBiometric())
                getBeneficiaryFingerPrints(identityNo);
            else
                getMvpView().showPinView(cardPin, programmesList, objectCardData);
        } else {
            getMvpView().hideLoading();
            getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.NoPrograms).setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                    readCardDetails();
                }
            }));
        }
    }

    @Override
    public void setProgramId(int programId) {
        Topups topups = getDataManager().getTopupDetails();
        topups.setProgrammeid(String.valueOf(programId));
        getDataManager().setTopupDetails(topups);
    }

    @Override
    public void getBeneficiaryFingerPrints(String identityNo) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            JSONObject object = new JSONObject();
            try {
                object.put("identityNo", identityNo);
                object.put("type", "BENEFICIARY");
                RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());
                if (getMvpView().isNetworkConnected()) {
                    getDataManager().getBeneficiaryFingerPrint("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                try {
                                    List<String> fps = new ArrayList<>();
                                    assert response.body() != null;
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    JSONObject fingers = jsonObject.getJSONObject("beneficiaryFingerPrint");
                                    if (fingers.length() > 0) {
                                        if (!fingers.isNull("leftFinger3B64"))
                                            fps.add(fingers.getString("leftFinger3B64"));

                                        if (!fingers.isNull("rightFinger3B64"))
                                            fps.add(fingers.getString("rightFinger3B64"));

                                        if (!fingers.isNull("rightFinger2B64"))
                                            fps.add(fingers.getString("rightFinger2B64"));

                                        if (!fingers.isNull("leftFinger2B64"))
                                            fps.add(fingers.getString("leftFinger2B64"));

                                        if (!fingers.isNull("leftFinger1B64"))
                                            fps.add(fingers.getString("leftFinger1B64"));

                                        if (!fingers.isNull("leftIndexB64"))
                                            fps.add(fingers.getString("leftIndexB64"));

                                        if (!fingers.isNull("rightFinger1B64"))
                                            fps.add(fingers.getString("rightFinger1B64"));

                                        if (!fingers.isNull("rightThumbB64"))
                                            fps.add(fingers.getString("rightThumbB64"));

                                        if (!fingers.isNull("rightIndexB64"))
                                            fps.add(fingers.getString("rightIndexB64"));

                                        if (!fingers.isNull("leftThumbB64"))
                                            fps.add(fingers.getString("leftThumbB64"));

                                        getMvpView().showBiometricView(programmesList, fps, objectCardData);

                                    } else {
                                        getMvpView().hideLoading();
                                        getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.NoBiometricFound).setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                                readCardDetails();
                                            }
                                        }));
                                    }
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                            readCardDetails();
                                        }
                                    }));
                                }

                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                try {
                                    assert response.errorBody() != null;
                                    JSONObject object1 = new JSONObject(response.errorBody().string());
                                    getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), object1.getString("message")).setConfirmClickListener(sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                            readCardDetails();
                                        }
                                    }));
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
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
                            handleApiFailure(call, t);
                        }
                    });

                }
            } catch (Exception e) {
                getMvpView().hideLoading();
                getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardDetails();
                    }
                }));
            }
        }//get FP from db
        else {
            List<String> fps = new ArrayList<>();
            Beneficiary beneficiary = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.Bio.eq("TRUE"), BeneficiaryDao.Properties.IdentityNo.eq(identityNo)).unique();
            if (beneficiary != null) {
                if (beneficiary.getBioVerifyStatus().equalsIgnoreCase("Approved")) {
                    BeneficiaryBio bio = getDataManager().getDaoSession().getBeneficiaryBioDao().queryBuilder().where(BeneficiaryBioDao.Properties.BeneficiaryId.eq(identityNo)).unique();
                    if (bio != null) {
                        if (bio.getF1() != null)
                            fps.add(bio.getF1());

                        if (bio.getF2() != null)
                            fps.add(bio.getF2());

                        if (bio.getF3() != null)
                            fps.add(bio.getF3());

                        if (bio.getF4() != null)
                            fps.add(bio.getF4());

                        if (bio.getFplf() != null)
                            fps.add(bio.getFplf());

                        if (bio.getFpli() != null)
                            fps.add(bio.getFpli());

                        if (bio.getFplt() != null)
                            fps.add(bio.getFplt());

                        if (bio.getFprf() != null)
                            fps.add(bio.getFprf());

                        if (bio.getFpri() != null)
                            fps.add(bio.getFpri());

                        if (bio.getFprt() != null)
                            fps.add(bio.getFprt());

                        getMvpView().showBiometricView(programmesList, fps, objectCardData);

                    } else {
                        getMvpView().hideLoading();
                        getMvpView().showMessage(R.string.NoBiometricFound);
                    }
                } else {
                    String msg;
                    switch (beneficiary.getBioVerifyStatus()) {
                        case "PENDING":
                            msg = activity.getString(R.string.yourBioStatusIs)
                                    .concat(" : ".concat(beneficiary.getBioVerifyStatus()).concat(" . ").concat(activity.getString(R.string.KindlyApprove).concat(" ").concat(activity.getString(R.string.beneficiary_status_pending))));
                            break;

                        case "VERIFIED":
                            msg = activity.getString(R.string.yourBioStatusIs)
                                    .concat(" : ".concat(beneficiary.getBioVerifyStatus()).concat(" . ").concat(activity.getString(R.string.KindlyApprove).concat(" ").concat(activity.getString(R.string.beneficiary_status_verified))));
                            break;

                        default:
                            msg = activity.getString(R.string.yourBioStatusIs)
                                    .concat(" : ".concat(beneficiary.getBioVerifyStatus()).concat(" . ").concat(activity.getString(R.string.KindlyApprove)));
                            break;
                    }

                    getMvpView().hideLoading();
                    getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, activity.getString(R.string.alert), msg)
                            .setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (beneficiary.getBioVerifyStatus().equals("VERIFIED")) {
                                    getMvpView().openBeneficiaryActivity();
                                }
                            }).show();
                }

            } else {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.NoBiometricFound);
            }
        }
    }

   /* @Override
    public void setProducts(String programId,List<Integer> purchasedItemIds) {
        getMvpView().showLoading(activity.getString(R.string.title_loading));
        List<ServiceDetails> serviceDetailsList = getDataManager().getDaoSession().getServiceDetailsDao().queryBuilder().where(
                ServiceDetailsDao.Properties.ProgramId.eq(programId)).list();
        double totalAmount = 0;
        List<PurchasedProducts> purchasedProductsList = new ArrayList<>();
        for (ServiceDetails details : serviceDetailsList){
            if (!purchasedItemIds.contains(Integer.parseInt(details.getServiceId()))){
                Programs programs = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(programId)).limit(1).unique();
                Services services = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.ServiceId.eq(details.getServiceId())).limit(1).unique();

                //
                if (services!=null){
                    if (services.getMaxQuantity() > 0) {
                        List<ServicePrices> prices = getDataManager().getDaoSession().getServicePricesDao().queryBuilder().where(ServicePricesDao.Properties.ServiceId.eq(services.getServiceId()),
                                ServicePricesDao.Properties.Currency.eq(programs.getProgramCurrency())).list();
                        if (prices != null && prices.size() > 0) {
                            PurchasedProducts purchasedProducts = new PurchasedProducts();
                            purchasedProducts.setCardNumber(getDataManager().getTopupDetails().getCardnumber());
                            purchasedProducts.setServiceId(services.getServiceId());
                            purchasedProducts.setProgrammeId(getDataManager().getTopupDetails().getProgrammeid());
                            String[] uom = prices.get(0).getUom().split("-");
                            double totalPrice = (prices.get(0).getMaxPrice() / Double.parseDouble(uom[0])) * services.getMaxQuantityBenf();
                            totalAmount = totalAmount + totalPrice;
                            purchasedProducts.setTotalPrice(String.valueOf(totalPrice));
                            purchasedProducts.setVoucherId(getDataManager().getTopupDetails().getVoucherid());
                            purchasedProducts.setUom(prices.get(0).getUom());
                            purchasedProducts.setQuantity(String.valueOf(services.getMaxQuantityBenf()));
                            purchasedProducts.setMaxPrice(String.valueOf(prices.get(0).getMaxPrice()));
                            purchasedProducts.setServiceImage(services.getServiceImage());
                            purchasedProducts.setServiceName(services.getServiceName());
                            purchasedProductsList.add(purchasedProducts);
                            purchasedItemIds.add(Integer.parseInt(services.getServiceId()));
                        } else {
                            getMvpView().hideLoading();
                            getMvpView().showMessage(R.string.alert, R.string.NoUoms);
                        }
                    }
                }else {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(R.string.alert,R.string.NoUoms);
                }
            }
        }
        Topups topups = getDataManager().getTopupDetails();
        double price = Double.parseDouble(topups.getVouchervalue()) - totalAmount;
        if (price >= 0) {
            topups.setVouchervalue(String.valueOf(price));
            topups.setPurchasedIds(purchasedItemIds);
            getDataManager().setTopupDetails(topups);
            getDataManager().getDaoSession().getPurchasedProductsDao().insertInTx(purchasedProductsList);
            getMvpView().openCartActivity();
        }else {
            getMvpView().hideLoading();
            getMvpView().showMessage(R.string.alert,R.string.maxPriceCannotExceeds);
        }
    }
*/
    private void topUpCard(List<com.compastbc.core.data.db.model.Topups> topupsList, JSONArray originalProgramArray) {
        JSONObject topupJsonObject = new JSONObject();
        String voucherOldValue = "0", voucherOldNo = "0";

        try {
            if (originalProgramArray.length() == 0) {
                //Insert new program in blank card
                for (int i = 0; i < topupsList.size(); i++) {
                    com.compastbc.core.data.db.model.Topups topupBean = topupsList.get(i);
                    updateTopupLogs(topupBean, voucherOldValue, voucherOldNo);
                    //Adding new program topup
                    JSONObject object = new JSONObject();
                    object.put("vouchervalue", topupBean.getVoucherValue());
                    object.put("voucherno", topupBean.getVocherIdNo());
                    object.put("programmeid", topupBean.getProgrammeId());
                    JSONArray purchaseItemIdArray = new JSONArray(new ArrayList());
                    object.put("purchasedItemId", purchaseItemIdArray);
                    object.put("voucherid", topupBean.getVoucherId());
                    object.put("startDate", topupBean.getStartDate().getTime());

                    originalProgramArray.put(object);
                }
            } else {
                Type listType = new TypeToken<List<Integer>>() {
                }.getType();

                for (int i = 0; i < topupsList.size(); i++) {

                    com.compastbc.core.data.db.model.Topups topupBean = topupsList.get(i);
                    boolean isProgramFound = false;

                    for (int j = 0; j < originalProgramArray.length(); j++) {
                        long cardTopupStartDate;
                        List<Integer> purchasedItem;

                        JSONObject programObj = originalProgramArray.getJSONObject(j);
                        // if programs present in cards and device topup programs are same
                        if (programObj.getString("programmeid").equalsIgnoreCase(topupBean.getProgrammeId())) {
                            isProgramFound = true;

                            voucherOldValue = programObj.getString("vouchervalue");
                            voucherOldNo = programObj.getString("voucherno");
                   /* if(programObj.getString("voucherno").equalsIgnoreCase(topupBean.getVocherIdNo()))
                        vouchervalue = programObj.getString("vouchervalue");
                    else*/
                            // if current date is greater than card timestamp then do carry forward else amount remains same....
                            if (getDataManager().getConfigurableParameterDetail().isCarryForward() &&
                                    programObj.getLong("startDate") < (topupBean.getStartDate().getTime())) {
                                cardTopupStartDate = topupBean.getStartDate().getTime();
                                purchasedItem = new ArrayList<>();
                                vouchervalue = String.valueOf(Float.parseFloat(programObj.getString("vouchervalue")) + Float.parseFloat(topupBean.getVoucherValue()));
                            }
                            //update topup for next cycle
                            else if (programObj.getLong("startDate") < (topupBean.getStartDate().getTime())) {
                                cardTopupStartDate = topupBean.getStartDate().getTime();
                                purchasedItem = new ArrayList<>();
                                vouchervalue = topupBean.getVoucherValue();
                            } else {
                                cardTopupStartDate = programObj.getLong("startDate");
                                purchasedItem = new Gson().fromJson(programObj.getJSONArray("purchasedItemId").toString(), listType);
                                vouchervalue = programObj.getString("vouchervalue");
                            }

                            updateTopupLogs(topupBean, voucherOldValue, voucherOldNo);

                            //update card program data to specific array index
                            JSONObject object = new JSONObject();
                            object.put("vouchervalue", vouchervalue);
                            object.put("voucherno", topupBean.getVocherIdNo());
                            object.put("programmeid", topupBean.getProgrammeId());
                            JSONArray purchaseItemIdArray = new JSONArray(purchasedItem);
                            object.put("purchasedItemId", purchaseItemIdArray);
                            object.put("voucherid", topupBean.getVoucherId());
                            object.put("startDate", cardTopupStartDate);

                            originalProgramArray.put(j, object);
                            break;
                        }
                    }

                    //if program not found then add new program into the array
                    if (!isProgramFound) {
                        updateTopupLogs(topupBean, voucherOldValue, voucherOldNo);
                        //Adding new program topup
                        JSONObject object = new JSONObject();
                        object.put("vouchervalue", topupBean.getVoucherValue());
                        object.put("voucherno", topupBean.getVocherIdNo());
                        object.put("programmeid", topupBean.getProgrammeId());
                        JSONArray purchaseItemIdArray = new JSONArray(new ArrayList());
                        object.put("purchasedItemId", purchaseItemIdArray);
                        object.put("voucherid", topupBean.getVoucherId());
                        object.put("startDate", topupBean.getStartDate().getTime());

                        originalProgramArray.put(object);
                    }
                }
            }

            topupJsonObject.put("programs", originalProgramArray);
            topupJsonObject.put("benid", topupsList.get(0).getBeneficiaryId());
            topupJsonObject.put("cardno", topupsList.get(0).getCardNumber());
            objectCardData = topupJsonObject;

            //before writing validate json formation
            new JSONObject(topupJsonObject.toString());
            //AppLogger.e("FinalJSON", topupJsonObject.toString());

            doValidateWriteCardData(topupJsonObject, originalProgramArray, false);
        } catch (Exception e) {
            getMvpView().uploadExceptionData(objectCardData.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
            getMvpView().hideLoading();
            //getMvpView().showMessage(e.toString());
            getMvpView().showMessage(R.string.card_error_write_data);
        }
    }

    private void doWriteCardData(JSONObject topupJsonObject, JSONArray originalProgramArray) {
        nfcReader.doWriteCardData(NFCReader.CARD_DATA, topupJsonObject.toString(), new NFCListener() {
            @Override
            public void onSuccess(int flag) {
                if (flag == 0) {
                    List<Integer> id = new ArrayList<>();
                    List<List<Integer>> canDoTxn = new ArrayList<>();
                    for (int i = 0; i < originalProgramArray.length(); i++) {
                        try {
                            JSONObject programObj = originalProgramArray.getJSONObject(i);
                            id.add(Integer.parseInt(programObj.getString("programmeid")));
                            Type listType = new TypeToken<List<Integer>>() {
                            }.getType();
                            List<Integer> purchasedItemIds = getDataManager().getGson().fromJson(programObj.getJSONArray("purchasedItemId").toString(), listType);
                            canDoTxn.add(purchasedItemIds);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getMvpView().uploadExceptionData(originalProgramArray.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                        }
                    }
                    deleteCardDataAfterSuccess();
                    findPrograms(id, canDoTxn);
                } else {
                    getMvpView().hideLoading();
                    saveCardDataForRestore(topupJsonObject);
                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.alert_rewrite).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        doValidateWriteCardData(topupJsonObject, originalProgramArray, true);
                    }));
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, false);
    }

    private void deleteCardDataAfterSuccess() {
        NFCCardData nfcCardBean = getDataManager().getDaoSession().getNFCCardDataDao().queryBuilder().where(NFCCardDataDao.Properties.CardID.eq(identityNo)).unique();
        if (nfcCardBean != null) {
            getDataManager().getDaoSession().getNFCCardDataDao().delete(nfcCardBean);
        }
    }

    private void saveCardDataForRestore(JSONObject topupJsonObject) {
        NFCCardData nfcCardData = new NFCCardData();
        nfcCardData.setBeneficiaryName(beneficiaryName);
        nfcCardData.setCardID(identityNo);
        nfcCardData.setCardNumber(cardNo);
        nfcCardData.setCreatedDate(new Date());
        nfcCardData.setCardJsonObjData(topupJsonObject.toString());
        getDataManager().getDaoSession().getNFCCardDataDao().save(nfcCardData);
    }

    private void doValidateWriteCardData(JSONObject topupJsonObject, JSONArray originalProgramArray, boolean beep) {
        getMvpView().showLoading(activity.getString(R.string.title_card_tap));
        nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                if (data != null && !data.isEmpty()) {
                    try {
                        JSONObject object = new JSONObject(data);
                        String cardNo = object.getString("cardNo");
                        if (cardNo.equalsIgnoreCase(getDataManager().getTopupDetails().getCardnumber())) {
                            doWriteCardData(topupJsonObject, originalProgramArray);
                        } else {
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, activity.getString(R.string.invalid_card_txn, getDataManager().getTopupDetails().getBeneficiaryName())).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    doValidateWriteCardData(topupJsonObject, originalProgramArray, beep);
                                }
                            }));
                        }
                    } catch (Exception e) {
                        getMvpView().hideLoading();
                        getMvpView().uploadExceptionData(topupJsonObject.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                        //getMvpView().showMessage(e.toString());
                        getMvpView().showMessage(R.string.card_error_write_data);
                    }
                } else {
                    getMvpView().hideLoading();
                    getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_read_fail).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                            doValidateWriteCardData(topupJsonObject, originalProgramArray, beep);
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

    private void updateTopupLogs(com.compastbc.core.data.db.model.Topups topupBean, String voucherOldValue, String voucherOldNo) {
        String android_id = CommonUtils.getDeviceId(activity);
        TopupLogs topupsLogs = new TopupLogs();

        topupsLogs.setIsUploaded("0");
        topupsLogs.setNtopupValue(topupBean.getVoucherValue());
        topupsLogs.setDeviceIdNo(android_id);
        topupsLogs.setRefNo(android_id + android_id + System.currentTimeMillis());
        topupsLogs.setNvoucherIdNo(topupBean.getVocherIdNo());
        topupsLogs.setTopupTime(CalenderUtils.getDateTime(CalenderUtils.DB_TIMESTAMP_FORMAT, Locale.US));
        topupsLogs.setCardNo(topupBean.getCardNumber());
        topupsLogs.setProgrammeId(topupBean.getProgrammeId());
        topupsLogs.setNCardBal(vouchervalue == null ? "0" : vouchervalue);
        topupsLogs.setUserName(getDataManager().getUserDetail().getUser());

        topupsLogs.setOCardBal(voucherOldValue);
        topupsLogs.setOvoucherIdNo(voucherOldNo);

        getDataManager().getDaoSession().getTopupLogsDao().insertOrReplace(topupsLogs);
    }
}
