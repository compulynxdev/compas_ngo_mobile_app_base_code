package com.compastbc.ui.main.appupdate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.core.utils.PermissionUtils;
import com.compastbc.ui.base.BaseDialog;

/**
 * Created by hemant.
 * Date: 20/8/19
 * Time: 2:10 PM
 */

public class AppUpdateDialog extends BaseDialog implements AppUpdateDialogMvpView {

    private static final String TAG = "AppUpdateDialog";

    private AppUpdateDialogPresenter<AppUpdateDialogMvpView> mPresenter;

    private UpdateDialogCallback callback;
    private String appVersion = "", fileSize = "0";

    public AppUpdateDialog() {
        // Required empty public constructor
    }

    public static AppUpdateDialog newInstance(Bundle bundle, UpdateDialogCallback callback) {
        AppUpdateDialog appUpdateDialog = new AppUpdateDialog();
        appUpdateDialog.setArguments(bundle);
        appUpdateDialog.setCallback(callback);
        return appUpdateDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appVersion = getArguments().getString("version");
            fileSize = getArguments().getString("fileSize");
        }
    }

    private void setCallback(UpdateDialogCallback callback) {
        this.callback = callback;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_app_update, container, false);
        mPresenter = new AppUpdateDialogPresenter<>(getDataManager());
        mPresenter.onAttach(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_size = view.findViewById(R.id.tv_size);
        tv_title.setText(getString(R.string.compas_tbc_v).concat(appVersion));
        tv_size.setText(getString(R.string.update_size).concat(String.valueOf(Math.round(Long.parseLong(fileSize) / (1024 * 1024)))).concat(" ").concat(getString(R.string.mb)));
        view.findViewById(R.id.btn_update).setOnClickListener(view1 -> {
            if (PermissionUtils.isStoragePermissionGranted(getActivity())) {
                if (callback != null) callback.onUpdateClick();
                dismiss();
            }
        });
    }

    @Override
    public void dismissDialog() {
        super.dismissDialog(TAG);
    }

    @Override
    public void onDestroyView() {
        mPresenter.onDetach();
        super.onDestroyView();
    }

    public interface UpdateDialogCallback {
        void onUpdateClick();
    }
}