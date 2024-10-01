package com.compastbc.nfcprint.nfc.nfc_pos;

import com.compastbc.core.utils.AppLogger;
import com.compastbc.nfcprint.nfc.NFCReadListDataOrErrorListener;
import com.compastbc.nfcprint.nfc.NFCReadStatus;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.pos.device.SDKException;
import com.pos.device.picc.MifareDesfire;
import com.pos.device.picc.PiccReader;
import com.pos.device.picc.PiccReaderCallback;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Deflater;

/**
 * Created by Hemant Sharma on 25-09-19.
 * Divergent software labs pvt. ltd
 */
public final class NFCPosSdk {
    private static final String TAG = "NFCPosSdk";
    private static NFCPosSdk ourInstance;
    //NFC mode for pos device
    private final int timeout = 50000;  //300000 maximum 5 min to read card data
    //application id's
    private final byte[] appId = new byte[]{0x1, 0x00, 0x00};
    private final byte[] appIdZero = new byte[]{0x00, 0x00, 0x00};
    private PiccReader piccReader;
    private MifareDesfire mifareDesfire;
    private boolean isAppSelect = false;

    private NFCPosSdk() {

    }

    public static NFCPosSdk getInstance() {
        if (ourInstance == null) {
            ourInstance = new NFCPosSdk();
        }
        return ourInstance;
    }

    private static byte[] addAll(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + (array2 == null ? 0 : array2.length));
        assert array2 != null;
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    private MifareDesfire getMifareDesfire() {
        if (mifareDesfire == null) {
            try {
                mifareDesfire = MifareDesfire.connect();
            } catch (SDKException e) {
                e.printStackTrace();
            }
        }
        return mifareDesfire;
    }

    private void init() {
        closeNfcReader();
        if ((CardType.INMODE_NFC.getVal()) != 0) {
            piccReader = PiccReader.getInstance();
        }
    }

    public void getCard(CardPreparationListener cardPreparationListener) {
        if (cardPreparationListener == null) return;
        mifareDesfire = null;
        isAppSelect = false;
        init();
        new Thread() {
            @Override
            public void run() {
                if ((CardType.INMODE_NFC.getVal()) != 0) {
                    piccReader.startSearchCard(timeout, (flag, nfcType) -> {
                        switch (flag) {
                            case PiccReaderCallback.SUCCESS:
                                if (getMifareDesfire() == null) {
                                    cardPreparationListener.onCardFail();
                                } else {
                                    if (nfcType == PiccReader.MIFARE_DESFIRE) {
                                        cardPreparationListener.onCardReady(getMifareDesfire());
                                    } else {
                                        updateNotSupportedCardStatus(cardPreparationListener, nfcType);
                                    }
                                }
                                break;

                            /*case PiccReaderCallback.USER_CANCEL:
                                cardPreparationListener.onUserCancel();
                                break;*/

                            case PiccReaderCallback.TIMEOUT_ERROR:
                                cardPreparationListener.onTimeOutError();
                                break;

                            case PiccReaderCallback.UNSUPPORTED_CARD:
                                updateNotSupportedCardStatus(cardPreparationListener, 100);
                                break;
                        }
                    });
                } else {
                    cardPreparationListener.onCardFail();
                }
            }
        }.start();
    }

    private void updateNotSupportedCardStatus(CardPreparationListener cardPreparationListener, int nfcType) {
        switch (nfcType) {
            case PiccReader.MIFARE_ONE_S50:
                cardPreparationListener.onUnsupportedCard("MIFARE_ONE_S50 ");
                break;

            case PiccReader.MIFARE_ONE_S70:
                cardPreparationListener.onUnsupportedCard("MIFARE_ONE_S70 ");
                break;

            case PiccReader.UNKNOWN_TYPEA:
                cardPreparationListener.onUnsupportedCard("UNKNOWN_TYPEA ");
                break;

            case PiccReader.MIFARE_ULTRALIGHT:
                cardPreparationListener.onUnsupportedCard("MIFARE_ULTRALIGHT ");
                break;

            default:
                cardPreparationListener.onUnsupportedCard("UNSUPPORTED_CARD ");
                break;
        }
    }

    //0 success or 1 fail
    public int doActivateCard(MifareDesfire mifareDesfire, String personalDetail, String cardPin, boolean isCardDataAlreadyStored) {
        int ret;

        int keyNo = 0;
        byte[] key = new byte[16];
        int selectApp0 = mifareDesfire.selectApplication(appIdZero);
        AppLogger.d(TAG, "  on selecting the app0  " + selectApp0);

        boolean onauthentication = mifareDesfire.authenticate(keyNo, key);
        AppLogger.d(TAG, " on authenticating the app0 " + onauthentication);

        MifareDesfire.DesfireAppInfo appinfo = new MifareDesfire.DesfireAppInfo();
        appinfo.AID = appId;
        appinfo.masterKeySetting = 0;
        appinfo.cryptoMode = 0;
        appinfo.numberOfKey = 1;
        MifareDesfire.DesfireStdBackupfileInfo fileSettings = new MifareDesfire.DesfireStdBackupfileInfo();
        fileSettings.changeAccessRightKeyNo = 0;
        fileSettings.communicationSettings = 0;
        fileSettings.readAndWriteAccessRightKeyNo = 0;
        ret = mifareDesfire.createApplication(appinfo);
        ret = mifareDesfire.selectApplication(appId);
        AppLogger.d(TAG, "Return value on creating application   " + ret);
        onauthentication = mifareDesfire.authenticate(keyNo, key);
        AppLogger.d(TAG, " on authenticating the app1 " + onauthentication);

        fileSettings.fileSize = 1000;
        ret = mifareDesfire.createStdDatafile(NFCReader.PERSONAL_DETAIL, fileSettings);
        AppLogger.d(TAG, "Return value on creating PERSONAL_DETAIL   " + ret);
        fileSettings.fileSize = 100;
        ret = mifareDesfire.createStdDatafile(NFCReader.CARD_PIN, fileSettings);
        AppLogger.d(TAG, "Return value on creating CARD_PIN   " + ret);

        if (!isCardDataAlreadyStored) {
            fileSettings.fileSize = 3000;
            ret = mifareDesfire.createStdDatafile(NFCReader.CARD_DATA, fileSettings);
            AppLogger.d(TAG, "Return value on creating CARD_DATA   " + ret);
        }

        ret = mifareDesfire.writeData(NFCReader.PERSONAL_DETAIL, 0, 0, personalDetail.getBytes());
        AppLogger.d(TAG, "Return value on writing the data to the PERSONAL_DETAIL   " + ret);
        ret = mifareDesfire.writeData(NFCReader.CARD_PIN, 0, 0, cardPin.getBytes());
        AppLogger.d(TAG, "Return value on writing the data to the CARD_PIN   " + ret);

        if (!isCardDataAlreadyStored) {
            JSONObject object = new JSONObject();
            ret = mifareDesfire.writeData(NFCReader.CARD_DATA, 0, 0, object.toString().getBytes());
        }

        closeNfcReader();
        return ret;
    }

    //0 success or 1 fail or -1 for card
    public int doWriteCardData(MifareDesfire mifareDesfire, int fileNo, String data) {
        if (!isAppSelect) selectApp();
        int ret = mifareDesfire.writeData(fileNo, 0, 0, data.getBytes());
        AppLogger.e(TAG, "doWriteCardData : " + ret);
        return ret;
    }

    private int selectApp() {
        int keyNo = 0;
        int ret;
        byte[] key = new byte[16];
        int selectApp0 = mifareDesfire.selectApplication(appIdZero);
        AppLogger.d(TAG, "selectApp 1 :" + selectApp0);
        byte[] uid = mifareDesfire.getUID();
        String cardUID = byteToString(uid);
        AppLogger.d(TAG, "selectApp 2 :" + cardUID);
        boolean onAuthentication = mifareDesfire.authenticate(keyNo, key);
        AppLogger.d(TAG, "selectApp 3 :" + onAuthentication);
        ret = mifareDesfire.selectApplication(appId);
        AppLogger.d(TAG, "selectApp 4 :" + ret);
        onAuthentication = mifareDesfire.authenticate(keyNo, key);
        AppLogger.d(TAG, "selectApp 5 :" + onAuthentication);

        isAppSelect = true;

        return ret;
    }

    private void selectApp(NFCReadStatus statusListener) {
        int keyNo = 0;
        int ret;
        byte[] key = new byte[16];
        int selectApp0 = mifareDesfire.selectApplication(appIdZero);
        AppLogger.d(TAG, "selectApp 1 :" + selectApp0);
        byte[] uid = mifareDesfire.getUID();
        String cardUID = byteToString(uid);
        AppLogger.d(TAG, "selectApp 2 :" + cardUID);
        boolean onAuthentication = mifareDesfire.authenticate(keyNo, key);
        AppLogger.d(TAG, "selectApp 3 :" + onAuthentication);
        ret = mifareDesfire.selectApplication(appId);
        AppLogger.d(TAG, "selectApp 4 :" + ret);
        onAuthentication = mifareDesfire.authenticate(keyNo, key);
        AppLogger.d(TAG, "selectApp 5 :" + onAuthentication);

        isAppSelect = true;

        if (ret == -1 && onAuthentication) {
            statusListener.cardNotActivated();
        } else if (ret == 0 && onAuthentication){
            statusListener.cardAuthenticated();
        } else {
            statusListener.cardReadFail();
        }
    }

    public void doReadCardData(MifareDesfire mifareDesfire, NFCReadListDataOrErrorListener nfcListener, int... fileNos) {
        selectApp(new NFCReadStatus() {
            @Override
            public void cardNotActivated() {
                nfcListener.cardNotActivated();
            }

            @Override
            public void cardAuthenticated() {
                List<String> list = new ArrayList<>();
                try {
                    for (int readDataFrom : fileNos) {
                        if (readDataFrom == NFCReader.PERSONAL_DETAIL) {
                            String data = read_personaldetails_file(mifareDesfire);
                            String finalData = data == null ? "" : data.trim();
                            list.add(finalData.trim());
                        } else if (readDataFrom == NFCReader.CARD_PIN) {
                            String data = read_cardpin_file(mifareDesfire);
                            String finalData = data == null ? "" : data.trim();
                            list.add(finalData);
                        } else if (readDataFrom == NFCReader.CARD_DATA) {
                            String data = read_carddata_file(mifareDesfire);
                            String finalData = data == null ? "" : data.trim();
                            list.add(finalData);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nfcListener.onSuccessRead(list);
            }

            @Override
            public void cardReadFail() {
                nfcListener.onFail("ReadingError", "");
            }
        });
    }

    public void doReadCardData(MifareDesfire mifareDesfire, int fileName, CardReadCallback cardListener) {
        if (cardListener == null) return;
        selectApp(new NFCReadStatus() {
            @Override
            public void cardNotActivated() {
                cardListener.cardNotActivated();
            }

            @Override
            public void cardAuthenticated() {
                if (fileName == NFCReader.PERSONAL_DETAIL) {
                    String data = read_personaldetails_file(mifareDesfire);
                    String finalData = data == null ? "" : data.trim();
                    cardListener.onReadSuccess(finalData);
                } else if (fileName == NFCReader.CARD_PIN) {
                    String data = read_cardpin_file(mifareDesfire);
                    String finalData = data == null ? "" : data.trim();
                    cardListener.onReadSuccess(finalData);
                } else if (fileName == NFCReader.CARD_DATA) {
                    String data = read_carddata_file(mifareDesfire);
                    String finalData = data == null ? "" : data.trim();
                    cardListener.onReadSuccess(finalData);
                } else {
                    cardListener.onReadSuccess("");
                }
            }

            @Override
            public void cardReadFail() {
                cardListener.cardReadFail();
            }
        });
    }

    public int doFormat(MifareDesfire mifareDesfire) {
        try {
            AppLogger.d("Format 1 :", "Space: " + mifareDesfire.getFreeMemory());
            int keyNo = 0;
            byte[] key = new byte[16];
            boolean authRet = mifareDesfire.authenticate(keyNo, key);
            AppLogger.d("Format 2 :", "authRet: " + authRet);
            int ret = mifareDesfire.formatPicc();
            AppLogger.d("Format 3 :", "Status: " + ret + "\nSpace: " + mifareDesfire.getFreeMemory());

            return ret;
        } catch (Exception e) {
            // description = "This card has not been format";
            AppLogger.d(TAG, "Format Exception 4 :" + e.toString());
            return -1;
        }
    }

    public int doDeleteFile(MifareDesfire mifareDesfire, int... fileName) {

        try {
            int ret = selectApp();
            for (int file : fileName) {
                ret = mifareDesfire.deleteFile(file);
            }
            AppLogger.d("FormatTest", "Status: " + ret + "\nSpace: " + mifareDesfire.getFreeMemory());

            return ret;
        } catch (Exception e) {
            // description = "This card has not been format";
            AppLogger.d(TAG, "Format exception" + e.toString());
            return -1;
        }
    }

    private String read_personaldetails_file(MifareDesfire desfire) {
        int fileName = NFCReader.PERSONAL_DETAIL;
        AppLogger.d("##data", "Before reading the card");
        byte[] outData0 = desfire.readData(fileName, 0, 0, 300);
        if (outData0 == null)
            return null;
        byte[] outData1 = desfire.readData(fileName, 0, 300, 300);
        if (outData1 == null)
            return new String(outData0);
        byte[] outData2 = desfire.readData(fileName, 0, 600, 900);
        if (outData2 == null) {
            return new String(addAll(outData0, outData1));
        }
        byte[] dest1 = addAll(outData0, outData1);
        byte[] dest2 = addAll(dest1, outData2);
        byte[] outData3 = desfire.readData(fileName, 0, 900, 100);
        if (outData3 == null) {
            return new String(dest2);
        }
        byte[] dest3 = addAll(dest2, outData3);
        return new String(dest3);

    }

    private String read_carddata_file(MifareDesfire desfire) {
        int fileName = NFCReader.CARD_DATA;
        AppLogger.d("##data", "Before reading the card");
        byte[] outData0 = desfire.readData(fileName, 0, 0, 300);
        if (outData0 == null)
            return null;
        byte[] outData1 = desfire.readData(fileName, 0, 300, 300);
        if (outData1 == null)
            return new String(outData0);
        byte[] outData2 = desfire.readData(fileName, 0, 600, 300);
        if (outData2 == null) {
            return new String(addAll(outData0, outData1));
        }
        byte[] dest1 = addAll(outData0, outData1);
        byte[] dest2 = addAll(dest1, outData2);
        byte[] outData3 = desfire.readData(fileName, 0, 900, 300);
        if (outData3 == null) {
            return new String(dest2);
        }
        byte[] dest3 = addAll(dest2, outData3);
        byte[] outData4 = desfire.readData(fileName, 0, 1200, 300);
        if (outData4 == null) {
            return new String(dest3);
        }
        byte[] dest4 = addAll(dest3, outData4);
        byte[] outData5 = desfire.readData(fileName, 0, 1500, 300);
        if (outData5 == null) {
            return new String(dest4);
        }
        byte[] dest5 = addAll(dest4, outData5);
        byte[] outData6 = desfire.readData(fileName, 0, 1800, 300);
        if (outData6 == null) {
            return new String(dest5);
        }
        byte[] dest6 = addAll(dest5, outData6);
        byte[] outData7 = desfire.readData(fileName, 0, 2100, 300);
        if (outData7 == null) {
            return new String(dest6);
        }
        byte[] dest7 = addAll(dest6, outData7);
        byte[] outData8 = desfire.readData(fileName, 0, 2400, 300);
        if (outData8 == null) {
            return new String(dest7);
        }
        byte[] dest8 = addAll(dest7, outData8);
        byte[] outData9 = desfire.readData(fileName, 0, 2700, 300);
        if (outData9 == null) {
            return new String(dest8);
        }
        byte[] dest9 = addAll(dest8, outData9);
        return new String(dest9);

    }

    private String read_cardpin_file(MifareDesfire desfire) {
        int fileName = NFCReader.CARD_PIN;
        AppLogger.d("##data", "Before reading the card");
        byte[] outData0 = desfire.readData(fileName, 0, 0, 100);
        if (outData0 == null)
            return null;
        else {
            return new String(outData0);
        }
    }

    private byte[] compress(String inputString) {
        byte[] input = inputString.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[input.length];
        Deflater compressed = new Deflater();
        compressed.setInput(input);
        compressed.finish();
        compressed.deflate(output);
        compressed.end();
        return output;
    }

    private String byteToString(byte[] uid) {
        StringBuilder uidhex = new StringBuilder();
        for (byte b : uid) {

            String x = Integer.toHexString(((int) b & 0xff));
            if (x.length() == 1) {
                x = '0' + x;
            }
            uidhex.append(x);
        }
        return uidhex.toString();
    }

    public void closeNfcReader() {
        try {
            mifareDesfire = null;
            if (piccReader != null) {
                piccReader.stopSearchCard();
                piccReader.release();
            }
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }
}
