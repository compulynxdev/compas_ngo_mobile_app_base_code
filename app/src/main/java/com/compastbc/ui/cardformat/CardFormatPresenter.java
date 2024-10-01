package com.compastbc.ui.cardformat;

import android.app.Activity;
import android.content.Intent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.nfcprint.nfc.NFCListener;
import com.compastbc.nfcprint.nfc.NFCReadDataListener;
import com.compastbc.nfcprint.nfc.NFCReader;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CardFormatPresenter<V extends CardFormatMvpView> extends BasePresenter<V>
        implements CardFormatMvpPresenter<V> {

    private final NFCReader nfcReader;

    CardFormatPresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        nfcReader = NFCReader.getInstance(activity);
    }

    @Override
    public void formatCard(Intent intent, boolean isCleanFormat) {
        MaterialDialog dialog = getMvpView().materialDialog(R.string.card_format, R.string.card_remove);
        nfcReader.setIntent(intent);

        if (isCleanFormat) {
            nfcReader.doFormat(new NFCListener() {
                @Override
                public void onSuccess(int flag) {
                    dialog.dismiss();
                    if (flag == 0) {
                        getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.great, R.string.card_format_success)
                                .setConfirmClickListener(sweetAlertDialog -> getMvpView().onFormatSuccess())
                                .show();
                    } else {
                        getMvpView().showMessage(R.string.error, R.string.card_error_format);
                    }
                }

                @Override
                public void onFail(String TAG, String msg) {
                    dialog.dismiss();
                }
            }, true);
        } else {
            nfcReader.doReadCardDataForActivate(NFCReader.CARD_DATA, new NFCReadDataListener() {
                @Override
                public void onSuccess(String data) {
                    JSONObject dataObj = null;
                    try {
                        dataObj = new JSONObject(data);
                        AppLogger.d("FormatTest", "Card Data:- " + dataObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //data empty format card else delete other data except txn detail
                    if (dataObj == null || dataObj.length() == 0 || data.isEmpty()) {
                        AppLogger.d("FormatTest", "Card Data Blank means format the card");
                        nfcReader.doFormat(new NFCListener() {
                            @Override
                            public void onSuccess(int flag) {
                                dialog.dismiss();
                                if (flag == 0) {
                                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.great, R.string.card_format_success)
                                            .setConfirmClickListener(sweetAlertDialog -> getMvpView().onFormatSuccess())
                                            .show();
                                } else {
                                    getMvpView().showMessage(R.string.error, R.string.card_error_format);
                                }
                            }

                            @Override
                            public void onFail(String TAG, String msg) {
                                dialog.dismiss();
                            }
                        }, false);
                    } else {
                        dialog.dismiss();
                        nfcReader.doDeleteFile(new NFCListener() {
                            @Override
                            public void onSuccess(int flag) {
                                if (flag == 0) {
                                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.great, R.string.card_format_success)
                                            .setConfirmClickListener(sweetAlertDialog -> getMvpView().onFormatSuccess())
                                            .show();
                                } else {
                                    getMvpView().showMessage(R.string.error, R.string.card_error_format);
                                }
                            }

                            @Override
                            public void onFail(String TAG, String msg) {
                            }
                        }, false, NFCReader.PERSONAL_DETAIL, NFCReader.CARD_PIN);
                    }
                }

                @Override
                public void onFail(String TAG, String msg) {
                    dialog.dismiss();
                }
            }, true);
        }
    }
}
