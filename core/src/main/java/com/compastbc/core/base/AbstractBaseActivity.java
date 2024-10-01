package com.compastbc.core.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.core.CoreApplication;
import com.compastbc.core.R;
import com.compastbc.core.data.AppDataManager;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.core.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class AbstractBaseActivity extends AppCompatActivity implements MvpView, AbstractBaseFragment.Callback {

    private final AppCompatActivity activity = this;
    private Dialog mProgressBar;
    private SweetAlertDialog sweetAlertDialog;
    private MaterialDialog materialDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateLanguage(getDataManager().getLanguage());
    }

    protected AppCompatActivity getActivity() {
        return activity;
    }

    protected AppDataManager getDataManager() {
        return CoreApplication.getInstance().getDataManager();
    }

    public void replaceFragment(@NonNull Fragment fragmentHolder, int layoutId) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            String fragmentName = fragmentHolder.getClass().getName();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(layoutId, fragmentHolder, fragmentName);
            fragmentTransaction.addToBackStack(fragmentName);
            fragmentTransaction.commit();
            hideKeyboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceFragment(@NonNull Fragment fragmentHolder, int layoutId, boolean addToBackStack) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            String fragmentName = fragmentHolder.getClass().getName();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(layoutId, fragmentHolder, fragmentName);
            if (addToBackStack) fragmentTransaction.addToBackStack(fragmentName);
            fragmentTransaction.commit();
            hideKeyboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFragment(@NonNull Fragment fragment, int layoutId, boolean addToBackStack) {
        try {
            String fragmentName = fragment.getClass().getName();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setEnterTransition(null);
            }
            fragmentTransaction.add(layoutId, fragment, fragmentName);
            if (addToBackStack) fragmentTransaction.addToBackStack(fragmentName);
            fragmentTransaction.commit();

            hideKeyboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public Fragment getCurrentFragment() {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
            return fragmentManager.findFragmentByTag(fragmentTag);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateLanguage(String appLanguage) {

        switch (appLanguage.toLowerCase()) {
            //English
            case "english":
                AppUtils.setLanguage(this, "en");
                break;

            //arabic
            case "arabic":
                AppUtils.setLanguage(this, "ar");
                break;

            case "swahili":
                AppUtils.setLanguage(this, "sw");
                break;

            case "ugandan":
                AppUtils.setLanguage(this, "sw-rUG");
                break;

            case "rwandan":
                AppUtils.setLanguage(this, "rw-rRW");
                break;

            case "somali":
                AppUtils.setLanguage(this, "so-rSO");
                break;

            case "ethiopian":
                AppUtils.setLanguage(this, "so-rET");
                break;
        }
    }

    protected void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void showLoading() {
       showLoading("");
    }

    @Override
    public void hideLoading() {
        if (mProgressBar != null && mProgressBar.isShowing()) {
            mProgressBar.cancel();
        }
    }

    @Override
    public boolean verifyDeviceModel(String modelName) {
        return Build.MODEL.equals(modelName);
    }
    public boolean verifyDeviceModel(List<String> listOfSupportedDevices) {
        if (listOfSupportedDevices == null) return false;
        for (String modelName : listOfSupportedDevices){
            if (verifyDeviceModel(modelName)) return true;
        }
        return false;
    }
    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.colorWhite));
        snackbar.show();
    }

    @Override
    public void onError(String message) {
        if (message != null) {
            showSnackBar(message);
        } else {
            showSnackBar(getString(R.string.some_error));
        }
    }

    @Override
    public void onError(@StringRes int resId) {
        onError(getString(resId));
    }

    @Override
    public void showMessage(String message) {
        if (message != null) {
            SweetAlertDialog dialog = sweetAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.error), message)
                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
            dialog.setCancelable(false);
            show(dialog);
        } else {
            SweetAlertDialog dialog = sweetAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.error), getString(R.string.some_error))
                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
            dialog.setCancelable(false);
            show(dialog);
        }
    }

    @Override
    public void showMessage(@StringRes int title, @StringRes int message) {
        showMessage(getString(title), getString(message));
    }

    @Override
    public void showMessage(String title, String message) {
        if (message != null) {
            SweetAlertDialog dialog = sweetAlertDialog(SweetAlertDialog.WARNING_TYPE, title, message)
                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
            dialog.setCancelable(false);
            show(dialog);
        } else {
            SweetAlertDialog dialog = sweetAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.error), getString(R.string.some_error))
                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
            dialog.setCancelable(false);
            show(dialog);
        }
    }

    @Override
    public void show(SweetAlertDialog sweetAlertDialog) {
        if (!activity.isFinishing() && sweetAlertDialog != null) {
            sweetAlertDialog.show();
        }
    }

    @Override
    public SweetAlertDialog sweetAlert(@StringRes int title, @StringRes int message) {
        SweetAlertDialog dialog = sweetAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(title), getString(message));
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public SweetAlertDialog sweetAlert(String title, String message) {
        SweetAlertDialog dialog = sweetAlertDialog(SweetAlertDialog.WARNING_TYPE, title, message);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public SweetAlertDialog sweetAlert(int title, String message) {
        SweetAlertDialog dialog = sweetAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(title), message);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public SweetAlertDialog sweetAlert(int alertType, String title, String content) {
        SweetAlertDialog dialog = sweetAlertDialog(alertType, title, content);
        dialog.setCancelable(false);
        return dialog;
    }

    private SweetAlertDialog sweetAlertDialog(int alertType, String title, String content) {
        if (sweetAlertDialog != null && sweetAlertDialog.isShowing()) sweetAlertDialog.dismiss();

        sweetAlertDialog = new SweetAlertDialog(getActivity(), alertType)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText(getString(R.string.Ok));

        sweetAlertDialog.setCancelable(false);
        return sweetAlertDialog;
    }

    private SweetAlertDialog sweetAlertDialog(int alertType, int title, int content) {
        SweetAlertDialog dialog = sweetAlertDialog(alertType, getString(title), getString(content));
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public MaterialDialog materialDialog(int title, int message) {
        return materialAlertDialog(getString(title), getString(message));
    }

    @Override
    public MaterialDialog materialDialog(String title, String message) {
        return materialAlertDialog(title, message);
    }

    private MaterialDialog materialAlertDialog(String title, String content) {
        if (materialDialog != null && materialDialog.isShowing()) materialDialog.dismiss();

        materialDialog = new MaterialDialog.Builder(this)
                .title(title)
                .content(content)
                .progress(true, 0)
                .progressIndeterminateStyle(false).show();

        return materialDialog;
    }

    @Override
    public void hideSweetAlertDialog() {
        if (sweetAlertDialog != null && sweetAlertDialog.isShowing()) sweetAlertDialog.dismiss();
    }

    public void hideMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) materialDialog.dismiss();
    }

    @Override
    public void showMessage(@StringRes int message) {
        showMessage(getString(message));
    }

    @Override
    public boolean isNetworkConnected() {
        if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
            return true;
        } else {
            showMessage(R.string.connection_error);
            return false;
        }
    }

    @Override
    public boolean isNetworkConnected(boolean isMsgShow) {
        if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
            return true;
        } else {
            if (isMsgShow)
                showMessage(R.string.connection_error);
            return false;
        }
    }

    @Override
    public void hideKeyboard() {
        if (getCurrentFocus() == null) return;

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    public SweetAlertDialog sweetAlert(int alertType, int title, int content) {
        return sweetAlertDialog(alertType, title, content);
    }

    protected abstract void setUp();

    public interface TokenCallback {
        void onSuccess();
    }

    public interface DownloadDataCallback {
        void onSuccess();
    }

    @Override
    public void showLoading(String label) {
        try {
            hideLoading();
            mProgressBar = CommonUtils.showLoadingDialog(this, label);
            mProgressBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
