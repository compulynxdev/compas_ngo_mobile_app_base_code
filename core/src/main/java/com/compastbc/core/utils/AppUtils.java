package com.compastbc.core.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by hemant.
 * Date: 30/8/18
 * Time: 2:34 PM
 */

public final class AppUtils {

    private static final String TAG = "AppUtils";

    private AppUtils() {
        // This utility class is not publicly instantiable
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void setLanguage(Context context, String code) {
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return formatSize(availableBlocks * blockSize);
    }

    private static long formatSize(long size) {
        if (size >= 1024) {
            size /= 1024;
            if (size >= 1024) {
                size /= 1024;
            }
        }
        return size;
    }

    public static String getBluetoothAddress() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String ret = null;
        for (BluetoothDevice dev : pairedDevices) {
            AppLogger.e(TAG, dev.getName() + " ");
            if (dev.getName().equals("BT-SPP")) {
                ret = dev.getAddress();
            } else if (dev.getName().equals("POS_Printer")) {
                ret = dev.getAddress();
            } else ret = dev.getAddress();
        }
        return ret;
    }

    public static BluetoothDevice getBluetoothDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice ret = null;
        for (BluetoothDevice dev : pairedDevices) {
            AppLogger.e(TAG, dev.getName() + " ");
            if (dev.getName().equals("BT-SPP")) {
                ret = dev;
            } else if (dev.getName().equals("POS_Printer")) {
                ret = dev;
            } else {
                ret = dev;
            }
        }

        return ret;
    }

    public static RequestBody createPartFromString(String data) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, data);
    }

    public static RequestBody createBody(String bodyContent, String data) {
        return RequestBody.create(MediaType.parse(bodyContent), data);
    }

    public static MultipartBody.Part prepareFilePart(Context context, String partName, Uri uri) {
        File file = new File(getRealPathFromURI(context, uri));
        // create RequestBody instance from filese
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(Objects.requireNonNull(context.getContentResolver().getType(uri))),
                        file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, "Image.jpg", requestFile);
    }

    public static MultipartBody.Part prepareFilePart(String key, String fileName, String fileType, File file) {
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(fileType),
                        file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(key, fileName, requestFile);
    }

    private static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public static byte[] getByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();
    }

    public static Bitmap getBitmap(byte[] bitmap) {
        return BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
    }


    public static String getAppVersionName(Context context) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    public static String readFileAsString(File filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;
        try {
            /*File path = new File(Environment.getExternalStorageDirectory() +
                    File.separator + AppConstants.FOLDER_NAME.concat("/").concat(AppConstants.FILE_NAME));*/
            in = new BufferedReader(new FileReader(filePath));
            while ((line = in.readLine()) != null) stringBuilder.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static boolean deleteFile(File path) {
        if (path != null && path.exists()) {
            return path.delete();
        } else return false;
    }

    public static boolean containNumbersOnly(String source) {
        boolean result;
        Pattern pattern = Pattern.compile("\\d+.\\d+"); //correct pattern for both float and integer.

        result = pattern.matcher(source).matches();
        /*if(result){
            System.out.println("\"" + source + "\""  + " is a number");
        }else
            System.out.println("\"" + source + "\""  + " is a String");*/
        return result;
    }
  /*  public static void deleteDirectory(File folderPath) {
        if (folderPath != null && folderPath.exists()) {
            try {
                FileUtils.deleteDirectory(folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    public static String replaceNonstandardDigits(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (isNonstandardDigit(ch)) {
                int numericValue = Character.getNumericValue(ch);
                if (numericValue >= 0) {
                    builder.append(numericValue);
                }
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static boolean isNonstandardDigit(char ch) {
        return Character.isDigit(ch) && !(ch >= '0' && ch <= '9');
    }
}
