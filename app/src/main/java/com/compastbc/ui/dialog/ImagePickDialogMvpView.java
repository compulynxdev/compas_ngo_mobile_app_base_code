package com.compastbc.ui.dialog;

import android.content.Intent;

import com.compastbc.core.base.DialogMvpView;

/**
 * Created by Hemant on 26/08/19.
 */

public interface ImagePickDialogMvpView extends DialogMvpView {
    void getStartCameraIntent(Intent intent);

    void setImagePath(String path);
}
