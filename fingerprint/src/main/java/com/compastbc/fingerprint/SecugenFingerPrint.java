package com.compastbc.fingerprint;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.compastbc.core.utils.AppLogger;

import java.util.Arrays;
import java.util.List;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;

/**
 * Created by Hemant Sharma on 29-01-20.
 * Divergent software labs pvt. ltd
 */
public final class SecugenFingerPrint implements FingerprintCallback {
    private static final String TAG = "SecugenFingerPrint";

    private static SecugenFingerPrint instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Context context;
    private JSGFPLib sgfplib;
    private int mImageWidth, mImageHeight;
    private byte[] mRegisterTemplate, mVerifyTemplate;
    private int matchPercentage = 65;

    private SecugenFingerPrint(Context context) {
        this.context = context;
        initSecugen();
    }

    public static SecugenFingerPrint getInstance(Context context) {
        if (instance == null)
            instance = new SecugenFingerPrint(context);
        else instance.setContext(context);

        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void setMatchPercentage(int matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    private void initSecugen() {
        close();
        /*Init secugen fp*/
        sgfplib = new JSGFPLib((UsbManager) context.getSystemService(Context.USB_SERVICE));
        // bSecuGenDeviceOpened = false;
        int[] mMaxTemplateSize = new int[1];

        long error = sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE) {
            long finalError = error;
            handler.post(() -> {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
                if (finalError == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
                    dlgAlert.setMessage(context.getString(R.string.fingerprint_device_not_found));
                else dlgAlert.setMessage(context.getString(R.string.fingerprint_init_failed));
                dlgAlert.setTitle(context.getString(R.string.secugen_fingerprint));
                dlgAlert.setPositiveButton(context.getString(R.string.Ok),
                        (dialog, whichButton) -> dialog.dismiss()
                );
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
                sgfplib = null;
            });
        } else {
            UsbDevice usbDevice = sgfplib.GetUsbDevice();
            if (usbDevice == null) {
                handler.post(() -> {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
                    dlgAlert.setMessage(context.getString(R.string.secugen_fingerprint_not_found));
                    dlgAlert.setTitle(context.getString(R.string.secugen_fingerprint));
                    dlgAlert.setPositiveButton(context.getString(R.string.Ok),
                            (dialog, whichButton) -> dialog.dismiss()
                    );
                    dlgAlert.setCancelable(false);
                    dlgAlert.create().show();
                    sgfplib = null;
                });
            } else {
                boolean hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice);
                if (!hasPermission) {
                    sgfplib.GetUsbManager().requestPermission(usbDevice, PendingIntent.getBroadcast(context, 0, new Intent(FingerprintReaderInit.ACTION_USB_PERMISSION), 0));
                }
                if (hasPermission) {
                    error = sgfplib.OpenDevice(0);
                    if (error == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                        //bSecuGenDeviceOpened = true;
                        SGDeviceInfoParam deviceInfo = new SGDeviceInfoParam();
                        sgfplib.GetDeviceInfo(deviceInfo);

                        mImageWidth = deviceInfo.imageWidth;
                        mImageHeight = deviceInfo.imageHeight;
                        // mImageDPI = deviceInfo.imageDPI;

                        //sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
                        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
                        sgfplib.GetMaxTemplateSize(mMaxTemplateSize);

                        mRegisterTemplate = new byte[mMaxTemplateSize[0]];
                        mVerifyTemplate = new byte[mMaxTemplateSize[0]];

                        // sgfplib.WriteData(SGFDxConstant.WRITEDATA_COMMAND_ENABLE_SMART_CAPTURE, (byte)0);
                    } else {
                        sgfplib = null;
                        AppLogger.d(TAG, "Waiting for USB Permission\n");
                    }
                }
            }
        }
    }

    @Override
    public void captureFingerPrint(FingerprintReaderInit.FingerPrintCallback callback) {
        if (sgfplib == null || mImageWidth == 0 || mImageHeight == 0) {
            initSecugen();
            return;
        }
        byte[] buffer = new byte[mImageWidth * mImageHeight];
        sgfplib.GetImage(buffer);

        if (callback != null)
            callback.onFingerPrintCapture(FingerprintUtils.toGrayscale(buffer, mImageWidth, mImageHeight));
    }

    @Override
    public void captureFingerPrintWithEncodeData(FingerprintReaderInit.FingerPrintDataCallback callback) {
        if (sgfplib == null || mImageWidth == 0 || mImageHeight == 0) {
            initSecugen();
            return;
        }
        byte[] mRegisterImage = new byte[mImageWidth * mImageHeight];

        sgfplib.GetImage(mRegisterImage);

        //sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);

        int[] quality = new int[1];
        sgfplib.GetImageQuality(mImageWidth, mImageHeight, mRegisterImage, quality);

        if (quality[0] > matchPercentage) {
            SGFingerInfo fpInfo = new SGFingerInfo();
            /*fpInfo.FingerNumber = 1;
            fpInfo.ImageQuality = quality[0];
            fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
            fpInfo.ViewNumber = 1;*/

            Arrays.fill(mRegisterTemplate, (byte) 0);

            sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);

            int[] size = new int[1];
            sgfplib.GetTemplateSize(mRegisterTemplate, size);

            callback.onFingerPrintCapture(FingerprintUtils.toGrayscale(mRegisterImage, mImageWidth, mImageHeight), Base64.encodeToString(mRegisterTemplate, Base64.DEFAULT), quality[0]);
        } else {
            if (quality[0] == 0) {
                initSecugen();
            }
            callback.onFingerPrintQualityError();
        }
    }

    @Override
    public void captureFingerPrintWithEncodeData(String TAG, FingerprintReaderInit.FingerPrintDataTagCallback callback) {
        if (sgfplib == null || mImageWidth == 0 || mImageHeight == 0) {
            initSecugen();
            return;
        }
        byte[] mRegisterImage = new byte[mImageWidth * mImageHeight];

        sgfplib.GetImage(mRegisterImage);

        //sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);

        int[] quality = new int[1];
        sgfplib.GetImageQuality(mImageWidth, mImageHeight, mRegisterImage, quality);

        if (quality[0] > matchPercentage) {
            SGFingerInfo fpInfo = new SGFingerInfo();
            /*fpInfo.FingerNumber = 1;
            fpInfo.ImageQuality = quality[0];
            fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
            fpInfo.ViewNumber = 1;*/

            Arrays.fill(mRegisterTemplate, (byte) 0);

            sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);

            int[] size = new int[1];
            sgfplib.GetTemplateSize(mRegisterTemplate, size);

            callback.onFingerPrintCapture(TAG, FingerprintUtils.toGrayscale(mRegisterImage, mImageWidth, mImageHeight), Base64.encodeToString(mRegisterTemplate, Base64.DEFAULT), quality[0]);
        } else {
            if (quality[0] == 0) {
                initSecugen();
            }
            callback.onFingerPrintQualityError(TAG);
        }
    }

    @Override
    public void verifyCaptureFingerPrint(String oldFingerPrint, FingerprintReaderInit.FingerPrintVerifyCallback callback) {
        if (sgfplib == null || mImageWidth == 0 || mImageHeight == 0) {
            initSecugen();
            return;
        }
        byte[] mRegisterTemplate = Base64.decode(oldFingerPrint.getBytes(), Base64.DEFAULT);

        byte[] mVerifyImage = new byte[mImageWidth * mImageHeight];

        sgfplib.GetImage(mVerifyImage);

        //sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
        sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);

        int[] quality = new int[1];
        sgfplib.GetImageQuality(mImageWidth, mImageHeight, mVerifyImage, quality);

        if (quality[0] > matchPercentage) {
            SGFingerInfo fpInfo = new SGFingerInfo();
            /*fpInfo.FingerNumber = 1;
            fpInfo.ImageQuality = quality[0];
            fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
            fpInfo.ViewNumber = 1;*/

            Arrays.fill(mVerifyTemplate, (byte) 0);

            sgfplib.CreateTemplate(fpInfo, mVerifyImage, mVerifyTemplate);

            int[] size = new int[1];
            sgfplib.GetTemplateSize(mVerifyTemplate, size);

            boolean[] matched = new boolean[1];
            sgfplib.MatchTemplate(mRegisterTemplate, mVerifyTemplate, SGFDxSecurityLevel.SL_NORMAL, matched);

            callback.onFingerPrintVerify(FingerprintUtils.toGrayscale(mVerifyImage, mImageWidth, mImageHeight), matched[0]);
        } else {
            if (quality[0] == 0) {
                initSecugen();
            }
            callback.onFingerPrintQualityError();
        }
    }

    @Override
    public boolean verifyFingerPrints(String oldFp, String newFp) {
        if (oldFp == null || newFp == null) return false;

        boolean[] matched = new boolean[1];

        byte[] oldFpData = Base64.decode(oldFp.getBytes(), Base64.DEFAULT);
        byte[] newFpData = Base64.decode(newFp.getBytes(), Base64.DEFAULT);
        sgfplib.MatchTemplate(oldFpData, newFpData, SGFDxSecurityLevel.SL_NORMAL, matched);

        return matched[0];
    }

    @Override
    public boolean verifyFingerPrints(String newFp, List<String> oldFpList) {
        if (oldFpList == null || newFp == null) return false;

        boolean[] matched = new boolean[1];

        byte[] newFpData = Base64.decode(newFp.getBytes(), Base64.DEFAULT);
        for (String oldFp : oldFpList) {
            if (oldFp == null) continue;
            byte[] oldFpData = Base64.decode(oldFp.getBytes(), Base64.DEFAULT);
            sgfplib.MatchTemplate(oldFpData, newFpData, SGFDxSecurityLevel.SL_NORMAL, matched);

            if (matched[0]) break;
        }
        return matched[0];
    }

    @Override
    public void close() {
        /*Secugen variable destroy*/
        if (sgfplib != null) {
            //sgfplib.CloseDevice();
            mRegisterTemplate = null;
            mVerifyTemplate = null;
            sgfplib.Close();
        }
    }
}
