package com.compastbc.core.base;

/*
 * Created by Hemant Sharma on 27/01/19.
 */

import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Base interface that any class that wants to act as a View in the MVP (Model View Presenter)
 * pattern must implement. Generally this interface will be extended by a more specific interface
 * that then usually will be implemented by an Activity or Fragment.
 */
public interface MvpView {

    void showLoading();

    void showLoading(String label);

    void hideLoading();

    void openActivityOnTokenExpire();

    void onError(@StringRes int resId);

    void onError(String message);

    void showMessage(String message);

    void showMessage(@StringRes int resId);

    void showMessage(String title, String message);

    void showMessage(@StringRes int title, @StringRes int message);

    SweetAlertDialog sweetAlert(String title, String message);

    SweetAlertDialog sweetAlert(@StringRes int title, @StringRes int message);

    SweetAlertDialog sweetAlert(@StringRes int title, String message);

    void show(SweetAlertDialog sweetAlertDialog);

    SweetAlertDialog sweetAlert(int alertType, String title, String content);

    SweetAlertDialog sweetAlert(int alertType, @StringRes int title, @StringRes int content);

    MaterialDialog materialDialog(String title, String message);

    MaterialDialog materialDialog(@StringRes int title, @StringRes int message);

    boolean verifyDeviceModel(String modelName);

    boolean verifyDeviceModel(List<String> listOfSupportedDevices);

    void hideSweetAlertDialog();

    boolean isNetworkConnected();

    boolean isNetworkConnected(boolean isMsgShow);

    void hideKeyboard();

    void createLog(String activityName, String action);

    void createAttendanceLog();

    void getAccessToken(String userName, String pwd, AbstractBaseActivity.TokenCallback tokenCallback);

    void getAccessToken(AbstractBaseActivity.TokenCallback tokenCallback);

    void downloadMasterData(AbstractBaseActivity.DownloadDataCallback downloadDataCallback);

    void uploadExceptionData(String data,String methodName,int lineNo,String className,String exception);

    }
