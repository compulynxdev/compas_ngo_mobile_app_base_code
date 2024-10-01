package com.compastbc.ui.cardformat;


import android.content.Intent;

import com.compastbc.core.base.MvpPresenter;

public interface CardFormatMvpPresenter<V extends CardFormatMvpView> extends MvpPresenter<V> {

    void formatCard(Intent intent, boolean isCleanFormat);
}
