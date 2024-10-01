package com.compastbc.core.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.ConfigurableParameters;
import com.compastbc.core.data.network.model.Configuration;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.utils.AppLogger;

import java.util.HashMap;

/**
 * Created by hemant
 * Date: 10/4/18.
 */

public final class AppPreferencesHelper implements PreferencesHelper {

    private static final String APP_PREFERENCE = "AppPreference";
    private static final String APP_BENF_PREFERENCE = "APP_BENF_PREFERENCE";

    private static final String PREF_KEY_USER_LOGGED_IN_MODE = "PREF_KEY_USER_LOGGED_IN_MODE";
    private static final String PREF_KEY_APP_LANGUAGE = "PREF_KEY_APP_LANGUAGE";
    private static final String PREF_KEY_CURRENCY = "PREF_KEY_CURRENCY";
    //User detail info
    private static final String PREF_KEY_VOUCHER_ID = "PREF_KEY_VOUCHER_ID";
    private static final String PREF_KEY_PROGRAMME_ID = "PREF_KEY_PROGRAMME_ID";
    private static final String PREF_KEY_CARD_NUMBER = "PREF_KEY_CARD_NUMBER";
    private static final String PREF_KEY_STATUS = "PREF_KEY_STATUS";
    private static final String PREF_KEY_LAT = "PREF_KEY_LAT";
    private static final String PREF_KEY_LNG = "PREF_KEY_LNG";
    private static final String PREF_KEY_ERROR = "PREF_KEY_ERROR";
    private static final String PREF_KEY_LEVEL = "PREF_KEY_LEVEL";
    private static final String PREF_KEY_VALUE_VOUCHER = "PREF_KEY_VALUE_VOUCHER";
    private static final String PREF_KEY_UID = "PREF_KEY_UID";
    private static final String PREF_KEY_DOWNLOAD_STATUS = "PREF_KEY_DOWNLOAD_STATUS";
    private static final String PREF_KEY_CAMP_ID = "PREF_KEY_CAMP_ID";
    private static final String PREF_KEY_MERCHANT_MASTER_ID = "PREF_KEY_MERCHANT_MASTER_ID";
    private static final String PREF_KEY_CYCLE = "PREF_KEY_CYCLE";
    private static final String PREF_KEY_VOUCHER_NUM = "PREF_KEY_VOUCHER_NUM";
    private static final String PREF_KEY_RATION_NUM = "PREF_KEY_RATION_NUM";
    private static final String PREF_KEY_T_COUNT = "PREF_KEY_T_COUNT";
    private static final String PREF_KEY_NET_SALES = "PREF_KEY_NET_SALES";
    private static final String PREF_KEY_DATE = "PREF_KEY_DATE";
    private static final String PREF_KEY_V_COUNT = "PREF_KEY_V_COUNT";
    private static final String PREF_KEY_V_AMT = "PREF_KEY_V_AMT";
    private static final String PREF_KEY_SALES = "PREF_KEY_SALES";
    private static final String PREF_KEY_INDEX = "PREF_KEY_INDEX";
    private static final String PREF_KEY_LOCATION_ID = "PREF_KEY_LOCATION_ID";
    private static final String PREF_KEY_LOCATION_NAME = "PREF_KEY_LOCATION_NAME";
    private static final String PREF_KEY_SALES_REPORT = "PREF_KEY_SALES_REPORT";
    private static final String PREF_KEY_VOID_TRANSACTION = "PREF_KEY_VOID_TRANSACTION";
    private static final String PREF_KEY_ACTIVITY_LOG = "PREF_KEY_ACTIVITY_LOG";
    private static final String PREF_KEY_ATTENDENCE_LOG = "PREF_KEY_ATTENDENCE_LOG";
    private static final String PREF_KEY_BIOMETRIC = "PREF_KEY_BIOMETRIC";
    private static final String PREF_KEY_MODE = "PREF_KEY_MODE";
    private static final String PREF_KEY_AUTOMATED = "PREF_KEY_AUTOMATED";
    private static final String PREF_KEY_UPLOAD_LOG = "PREF_KEY_UPLOAD_LOG";
    private static final String PREF_KEY_CARRY_FWD = "PREF_KEY_CARRY_FWD";
    private static final String PREF_KEY_USER = "PREF_KEY_USER";
    private static final String PREF_KEY_PASSWORD = "PREF_KEY_PASSWORD";
    private static final String PREF_KEY_AGENT_ID = "PREF_KEY_AGENT_ID";
    private static final String PREF_KEY_TIME = "PREF_KEY_TIME";
    private static final String PREF_KEY_FIRST_TIME = "PREF_KEY_FIRST_TIME";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final String ACCOUNT_ID = "ACCOUNT_ID";
    private static final String BIO_STATUS = "BIO_STATUS";

    private static final String DEVICE_ID = "DEVICEID";
    //Configuration parameters
    private static final String IP = "IP";
    private static final String PORT = "PORT";
    private static final String CONFIG_USER = "CONFIG_USER";
    private static final String CONFIG_PASSWORD = "CONFIG_PASSWORD";


    //Configurable parameters variable
    private static final String VOID_TRANSACTION = "VOID_TRANSACTION";
    private static final String ACTIVITY_LOG = "ACTIVITY_LOG";
    private static final String ATTENDANCE_LOG = "ATTENDANCE_LOG";
    private static final String SALES_REPORT = "SALES_REPORT";
    private static final String CARRY_FORWARD = "CARRY_FORWARD";
    private static final String BIOMETRIC = "BIOMETRIC";
    private static final String AUTOMATED = "AUTOMATED";
    private static final String ONLINE = "ONLINE";
    private static final String MINIMUM_FINGER = "MINIMUM_FINGER";
    private static final String MATCH_PERCENTAGE = "MATCH_PERCENTAGE";
    private static final String FINGERPRINT_ACTIVE = "FINGERPRINT_ACTIVE";
    private static final String IRIS_ACTIVE = "IRIS_ACTIVE";
    private static final String FACE_ACTIVE = "FACE_ACTIVE";
    private static final String IDENTIFICATION_TYPE = "IDENTIFICATION_TYPE";
    private static final String ID_LENGTH = "ID_LENGTH";
    //Current benf verify variable
    private static final String PREF_BENF_ID = "PREF_BENF_ID";
    private static final String PREF_IDENTITY_ID = "PREF_IDENTITY_ID";
    private static final String PREF_BENF_BIO_VERIFY_STATUS = "PREF_BENF_BIO_VERIFY_STATUS";


    // prefs object
    private final SharedPreferences getConfigurablePrefs;
    private final SharedPreferences configurationPrefs;
    private final SharedPreferences mPrefs;
    private final SharedPreferences topupPrefs;
    private final SharedPreferences curBenfInfo;

    public AppPreferencesHelper(Context context) {
        mPrefs = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);
        configurationPrefs = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);
        getConfigurablePrefs = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);
        topupPrefs = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);
        curBenfInfo = context.getSharedPreferences(APP_BENF_PREFERENCE, Context.MODE_PRIVATE);
    }


    @Override
    public HashMap<String, String> getHeader() {
        HashMap<String, String> mHeaderMap = new HashMap<>();
        /*mHeaderMap.put("authToken", mPrefs.getString(PREF_KEY_APP_AUTH_TOKEN, ""));
        mHeaderMap.put("Language", mPrefs.getString(PREF_KEY_APP_LANGUAGE, "english"));*/
        AppLogger.w("authToken & Language ", mHeaderMap.toString());
        return mHeaderMap;
    }

    @Override
    public boolean isLoggedIn() {
        return mPrefs.getBoolean(PREF_KEY_USER_LOGGED_IN_MODE, false);
    }

    @Override
    public void setLoggedIn(boolean isLoggedIn) {
        mPrefs.edit().putBoolean(PREF_KEY_USER_LOGGED_IN_MODE, isLoggedIn).apply();
    }


    @Override
    public Details getUserDetail() {
        Details userDetailInfo = new Details();

        userDetailInfo.setVoucherId(mPrefs.getString(PREF_KEY_VOUCHER_ID, ""));
        userDetailInfo.setProgrammeId(mPrefs.getString(PREF_KEY_PROGRAMME_ID, ""));
        userDetailInfo.setCardNumber(mPrefs.getString(PREF_KEY_CARD_NUMBER, ""));
        userDetailInfo.setStatus(mPrefs.getString(PREF_KEY_STATUS, ""));
        userDetailInfo.setLatitude(mPrefs.getLong(PREF_KEY_LAT, 0));
        userDetailInfo.setLongitude(mPrefs.getLong(PREF_KEY_LNG, 0));
        userDetailInfo.setError(mPrefs.getString(PREF_KEY_ERROR, ""));
        userDetailInfo.setLevel(mPrefs.getString(PREF_KEY_LEVEL, ""));
        userDetailInfo.setVoucherValue(mPrefs.getString(PREF_KEY_VALUE_VOUCHER, ""));
        userDetailInfo.setUid(mPrefs.getString(PREF_KEY_UID, ""));
        userDetailInfo.setDownloadStatus(mPrefs.getString(PREF_KEY_DOWNLOAD_STATUS, ""));
        userDetailInfo.setCampId(mPrefs.getString(PREF_KEY_CAMP_ID, ""));
        userDetailInfo.setMerchantMasterId(mPrefs.getString(PREF_KEY_MERCHANT_MASTER_ID, ""));
        userDetailInfo.setCycle(mPrefs.getString(PREF_KEY_CYCLE, ""));
        userDetailInfo.setVoucherNo(mPrefs.getString(PREF_KEY_VOUCHER_NUM, ""));
        //  userDetailInfo.setBenGroup(mPrefs.getString(PREF_KEY_BEN_GROUP, ""));
        userDetailInfo.setRationNo(mPrefs.getString(PREF_KEY_RATION_NUM, ""));
        userDetailInfo.setTcount(mPrefs.getString(PREF_KEY_T_COUNT, ""));
        userDetailInfo.setNetSales(mPrefs.getString(PREF_KEY_NET_SALES, ""));
        userDetailInfo.setDate(mPrefs.getString(PREF_KEY_DATE, ""));
        userDetailInfo.setVcount(mPrefs.getString(PREF_KEY_V_COUNT, ""));
        userDetailInfo.setvAmount(mPrefs.getString(PREF_KEY_V_AMT, ""));
        userDetailInfo.setSales(mPrefs.getString(PREF_KEY_SALES, ""));
        userDetailInfo.setIndex(mPrefs.getString(PREF_KEY_INDEX, ""));
        userDetailInfo.setLocationId(mPrefs.getString(PREF_KEY_LOCATION_ID, ""));
        userDetailInfo.setLocationName(mPrefs.getString(PREF_KEY_LOCATION_NAME, ""));
        userDetailInfo.setSalesReport(mPrefs.getBoolean(PREF_KEY_SALES_REPORT, false));
        userDetailInfo.setVoidTransaction(mPrefs.getBoolean(PREF_KEY_VOID_TRANSACTION, false));
        userDetailInfo.setActivityLog(mPrefs.getBoolean(PREF_KEY_ACTIVITY_LOG, false));
        userDetailInfo.setAttendanceLog(mPrefs.getBoolean(PREF_KEY_ATTENDENCE_LOG, false));
        userDetailInfo.setMode(mPrefs.getBoolean(PREF_KEY_MODE, false));
        userDetailInfo.setAutomated(mPrefs.getBoolean(PREF_KEY_AUTOMATED, false));
        userDetailInfo.setUploadLog(mPrefs.getBoolean(PREF_KEY_UPLOAD_LOG, false));

        userDetailInfo.setCarryForward(mPrefs.getBoolean(PREF_KEY_CARRY_FWD, false));
        userDetailInfo.setUser(mPrefs.getString(PREF_KEY_USER, ""));
        userDetailInfo.setPassword(mPrefs.getString(PREF_KEY_PASSWORD, ""));
        userDetailInfo.setAgentId(mPrefs.getString(PREF_KEY_AGENT_ID, ""));
        userDetailInfo.setTime(mPrefs.getString(PREF_KEY_TIME, ""));
        userDetailInfo.setBenIdLevel(mPrefs.getString(IDENTIFICATION_TYPE, ""));
        userDetailInfo.setBioStatus(mPrefs.getBoolean(BIO_STATUS, false));

        return userDetailInfo;
    }

    @Override
    public void setUserDetail(Details detail) {
        mPrefs.edit().putString(PREF_KEY_VOUCHER_ID, detail.getVoucherId()).apply();
        mPrefs.edit().putString(PREF_KEY_PROGRAMME_ID, detail.getProgrammeId()).apply();
        mPrefs.edit().putString(PREF_KEY_CARD_NUMBER, detail.getCardNumber()).apply();
        mPrefs.edit().putString(PREF_KEY_STATUS, detail.getStatus()).apply();
        mPrefs.edit().putLong(PREF_KEY_LAT, (long) detail.getLatitude()).apply();
        mPrefs.edit().putLong(PREF_KEY_LNG, (long) detail.getLongitude()).apply();
        mPrefs.edit().putString(PREF_KEY_ERROR, detail.getError()).apply();
        mPrefs.edit().putString(PREF_KEY_LEVEL, detail.getLevel()).apply();
        mPrefs.edit().putString(PREF_KEY_VALUE_VOUCHER, detail.getVoucherValue()).apply();
        mPrefs.edit().putString(PREF_KEY_UID, detail.getUid()).apply();
        mPrefs.edit().putString(PREF_KEY_DOWNLOAD_STATUS, detail.getDownloadStatus()).apply();
        mPrefs.edit().putString(PREF_KEY_CAMP_ID, detail.getCampId()).apply();
        mPrefs.edit().putString(PREF_KEY_MERCHANT_MASTER_ID, detail.getMerchantMasterId()).apply();
        mPrefs.edit().putString(PREF_KEY_CYCLE, detail.getCycle()).apply();
        mPrefs.edit().putString(PREF_KEY_VOUCHER_NUM, detail.getVoucherNo()).apply();
        //mPrefs.edit().putString(PREF_KEY_BEN_GROUP, detail.getBenGroup()).apply();
        mPrefs.edit().putString(PREF_KEY_RATION_NUM, detail.getRationNo()).apply();
        mPrefs.edit().putString(PREF_KEY_T_COUNT, detail.getTcount()).apply();
        mPrefs.edit().putString(PREF_KEY_NET_SALES, detail.getNetSales()).apply();
        mPrefs.edit().putString(PREF_KEY_DATE, detail.getDate()).apply();
        mPrefs.edit().putString(PREF_KEY_V_COUNT, detail.getVcount()).apply();
        mPrefs.edit().putString(PREF_KEY_V_AMT, detail.getvAmount()).apply();
        mPrefs.edit().putString(PREF_KEY_SALES, detail.getSales()).apply();
        mPrefs.edit().putString(PREF_KEY_INDEX, detail.getIndex()).apply();
        mPrefs.edit().putString(PREF_KEY_LOCATION_ID, detail.getLocationId()).apply();
        mPrefs.edit().putString(PREF_KEY_LOCATION_NAME, detail.getLocationName()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_SALES_REPORT, detail.isSalesReport()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_VOID_TRANSACTION, detail.isVoidTransaction()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_ACTIVITY_LOG, detail.isActivityLog()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_ATTENDENCE_LOG, detail.isAttendanceLog()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_MODE, detail.isMode()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_AUTOMATED, detail.isAutomated()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_UPLOAD_LOG, detail.isUploadLog()).apply();

        mPrefs.edit().putBoolean(PREF_KEY_CARRY_FWD, detail.isCarryForward()).apply();
        mPrefs.edit().putString(PREF_KEY_USER, detail.getUser()).apply();
        mPrefs.edit().putString(PREF_KEY_PASSWORD, detail.getPassword()).apply();
        mPrefs.edit().putString(PREF_KEY_AGENT_ID, detail.getAgentId()).apply();
        mPrefs.edit().putString(PREF_KEY_TIME, detail.getTime()).apply();
        mPrefs.edit().putBoolean(PREF_KEY_USER_LOGGED_IN_MODE, true).apply();
        mPrefs.edit().putString(IDENTIFICATION_TYPE, detail.getBenIdLevel()).apply();
        mPrefs.edit().putBoolean(BIO_STATUS, detail.isBioStatus()).apply();

    }

    public boolean isFirstTime() {
        return mPrefs.getBoolean(PREF_KEY_FIRST_TIME, true);
    }

    @Override
    public void setFirstTimeStatus(boolean isFirst) {
        mPrefs.edit().putBoolean(PREF_KEY_FIRST_TIME, isFirst).apply();
    }

    @Override
    public Configuration getConfigurationDetail() {
        Configuration configuration = new Configuration();
        configuration.setUrl(configurationPrefs.getString(IP, ""));
        configuration.setPort(configurationPrefs.getString(PORT, ""));
        configuration.setUsername(configurationPrefs.getString(CONFIG_USER, ""));
        configuration.setPassword(configurationPrefs.getString(CONFIG_PASSWORD, ""));
        configuration.setAccess_token(configurationPrefs.getString(ACCESS_TOKEN, ""));
        configuration.setRefresh_token(configurationPrefs.getString(REFRESH_TOKEN, ""));
        configuration.setAccountId(configurationPrefs.getString(ACCOUNT_ID, ""));
        return configuration;
    }

    @Override
    public void setConfigurationDetail(Configuration configuration) {
        configurationPrefs.edit().putString(IP, configuration.getUrl()).apply();
        configurationPrefs.edit().putString(PORT, configuration.getPort()).apply();
        configurationPrefs.edit().putString(CONFIG_USER, configuration.getUsername()).apply();
        configurationPrefs.edit().putString(CONFIG_PASSWORD, configuration.getPassword()).apply();
        configurationPrefs.edit().putString(ACCESS_TOKEN, configuration.getAccess_token()).apply();
        configurationPrefs.edit().putString(REFRESH_TOKEN, configuration.getRefresh_token()).apply();
        configurationPrefs.edit().putString(ACCOUNT_ID, configuration.getAccountId()).apply();
    }

    @Override
    public void setUser(String userName) {
        mPrefs.edit().putString(PREF_KEY_USER, userName).apply();
    }

    @Override
    public void setPassword(String userPassword) {
        mPrefs.edit().putString(PREF_KEY_PASSWORD, userPassword).apply();
    }

    @Override
    public String getUserName() {
        return mPrefs.getString(PREF_KEY_USER, "");
    }

    @Override
    public String getUserPassword() {
        return mPrefs.getString(PREF_KEY_PASSWORD, "");
    }

    @Override
    public String getDeviceId() {
        return mPrefs.getString(DEVICE_ID, "");
    }

    @Override
    public void setDeviceId(String deviceId) {
        mPrefs.edit().putString(DEVICE_ID, deviceId).apply();
    }

    @Override
    public ConfigurableParameters getConfigurableParameterDetail() {
        ConfigurableParameters parameters = new ConfigurableParameters();
        parameters.setIdType(getConfigurablePrefs.getString(IDENTIFICATION_TYPE, ""));
        parameters.setActivityLog(getConfigurablePrefs.getBoolean(ACTIVITY_LOG, false));
        parameters.setAttendanceLog(getConfigurablePrefs.getBoolean(ATTENDANCE_LOG, false));
        parameters.setSalesReport(getConfigurablePrefs.getBoolean(SALES_REPORT, false));
        parameters.setCarryForward(getConfigurablePrefs.getBoolean(CARRY_FORWARD, false));
        parameters.setOnline(getConfigurablePrefs.getBoolean(ONLINE, false));
        parameters.setBiometric(getConfigurablePrefs.getBoolean(BIOMETRIC, false));
        parameters.setAutomated(getConfigurablePrefs.getBoolean(AUTOMATED, false));
        parameters.setVoidTransaction(getConfigurablePrefs.getBoolean(VOID_TRANSACTION, false));
        parameters.setMatchingPercentage(getConfigurablePrefs.getInt(MATCH_PERCENTAGE, 0));
        parameters.setIdLength(getConfigurablePrefs.getInt(ID_LENGTH, 0));
        parameters.setMinimumFinger(getConfigurablePrefs.getInt(MINIMUM_FINGER, 4));
        parameters.setFingerPrintActive(getConfigurablePrefs.getBoolean(FINGERPRINT_ACTIVE, false));
       return parameters;
    }

    @Override
    public void setConfigurableParameterDetail(ConfigurableParameters parameters) {
        getConfigurablePrefs.edit().putString(IDENTIFICATION_TYPE, parameters.getIdType()).apply();
        getConfigurablePrefs.edit().putInt(MATCH_PERCENTAGE, parameters.getMatchingPercentage()).apply();
        getConfigurablePrefs.edit().putInt(MINIMUM_FINGER, parameters.getMinimumFinger()).apply();
        getConfigurablePrefs.edit().putInt(ID_LENGTH, parameters.getIdLength()).apply();
        getConfigurablePrefs.edit().putBoolean(BIOMETRIC, parameters.isBiometric()).apply();
        getConfigurablePrefs.edit().putBoolean(VOID_TRANSACTION, parameters.isVoidTransaction()).apply();
        getConfigurablePrefs.edit().putBoolean(CARRY_FORWARD, parameters.isCarryForward()).apply();
        getConfigurablePrefs.edit().putBoolean(ACTIVITY_LOG, parameters.isActivityLog()).apply();
        getConfigurablePrefs.edit().putBoolean(ATTENDANCE_LOG, parameters.isAttendanceLog()).apply();
        getConfigurablePrefs.edit().putBoolean(ONLINE, parameters.getOnline()).apply();
        getConfigurablePrefs.edit().putBoolean(AUTOMATED, parameters.isAutomated()).apply();
        getConfigurablePrefs.edit().putBoolean(SALES_REPORT, parameters.isSalesReport()).apply();
        getConfigurablePrefs.edit().putBoolean(FINGERPRINT_ACTIVE, parameters.isFingerPrintActive()).apply();
       }

    @Override
    public String getLanguage() {
        return configurationPrefs.getString(PREF_KEY_APP_LANGUAGE, "english");
    }

    @Override
    public void setLanguage(String language) {
        configurationPrefs.edit().putString(PREF_KEY_APP_LANGUAGE, language).apply();
    }

    @Override
    public Beneficiary getCurrentVerifyBenfInfo() {
        Beneficiary beneficiary = new Beneficiary();

        beneficiary.setBeneficiaryId(curBenfInfo.getString(PREF_BENF_ID, ""));
        beneficiary.setIdentityNo(curBenfInfo.getString(PREF_IDENTITY_ID, ""));
        beneficiary.setBioVerifyStatus(curBenfInfo.getString(PREF_BENF_BIO_VERIFY_STATUS, ""));

        return beneficiary;
    }

    @Override
    public void setCurrentVerifyBenfInfo(Beneficiary currentVerifyBenfInfo) {
        curBenfInfo.edit().putString(PREF_BENF_ID, currentVerifyBenfInfo.getBeneficiaryId()).apply();
        curBenfInfo.edit().putString(PREF_IDENTITY_ID, currentVerifyBenfInfo.getIdentityNo()).apply();
        curBenfInfo.edit().putString(PREF_BENF_BIO_VERIFY_STATUS, currentVerifyBenfInfo.getBioVerifyStatus()).apply();
    }

    @Override
    public String getCurrency() {
        return getConfigurablePrefs.getString(PREF_KEY_CURRENCY, "$");
    }

    @Override
    public void setCurrency(String currency) {
        getConfigurablePrefs.edit().putString(PREF_KEY_CURRENCY, currency).apply();
    }

}
