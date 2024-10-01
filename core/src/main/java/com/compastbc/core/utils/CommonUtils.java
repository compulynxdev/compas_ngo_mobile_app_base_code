package com.compastbc.core.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.compastbc.core.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by hemant
 * Date: 07/08/18.
 */

public final class CommonUtils {

    private static Toast toast;

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static InputFilter noSpaceFilter() {
        return (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
    }

    @SuppressLint("all")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String loadJSONFromAsset(Context context, String jsonFileName) throws IOException {
        AssetManager manager = context.getAssets();
        InputStream is = manager.open(jsonFileName);

        int size = is.available();
        byte[] buffer = new byte[size];
        // is.read(buffer);
        is.close();

        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static Dialog showLoadingDialog(Context context, String label) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setContentView(R.layout.custom_progress);
        TextView tvLabel = dialog.findViewById(R.id.tv_label);
        if (label == null || label.isEmpty()) {
            tvLabel.setVisibility(View.GONE);
        } else {
            tvLabel.setVisibility(View.VISIBLE);
            tvLabel.setText(label);
        }

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static void showToast(AppCompatActivity activity, String message, int len) {
        if (toast != null) toast.cancel();

        // Create the object once.
        toast = Toast.makeText(activity, message, len);
        toast.show();
    }

    public static void snackbar(AppCompatActivity activity, @NonNull View coordinatorLayout, @NonNull String message) {
        if (toast != null) toast.cancel();
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#ffffff"));  //old color 1976d2
        textView.setGravity(Gravity.CENTER);
        snackbar.setActionTextColor(Color.parseColor("#1976d2"));
        sbView.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimaryDark));  //Color.WHITE
        snackbar.show();
    }


}
