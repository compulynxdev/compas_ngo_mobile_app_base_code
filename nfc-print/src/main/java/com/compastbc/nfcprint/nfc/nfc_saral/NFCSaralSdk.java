package com.compastbc.nfcprint.nfc.nfc_saral;

import android.app.Activity;
import android.content.Intent;

import com.compastbc.nfcprint.R;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.DESFireFile;
import com.nxp.nfclib.desfire.EV1ApplicationKeySettings;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.interfaces.IKeyData;

import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import static com.nxp.nfclib.CardType.DESFireEV1;

public final class NFCSaralSdk {
    /*NFC Constants*/
    private static final String KEY_APP_MASTER = "This is my key";
    private static final String ALIAS_KEY_AES128 = "key_aes_128";
    private static final String ALIAS_KEY_2KTDES = "key_2ktdes";
    private static final String ALIAS_KEY_2KTDES_ULC = "key_2ktdes_ulc";
    private static final String ALIAS_DEFAULT_FF = "alias_default_ff";
    private static final String ALIAS_KEY_AES128_ZEROES = "alias_default_00";

    private static NFCSaralSdk INSTANCE = null;
    private final byte[] appId = new byte[]{0x1, 0x00, 0x00};
    private NxpNfcLib libNFCInstance = null;
    private IDESFireEV1 desFireEV1;
    private IKeyData objKEY_2KTDES = null;
    private int timeOut = 2000;
    //public static int TXN_HISTORY = 10;

    private NFCSaralSdk() {
    }

    private NFCSaralSdk(Activity activity) {
        initialiseNFC(activity);
        initializeKeys(activity);
        initializeCipherInitVector();
    }

    public static NFCSaralSdk getInstance(Activity activity) {
        if (INSTANCE == null)
            return INSTANCE = new NFCSaralSdk(activity);

        return INSTANCE;
    }

    public NxpNfcLib getLibNFCInstance() {
        return libNFCInstance;
    }

    private void initialiseNFC(Activity activity) {
        if (libNFCInstance == null) {
            libNFCInstance = NxpNfcLib.getInstance();
            try {
                //String packageKey = "b927aab8842ab54cbaf2ea1df9917159";
                libNFCInstance.registerActivity(activity, activity.getString(R.string.nfc_package_key));
            } catch (NxpNfcLibException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initializeKeys(Activity activity) {
        KeyInfoProvider infoProvider = KeyInfoProvider.getInstance(activity.getApplicationContext());
        infoProvider.setKey(ALIAS_KEY_2KTDES, SampleAppKeys.EnumKeyType.EnumDESKey, SampleAppKeys.KEY_2KTDES);
        infoProvider.setKey(ALIAS_KEY_AES128, SampleAppKeys.EnumKeyType.EnumAESKey, SampleAppKeys.KEY_AES128);
        infoProvider.setKey(ALIAS_KEY_AES128_ZEROES, SampleAppKeys.EnumKeyType.EnumAESKey, SampleAppKeys.KEY_AES128_ZEROS);
        infoProvider.setKey(ALIAS_DEFAULT_FF, SampleAppKeys.EnumKeyType.EnumMifareKey, SampleAppKeys.KEY_DEFAULT_FF);
        //Initialise keys
        infoProvider.getKey(ALIAS_KEY_2KTDES_ULC, SampleAppKeys.EnumKeyType.EnumDESKey);
        objKEY_2KTDES = infoProvider.getKey(ALIAS_KEY_2KTDES, SampleAppKeys.EnumKeyType.EnumDESKey);
        infoProvider.getKey(ALIAS_KEY_AES128, SampleAppKeys.EnumKeyType.EnumAESKey);
        infoProvider.getKey(ALIAS_KEY_AES128_ZEROES, SampleAppKeys.EnumKeyType.EnumAESKey);
        infoProvider.getMifareKey(ALIAS_DEFAULT_FF);
    }

    private void initializeCipherInitVector() {
        try {
            //initialise cipher vector
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        /* set Application Master Key */
        byte[] bytesKey = KEY_APP_MASTER.getBytes();

        /* Initialize init vector of 16 bytes with 0xCD. It could be anything */
        byte[] ivSpec = new byte[16];
        Arrays.fill(ivSpec, (byte) 0xCD);
        IvParameterSpec iv = new IvParameterSpec(ivSpec);
    }

    private IDESFireEV1 getDesFireEV(Intent intent) {
        if (libNFCInstance == null) return null;
        try {
            CardType type = libNFCInstance.getCardType(intent);

            if (type == DESFireEV1) {
                return DESFireFactory.getInstance().getDESFire(libNFCInstance.getCustomModules());
            }
        } catch (NxpNfcLibException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (byte aByte : bytes) {
            long value = aByte & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

    private String ByteArrayToHexString(byte[] inarray) {
        AppLogger.d("ByteArrayToHexString", Arrays.toString(inarray));

        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        StringBuilder out = new StringBuilder();

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out.append(hex[i]);
            i = in & 0x0f;
            out.append(hex[i]);
        }
        AppLogger.d("ByteArrayToHexString", String.format(Locale.US, "%0" + (inarray.length * 2) + "X", new BigInteger(1, inarray)));
        return out.toString();
    }

    private boolean isEmptyByteArray(byte[] byteArray) {
        for (byte b : byteArray) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void doFormat(Intent intent, SaralWriteDataListener listener) {
        if (listener == null) return;
        closeNfcReader();
        desFireEV1 = getDesFireEV(intent);
        if (desFireEV1 == null) listener.onUnsupportedCard(R.string.card_not_supported);

        try {
            desFireEV1.getReader().setTimeout(timeOut);
            desFireEV1.selectApplication(0);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);
            desFireEV1.format();
            listener.onDataReceived(0);
        } catch (Exception e) {
            listener.onDataReceived(1);
        }
    }

    public void doDeleteFile(Intent intent, SaralWriteDataListener listener, int... fileName) {
        if (listener == null) return;
        closeNfcReader();
        desFireEV1 = getDesFireEV(intent);
        if (desFireEV1 == null) listener.onUnsupportedCard(R.string.card_not_supported);
        try {
            desFireEV1.getReader().setTimeout(timeOut);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            desFireEV1.selectApplication(0);
            desFireEV1.selectApplication(appId);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            for (int file : fileName) {
                desFireEV1.deleteFile(file);
            }

            listener.onDataReceived(0);
        } catch (Exception e) {
            listener.onDataReceived(1);
        }
    }

    public void doWriteCardData(Intent intent, int fileName, String data, SaralWriteDataListener listener) {
        if (listener == null) return;
        closeNfcReader();
        desFireEV1 = getDesFireEV(intent);
        if (desFireEV1 == null) listener.onUnsupportedCard(R.string.card_not_supported);
        try {
            desFireEV1.getReader().setTimeout(timeOut);
            desFireEV1.selectApplication(0);
            desFireEV1.selectApplication(appId);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            desFireEV1.writeData(fileName, 0, data.getBytes());
            listener.onDataReceived(0);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onDataReceived(1);
        }
    }

    public void doWriteCardData(Intent intent, HashMap<Integer, String> data, SaralWriteDataListener listener) {
        if (listener == null) return;
        closeNfcReader();
        desFireEV1 = getDesFireEV(intent);
        if (desFireEV1 == null) listener.onUnsupportedCard(R.string.card_not_supported);
        try {
            desFireEV1.getReader().setTimeout(timeOut);
            desFireEV1.selectApplication(0);
            desFireEV1.selectApplication(appId);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            for (Map.Entry<Integer, String> tmp : data.entrySet()) {
                desFireEV1.writeData(tmp.getKey(), 0, tmp.getValue().getBytes());
            }
            listener.onDataReceived(0);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onDataReceived(1);
        }
    }

    public void doActivateCard(Intent intent, String personalData, String cardPin, boolean isCardDataAlreadyStored, SaralWriteDataListener listener) {
        if (listener == null) return;
        closeNfcReader();
        desFireEV1 = getDesFireEV(intent);
        if (desFireEV1 == null) listener.onUnsupportedCard(R.string.card_not_supported);

        //if card has no data then setup data else just authnticate and create remaining file
        if (!isCardDataAlreadyStored) {
            desFireEV1.selectApplication(0);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            EV1ApplicationKeySettings.Builder appSetBuilder = new EV1ApplicationKeySettings.Builder();
            EV1ApplicationKeySettings appSettings = appSetBuilder.setAppKeySettingsChangeable(true)
                    .setAppMasterKeyChangeable(true)
                    .setAuthenticationRequiredForDirectoryConfigurationData(false)
                    .setKeyTypeOfApplicationKeys(KeyType.TWO_KEY_THREEDES).build();
            desFireEV1.createApplication(appId, appSettings);
            desFireEV1.selectApplication(appId);
        } else {
            desFireEV1.selectApplication(appId);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
        }

        //Create files
        desFireEV1.createFile(NFCReader.PERSONAL_DETAIL, new DESFireFile.StdDataFileSettings(
                IDESFireEV1.CommunicationType.Plain, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 1000));
        desFireEV1.createFile(NFCReader.CARD_PIN, new DESFireFile.StdDataFileSettings(
                IDESFireEV1.CommunicationType.Plain, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 100));

        if (!isCardDataAlreadyStored) {
            desFireEV1.createFile(NFCReader.CARD_DATA, new DESFireFile.StdDataFileSettings(
                    IDESFireEV1.CommunicationType.Plain, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 3000));
        }

        desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
        try {
            desFireEV1.writeData(NFCReader.PERSONAL_DETAIL, 0, personalData.trim().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            desFireEV1.writeData(NFCReader.CARD_PIN, 0, cardPin.trim().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!isCardDataAlreadyStored) {
            try {
                JSONObject object = new JSONObject();
                desFireEV1.writeData(NFCReader.CARD_DATA, 0, object.toString().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        closeNfcReader();
        listener.onDataReceived(0);
    }

    public void doReadCardData(Intent intent, int fileName, SaralReadCardDataListener listener) {
        if (listener == null) return;
        closeNfcReader();
        desFireEV1 = getDesFireEV(intent);
        if (desFireEV1 == null) listener.onUnsupportedCard(R.string.card_not_supported);
        try {
            desFireEV1.getReader().setTimeout(timeOut);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            desFireEV1.selectApplication(0);
            desFireEV1.selectApplication(appId);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            byte[] byteArray = desFireEV1.readData(fileName, 0, 0);

            String data = isEmptyByteArray(byteArray) ? "" : new String(byteArray);
            listener.onDataReceived(data.trim());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("Application Not Found")) {
                listener.cardNotActivated();
            } else {
                listener.onDataReceived("");
            }
        }
    }

    public void doReadCardData(Intent intent, SaralReadListDataListener listener, int... fileNames) {
        if (listener == null) return;
        closeNfcReader();
        desFireEV1 = getDesFireEV(intent);
        if (desFireEV1 == null) listener.onUnsupportedCard(R.string.card_not_supported);

        List<String> list = null;
        try {
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);
            desFireEV1.getReader().setTimeout(timeOut);
            desFireEV1.selectApplication(0);
            desFireEV1.selectApplication(appId);
            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, objKEY_2KTDES);

            list = new ArrayList<>();
            for (int readDataFrom : fileNames) {
                try {
                    String data = new String(desFireEV1.readData(readDataFrom, 0, 0));
                    list.add(data.trim());
                } catch (Exception e) {
                    list.add("");
                }
            }
            closeNfcReader();
            listener.onDataReceived(list);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("Application Not Found")) {
                listener.cardNotActivated();
            } else {
                listener.onDataReceived(list);
            }
        }
    }

    public void closeNfcReader() {
        if (desFireEV1 != null)
            desFireEV1.getReader().close();
    }
}

