package com.compastbc.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * Created by hemant
 * Date: 12/12/17
 */

public final class PermissionUtils {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private PermissionUtils() {
        // This permission class is not publicly instantiable
    }

    //Permission function starts from here
    public static boolean RequestMultiplePermissionCamera(Activity activity) {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (FirstPermissionResult != PackageManager.PERMISSION_GRANTED | SecondPermissionResult != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            // Creating String Array with Permissions.
            ActivityCompat.requestPermissions(activity, new String[]
                    {
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    }, AppConstants.REQUEST_MULTIPLE_CAMERA_PERMISSIONS);
            return false;
        } else {
            return true;
        }
    }

    public static boolean requestMultiplePermission(Activity activity) {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int FourPermissionResult = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (FirstPermissionResult != PackageManager.PERMISSION_GRANTED | SecondPermissionResult != PackageManager.PERMISSION_GRANTED
                | ThirdPermissionResult != PackageManager.PERMISSION_GRANTED | FourPermissionResult != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            // Creating String Array with Permissions.
            ActivityCompat.requestPermissions(activity, new String[]
                    {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                    }, AppConstants.REQUEST_MULTIPLE_PERMISSIONS);
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkLocationPermission(AppCompatActivity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    AppConstants.MY_PERMISSIONS_REQUEST_LOCATION);

            return false;
        } else {
            return true;
        }
    }

    public static boolean checkCameraPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    AppConstants.MY_PERMISSIONS_REQUEST_CAMERA);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    /**
     * Determine if the app has permission to access storage
     *
     * @param context use this context for checking
     * @return true if permission exists
     */
    public static boolean haveStoragePermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request permission to access storage
     *
     * @param activity use this activity for the request
     */
    public static void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
        );
    }

    /**
     * Determine if storage access permission was granted
     *
     * @return true if permission was granted
     */
    public static boolean obtainedStoragePermission(int requestCode, int[] grantResuts) {
        return requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE &&
                grantResuts.length > 0 &&
                grantResuts[0] == PackageManager.PERMISSION_GRANTED;
    }


}

