package com.compastbc.ui.login.dialog;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.compastbc.R;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.ui.base.BaseDialog;


public class DeviceInfoDialog extends BaseDialog {

    private static final String TAG = DeviceInfoDialog.class.getSimpleName();

    public static DeviceInfoDialog newInstance() {
        Bundle args = new Bundle();

        DeviceInfoDialog fragment = new DeviceInfoDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_device_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }


    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }

    @Override
    protected void setUp(View view) {
        view.findViewById(R.id.img_close).setOnClickListener(view1 -> dismissDialog(TAG));

        TextView tv_name = view.findViewById(R.id.tv_name);
        TextView tv_id = view.findViewById(R.id.tv_id);
        tv_name.setText(getString(R.string.model).concat(" : ").concat(Build.MODEL));
        tv_id.setText(getString(R.string.MacID).concat(" : ").concat(CommonUtils.getDeviceId(getBaseActivity())));
    }
}
