package com.compastbc.core.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hemant
 * Date: 10/4/18.
 */

public final class AppConstants {

    //Pagination Limit
    public static final int LIMIT = 20;
    public static final int DOWNLOAD_LIMIT = 500;

    // common file and image path
    public static final int REQUEST_TAKE_PHOTO = 51;
    public static final int REQUEST_GALLERY = 52;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 53;

    // key for run time permissions
    public final static int REQUEST_CHECK_SETTINGS_GPS = 96;
    public static final int REQUEST_MULTIPLE_CAMERA_PERMISSIONS = 98;
    public final static int REQUEST_MULTIPLE_PERMISSIONS = 100;
    public final static int MY_PERMISSIONS_REQUEST_LOCATION = 101;
    public final static int MY_PERMISSIONS_REQUEST_CAMERA = 102;
    public final static String BASIC_AUTHORISATION = "Basic ZGV2Z2xhbi1jbGllbnQ6ZHVy";
    public static final String LANG_ENGLISH = "english";
    public static final String LANG_ARABIC = "arabic";

    //gmail credentials
    public static final String EMAIL = "digitalidentity.compulynx@gmail.com"; //your-gmail-username
    public static final String PASSWORD = "Imflp@2020"; //your-gmail-password
    //Synchronise
    public static final int PORT = 40818;
    public static final String FOLDER_NAME = "VendorData";
    public static final String FILE_NAME = "vendor_data.txt";
    public static final String ACK_FILE_NAME = "ack_data.txt";

    public static final String MODEL_NEWPOS = "NEW9220";
    public static final String MODEL_RG80 = "RG80";
    public static final String MODEL_SARAL = "S700";
    public static final String MODEL_SUNMI = "SUNMI";

    public static final List<String> listOfSupportedDevices = Arrays.asList(
            MODEL_RG80,
            MODEL_SARAL
    );

    public static final int TRANSACTION_VIEW = 0;
    public static final int UPDATE_VIEW = 1;
    public static final int CARD_ACTIVATION_VIEW = 2;
    public static final int BENEFICIARY_VIEW = 3;
    public static final int CHANGE_CARD_PIN_VIEW = 4;
    public static final int CARD_BALANCE_VIEW = 5;
    public static final int VOID_TRANSACTION_VIEW = 6;
    public static final int CHANGE_AGENT_PWD_VIEW = 7;
    public static final int SETTINGS_VIEW = 8;
    public static final int FORMAT_CARD_VIEW = 9;
    public static final int SYNC_VIEW = 10;
    public static final int REPORTS_VIEW = 11;
    public static final int CARD_RESTORE_VIEW = 12;

    public static final int SUMMARY_VIEW = 0;
    public static final int X_VIEW = 1;
    public static final int COMMODITY_VIEW = 2;
    public static final int SALES_TXN_VIEW = 3;
    public static final int VENDOR_SUMMARY_VIEW = 4;
    public static final int SUBMIT_VIEW = 5;
    public static final int SYNC_REPORT_VIEW = 6;
    public static final int VOID_REPORT_VIEW = 7;
    public static final int SALES_BASKET_VIEW = 8;
    public static boolean beneficiary = false;
    public static int fpcount;
    public static int[] fpQuality;
    public static String CONTENT_TYPE_TEXT = "text/plain";
    public static String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private AppConstants() {
        // This class is not publicly instantiable
    }
}
