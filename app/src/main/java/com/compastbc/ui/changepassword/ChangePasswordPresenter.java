package com.compastbc.ui.changepassword;

import android.app.Activity;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.nfcprint.nfc.NFCListener;
import com.compastbc.nfcprint.nfc.NFCReadDataListener;
import com.compastbc.nfcprint.nfc.NFCReader;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordPresenter<V extends ChangePasswordMvpView> extends BasePresenter<V>
        implements ChangePasswordMvpPresenter<V> {

    private final NFCReader nfcReader;

    ChangePasswordPresenter(Activity activity, DataManager dataManager) {
        super(dataManager);
        nfcReader = NFCReader.getInstance(activity);
    }

    @Override
    public void readCardDetails() {
        nfcReader.doReadCardData(NFCReader.PERSONAL_DETAIL, new NFCReadDataListener() {
            @Override
            public void onSuccess(String data) {
                try {
                    if (data != null && !data.isEmpty()) {
                        JSONObject object1 = new JSONObject(data);
                        String name = object1.getString("name");
                        String identification = object1.getString("rationalNo");
                        getMvpView().setUpdate(true);
                        getMvpView().showDetails(name, identification);
                    } else {
                        getMvpView().hideDialog();
                        getMvpView().showMessage(R.string.error, R.string.card_read_fail);
                    }
                } catch (Exception e) {
                    getMvpView().hideDialog();
                    e.printStackTrace();
                    getMvpView().showMessage(R.string.card_error_read_data);
                }
            }

            @Override
            public void onFail(String TAG, String msg) {
                getMvpView().hideDialog();
            }
        }, true);


    }

    @Override
    public void writeCardDetails(String newPass, String identification) {
        try {
            nfcReader.doReadCardData(NFCReader.PERSONAL_DETAIL, new NFCReadDataListener() {
                @Override
                public void onSuccess(String data) {
                    if (data != null && !data.isEmpty()) {
                        JSONObject object1;
                        try {
                            object1 = new JSONObject(data);
                            String identification1 = object1.getString("rationalNo");
                            if (identification.equalsIgnoreCase(identification1)) {
                                nfcReader.doReadCardData(NFCReader.CARD_PIN, new NFCReadDataListener() {
                                    @Override
                                    public void onSuccess(String data) {
                                        getMvpView().hideDialog();
                                        try {
                                            JSONObject object = new JSONObject(data);
                                            if (object.length() > 0) {
                                                object.put("pin", newPass);
                                                nfcReader.doWriteCardData(NFCReader.CARD_PIN, object.toString(), new NFCListener() {
                                                    @Override
                                                    public void onSuccess(int flag) {
                                                        if (flag == 0)
                                                            getMvpView().show(getMvpView().sweetAlert(2, R.string.success, R.string.change_password_successfully)
                                                                    .setConfirmClickListener(sweetAlertDialog -> getMvpView().changePasswordSuccess()));
                                                        else
                                                            getMvpView().showMessage(R.string.UnableToWrite);
                                                    }

                                                    @Override
                                                    public void onFail(String TAG, String msg) {

                                                    }
                                                }, false);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFail(String TAG, String msg) {

                                    }
                                }, false);

                            } else {
                                getMvpView().hideDialog();
                                getMvpView().showMessage(R.string.tap_right_card);
                            }
                        } catch (JSONException e) {
                            getMvpView().hideDialog();
                            e.printStackTrace();
                        }


                    } else {
                        getMvpView().hideDialog();
                        getMvpView().showMessage(R.string.error, R.string.card_error_write_data);
                    }
                }

                @Override
                public void onFail(String TAG, String msg) {
                    getMvpView().hideDialog();
                }
            }, true);

        } catch (Exception e) {
            getMvpView().hideDialog();
            getMvpView().showMessage(R.string.some_error);
        }
    }
}
