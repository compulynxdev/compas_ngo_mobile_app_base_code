package com.compastbc.ui.login.fingerprint.imagecapture;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;

public class ImageCapturePresenter<V extends ImageCaptureMvpView> extends BasePresenter<V>
        implements ImageCaptureMvpPresenter<V> {

    public ImageCapturePresenter(DataManager dataManager) {
        super(dataManager);
    }
}
