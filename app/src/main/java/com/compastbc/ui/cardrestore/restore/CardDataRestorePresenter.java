package com.compastbc.ui.cardrestore.restore;

import android.app.Activity;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.data.db.model.NFCCardData;
import com.compastbc.nfcprint.nfc.NFCListener;
import com.compastbc.nfcprint.nfc.NFCReadDataListener;
import com.compastbc.nfcprint.nfc.NFCReader;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

class CardDataRestorePresenter<V extends CardDataRestoreMvpView> extends BasePresenter<V>
        implements CardDataRestoreMvpPresenter<V> {

    private static final String TAG = "CardDataRestorePresenter";
    private final Activity activity;
    private final NFCReader nfcReader;
    private NFCCardData nfcCardDataBean;

    CardDataRestorePresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        this.activity = activity;
        nfcReader = NFCReader.getInstance(activity);
    }

    @Override
    public void doCardDataRestore(NFCCardData nfcCardDataBean) {
        try {
            this.nfcCardDataBean = nfcCardDataBean;
            JSONObject topupJsonObject = new JSONObject(nfcCardDataBean.getCardJsonObjData());

            doValidateWriteCardData(topupJsonObject);
        } catch (Exception e) {
            getMvpView().showMessage(R.string.card_error_write_data);
        }
    }

    private void doValidateWriteCardData(JSONObject topupJsonObject) {
        nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                if (data != null && !data.isEmpty()) {
                    try {
                        JSONObject object = new JSONObject(data);
                        String cardNo = object.getString("cardNo");
                        if (cardNo.equalsIgnoreCase(nfcCardDataBean.getCardNumber())) {
                            getMvpView().showLoading(activity.getString(R.string.title_card_tap));
                            doWriteCardData(topupJsonObject);
                        } else {
                            getMvpView().hideLoading();
                            getMvpView().show(getMvpView().sweetAlert(R.string.alert, activity.getString(R.string.invalid_card_txn, nfcCardDataBean.getBeneficiaryName())).setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                if (getMvpView().verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
                                    doValidateWriteCardData(topupJsonObject);
                                }
                            }));
                        }
                    } catch (Exception e) {
                        getMvpView().uploadExceptionData(topupJsonObject.toString(), Thread.currentThread().getStackTrace()[2].getMethodName(), Thread.currentThread().getStackTrace()[2].getLineNumber(), TAG, e.toString());
                        getMvpView().hideLoading();
                        //getMvpView().showMessage(e.toString());
                        getMvpView().showMessage(R.string.card_error_write_data);
                    }
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideLoading();
            }
        }, true);
    }

    private void doWriteCardData(JSONObject topupJsonObject) {
        nfcReader.doWriteCardData(NFCReader.CARD_DATA, topupJsonObject.toString(), new NFCListener() {
            @Override
            public void onSuccess(int flag) {
                if (flag == 0) {
                    deleteCardDataAfterSuccess();
                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.great, R.string.card_restore_success)
                            .setConfirmClickListener(sweetAlertDialog -> getMvpView().onCardRestoreSuccess())
                            .show();
                } else {
                    getMvpView().hideLoading();
                    getMvpView().show(getMvpView().sweetAlert(R.string.alert, R.string.alert_rewrite).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        doValidateWriteCardData(topupJsonObject);
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
        if (nfcCardDataBean != null) {
            getDataManager().getDaoSession().getNFCCardDataDao().delete(nfcCardDataBean);
        }
    }
}
