package com.compastbc.nfcprint.nfc.nfc_saral;

/**
 * Keys used by the Sample Application are declared here.
 * Created by nxp69547 on 8/3/2016.
 */
public final class SampleAppKeys {

    /**
     * 16 bytes 2KTDES_ULC Key.
     */
    public static final byte[] KEY_2KTDES_ULC = {(byte) 0x49, (byte) 0x45,
            (byte) 0x4D, (byte) 0x4B, (byte) 0x41, (byte) 0x45, (byte) 0x52,
            (byte) 0x42, (byte) 0x21, (byte) 0x4E, (byte) 0x41, (byte) 0x43,
            (byte) 0x55, (byte) 0x4F, (byte) 0x59, (byte) 0x46};
    public static final byte[] SECRET_KEY_AES128 = {(byte) 0x20, (byte) 0x62,
            (byte) 0x2A, (byte) 0x48, (byte) 0xB4, (byte) 0x4E, (byte) 0x2B,
            (byte) 0x2D, (byte) 0xC8, (byte) 0x2E, (byte) 0xAB, (byte) 0x36,
            (byte) 0x49, (byte) 0x24, (byte) 0x9B, (byte) 0xED};
    /**
     * Default key with Value FF.
     */
    static final byte[] KEY_DEFAULT_FF = {(byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * 16 bytes AES128 Key.
     */
    static final byte[] KEY_AES128 = {(byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * 16 bytes AES128 Key.
     */
    static final byte[] KEY_AES128_ZEROS = {(byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    /**
     * 24 bytes 2KTDES Key.
     */
    static final byte[] KEY_2KTDES = {(byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    /**
     * Private constructor restricts Implementation.
     */
    private SampleAppKeys() {

    }


    /**
     * Only these types of Keys can be stored by the Helper class.
     */
    public enum EnumKeyType {
        EnumAESKey,
        EnumDESKey,
        EnumMifareKey
    }

}
