package com.compastbc.core.data.network;

import com.compastbc.core.CoreApplication;
import com.compastbc.core.data.AppDataManager;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.AppUtils;

/**
 * Created by hemant.
 * Date: 30/8/18
 * Time: 2:30 PM
 */

public final class Webservices {

    public static final String WEB_BNF_LIST_N_FILTER = "ngo/rest/online/filter_beneficiary/";  //GET
    static final String CHANGE_AGENT_PASSWORD = "auth/api/v1/user/change_mobile_password";
    static final String DOWNLOAD_BNF_PAGE = "ngo/rest/user/downloadBnfPage";
    static final String DOWNLOAD_BNF_TOPUP = "ngo/rest/user/downloadTopupPage";
    static final String WEB_APK_VERSION_CHECK = "configuration/rest/account/getOnlineAppVersion";
    static final String GET_APK_TYPE = "configuration/rest/account/getApkType";
    static final String WEB_UPLOAD_TRANSACTION = "ngo/rest/user/updAndroidTrans";
    static final String WEB_UPLOAD_ATTENDANCE = "ngo/rest/user/updAttendanceLog";
    static final String WEB_UPLOAD_ARCHIVE = "ngo/rest/transaction/gtTransArchiveLog";
    static final String GET_AGENT_BIO = "ngo/rest/online/get_agent_finger_print";
    static final String WEB_UPLOAD_TOPUPLOGS = "ngo/rest/transaction/gtTopupLogDetails";
    static final String WEB_UPLOAD_ACTIVITIES = "ngo/rest/user/save/activity/log";
    static final String WEB_UPLOAD_AGENTS = "ngo/rest/user/uploadAgentFP";
    static final String WEB_UPLOAD_BENEFICIARY = "ngo/rest/user/uploadBnf";
    static final String GET_BENEFICIARY_BY_IDNO = "ngo/rest/online/check_biometric_by_identity_no";
    static final String GET_BENEFICIARY_BY_CARDNO = "ngo/rest/online/check_biometric_by_card_no";
    static final String GET_BENEFICIARY_BY_IDENTITYNUMBER = "ngo/rest/online/get_beneficiary_by_identity_no";
    static final String WEB_ACCESS_TOKEN = "auth/oauth/token";
    /*Auth Apis*/
    static final String WEB_MASTER_URL = "ngo/rest/user/downloadVo";  //GET
    static final String WEB_AGENT_BIO_UPDATE = "ngo/rest/online/upload_agent_finger_print";  //POST
    /*BeneficiaryActivity Api's*/
    static final String WEB_BNF_FIND = "findBeneficiary";  //GET
    static final String WEB_BNF_BIO = "getBnfBio";  //GET
    static final String WEB_BNF_VERIFY_STATUS = "getBnfVerifyStatus";  //GET
    static final String WEB_BNF_BIO_SAVE = "saveBnfFingers";  //POST
    static final String WEB_BNF_GET_FINGERS = "getBnfFingers";  //GET
    static final String WEB_BNF_ADD = "ngo/rest/online/create_beneficiary";  //POST
    static final String WEB_BNF_FIND_BY_IDENTIFICATION = "findByRation";  //GET
    static final String WEB_BNF_FIND_BY_CARD = "findByCardno";  //GET
    static final String WEB_SEARCH_BNF = "searchBnf";  //GET
    static final String WEB_BNF_UPDATE = "ngo/rest/online/update_beneficiary/";  //POST
    static final String UPLOAD_BENEFICIARY_FINGERPRINT = "ngo/rest/online/upload_beneficiary_finger_print";
    /*NFC Card Api's*/
    static final String WEB_CARD_ACTIVATION = "setActivation";  //POST
    static final String WEB_CARD_FIND_BLOCK = "findBlocked";  //GET
    /*Other Api's*/
    static final String WEB_TOPUP_FIND = "findTopup";  //GET
    static final String WEB_PROGRAMME_FIND = "findPrograms";  //GET
    static final String WEB_GET_VOUCHER = "getVouchers";  //GET
    static final String WEB_GET_COMMODITY = "getCommodities";  //GET
    static final String WEB_GET_UOM_BY_ID = "getUomsById";  //GET
    static final String WEB_UPLOAD_PENDING_SYNCS = "ngo/rest/user/upload_pending_syncs";
    /*transaction api's*/
    static final String GET_BLOCK_CARD = "ngo/rest/online/get_blocked_card_by_card_no";
    static final String GET_TOPUPS = "ngo/rest/online/getTopupByCardNo";
    static final String GET_PROGRAMS_BY_TOPUPS = "ngo/rest/online/find_programmes";
    static final String GET_VOUCHERS_BY_PROGRAMS = "ngo/rest/online/get_voucher_by_programme_id";
    static final String GET_COMMODITIES_BY_VOUCHERS = "ngo/rest/online/get_commodities_by_voucher_id";
    static final String GET_UOM_BY_SERVICE_ID = "ngo/rest/online/get_uom_by_service_id";
    static final String UploadTransactions = "ngo/rest/online/update_android_transaction";
    static final String GET_FP_DETAIL = "ngo/rest/online/get_finger_print_detail";
    //VOID transaction api
    static final String FIND_LAST_TRANSACTION = "ngo/rest/online/find_last_transaction";
    static final String SET_LAST_TRANSACTION = "ngo/rest/online/set_last_transaction";
    //summary report
    static final String GET_COUNT_DETAIL = "ngo/rest/online/get_count_detail";
    //xreport
    static final String GET_TRANSACTION_DETAIL = "ngo/rest/online/get_transaction_detail";
    //sales transaction history report
    static final String GET_TRANSACTION_HISTORY = "ngo/rest/online/get_transaction_history";
    //void report
    static final String GET_VOID_TRANSACTION = "ngo/rest/online/get_void_transaction";
    //commodity report
    static final String GET_COMMODITY_REPORT_DATA = "ngo/rest/online/daily_commodity_report";
    //sales basket report
    static final String GET_SALE_PROGRAM = "ngo/rest/online/get_sale_programmes";
    static final String GET_SALE_CATEGORIES = "ngo/rest/online/get_sale_categories";
    static final String GET_SALE_COMMODITIES = "ngo/rest/online/get_sale_commodities";
    static final String GET_SALE_UOM = "ngo/rest/online/get_sale_uom";
    static final String GET_SALE_BENEFICIARY = "ngo/rest/online/get_sale_beneficiary_detail";
    public static String BASE_URL = getBaseUrl();

    private Webservices() {
        // This class is not publicly instantiable
    }

    public static String getApkDownloadPath(String finalAppName) {
        AppDataManager dataManager = CoreApplication.getInstance().getDataManager();
        String finalAppPath;

        if (AppUtils.containNumbersOnly(dataManager.getConfigurationDetail().getPort())) {
            finalAppPath = "http://" + dataManager.getConfigurationDetail().getUrl() + ":" + dataManager.getConfigurationDetail().getPort().concat("/apk/").concat(finalAppName);
        } else {
            finalAppPath = "http://" + dataManager.getConfigurationDetail().getUrl() + "/" + dataManager.getConfigurationDetail().getPort().concat("/apk/").concat(finalAppName);
        }

        return finalAppPath;
    }

    public static String getBaseUrl() {
        AppDataManager dataManager = CoreApplication.getInstance().getDataManager();
        String baseUrl;

        if (AppUtils.containNumbersOnly(dataManager.getConfigurationDetail().getPort())) {
            baseUrl = "http://" + dataManager.getConfigurationDetail().getUrl() + ":" + dataManager.getConfigurationDetail().getPort() + "/compas/";
        } else {
            baseUrl = "http://" + dataManager.getConfigurationDetail().getUrl() + "/" + dataManager.getConfigurationDetail().getPort() + "/compas/";
        }
        AppLogger.e("BaseURL", baseUrl);
        return baseUrl;
    }


}
