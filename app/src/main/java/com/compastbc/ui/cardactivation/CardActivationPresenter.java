package com.compastbc.ui.cardactivation;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.nfcprint.nfc.NFCListener;
import com.compastbc.nfcprint.nfc.NFCReadListDataOrErrorListener;
import com.compastbc.nfcprint.nfc.NFCReader;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardActivationPresenter<V extends CardActivationMvpView> extends BasePresenter<V>
        implements CardActivationMvpPresenter<V> {

    private final Activity activity;
    private final NFCReader nfcReader;
    private JSONObject bnfJsonObject;
    private MaterialDialog dialog;

    CardActivationPresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        this.activity = activity;
        nfcReader = NFCReader.getInstance(activity);
    }

    @Override
    public void findBeneficiary(String idno) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            Map<String, String> map = new HashMap<>();
            map.put("identityNo", idno);
            map.put("locationId",getDataManager().getUserDetail().getLocationId());
            if (getMvpView().isNetworkConnected()) {
                getDataManager().getBeneficiaryByIdentityno("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        getMvpView().hideLoading();
                        try {
                            getMvpView().hideLoading();
                            if (response.code() == 200) {
                                assert response.body() != null;
                                JSONObject object = new JSONObject(response.body().string());
                                if (object.length() > 0) {
                                    bnfJsonObject = object;
                                    getMvpView().showBeneficiaryDetails(object);
                                } else getMvpView().showMessage(R.string.NoBenfFound);

                            } else if (response.code() == 401)
                                getMvpView().openActivityOnTokenExpire();

                            else {
                                assert response.errorBody() != null;
                                JSONObject object;
                                object = new JSONObject(response.errorBody().string());
                                getMvpView().showMessage(object.getString("message"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            getMvpView().showMessage(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        handleApiFailure(call, t);
                    }
                });
            }
        } else {
            Beneficiary beneficiary = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IdentityNo.eq(idno)).unique();
            getMvpView().hideLoading();
            if (beneficiary != null) {
                try {
                    bnfJsonObject = new JSONObject();
                    bnfJsonObject.put("firstName", beneficiary.getFirstName());
                    bnfJsonObject.put("identityNo", beneficiary.getIdentityNo());
                    bnfJsonObject.put("gender", beneficiary.getGender());
                    bnfJsonObject.put("cardpin", beneficiary.getCardPin());
                    bnfJsonObject.put("cardNumber", beneficiary.getCardNumber());
                    bnfJsonObject.put("dateOfBirth", beneficiary.getDateOfBirth());
                    getMvpView().showBeneficiaryDetails(bnfJsonObject);
                } catch (Exception e) {
                    getMvpView().showMessage(e.getMessage());
                }
            } else {
                getMvpView().showMessage(R.string.idNotExist);
            }
        }

    }

    @Override
    public void verifyInput(String input) {
        if (input != null && !input.isEmpty())
            findBeneficiary(input);
        else getMvpView().showMessage(R.string.enter_identificationno);
    }

    @Override
    public void doActivateCard() {
        dialog = getMvpView().materialDialog(R.string.CardActivation, R.string.card_remove);
        try {
            //read card and check card is activated or blank
            nfcReader.doReadCardDataForActivate(new NFCReadListDataOrErrorListener() {

                @Override
                public void onSuccessRead(List<String> data) {
                    if (dialog != null) dialog.dismiss();
                    if (data.isEmpty()) {
                        doWriteDataToCard(false);
                    } else {
                        //blank card
                        String tmpId = "", tmpName = "";

                        try {
                            JSONObject jsonObject = new JSONObject(data.get(0));
                            tmpId = jsonObject.getString("rationalNo");
                            tmpName = jsonObject.getString("name");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (tmpName.isEmpty()) {
                            if (data.size() > 1 && (data.get(1) == null || data.get(1).isEmpty())) {
                                doWriteDataToCard(false);
                            } else {
                                String oldCardNo = "";
                                String inputCardNO = "";

                                try {
                                    inputCardNO = bnfJsonObject.getString("cardNumber");

                                    JSONObject jsonObjectCardData = new JSONObject(data.get(1));
                                    oldCardNo = jsonObjectCardData.getString("cardno");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (oldCardNo.equalsIgnoreCase(inputCardNO)) {
                                    doWriteDataToCard(true);
                                } else {
                                    String msg = activity.getString(R.string.this_card_belongs_to_2, oldCardNo);
                                    getMvpView().showMessage(activity.getString(R.string.alert), msg);
                                }
                            }
                        } else {
                            //already activated card
                            String msg = activity.getString(R.string.this_card_belongs_to).concat(" ").concat(tmpName).concat(" ").concat(activity.getString(R.string.identication_num_col)).concat(" ").concat(tmpId);
                            getMvpView().showMessage(activity.getString(R.string.alert), msg);
                        }
                    }
                }

                @Override
                public void onFail(String TAG, String msg) {
                    if (dialog != null) dialog.dismiss();
                }

                @Override
                public void cardNotActivated() {
                    if (dialog != null) dialog.dismiss();
                    //card blank means we need to write data into the card

                    doWriteDataToCard(false);
                }
            }, true);
        } catch (Exception e) {
            if (dialog != null) dialog.dismiss();
            getMvpView().showMessage(R.string.error, R.string.card_activate_fail);
        }

    }

    private void doWriteDataToCard(boolean isCardDataAlreadyStored) {
        //write operation perform here
        JSONObject personalData = new JSONObject();
        JSONObject cardpin = new JSONObject();
        try {
            personalData.put("name", bnfJsonObject.getString("firstName"));
            personalData.put("rationalNo", bnfJsonObject.getString("identityNo"));
            AppLogger.d("personalData", personalData.toString());

            cardpin.put("pin", bnfJsonObject.getString("cardpin"));
            cardpin.put("cardNo", bnfJsonObject.getString("cardNumber"));
            AppLogger.d("cardpin", cardpin.toString());

            nfcReader.doActivateCard(personalData.toString(), cardpin.toString(), new NFCListener() {
                @Override
                public void onSuccess(int flag) {
                    if (flag == 0) {
                        if (dialog != null) dialog.dismiss();
                        getMvpView().show(getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.great, R.string.card_activated)
                                .setConfirmClickListener(sweetAlertDialog -> getMvpView().cardActivationSuccess()));
                    } else {
                        if (dialog != null) dialog.dismiss();
                        getMvpView().showMessage(R.string.error, R.string.card_error_write_data);
                    }
                }

                @Override
                public void onFail(String TAG, String msg) {
                    if (dialog != null) dialog.dismiss();
                }
            }, false, isCardDataAlreadyStored);
        } catch (Exception e) {
            e.printStackTrace();
            getMvpView().showMessage(R.string.error, R.string.card_error_write_data);
        }
    }
}
