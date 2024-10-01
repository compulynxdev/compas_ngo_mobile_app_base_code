package com.compastbc.nfcprint.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;

import androidx.annotation.StringRes;

import com.compastbc.nfcprint.R;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.nfcprint.nfc.nfc_pos.CardPreparationListener;
import com.compastbc.nfcprint.nfc.nfc_pos.CardReadCallback;
import com.compastbc.nfcprint.nfc.nfc_pos.NFCPosSdk;
import com.compastbc.nfcprint.nfc.nfc_saral.NFCSaralSdk;
import com.compastbc.nfcprint.nfc.nfc_saral.SaralReadCardDataListener;
import com.compastbc.nfcprint.nfc.nfc_saral.SaralReadListDataListener;
import com.compastbc.nfcprint.nfc.nfc_saral.SaralWriteDataListener;
import com.pos.device.SDKException;
import com.pos.device.beeper.Beeper;
import com.pos.device.picc.MifareDesfire;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Hemant Sharma on 07-02-20.
 * Divergent software labs pvt. ltd
 */
public final class NFCReader implements NFCCallback {

    /*NFC card Write data constant*/
    public final static int CARD_PIN = 2;
    public final static int PERSONAL_DETAIL = 0;
    public final static int CARD_DATA = 1;

    private static NFCReader instance;
    private final String modelName;
    private Activity activity;
    private NFCPosSdk nfcPosSdk;
    private NFCSaralSdk nfcSaralSdk;
    private Intent intent;
    private SweetAlertDialog sweetAlertDialog;

    //��������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������

    private NFCReader(Activity activity) {
        setActivity(activity);
        modelName = Build.MODEL;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk = NFCPosSdk.getInstance();
                break;

            case AppConstants.MODEL_SARAL:
            default:
                nfcSaralSdk = NFCSaralSdk.getInstance(activity);
                break;
        }
    }

    public static NFCReader getInstance(Activity activity) {
        if (instance == null)
            instance = new NFCReader(activity);
        else instance.setActivity(activity);
        return instance;
    }

    private void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void onNfcStatusListener(String TAG, NFCVerifyCallback callback) {
        if (callback == null) return;
        switch (modelName) {

            case AppConstants.MODEL_NEWPOS:
                callback.onNfcEnable(TAG);
                break;

            case AppConstants.MODEL_SUNMI:
            case AppConstants.MODEL_SARAL:
            default:
                NfcManager manager = (NfcManager) activity.getSystemService(Context.NFC_SERVICE);
                assert manager != null;
                NfcAdapter adapter = manager.getDefaultAdapter();
                if (adapter == null) {
                    //NFC not supported
                    callback.onNfcNotSupported();
                } else if (adapter.isEnabled()) {
                    // adapter exists and is enabled.
                    callback.onNfcEnable(TAG);
                } else {
                    //NFC Disable
                    callback.onNFcDisable();
                }
                break;
        }
    }

    @Override
    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    @Override
    public void doActivateCard(String personalData, String cardPin, NFCListener nfcListener, Boolean beep, Boolean isCardDataAlreadyStored) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }
                        nfcListener.onSuccess(nfcPosSdk.doActivateCard(mifareDesfire, personalData, cardPin, isCardDataAlreadyStored));
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doActivateCard(personalData, cardPin, nfcListener, beep, isCardDataAlreadyStored);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doActivateCard(personalData, cardPin, nfcListener, beep, isCardDataAlreadyStored);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doActivateCard(personalData, cardPin, nfcListener, beep, isCardDataAlreadyStored);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doActivateCard(intent, personalData, cardPin, isCardDataAlreadyStored, new SaralWriteDataListener() {
                        @Override
                        public void onDataReceived(int flag) {
                            nfcListener.onSuccess(flag);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    });
                    intent = null;
                }
                break;
        }
    }

    @Override
    public void doReadCardDataForActivate(NFCReadListDataOrErrorListener nfcListener, Boolean beep) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }

                        nfcPosSdk.doReadCardData(mifareDesfire, new NFCReadListDataOrErrorListener() {
                            @Override
                            public void onSuccessRead(List<String> data) {
                                nfcListener.onSuccessRead(data);
                            }

                            @Override
                            public void onFail(String TAG, String msg) {
                                nfcListener.onFail("ReadingError", activity.getString(R.string.card_read_fail));
                                sweetAlert(R.string.alert, activity.getString(R.string.card_read_fail))
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            doReadCardDataForActivate(nfcListener, beep);
                                        }).show();
                            }

                            @Override
                            public void cardNotActivated() {
                                nfcListener.cardNotActivated();
                            }
                        }, NFCReader.PERSONAL_DETAIL, NFCReader.CARD_DATA);
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardDataForActivate(nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardDataForActivate(nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardDataForActivate(nfcListener, beep);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doReadCardData(intent, new SaralReadListDataListener() {
                        @Override
                        public void onDataReceived(List<String> data) {
                            nfcListener.onSuccessRead(data == null ? new ArrayList<>() : data);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }

                        @Override
                        public void cardNotActivated() {
                            nfcListener.cardNotActivated();
                        }
                    }, NFCReader.PERSONAL_DETAIL, NFCReader.CARD_DATA);
                    intent = null;
                }
                break;
        }
    }

    @Override
    public void doReadCardDataForActivate(int fileName, NFCReadDataListener nfcListener, Boolean beep) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }
                        nfcPosSdk.doReadCardData(mifareDesfire, fileName, new CardReadCallback() {
                            @Override
                            public void onReadSuccess(String data) {
                                nfcListener.onSuccess(data == null ? "" : data);
                            }

                            @Override
                            public void cardNotActivated() {
                                //set blank means need to format this card before activation
                                nfcListener.onSuccess("");
                            }

                            @Override
                            public void cardReadFail() {
                                nfcListener.onFail("ReadingError", activity.getString(R.string.card_read_fail));
                                sweetAlert(R.string.alert, activity.getString(R.string.card_read_fail))
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            doReadCardDataForActivate(fileName, nfcListener, beep);
                                        }).show();
                            }
                        });
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardDataForActivate(fileName, nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardDataForActivate(fileName, nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardDataForActivate(fileName, nfcListener, beep);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doReadCardData(intent, fileName, new SaralReadCardDataListener() {

                        @Override
                        public void onDataReceived(String data) {
                            nfcListener.onSuccess(data);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }

                        @Override
                        public void cardNotActivated() {
                            //set blank means need to format this card before activation
                            nfcListener.onSuccess("");
                        }
                    });
                    intent = null;
                }
                break;
        }
    }

    @Override
    public void doReadCardData(int fileName, NFCReadDataListener nfcListener, Boolean beep) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }
                        nfcPosSdk.doReadCardData(mifareDesfire, fileName, new CardReadCallback() {
                            @Override
                            public void onReadSuccess(String data) {
                                nfcListener.onSuccess(data);
                            }

                            @Override
                            public void cardNotActivated() {
                                nfcListener.onFail("CardNotActivated", activity.getString(R.string.cardNotActivated));
                                sweetAlert(R.string.alert, activity.getString(R.string.cardNotActivated))
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            doReadCardData(fileName, nfcListener, beep);
                                        }).show();
                            }

                            @Override
                            public void cardReadFail() {
                                nfcListener.onFail("ReadingError", activity.getString(R.string.card_read_fail));
                                sweetAlert(R.string.alert, activity.getString(R.string.card_read_fail))
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            doReadCardData(fileName, nfcListener, beep);
                                        }).show();
                            }
                        });
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardData(fileName, nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardData(fileName, nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardData(fileName, nfcListener, beep);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doReadCardData(intent, fileName, new SaralReadCardDataListener() {
                        @Override
                        public void onDataReceived(String data) {
                            nfcListener.onSuccess(data);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }

                        @Override
                        public void cardNotActivated() {
                            nfcListener.onFail("CardNotActivated", activity.getString(R.string.cardNotActivated));
                            sweetAlert(R.string.alert, activity.getString(R.string.cardNotActivated))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    });
                    intent = null;
                }
                break;
        }
    }

    @Override
    public void doReadCardByList(NFCReadListDataListener nfcListener, Boolean beep, int... fileName) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }
                        nfcPosSdk.doReadCardData(mifareDesfire, new NFCReadListDataOrErrorListener() {
                            @Override
                            public void onSuccessRead(List<String> data) {
                                nfcListener.onSuccessRead(data);
                            }

                            @Override
                            public void onFail(String TAG, String msg) {
                                nfcListener.onFail("ReadingError", activity.getString(R.string.card_read_fail));
                                sweetAlert(R.string.alert, activity.getString(R.string.card_read_fail))
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            doReadCardByList(nfcListener, beep, fileName);
                                        }).show();
                            }

                            @Override
                            public void cardNotActivated() {
                                nfcListener.onFail("CardNotActivated", activity.getString(R.string.cardNotActivated));
                                sweetAlert(R.string.alert, activity.getString(R.string.cardNotActivated))
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            doReadCardByList(nfcListener, beep, fileName);
                                        }).show();
                            }
                        }, fileName);
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardByList(nfcListener, beep, fileName);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardByList(nfcListener, beep, fileName);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doReadCardByList(nfcListener, beep, fileName);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doReadCardData(intent, new SaralReadListDataListener() {
                        @Override
                        public void onDataReceived(List<String> data) {
                            nfcListener.onSuccessRead(data);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }

                        @Override
                        public void cardNotActivated() {
                            nfcListener.onFail("CardNotActivated", activity.getString(R.string.cardNotActivated));
                            sweetAlert(R.string.alert, activity.getString(R.string.cardNotActivated))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    }, fileName);
                    intent = null;
                }
                break;
        }
    }

    @Override
    public void doWriteCardData(int fileName, String data, NFCListener nfcListener, Boolean beep) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }
                        nfcListener.onSuccess(nfcPosSdk.doWriteCardData(mifareDesfire, fileName, data));
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doWriteCardData(fileName, data, nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doWriteCardData(fileName, data, nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doWriteCardData(fileName, data, nfcListener, beep);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doWriteCardData(intent, fileName, data, new SaralWriteDataListener() {
                        @Override
                        public void onDataReceived(int flag) {
                            nfcListener.onSuccess(flag);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    });
                    intent = null;
                }
                break;
        }
    }

    @Override
    public void doDeleteFile(NFCListener nfcListener, Boolean beep, int... fileName) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }
                        nfcListener.onSuccess(nfcPosSdk.doDeleteFile(mifareDesfire, fileName));
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doFormat(nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doFormat(nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doFormat(nfcListener, beep);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doDeleteFile(intent, new SaralWriteDataListener() {
                        @Override
                        public void onDataReceived(int flag) {
                            nfcListener.onSuccess(flag);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    }, fileName);
                    intent = null;
                }
                break;
        }
    }

    @Override
    public void doFormat(NFCListener nfcListener, Boolean beep) {
        if (nfcListener == null) return;
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.getCard(new CardPreparationListener() {

                    @Override
                    public void onCardReady(MifareDesfire mifareDesfire) {
                        if (beep) {
                            try {
                                Beeper.getInstance().beep(3000, 500);
                            } catch (SDKException e) {
                                e.printStackTrace();
                            }
                        }
                        nfcListener.onSuccess(nfcPosSdk.doFormat(mifareDesfire));
                    }

                    @Override
                    public void onTimeOutError() {
                        nfcListener.onFail("TimeOutError", activity.getString(R.string.timeout_error));
                        sweetAlert(R.string.alert, activity.getString(R.string.timeout_error))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doFormat(nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onUnsupportedCard(String cardName) {
                        nfcListener.onFail("UnsupportedCard", cardName.concat(activity.getString(R.string.not_supported)));
                        sweetAlert(R.string.alert, cardName.concat(activity.getString(R.string.not_supported)))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doFormat(nfcListener, beep);
                                }).show();
                    }

                    @Override
                    public void onCardFail() {
                        nfcListener.onFail("CardFail", activity.getString(R.string.card_error_data));
                        sweetAlert(R.string.alert, activity.getString(R.string.card_error_data))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    doFormat(nfcListener, beep);
                                }).show();
                    }
                });
                break;

            case AppConstants.MODEL_SARAL:
            default:
                if (intent != null) {
                    nfcSaralSdk.doFormat(intent, new SaralWriteDataListener() {
                        @Override
                        public void onDataReceived(int flag) {
                            nfcListener.onSuccess(flag);
                        }

                        @Override
                        public void onUnsupportedCard(int msg) {
                            nfcListener.onFail("UnsupportedCard", activity.getString(msg));
                            sweetAlert(R.string.alert, activity.getString(msg))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    });
                    intent = null;
                }
                break;
        }
    }


    @Override
    public void closeNfcReader() {
        switch (modelName) {
            case AppConstants.MODEL_SUNMI:
                break;

            case AppConstants.MODEL_NEWPOS:
                nfcPosSdk.closeNfcReader();
                break;

            case AppConstants.MODEL_SARAL:
            default:
                nfcSaralSdk.closeNfcReader();
                break;
        }
    }

    private SweetAlertDialog sweetAlert(@StringRes int title, String content) {
        if (sweetAlertDialog != null && sweetAlertDialog.isShowing()) sweetAlertDialog.dismiss();

        sweetAlertDialog = new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(activity.getString(title))
                .setContentText(content)
                .setConfirmText(activity.getString(R.string.Ok));

        sweetAlertDialog.setCancelable(false);
        return sweetAlertDialog;
    }
}
