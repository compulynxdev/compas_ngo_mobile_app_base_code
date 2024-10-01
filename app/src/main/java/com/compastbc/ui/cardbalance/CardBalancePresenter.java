package com.compastbc.ui.cardbalance;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.network.model.CardBalanceBean;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.nfcprint.nfc.NFCReadListDataListener;
import com.compastbc.nfcprint.nfc.NFCReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardBalancePresenter<V extends CardBalanceMvpView> extends BasePresenter<V>
        implements CardBalanceMvpPresenter<V> {

    private final Activity activity;
    private final NFCReader nfcReader;
    private String identityNo, beneficiaryName, cardNo;
    private final List<Programs> programmesList = new ArrayList<>();
    private final List<Topups> topupsList = new ArrayList<>();

    CardBalancePresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        this.activity = activity;
        nfcReader = NFCReader.getInstance(activity);

    }

    @Override
    public void readCardBalance() {
        nfcReader.doReadCardByList(new NFCReadListDataListener() {

            @Override
            public void onSuccessRead(List<String> data) {
                doNextProcess(data);
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, true, NFCReader.PERSONAL_DETAIL, NFCReader.CARD_PIN, NFCReader.CARD_DATA);

    }

    private void doNextProcess(List<String> status) {
        try {
            if (status == null) {
                getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_read_fail).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardBalance();
                    }
                }));
            } else if (status.isEmpty()) {
                getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.no_topups).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardBalance();
                    }
                }));
            } else {
                getMvpView().showLoading();
                JSONObject person = new JSONObject(status.get(0));
                identityNo = person.getString("rationalNo");
                beneficiaryName = person.getString("name");
                JSONObject object = new JSONObject(status.get(1));
                cardNo = object.getString("cardNo");
                if (getDataManager().getConfigurableParameterDetail().isOnline()) {
                    findBlockCard(cardNo);
                } else {
                    findBalanceFromDb(status);
                }
            }
        } catch (Exception e) {
            getMvpView().hideLoading();
            getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_error_read_data).setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                    readCardBalance();
                }
            }));
        }
    }

    private void findBalanceFromDb(List<String> status) {
        if (status.size() > 2) {
            try {
                JSONObject object = new JSONObject(status.get(2));
                if (object.length() > 0) {
                    JSONArray programsArray = object.getJSONArray("programs");
                    List<Topups> cardTopups = new ArrayList<>();
                    List<com.compastbc.core.data.db.model.Topups> topupsList = getDataManager().getDaoSession().getTopupsDao().queryBuilder().where(TopupsDao.Properties.CardNumber.eq(cardNo)).list();
                    for (int i = 0; i < programsArray.length(); i++) {
                        Topups topups = new Topups();
                        topups.setCardnumber(cardNo);
                        topups.setBeneficiaryName(beneficiaryName);
                        topups.setIdentificationNumber(identityNo);
                        topups.setVouchervalue(programsArray.getJSONObject(i).getString("vouchervalue"));
                        topups.setProgrammeid(programsArray.getJSONObject(i).getString("programmeid"));
                        cardTopups.add(topups);
                    }
                    if (cardTopups.size() > 0 && topupsList.size() > 0) {
                        for (int i = 0; i < cardTopups.size(); i++) {
                            int count = 0;
                            for (int j = 0; j < topupsList.size(); j++) {
                                if (cardTopups.get(i).getProgrammeid().equalsIgnoreCase(topupsList.get(j).getProgrammeId())) {
                                    count = 1;
                                }
                            }
                            if (count != 1) {
                                cardTopups.remove(i);
                                --i;
                            }
                        }
                        List<Programs> programsList = new ArrayList<>();
                        for (int i = 0; i < cardTopups.size(); i++) {
                            Programs programs = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(cardTopups.get(i).getProgrammeid())).unique();
                            if (programs != null)
                                programsList.add(programs);
                        }
                        if (!programsList.isEmpty())
                            createBean(cardTopups, programsList);
                        else {
                            getMvpView().hideLoading();
                            getMvpView().showMessage(R.string.no_topups);
                        }
                    } else {
                        getMvpView().hideLoading();
                        getMvpView().showMessage(R.string.no_topups);
                    }
                } else {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(R.string.no_topups);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                getMvpView().hideLoading();
                getMvpView().show(getMvpView().sweetAlert(R.string.error, R.string.card_error_read_data).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                        readCardBalance();
                    }
                }));
            }
        } else {
            getMvpView().hideLoading();
            getMvpView().showMessage(R.string.no_topups);
        }
    }

    @Override
    public void findTopups(String cardNo) {
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
                                    topupsList.clear();
                                    List<Integer> programId = new ArrayList<>();
                                    for (int i = 0; i < object.length(); i++) {
                                        Topups topups = new Topups();
                                        topups.setCardnumber(cardNo);
                                        topups.setIdentificationNumber(identityNo);
                                        topups.setBeneficiaryName(beneficiaryName);
                                        topups.setVouchervalue(object.getJSONObject(i).getString("productPrice"));
                                        topups.setProgrammeid(object.getJSONObject(i).getString("programmeId"));
                                        programId.add(Integer.parseInt(object.getJSONObject(i).getString("programmeId")));
                                        topupsList.add(topups);
                                    }
                                    if (programId.size() > 0) {
                                        findPrograms(programId);
                                    }
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.NoTopups).setConfirmClickListener(sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                            readCardBalance();
                                        }
                                    }));

                                }
                            }
                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            assert response.errorBody() != null;
                            JSONObject object;
                            object = new JSONObject(response.errorBody().string());
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), object.getString("message")).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    readCardBalance();
                                }
                            }));
                        }
                    } catch (Exception e) {
                        getMvpView().hideLoading();
                        getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                readCardBalance();
                            }
                        }));
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    getMvpView().hideKeyboard();
                    handleApiFailure(call, t);
                }
            });
        }

    }

    @Override
    public void findPrograms(List<Integer> programId) {
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
                                programmesList.clear();
                                for (int i = 0; i < object.length(); i++) {
                                    Programs programmes = new Programs();
                                    programmes.setProgramId(object.getJSONObject(i).getString("programmeId"));
                                    programmes.setProgramName(object.getJSONObject(i).getString("programmeName"));
                                    programmes.setProgramCurrency(object.getJSONObject(i).getString("programCurrency"));
                                    programmes.setProductId(object.getJSONObject(i).getString("productId"));
                                    programmesList.add(programmes);
                                }
                                createBean(topupsList, programmesList);

                            } else {
                                getMvpView().hideLoading();
                                getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.NoPrograms).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                        readCardBalance();
                                    }
                                }));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), e.getMessage()).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    readCardBalance();
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
                                    readCardBalance();
                                }
                            }));
                        } catch (Exception e) {
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.ServerError).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    readCardBalance();
                                }
                            }));

                            e.printStackTrace();
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

    }

    @Override
    public void createBean(List<Topups> topups, List<Programs> programmesList) {
        List<CardBalanceBean> cardBalanceBeans = new ArrayList<>();
        for (int i = 0; i < topups.size(); i++) {
            for (int j = 0; j < programmesList.size(); j++) {
                if (topups.get(i).getProgrammeid().equalsIgnoreCase(programmesList.get(j).getProgramId())) {
                    CardBalanceBean cardBalanceBean = new CardBalanceBean();
                    cardBalanceBean.setProgramName(programmesList.get(j).getProgramName());
                    cardBalanceBean.setVoucherValue(programmesList.get(j).getProgramCurrency().concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(topups.get(i).getVouchervalue()))));
                    cardBalanceBeans.add(cardBalanceBean);
                    break;
                }
            }
        }
        if (cardBalanceBeans.size() > 0)
            getMvpView().showBalance(identityNo, beneficiaryName, cardNo, cardBalanceBeans);
        //dialog.dismiss();
    }

    @Override
    public void findBlockCard(String cardNo) {
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
                                        readCardBalance();
                                    }
                                }));
                            }

                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            assert response.errorBody() != null;
                            JSONObject object;
                            object = new JSONObject(response.errorBody().string());
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(activity.getString(R.string.alert), object.getString("message")).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    readCardBalance();
                                }
                            }));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        getMvpView().hideLoading();
                        getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.ServerError).setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                readCardBalance();
                            }
                        }));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    getMvpView().hideLoading();
                    handleApiFailure(call, t);
                }
            });
        }
    }
}
