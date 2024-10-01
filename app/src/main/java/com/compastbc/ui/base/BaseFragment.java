package com.compastbc.ui.base;

import com.compastbc.core.base.AbstractBaseFragment;


public abstract class BaseFragment extends AbstractBaseFragment {

    @Override
    public void openActivityOnTokenExpire() {
        if (getBaseActivity() != null) {
            getBaseActivity().openActivityOnTokenExpire();
        }
    }

    @Override
    public void createLog(String activityName, String action) {
        if (getBaseActivity() != null) getBaseActivity().createLog(activityName, action);
    }

    @Override
    public void createAttendanceLog() {
        if (getBaseActivity() != null) getBaseActivity().createAttendanceLog();
    }

    @Override
    public void getAccessToken(String userName, String pwd, BaseActivity.TokenCallback tokenCallback) {
        if (getBaseActivity() != null)
            getBaseActivity().getAccessToken(userName, pwd, tokenCallback);
    }

    @Override
    public void getAccessToken(BaseActivity.TokenCallback tokenCallback) {
        if (getBaseActivity() != null)
            getBaseActivity().getAccessToken(tokenCallback);
    }

    @Override
    public void downloadMasterData(BaseActivity.DownloadDataCallback dataCallback) {
        if (getBaseActivity() != null)
            getBaseActivity().downloadMasterData(dataCallback);
    }

    @Override
    public void uploadExceptionData(String data, String methodName, int lineNo, String className, String exception) {
        if (getBaseActivity() != null) {
            getBaseActivity().uploadExceptionData(data, methodName, lineNo, className, exception);
        }
    }

    @Override
    public void showLoading(String label) {
        if(getBaseActivity()!=null)
            getBaseActivity().showLoading(label);
    }
}

