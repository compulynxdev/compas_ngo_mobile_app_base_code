package com.compastbc.core.data;


import com.compastbc.core.data.db.DBHelper;
import com.compastbc.core.data.network.ApiHelper;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.core.data.prefs.PreferencesHelper;
import com.google.gson.Gson;


/**
 * Created by hemant
 * Date: 10/4/18.
 */

public interface DataManager extends PreferencesHelper, ApiHelper, DBHelper {
    Gson getGson();

    void changeBaseUrl(String baseUrl);

    Topups getTopupDetails();

    void setTopupDetails(Topups topups);
}
