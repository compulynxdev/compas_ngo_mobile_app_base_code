package com.compastbc.core.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.core.CoreApplication;
import com.compastbc.core.R;
import com.compastbc.core.data.AppDataManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by hemant.
 * Date: 31/5/18
 * Time: 1:10 PM
 */

public abstract class AbstractBaseBottomSheetDialog extends BottomSheetDialogFragment implements View.OnFocusChangeListener, DialogMvpView {

    private AbstractBaseActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    public void show(FragmentManager fragmentManager, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment prevFragment = fragmentManager.findFragmentByTag(tag);
        if (prevFragment != null) {
            transaction.remove(prevFragment);
        }
        transaction.addToBackStack(null);
        show(transaction, tag);
    }

    protected AppDataManager getDataManager() {
        return CoreApplication.getInstance().getDataManager();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AbstractBaseActivity) {
            AbstractBaseActivity mActivity = (AbstractBaseActivity) context;
            this.mActivity = mActivity;
            mActivity.onFragmentAttached();
        }
    }

    @Override
    public void showLoading() {
        if (mActivity != null) {
            mActivity.showLoading();
        }
    }

    @Override
    public void hideLoading() {
        if (mActivity != null) {
            mActivity.hideLoading();
        }
    }

    @Override
    public void onError(String message) {
        if (mActivity != null) {
            mActivity.onError(message);
        }
    }

    @Override
    public void onError(@StringRes int resId) {
        if (mActivity != null) {
            mActivity.onError(resId);
        }
    }

    @Override
    public void showMessage(String message) {
        if (mActivity != null) {
            mActivity.showMessage(message);
        }
    }

    @Override
    public void showMessage(@StringRes int resId) {
        if (mActivity != null) {
            mActivity.showMessage(resId);
        }
    }

    @Override
    public void showMessage(String title, String message) {
        if (mActivity != null) {
            mActivity.showMessage(title, message);
        }
    }

    @Override
    public void showMessage(@StringRes int title, @StringRes int message) {
        if (mActivity != null) {
            mActivity.showMessage(title, message);
        }
    }

    @Override
    public boolean verifyDeviceModel(String modelName) {
        return mActivity != null && mActivity.verifyDeviceModel(modelName);
    }

    @Override
    public boolean verifyDeviceModel(List<String> listOfSupportedDevices) {
        return mActivity != null && mActivity.verifyDeviceModel(listOfSupportedDevices);
    }

    @Override
    public void show(SweetAlertDialog sweetAlertDialog) {
        if (mActivity != null) mActivity.show(sweetAlertDialog);
    }

    @Override
    public SweetAlertDialog sweetAlert(@StringRes int title, @StringRes int message) {
        return mActivity.sweetAlert(title, message);
    }

    @Override
    public SweetAlertDialog sweetAlert(int title, String message) {
        return mActivity.sweetAlert(title, message);
    }

    @Override
    public SweetAlertDialog sweetAlert(int alertType, String title, String content) {
        return mActivity.sweetAlert(alertType, title, content);
    }

    @Override
    public MaterialDialog materialDialog(int title, int message) {
        return mActivity.materialDialog(title, message);
    }

    @Override
    public MaterialDialog materialDialog(String title, String message) {
        return mActivity.materialDialog(title, message);
    }

    @Override
    public void hideSweetAlertDialog() {
        if (mActivity != null) {
            mActivity.hideSweetAlertDialog();
        }
    }

    @Override
    public boolean isNetworkConnected() {
        return mActivity.isNetworkConnected();
    }

    @Override
    public boolean isNetworkConnected(boolean isMsgShow) {
        return mActivity.isNetworkConnected(isMsgShow);
    }

    @Override
    public SweetAlertDialog sweetAlert(int alertType, int title, int content) {
        return mActivity.sweetAlert(alertType, title, content);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void hideKeyboard() {
        if (mActivity != null) {
            mActivity.hideKeyboard();
        }
    }

    @Override
    public SweetAlertDialog sweetAlert(String title, String message) {
        return mActivity.sweetAlert(title, message);
    }

    public AbstractBaseActivity getBaseActivity() {
        return mActivity;
    }

    protected abstract void setUp(View view);

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            hideKeyboard();
        }
    }

    public void dismissDialog(String tag) {
        hideKeyboard();
        dismiss();
    }
}
