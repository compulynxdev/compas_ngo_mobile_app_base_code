package com.compastbc.ui.dialog;

import com.compastbc.core.base.MvpPresenter;

/**
 * Created by Hemant on 26/08/19.
 */

public interface ImagePickDialogMvpPresenter<V extends ImagePickDialogMvpView> extends MvpPresenter<V> {
    void dispatchTakePictureIntent();
}
