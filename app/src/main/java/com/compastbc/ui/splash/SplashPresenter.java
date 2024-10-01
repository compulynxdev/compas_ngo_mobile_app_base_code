package com.compastbc.ui.splash;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;

/**
 * Created by hemant sharma on 12/08/19.
 */

public class SplashPresenter<V extends SplashMvpView> extends BasePresenter<V>
        implements SplashMvpPresenter<V> {

    SplashPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);

        decideNextActivity();
    }

    private void decideNextActivity() {
        if (getDataManager().isLoggedIn()) {
            if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
                getMvpView().openMainActivity(1);
            } else {
                getMvpView().openMainActivity(0);
            }
        } else
            getMvpView().openLoginActivity();
    }
}
