package com.compastbc.ui.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.compastbc.core.CoreApplication;
import com.compastbc.core.R;
import com.compastbc.core.base.AbstractBaseActivity;
import com.compastbc.core.data.db.model.ActivityLog;
import com.compastbc.core.data.db.model.AttendanceLog;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.BeneficiaryGroups;
import com.compastbc.core.data.db.model.BlockCards;
import com.compastbc.core.data.db.model.Categories;
import com.compastbc.core.data.db.model.ConfigurableParameters;
import com.compastbc.core.data.db.model.ExceptionLog;
import com.compastbc.core.data.db.model.Language;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ServiceDetails;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.TopupLogsDao;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.db.model.Vouchers;
import com.compastbc.core.data.network.model.Configuration;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.nfcprint.print.BlueToothCallback;
import com.compastbc.nfcprint.print.PrintServices;
import com.compastbc.nfcprint.print.ReportPrintCallback;
import com.compastbc.ui.login.LoginActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AbstractBaseActivity {

    public static boolean isUploaded = false;
    private final int portNum = 3238;
    private final NetworkInterface networkInterface = null;
    //verify beneficiary variable
    private final Handler handler = new Handler(Looper.getMainLooper());
    protected FusedLocationProviderClient mFusedLocationClient;
    protected LocationRequest locationRequest;
    @NonNull
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                setLatLng(location);
            }
        }
    };
    private Boolean isPlayServiceAvailable;
    private MulticastSocket socket;
    private InetAddress group;
    private InetAddress ip;
    private WifiManager wifiManager;
    private String deviceIP;
    //download apk task
    private long downloadID;
    private File fileAPKPath;
    private DownloadManager downloadManager;
    private Uri uri;
    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id && fileAPKPath != null && fileAPKPath.exists() && downloadManager != null && uri != null) {
                try {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(uri,
                            downloadManager.getMimeTypeForDownloadedFile(downloadID));
                    startActivity(install);

                    unregisterReceiver(onDownloadComplete);
                } catch (Exception e) {
                    CommonUtils.showToast(BaseActivity.this, getString(R.string.error_apk_download), Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            } else {
                CommonUtils.showToast(BaseActivity.this, getString(R.string.error_apk_download), Toast.LENGTH_SHORT);
            }
        }
    };
    //for handle bluetooth enable event
    private ReportPrintCallback reportPrintCallback;

    /**
     * location getting task start here
     * when location not available this method on gps when user click ok
     */
    protected void onGpsAutomatic() {
        int permissionLocation = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

            locationRequest = new LocationRequest();
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            builder.setNeedBle(true);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

            Task<LocationSettingsResponse> task =
                    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

            task.addOnCompleteListener(task1 -> {
                try {
                    //getting target response use below code
                    LocationSettingsResponse response = task1.getResult(ApiException.class);

                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    int permissionLocation1 = ContextCompat
                            .checkSelfPermission(getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permissionLocation1 == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(getActivity(), location -> {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        // Logic to handle location object
                                        setLatLng(location);
                                    } else {
                                        //Location not available
                                        AppLogger.e("Test", "Location not available");
                                    }
                                });
                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        getActivity(),
                                        AppConstants.REQUEST_CHECK_SETTINGS_GPS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    /**
     * this method get location when available and store in static variable
     */
    public void updateLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        setLatLng(location);
                    } else {
                        //Location not available
                        onGpsAutomatic();
                    }
                });
    }

    protected void setLatLng(@NonNull Location location) {
        CoreApplication.LATITUDE = location.getLatitude();
        CoreApplication.LONGITUDE = location.getLongitude();
        AppLogger.e("Location", String.valueOf(CoreApplication.LATITUDE));

        /*if (address.isEmpty()) {
            address = getAddressFromLatLng(CoreApplication.LATITUDE, CoreApplication.LONGITUDE);
            AppLogger.e("Location ", address);
        }*/
    }

    protected String getAddressFromLatLng(Double latitude, Double longitude) {
        String result;
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            // String city = addresses.get(0).getLocality();
            //  String addressLine = addresses.get(0).getAddressLine(1);
            // String state = addresses.get(0).getAdminArea();
            // String country = addresses.get(0).getCountryName();

            result = addresses.get(0).getAddressLine(0);// Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (Exception e) {
            result = "";
        }
        return result;
    }
    /* location getting task end here */

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        if (verifyGoogleServices()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mFusedLocationClient == null)
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        }

        if (!getDataManager().getConfigurableParameterDetail().isOnline() && getDataManager().getConfigurableParameterDetail().isBiometric()) {
            try {
                socket = new MulticastSocket(portNum);
                socket.setInterface(ip);
                socket.setBroadcast(true);

                group = InetAddress.getByName("224.0.0.1");
                socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (getIp() == null)
                initDeviceConnection(false, null, null);
        }
    }

    private boolean verifyGoogleServices() {
        if (isPlayServiceAvailable == null) {
            GoogleApiAvailability api = GoogleApiAvailability.getInstance();
            int code = api.isGooglePlayServicesAvailable(getActivity());
            // Do Your Stuff Here
            isPlayServiceAvailable = code == ConnectionResult.SUCCESS;
        }
        return isPlayServiceAvailable;
    }

    /* Remove the location listener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        if (verifyGoogleServices()) {
            if (mFusedLocationClient == null)
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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

    @Override
    public void openActivityOnTokenExpire() {
        sweetAlert(3, R.string.error, R.string.sessionTimeOut).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            setDataAndLogout();
        }).show();
    }

    public void logOut() {
        sweetAlert(3, R.string.error, R.string.are_you_sure_logout).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            setDataAndLogout();
        }).show();
    }

    public void setDataAndLogout() {
        AppUtils.setLanguage(getActivity(), "en");
        getDataManager().setLoggedIn(false);

        Intent intent = LoginActivity.getStartIntent(getActivity());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public synchronized void createLog(String activityName, String action) {
        if (getDataManager().getConfigurableParameterDetail().getActivityLog()) {
            ActivityLog al = new ActivityLog();
            al.setUserName(getDataManager().getUserName());
            al.setActivity(activityName);
            al.setAction(action);
            al.setLocationId(getDataManager().getUserDetail().getLocationId());
            al.setDate(CalenderUtils.getTimestamp(CalenderUtils.DB_TIMESTAMP_FORMAT));
            al.setDeviceId(CommonUtils.getDeviceId(this));
            al.setLatitude(CoreApplication.LATITUDE);
            al.setLongitude(CoreApplication.LONGITUDE);
            al.setUniqueId(UUID.randomUUID().toString());
            getDataManager().getDaoSession().getActivityLogDao().save(al);
        }
    }

    @Override
    public void createAttendanceLog() {
        if (getDataManager().getConfigurableParameterDetail().getAttendanceLog()) {
            AttendanceLog al = new AttendanceLog();
            al.setUsername(getDataManager().getUserName());
            al.setLocationId(getDataManager().getUserDetail().getLocationId());
            al.setLoginDate(CalenderUtils.getTimestamp(CalenderUtils.DB_TIMESTAMP_FORMAT));
            al.setDeviceId(CommonUtils.getDeviceId(this));
            al.setLatitude(CoreApplication.LATITUDE);
            al.setLongitude(CoreApplication.LONGITUDE);
            al.setLoginSuccess(true);
            al.setUniqueId(UUID.randomUUID().toString());
            getDataManager().getDaoSession().getAttendanceLogDao().save(al);
        }
    }

    @Override
    public void getAccessToken(String userName, String pwd, TokenCallback tokenCallback) {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", userName);
        map.put("password", pwd);
        map.put("grant_type", "password");
        //map.put("apkType", "online");
        getDataManager().GetAccessToken(AppConstants.BASIC_AUTHORISATION, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                JSONObject object;
                try {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        object = new JSONObject(response.body().string());
                       /* JSONArray array = object.getJSONArray("role");
                        String role = "";
                        if (array.length() > 0)
                            role = array.get(0).toString();
                        if(role.equalsIgnoreCase("NGO_DEVICE_USER")) {*/
                        Configuration configuration = getDataManager().getConfigurationDetail();
                        configuration.setAccess_token(object.getString("access_token"));
                        configuration.setRefresh_token(object.getString("refresh_token"));
                        getDataManager().setConfigurationDetail(configuration);
                        if (tokenCallback != null) tokenCallback.onSuccess();
                        /*} else {
                            hideMaterialDialog();
                            hideLoading();
                            showMessage(R.string.InvalidRole);
                        }*/
                    } else if (response.code() == 400) {
                        hideMaterialDialog();
                        hideLoading();
                        showMessage(R.string.UnauthorisedAccess);
                    } else if (response.code() == 404) {
                        hideMaterialDialog();
                        hideLoading();
                        showMessage(R.string.error_404);
                    } else {
                        hideMaterialDialog();
                        assert response.errorBody() != null;
                        handleApiError(response.errorBody().string());
                    }

                } catch (Exception e) {
                    hideMaterialDialog();
                    hideLoading();
                    e.printStackTrace();
                    showMessage(R.string.some_error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideMaterialDialog();
                handleApiFailure(call, t);
            }
        });
    }

    @Override
    public void getAccessToken(TokenCallback tokenCallback) {
        getAccessToken(getDataManager().getUserName(), getDataManager().getUserPassword(), tokenCallback);
    }

    @Override
    public void downloadMasterData(DownloadDataCallback dataCallback) {
        showLoading();
        RequestBody deviceId = AppUtils.createBody(AppConstants.CONTENT_TYPE_TEXT, getDataManager().getDeviceId());
        getDataManager().UpdateMaster("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), deviceId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                JSONObject object;
                try {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        object = new JSONObject(responseData);
                        //Currency
                        if (object.has("currency"))
                            getDataManager().setCurrency(object.getString("currency"));

                        ConfigurableParameters parameters = getDataManager().getConfigurableParameterDetail();

                        if (object.has("attendance_log"))
                            parameters.setAttendanceLog(object.getBoolean("attendance_log"));

                        if (object.has("activity_log"))
                            parameters.setActivityLog(object.getBoolean("activity_log"));

                        if (object.has("carry_forward"))
                            parameters.setCarryForward(object.getBoolean("carry_forward"));

                        if (object.has("void_transaction"))
                            parameters.setVoidTransaction(object.getBoolean("void_transaction"));

                        if (object.has("bio_metric"))
                            parameters.setBiometric(object.getBoolean("biometric"));

                        if (object.has("sale_basket_report"))
                            parameters.setSalesReport(object.getBoolean("sale_basket_report"));

                        if (object.has("automated_background_sync"))
                            parameters.setAutomated(object.getBoolean("automated_background_sync"));

                        //identity number length
                        if (object.has("ben_id_level_length"))
                            parameters.setIdLength(object.getInt("ben_id_level_length"));
                        else parameters.setIdLength(10);

                       /* if(object.has("mode"))
                            parameters.setOnline(object.getBoolean("mode"));*/

                        if (object.has("biometricDetail")) {
                            JSONObject bioDetail = object.getJSONObject("biometricDetail");

                            if (bioDetail.has("minimumFinger"))
                                parameters.setMinimumFinger(bioDetail.getInt("minimumFinger"));

                            if (bioDetail.has("matchingPercentage"))
                                parameters.setMatchingPercentage(bioDetail.getInt("matchingPercentage"));

                            if (bioDetail.has("fingerPrintActive"))
                                parameters.setFingerPrintActive(bioDetail.getBoolean("fingerPrintActive"));

                            if (bioDetail.has("irisActive"))
                                parameters.setIrisActive(bioDetail.getBoolean("irisActive"));

                            if (bioDetail.has("faceActive"))
                                parameters.setFaceActive(bioDetail.getBoolean("faceActive"));
                        }
                        getDataManager().setConfigurableParameterDetail(parameters);

                        if (object.has("users")) {
                            JSONArray user = object.getJSONArray("users");
                            Details details = new Details();
                            if (user.length() > 0) {
                                //save data for offline
                                if (!getDataManager().getConfigurableParameterDetail().isOnline()) {
                                    getDataManager().getDaoSession().getConfigurableParametersDao().insert(parameters);

                                    JSONArray blockCards = object.getJSONArray("blockCards");
                                    List<BlockCards> cardsList = new ArrayList<>();
                                    for (int i = 0; i < blockCards.length(); i++) {
                                        BlockCards cards_table = new BlockCards();
                                        cards_table.setCardNo(blockCards.getJSONObject(i).getString("cardNo"));
                                        cards_table.setIdentityNo(blockCards.getJSONObject(i).getString("rationNo"));
                                        cardsList.add(cards_table);
                                    }
                                    getDataManager().getDaoSession().getBlockCardsDao().insertInTx(cardsList);

                                    JSONArray bnfGroups = object.getJSONArray("bnfGrps");
                                    List<BeneficiaryGroups> groupsList = new ArrayList<>();
                                    if (bnfGroups.length() > 0) {
                                        for (int i = 0; i < bnfGroups.length(); i++) {
                                            BeneficiaryGroups groups = new BeneficiaryGroups();
                                            groups.setBnfGrpId(bnfGroups.getJSONObject(i).getString("bnfGrpId"));
                                            groups.setBnfGrpName(bnfGroups.getJSONObject(i).getString("bnfGrpName"));
                                            groupsList.add(groups);
                                        }
                                        getDataManager().getDaoSession().getBeneficiaryGroupsDao().insertInTx(groupsList);
                                    }

                                    JSONArray programmes = object.getJSONArray("programmes");
                                    JSONArray services = object.getJSONArray("products");
                                    if (services.length() > 0) {
                                        List<Services> servicesList = new ArrayList<>();
                                        for (int i = 0; i < services.length(); i++) {
                                            Services service = new Services();
                                            String[] image = services.getJSONObject(i).getString("image").split(",");
                                            service.setServiceImage(image[1]);
                                            service.setServiceCode(services.getJSONObject(i).getString("productCode"));
                                            service.setServiceId(services.getJSONObject(i).getString("productId"));
                                            service.setServiceName(services.getJSONObject(i).getString("productName"));
                                            service.setServiceType(services.getJSONObject(i).getString("productType"));
                                            service.setCategoryId(services.getJSONObject(i).getString("categoryId"));
                                            if (services.getJSONObject(i).getString("productType").equalsIgnoreCase("commodity")) {
                                                service.setLocationId(services.getJSONObject(i).getString("locationId"));
                                                JSONArray servicePrices = services.getJSONObject(i).getJSONArray("priceDetails");
                                                List<ServicePrices> servicePricesList = new ArrayList<>();
                                                for (int j = 0; j < servicePrices.length(); j++) {
                                                    ServicePrices servicePrices1 = new ServicePrices();
                                                    servicePrices1.setMaxPrice(Double.parseDouble(servicePrices.getJSONObject(j).getString("maxPrice")));
                                                    servicePrices1.setServiceId(services.getJSONObject(i).getString("productId"));
                                                    if (servicePrices.getJSONObject(j).has("currency"))
                                                        servicePrices1.setCurrency(servicePrices.getJSONObject(j).getString("currency"));
                                                    String uom = servicePrices.getJSONObject(j).getString("quantity").concat("-").concat(servicePrices.getJSONObject(j).getString("uom"));
                                                    servicePrices1.setUom(uom);
                                                    servicePricesList.add(servicePrices1);
                                                }
                                                List<ServicePrices> list = getDataManager().getDaoSession().getServicePricesDao().loadAll();
                                                AppLogger.d("ServicePrice", list.size() + "");
                                                getDataManager().getDaoSession().getServicePricesDao().insertInTx(servicePricesList);
                                                List<ServicePrices> list2 = getDataManager().getDaoSession().getServicePricesDao().loadAll();
                                                AppLogger.d("ServicePrice", list2.size() + "");
                                            }
                                            servicesList.add(service);
                                        }
                                        getDataManager().getDaoSession().getServicesDao().insertInTx(servicesList);
                                    }

                                    if (programmes.length() > 0) {
                                        List<Programs> programsList = new ArrayList<>();
                                        for (int i = 0; i < programmes.length(); i++) {
                                            Programs programmes_table = new Programs();
                                            programmes_table.setProgramId(programmes.getJSONObject(i).getString("programmeId"));
                                            programmes_table.setProgramName(programmes.getJSONObject(i).getString("programmeName"));
                                            programmes_table.setProductId(programmes.getJSONObject(i).getString("productId"));
                                            if (programmes.getJSONObject(i).has("programCurrency"))
                                                programmes_table.setProgramCurrency(programmes.getJSONObject(i).getString("programCurrency"));
                                            if (programmes.getJSONObject(i).has("vouchers")) {
                                                JSONArray vouchers = programmes.getJSONObject(i).getJSONArray("vouchers");
                                                List<Vouchers> vouchersList = new ArrayList<>();
                                                for (int j = 0; j < vouchers.length(); j++) {
                                                    Vouchers voucher_table = new Vouchers();
                                                    JSONArray products = vouchers.getJSONObject(j).getJSONArray("products");
                                                    voucher_table.setVoucherId(vouchers.getJSONObject(j).getString("voucherId"));
                                                    voucher_table.setVoucherName(vouchers.getJSONObject(j).getString("voucherName"));
                                                    voucher_table.setProgramId(programmes.getJSONObject(i).getString("programmeId"));
                                                    List<ServiceDetails> serviceDetailsList = new ArrayList<>();
                                                    for (int k = 0; k < products.length(); k++) {
                                                        ServiceDetails service_detail_table = new ServiceDetails();
                                                        service_detail_table.setServiceId(products.getJSONObject(k).getString("serviceId"));
                                                        service_detail_table.setVoucherId(vouchers.getJSONObject(j).getString("voucherId"));
                                                        service_detail_table.setProgramId(programmes.getJSONObject(i).getString("programmeId"));
                                                        serviceDetailsList.add(service_detail_table);
                                                    }
                                                    getDataManager().getDaoSession().getServiceDetailsDao().insertInTx(serviceDetailsList);
                                                    vouchersList.add(voucher_table);
                                                }
                                                getDataManager().getDaoSession().getVouchersDao().insertInTx(vouchersList);
                                            }
                                            programsList.add(programmes_table);
                                        }
                                        getDataManager().getDaoSession().getProgramsDao().insertInTx(programsList);
                                    }

                                    JSONArray categories = object.getJSONArray("categories");
                                    if (categories.length() > 0) {
                                        List<Categories> categoriesList = new ArrayList<>();
                                        for (int i = 0; i < categories.length(); i++) {
                                            Categories category = new Categories();
                                            category.setCategoryId(categories.getJSONObject(i).getString("categoryId"));
                                            category.setCategoryName(categories.getJSONObject(i).getString("categoryName"));
                                            category.setProductId(categories.getJSONObject(i).getString("productId"));
                                            categoriesList.add(category);
                                        }
                                        getDataManager().getDaoSession().getCategoriesDao().insertInTx(categoriesList);
                                    }

                                    for (int i = 0; i < user.length(); i++) {
                                        Users user_table = new Users();
                                        user_table.setUsersId(user.getJSONObject(i).getString("userId"));
                                        user_table.setUsername(user.getJSONObject(i).getString("userName"));
                                        user_table.setPassword(user.getJSONObject(i).getString("password"));
                                        user_table.setLevel(user.getJSONObject(i).getString("level"));
                                        user_table.setIsuploaded("1");
                                        user_table.setLocationid(user.getJSONObject(i).getString("locationId"));
                                        user_table.setAgentId(user.getJSONObject(i).getString("agentId"));
                                        user_table.setLocationName(object.getString("locationName"));
                                        try {
                                            user_table.setBio(user.getJSONObject(i).getBoolean("bioStatus"));
                                            details.setBioStatus(user.getJSONObject(i).getBoolean("bioStatus"));
                                            if (user.getJSONObject(i).getBoolean("bioStatus")) {
                                                user_table.setIsuploaded("1");
                                                JSONObject fingers;
                                                fingers = user.getJSONObject(i).getJSONObject("fingerPrint");
                                                if (fingers.has("rightFinger3"))
                                                    user_table.setF1(fingers.getString("rightFinger3"));

                                                if (fingers.has("leftFinger2"))
                                                    user_table.setF2(fingers.getString("leftFinger2"));

                                                if (fingers.has("leftFinger3"))
                                                    user_table.setF3(fingers.getString("leftFinger3"));

                                                if (fingers.has("rightFinger2"))
                                                    user_table.setF4(fingers.getString("rightFinger2"));

                                                if (fingers.has("rightThumb"))
                                                    user_table.setFprt(fingers.getString("rightThumb"));

                                                if (fingers.has("rightIndex"))
                                                    user_table.setFpri(fingers.getString("rightIndex"));

                                                if (fingers.has("leftFinger1"))
                                                    user_table.setFplf(fingers.getString("leftFinger1"));

                                                if (fingers.has("leftIndex"))
                                                    user_table.setFpli(fingers.getString("leftIndex"));

                                                if (fingers.has("leftThumb"))
                                                    user_table.setFplt(fingers.getString("leftThumb"));

                                                if (fingers.has("rightFinger1"))
                                                    user_table.setFprf(fingers.getString("rightFinger1"));

                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        getDataManager().getDaoSession().getUsersDao().deleteAll();
                                        getDataManager().getDaoSession().getUsersDao().insert(user_table);
                                    }
                                }

                                //insert languages
                                if (object.has("multiLanguageList")) {
                                    if (!object.isNull("multiLanguageList")) {
                                        List<Language> languages = new ArrayList<>();
                                        JSONArray array = object.getJSONArray("multiLanguageList");
                                        for (int i = 0; i < array.length(); i++) {
                                            Language language = new Language();
                                            language.setLangName(array.getString(i));
                                            language.setLocalisationTitle(array.getString(i));
                                            language.setIsSelected(false);
                                            languages.add(language);
                                        }
                                        getDataManager().getDaoSession().getLanguageDao().deleteAll();
                                        getDataManager().getDaoSession().getLanguageDao().insertInTx(languages);
                                    }
                                }
                                details.setUser(user.getJSONObject(0).getString("userName"));
                                details.setLevel(user.getJSONObject(0).getString("level"));
                                details.setPassword(user.getJSONObject(0).getString("password"));
                                details.setAgentId(user.getJSONObject(0).getString("agentId"));
                                details.setUid(user.getJSONObject(0).getString("userId"));
                                details.setLocationId(user.getJSONObject(0).getString("locationId"));
                                if (user.getJSONObject(0).has("bioStatus"))
                                    details.setBioStatus(user.getJSONObject(0).getBoolean("bioStatus"));
                                if (object.has("locationName"))
                                    details.setLocationName(object.getString("locationName"));
                                if (object.has("ben_id_level"))
                                    details.setBenIdLevel(object.getString("ben_id_level"));
                                getDataManager().setUserDetail(details);
                                getDataManager().setFirstTimeStatus(false);
                                getDataManager().setLoggedIn(false);
                                hideLoading();
                                dataCallback.onSuccess();
                            }

                        } else {
                            hideLoading();
                            showMessage(R.string.noUsers);
                        }

                    } else {
                        assert response.errorBody() != null;
                        handleApiError(response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hideLoading();
                    showMessage(e.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleApiFailure(call, t);
            }
        });

    }

    @Override
    public void uploadExceptionData(String data, String methodName, int lineNo, String className, String exception) {
        ExceptionLog log = new ExceptionLog();
        JSONObject object = new JSONObject();
        try {
            object.put("cardData", data);
            object.put("methodName", methodName);
            object.put("lineNo", lineNo);
            object.put("screen name", className);
            object.put("exception", exception);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        log.setAgentId(getDataManager().getUserDetail().getAgentId());
        log.setDeviceId(getDataManager().getDeviceId());
        log.setDataObject(object.toString());
        log.setScreenName(className);
        log.setDeviceName(Build.MODEL);
        log.setCreatedDate(CalenderUtils.getDateTime(CalenderUtils.DB_TIMESTAMP_FORMAT, Locale.getDefault()));
        getDataManager().getDaoSession().getExceptionLogDao().insert(log);
    }

    /*Print report delegate start here*/
    public void getPrintUtil(ReportPrintCallback callback) {
        if (callback == null) return;
        reportPrintCallback = callback;

        if (verifyDeviceModel(AppConstants.MODEL_NEWPOS)) {
            PrintServices printServices = new PrintServices(getActivity());
            callback.onSuccess(printServices);
        } else {
            PrintServices.setOnBluetoothDeviceListener(new BlueToothCallback() {
                @Override
                public void onBlueToothNotSupported() {
                    show(sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.alert_bluetooth_not_support)
                            .setConfirmClickListener(sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                callback.onNavigateNextController();
                            }));
                }

                @Override
                public void onBlueToothConnected(BluetoothDevice bluetoothDevice) {
                    if (bluetoothDevice != null && bluetoothDevice.getAddress() != null) {
                        try {
                            BluetoothSocket bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.randomUUID());
                            bluetoothSocket.connect();
                            PrintServices printServices = new PrintServices(getActivity(), bluetoothSocket);
                            callback.onSuccess(printServices);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onPrintError(e);
                        }
                    } else {
                        callback.onPrintPairError();
                    }
                }

                @Override
                public void onBlueToothDisable() {
                    show(sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.alert_bluetooth_off).setCancelButton(R.string.cancel, sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        callback.onNavigateNextController();
                    }).setConfirmButton(R.string.dialog_ok, sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, PrintServices.REQUEST_ENABLE_BT);
                    }));
                }
            });
        }
    }

    public void doCheckAppVersion(final boolean isShowMsg, final AppUpdateCallback appUpdateCallback) {
        showLoading();

        if (isNetworkConnected()) {
            if (checkDBForSync()) {
                getDataManager().doUpdateApplication().enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        hideLoading();
                        JSONObject object;
                        try {
                            if (response.code() == 200) {
                                assert response.body() != null;
                                String tmpResponse = response.body().string();
                                object = new JSONObject(tmpResponse);

                                String appName = "", appVersion = "", fileSize = "";
                                if (object.has("appName"))
                                    appName = object.getString("appName");

                                if (object.has("version"))
                                    appVersion = object.getString("version");

                                if (object.has("fileSize"))
                                    fileSize = object.getString("fileSize");

                                //AppLogger.e("MyDialog", Utils.getAppVersionCode(getContext())+"");
                                if (!appName.isEmpty() && !appVersion.isEmpty()) {
                                    int flag = appVersion.compareTo(AppUtils.getAppVersionName(getActivity()));
                                    AppLogger.e("Flag", "" + flag);
                                    if (flag > 0) {
                                        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                        final String finalAppName = appName;
                                        Bundle bundle = new Bundle();
                                        bundle.putString("version", appVersion);
                                        bundle.putString("fileSize", fileSize);

                                        sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.app_update_alert))
                                                .setConfirmText(getString(R.string.dialog_ok))
                                                .setConfirmClickListener(sweetAlertDialog -> {
                                                    sweetAlertDialog.dismissWithAnimation();
                                                    if (appUpdateCallback != null) {
                                                        appUpdateCallback.onShowAppUpdateUI(finalAppName, bundle);
                                                    }
                                                })
                                                .setCancelText(getString(R.string.cancel))
                                                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation).show();
                                    } else {
                                        if (appUpdateCallback != null)
                                            appUpdateCallback.onCallNextApi();
                                        if (isShowMsg) {
                                            sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.alert_latest_ver_msg))
                                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                                        }
                                    }
                                } else {
                                    if (appUpdateCallback != null)
                                        appUpdateCallback.onCallNextApi();
                                    if (isShowMsg) {
                                        sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.alert_latest_ver_msg))
                                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                                    }
                                }
                            } else if (response.code() == 400) {
                                if (appUpdateCallback != null) appUpdateCallback.onFail();
                                if (isShowMsg) {
                                    sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.error), getString(R.string.UnauthorisedAccess))
                                            .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                                }
                            } else if (response.code() == 404) {
                                if (appUpdateCallback != null) appUpdateCallback.onFail();
                                if (isShowMsg) {
                                    sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.error), getString(R.string.error_404))
                                            .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                                }
                            } else {
                                if (appUpdateCallback != null) appUpdateCallback.onFail();
                                if (isShowMsg) {
                                    assert response.errorBody() != null;
                                    handleApiError(response.errorBody().string());
                                }
                            }
                        } catch (Exception e) {
                            if (appUpdateCallback != null) appUpdateCallback.onFail();
                            e.printStackTrace();
                            if (isShowMsg) {
                                sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.error), getString(R.string.some_error))
                                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        if (isShowMsg) {
                            handleApiFailure(call, t);
                        }
                        if (appUpdateCallback != null) appUpdateCallback.onFail();
                    }
                });
            } else {
                hideLoading();
                sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.Pleasesyncdatawithserver))
                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
            }
        }
    }

    public boolean checkDBForSync() {
        long count;
        count = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.IsUploaded.eq("0")).count();
        if (count == 0) {
            count = getDataManager().getDaoSession().getTransactionListProductsDao().count();
            if (count == 0) {
                count = getDataManager().getDaoSession().getTopupLogsDao().queryBuilder().where(TopupLogsDao.Properties.IsUploaded.eq("0")).count();
                if (count == 0) {
                    count = getDataManager().getDaoSession().getSyncLogsDao().count();
                    if (count == 0) {
                        count = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IsUploaded.eq("0")).count();
                        if (count == 0) {
                            count = getDataManager().getDaoSession().getUsersDao().queryBuilder().where(UsersDao.Properties.Isuploaded.eq("0")).count();
                        }
                    }
                }
            }
        }
        return count == 0;
    }

    public void beginAppDownload(String appPath, String finalAppName) {
        /*
        Create a DownloadManager.Request with all the information necessary to start the download
         */
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        destination += finalAppName;
        uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        fileAPKPath = new File(destination);
        if (fileAPKPath.exists())
            fileAPKPath.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(appPath));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(getString(R.string.app_name));// Title of the Download Notification
        request.setDescription(getString(R.string.downloading));// Description of the Download Notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);// Visibility of the download Notification
        request.setAllowedOverRoaming(false);
        request.setVisibleInDownloadsUi(true);
        //set destination
        request.setDestinationUri(uri);

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        assert downloadManager != null;
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
    }

    public void handleApiFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
        hideLoading();
        if (t.getMessage() != null)
            showMessage(t.getMessage());
        else
            showMessage(R.string.ServerError);
    }

    public void handleApiError(String response) {
        hideLoading();
        try {
            JSONObject tmpObject = new JSONObject(response);
            if (tmpObject.has("message")) {
                String msg = tmpObject.getString("message");
                if (!msg.isEmpty() && !msg.equals("null"))
                    sweetAlert(R.string.alert, msg).show();
                else {
                    if (tmpObject.has("error")) {
                        String error = tmpObject.getString("error");
                        if (!error.isEmpty() && !error.equals("null"))
                            sweetAlert(R.string.alert, error).show();
                        else sweetAlert(R.string.error, R.string.ServerError).show();
                    }
                }
            } else {
                sweetAlert(R.string.error, R.string.ServerError).show();
            }
        } catch (Exception e) {
            sweetAlert(R.string.error, e.getMessage()).show();
        }
    }

    public InetAddress getIp() {
        return ip;
    }

    public String getDeviceIP() {
        return deviceIP;
    }

    public void initDeviceConnection(boolean isSendData, SyncReceiverCallback syncReceiverCallback, ArrayList<String> values) {
        try {
            ip = null;
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {

                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (!inetAddress.isLoopbackAddress()) {
                        deviceIP = inetAddress.getHostAddress();
                    }

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress;
                        break;
                    }
                }
                if (ip != null) {
                    break;
                }
            }

            if (ip != null) {
                //do call data transfer code here
                establishDeviceConnection(isSendData, syncReceiverCallback, values);
            } /*else {
               showToast("No device connected in network");
            }*/
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void doSendBenfData(SyncReceiverCallback syncReceiverCallback, ArrayList<String> values) {
        receiverData(syncReceiverCallback);
        if (values.isEmpty()) {
            showToast(getString(R.string.no_device_connected));
        } else {
            SendData sendMessage = new SendData(getBenfData());
            sendMessage.execute((Void) null);
        }
    }

    private void showFailView(DatagramPacket recv) {
        sendDataReceiveAck(recv, recv.getAddress() + " : fail");
    }

    private void showSuccessView(DatagramPacket recv) {
        String recvIP = recv.getAddress().toString().replace("/", "");
        if (deviceIP != null && !deviceIP.equals(recvIP)) {
            //Todo check this it will create multiple times
            try {
                handler.post(() -> {
                    if (!getActivity().isFinishing()) {
                        sweetAlert(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.success), getString(R.string.data_receive_success))
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sendDataReceiveAck(recv, recv.getAddress() + " : success");
    }

    private synchronized void parseBenfData(String medd, DatagramPacket recv) {
        try {
            Beneficiary tmpBenf = new Beneficiary();
            JSONObject subObject = new JSONObject(medd);

            if (subObject.has("benfID"))
                tmpBenf.setBeneficiaryId(subObject.getString("benfID"));
            if (subObject.has("nationalID")) {
                tmpBenf.setMemberNumber(subObject.getString("nationalID"));
                tmpBenf.setIdentityNo(subObject.getString("nationalID"));
            }
            if (subObject.has("bioVerifyStatus")) {
                tmpBenf.setBioVerifyStatus(subObject.getString("bioVerifyStatus"));
            }

            //old query
            //List<Beneficiary> beneficiariesList = Beneficiary.find(Beneficiary.class, "nationalid = ?", tmpBenf.getIdentityNo());
            BeneficiaryDao beneficiaryDao = getDataManager().getDaoSession().getBeneficiaryDao();
            Beneficiary beneficiary = beneficiaryDao.queryBuilder().where(BeneficiaryDao.Properties.IdentityNo.eq(tmpBenf.getIdentityNo())).limit(1).unique();
            if (beneficiary != null) {
                beneficiary.setBioVerifyStatus(tmpBenf.getBioVerifyStatus());

                beneficiaryDao.save(beneficiary);
                showSuccessView(recv);
                AppLogger.e("Benf", "Success");
            } else {
                handler.post(() -> {
                    if (!getActivity().isFinishing()) {
                        sweetAlert(SweetAlertDialog.ERROR_TYPE, getString(R.string.fail), getString(R.string.sync_bnf_data_error))
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                    }
                });
            }
        } catch (Exception e) {
            showFailView(recv);
            e.printStackTrace();
            AppLogger.e("Benf", "Exception");
        }
    }

    private String getBenfData() {
        JSONObject subObject = new JSONObject();
        try {
            //getCurrentVerifyBenfInfo has only 3 field data is available...
            // if you want all data you need to set it from BenfBiometricVerificationActivity.java and AppPreferenceHelper class
            Beneficiary beneficiary = getDataManager().getCurrentVerifyBenfInfo();
            //List<Beneficiary> beneficiaries = Beneficiary.find(Beneficiary.class, "BIO_VERIFY_STATUS=? AND", String.valueOf(BenfAuthEnum.APPROVED));

            subObject.put("benfID", beneficiary.getBeneficiaryId());
            subObject.put("nationalID", beneficiary.getIdentityNo());
            subObject.put("bioVerifyStatus", beneficiary.getBioVerifyStatus());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return subObject.toString();
    }

    public void establishDeviceConnection(boolean isSendData, SyncReceiverCallback syncReceiverCallback, ArrayList<String> values) {
        if (wifiManager == null)
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);//Call Wi-Fi service

        WifiManager.MulticastLock lock =
                wifiManager.createMulticastLock("CompassNetwork");//create a lock to transfer data between peers
        lock.setReferenceCounted(true);
        lock.acquire();


        try {
            socket = new MulticastSocket(portNum);
            socket.setInterface(ip);
            socket.setBroadcast(true);

            group = InetAddress.getByName("224.0.0.1");//224.0.0.1
            socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);

            MulticastSocket fileSocket = new MulticastSocket(portNum + 1);
            fileSocket.setInterface(ip);
            fileSocket.setBroadcast(true);

            InetAddress fileGroup = InetAddress.getByName("224.0.0.2");
            fileSocket.joinGroup(new InetSocketAddress(fileGroup, (portNum + 1)), networkInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isSendData)
            doSendBenfData(syncReceiverCallback, values);
        else receiverData(syncReceiverCallback);
    }

    private void receiverData(SyncReceiverCallback syncReceiverCallback) {
        Thread newThread = new Thread() {

            public void run() {
                while (true) {
                    try {
                        byte[] recvPkt = new byte[512000];// max 65535 //111450

                        DatagramPacket recv = new DatagramPacket(recvPkt, recvPkt.length);//class to get data packet

                        socket.receive(recv);//get data from Multicast Socket

                        final String medd = new String(recvPkt, 0, recv.getLength());

                        //ack data
                        if (medd.contains("success")) {
                            try {
                                handler.post(() -> {
                                    if (!getActivity().isFinishing()) {
                                        sweetAlert(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.success), getString(R.string.dataSend)).setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            if (syncReceiverCallback != null) {
                                                syncReceiverCallback.onSyncSuccess();
                                            }
                                        }).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (medd.contains("fail")) {
                            try {
                                handler.post(() -> {
                                    if (!getActivity().isFinishing()) {
                                        sweetAlert(SweetAlertDialog.ERROR_TYPE, getString(R.string.fail), getString(R.string.somethingWentWrong)).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            parseBenfData(medd, recv);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        newThread.start();
    }

    private void sendDataReceiveAck(DatagramPacket recv, String msg) {
        // now send acknowledgement packet back to sender
        try {
            byte[] sendData = msg.getBytes(StandardCharsets.UTF_8);

            InetAddress IPAddress = recv.getAddress();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    IPAddress, recv.getPort());
            socket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToast(final String msg) {
        handler.post(() -> Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PrintServices.REQUEST_ENABLE_BT) {
            if (reportPrintCallback == null) return;
            if (resultCode == RESULT_OK) {
                getPrintUtil(reportPrintCallback);
            } else {
                reportPrintCallback.onNavigateNextController();
            }
        }
    }

    public void sendEmail() {
        new Thread(
                () -> {
                    List<ExceptionLog> logs = getDataManager().getDaoSession().getExceptionLogDao().queryBuilder().list();
                    if (!logs.isEmpty()) {
                        Gson gson = new Gson();
                        String listString = gson.toJson(logs, new TypeToken<ArrayList<ExceptionLog>>() {
                        }.getType());
                        Properties props = new Properties();
                        //Configuring properties for gmail
                        //If you are not using gmail you may need to change the values
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        //Creating a new session
                        //Authenticating the password
                        Session session = Session.getDefaultInstance(props,
                                new Authenticator() {
                                    //Authenticating the password
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(AppConstants.EMAIL, AppConstants.PASSWORD);
                                    }
                                });

                        try {
                            //Creating MimeMessage object
                            MimeMessage mm = new MimeMessage(session);

                            //Setting sender address
                            mm.setFrom(new InternetAddress(AppConstants.EMAIL));
                            //Adding receiver
                            mm.addRecipient(Message.RecipientType.TO, new InternetAddress("ebeneficiary@gmail.com"));
                            //Adding subject
                            mm.setSubject("ADRA Exception " + CalenderUtils.getDateTime(CalenderUtils.EXCEPTION_FORMAT, Locale.getDefault()));
                            //Adding message
                            //mm.setText("Uploaded On");
                            MimeBodyPart textBodyPart = new MimeBodyPart();
                            ByteArrayDataSource tds = new ByteArrayDataSource(listString.getBytes(StandardCharsets.UTF_8), "text/plain");
                            textBodyPart.setDataHandler(new DataHandler(tds));
                            textBodyPart.setHeader("Content-ID", "<text>");
                            textBodyPart.setFileName("Exception.txt");
                            Multipart multipart = new MimeMultipart();
                            multipart.addBodyPart(textBodyPart);
                            mm.setContent(multipart);
                            //Sending email
                            Transport.send(mm);

                            getDataManager().getDaoSession().getExceptionLogDao().deleteAll();
                            AppLogger.e("Mail", "Successfully send");
                        } catch (MessagingException e) {
                            e.printStackTrace();
                            AppLogger.e("Mail", "Sending Fail : " + e.toString());
                        }
                    }
                }
        ).start();
    }

    /*Data Sync and Received task start here*/
    public interface AppUpdateCallback {
        void onShowAppUpdateUI(String appName, Bundle bundle);

        void onCallNextApi();

        void onFail();
    }

    public interface SyncReceiverCallback {
        void onSyncSuccess();
    }

    @SuppressLint("StaticFieldLeak")
    private class SendData extends AsyncTask<Void, Void, Boolean> {//class for send data as byte arrays

        String textMsg;

        SendData(String message) {
            textMsg = message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                byte[] data = textMsg.getBytes(); //65535 //2774
                DatagramPacket packet = new DatagramPacket(data, data.length, group, portNum);

                socket.send(packet);
                return true;
            } catch (Exception e) {
                return false;
            }

        }
    }
}
