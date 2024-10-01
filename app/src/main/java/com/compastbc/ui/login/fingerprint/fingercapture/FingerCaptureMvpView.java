package com.compastbc.ui.login.fingerprint.fingercapture;

import android.graphics.Bitmap;

import com.compastbc.core.base.MvpView;

/**
 * Created by hemant sharma on 12/08/19.
 */


public interface FingerCaptureMvpView extends MvpView {

    void reset(int pos);

    void displayNextHand();

    void updateThumb(String fingerName, Bitmap bitmap);

    void updateLeftThumbUI(String state);

    void updateLeftFrontUI(String state);

    void updateLeftOneUI(String state);

    void updateLeftTwoUI(String state);

    void updateLeftIndexUI(String state);

    void updateRightThumbUI(String state);

    void updateRightFrontUI(String state);

    void updateRightOneUI(String state);

    void updateRightTwoUI(String state);

    void updateRightIndexUI(String state);
}
