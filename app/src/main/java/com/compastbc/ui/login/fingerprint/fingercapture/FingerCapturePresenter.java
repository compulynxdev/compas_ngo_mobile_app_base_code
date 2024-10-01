package com.compastbc.ui.login.fingerprint.fingercapture;

import android.content.Context;
import android.graphics.Bitmap;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.fingerprint.FingerprintReaderInit;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by hemant sharma on 12/08/19.
 */

public class FingerCapturePresenter<V extends FingerCaptureMvpView> extends BasePresenter<V>
        implements FingerCaptureMvpPresenter<V>, FingerprintReaderInit.FingerPrintDataTagCallback {

    private final Context context;
    private int counter, fcount = 0, fcounter = 0;
    private boolean skip = false, atnine = false, bfpC = false;
    private final boolean[] skippedFingers = new boolean[10];
    private final int[] fpQuality = new int[10];
    private String state = "";

    private FingerprintReaderInit fpInstance;
    private final MemberInfo memberInfo;

    FingerCapturePresenter(Context context, DataManager dataManager) {
        super(dataManager);
        this.context = context;
        memberInfo = new MemberInfo();
        for (int i = 0; i < 10; i++) {
            skippedFingers[i] = false;
        }
    }

    @Override
    public void onViewLoaded() {
        fpInstance = FingerprintReaderInit.getInstance(context);
        fpInstance.setMatchPercentage(getDataManager().getConfigurableParameterDetail().getMatchingPercentage());
    }

    @Override
    public void onLeftThumbClick() {
        if (checkFpInit("leftThumb")) {
            counter = 1;
            fcount = 0;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("leftThumb", this);
        }
    }

    @Override
    public void onLeftFrontClick() {
        if (checkFpInit("leftFront")) {
            counter = counter + 1;
            fcount = 1;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("leftFront", this);
        }
    }


    @Override
    public void onLeftOneCLick() {
        if (checkFpInit("leftOne")) {
            counter = counter + 1;
            fcount = 2;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("leftOne", this);
        }
    }

    @Override
    public void onLeftTwoCLick() {
        if (checkFpInit("leftTwo")) {
            counter = counter + 1;
            fcount = 3;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("leftTwo", this);
        }
    }

    @Override
    public void onLeftIndexClick() {
        if (checkFpInit("leftIndex")) {
            counter = counter + 1;
            fcount = 4;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("leftIndex", this);
        }
    }

    @Override
    public void onRightThumbClick() {
        if (checkFpInit("rightThumb")) {
            counter = counter + 1;
            fcount = 5;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("rightThumb", this);
        }
    }

    @Override
    public void onRightFrontClick() {
        if (checkFpInit("rightFront")) {
            counter = counter + 1;
            fcount = 6;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("rightFront", this);
        }
    }

    @Override
    public void onRightOneCLick() {
        if (checkFpInit("rightOne")) {
            counter = counter + 1;
            fcount = 7;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("rightOne", this);
        }
    }

    @Override
    public void onRightTwoCLick() {
        if (checkFpInit("rightTwo")) {
            counter = counter + 1;
            fcount = 8;
            state = "";
            fpInstance.captureFingerPrintWithEncodeData("rightTwo", this);
        }
    }

    @Override
    public void onRightIndexClick() {
        if (checkFpInit("rightIndex")) {
            counter = counter + 1;
            fcount = 9;
            state = "";
            atnine = true;
            fpInstance.captureFingerPrintWithEncodeData("rightIndex", this);
        }
    }

    @Override
    public MemberInfo getAllFingerPrintData() {
        return memberInfo;
    }

    private boolean checkFpInit(String value) {
        if (fpInstance == null) {
            state = "";
            updateUI(state, value, 0);
            return false;
        } else return true;
    }

    private void updateUI(String state, String value, int quality) {
        switch (value) {
            case "leftThumb":
                if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;
                    getMvpView().updateLeftThumbUI(state);
                } else if (state.equalsIgnoreCase("okay")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateLeftThumbUI("");
                        if (skip) {
                            if (fcounter < 5) {
                                String desc = "" + (5 - fcounter) + "  fingerprint templates have been skipped. Proceed to capture for the other hand?";
                            } else {
                                getMvpView().displayNextHand();
                            }
                        }
                    } else {
                        getMvpView().updateLeftThumbUI(state);
                    }
                }
                break;

            case "leftFront":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateLeftFrontUI("nomatch2");
                        if (skip) {
                            if (fcounter < 5) {
                                String desc = "" + (5 - fcounter) + "  fingerprint templates have been skipped. Proceed to capture for the other hand?";
                            } else {
                                getMvpView().displayNextHand();
                            }
                        }
                    } else {
                        getMvpView().updateLeftFrontUI(state);
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    getMvpView().updateLeftFrontUI(state);
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;
                    getMvpView().updateLeftFrontUI(state);
                }
                break;

            case "leftOne":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateLeftOneUI("nomatch2");
                        if (skip) {
                            if (fcounter < 5) {
                                String desc = "" + (5 - fcounter) + "  fingerprint templates have been skipped. Proceed to capture for the other hand?";
                            } else {
                                getMvpView().displayNextHand();
                            }
                        }
                    } else {
                        getMvpView().updateLeftOneUI(state);
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    getMvpView().updateLeftOneUI(state);
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;
                    getMvpView().updateLeftOneUI(state);
                }
                break;

            case "leftTwo":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateLeftTwoUI("nomatch2");
                        if (skip) {
                            if (fcounter < 5) {
                                String desc = "" + (5 - fcounter) + "  fingerprint templates have been skipped. Proceed to capture for the other hand?";
                            } else {
                                getMvpView().displayNextHand();
                            }
                        }
                    } else {
                        getMvpView().updateLeftTwoUI(state);
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    getMvpView().updateLeftTwoUI(state);
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;
                    getMvpView().updateLeftTwoUI(state);
                }
                break;

            case "leftIndex":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (fcounter < 5)
                        getMvpView().updateLeftIndexUI("nomatch2");

                    skip = true;

                    if (fcounter == 5) {
                        getMvpView().updateLeftIndexUI("nomatch2");
                        getMvpView().displayNextHand();
                        return;
                    }
                    if (skippedFingers[fcount]) {
                        getMvpView().updateLeftIndexUI("nomatch3");
                        if (skip) {
                            String desc = "" + (5 - fcounter) + "  fingerprint templates have been skipped. Proceed to capture for the other hand?";
                            if (bfpC && AppConstants.beneficiary) {
                                getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, context.getString(R.string.warning), desc)
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            getMvpView().displayNextHand();
                                        }).show();
                            }
                        }
                    } else {
                        if (skip) {
                            String desc = "" + (5 - fcounter) + "  fingerprint templates have been skipped. Proceed to capture for the other hand?";
                            if (bfpC && AppConstants.beneficiary) {
                                getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, context.getString(R.string.warning), desc)
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            getMvpView().displayNextHand();
                                        }).show();
                            }
                        } else {
                            getMvpView().updateLeftIndexUI(state);
                        }
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    getMvpView().updateLeftIndexUI(state);
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;

                    if (fcounter < 5) {
                        String desc = "" + (5 - fcounter) + "  fingerprint templates have been skipped. Proceed to capture for the other hand?";
                        if (bfpC && AppConstants.beneficiary) {
                            getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, context.getString(R.string.warning), desc)
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    }

                    getMvpView().updateLeftIndexUI(state);
                }
                break;

            case "rightThumb":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateRightThumbUI("nomatch2");
                        if (atnine) {
                            if (fcounter < 10) {
                                String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                                if (fcounter == 0)
                                    desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                            } /*else {
                                fpC = true; //->
                            }*/
                        }
                    } else {
                        getMvpView().updateRightThumbUI(state);
                        //fpC = true;
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    if (atnine) {
                        if (fcounter < 10) {
                            AppConstants.fpcount = fcounter;
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            AppConstants.fpcount = fcounter;
                            //fpC = true;  //->
                        }
                    }
                    getMvpView().updateRightThumbUI(state);
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } /*else {
                            fpC = true;  //->
                        }*/
                    }

                    getMvpView().updateRightThumbUI(state);
                }
                break;

            case "rightFront":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateRightFrontUI("nomatch2");
                        if (atnine) {
                            if (fcounter < 10) {
                                String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                                if (fcounter == 0)
                                    desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                            } /*else {
                                fpC = true;  //->
                            }*/
                        }
                    } else {
                        getMvpView().updateRightFrontUI(state);
                        //  fpC = true;
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        }/* else {
                            fpC = true; //->
                        }*/
                    }

                    getMvpView().updateRightFrontUI(state);
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;

                    getMvpView().updateRightFrontUI(state);

                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = (10 - fcounter) + "".concat("  fingerprint templates have been skipped.Save with the captured  finger printds templates?");
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            //fpC = true; //->
                            getMvpView().updateRightFrontUI("low2");
                        }
                    }
                }
                break;

            case "rightOne":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateRightOneUI("nomatch2");
                        if (atnine) {
                            if (fcounter < 10) {
                                String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                                if (fcounter == 0)
                                    desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                            } else {
                                //fpC = true; //->
                                getMvpView().updateRightOneUI("nomatch3");
                            }
                        }
                    } else {
                        getMvpView().updateRightOneUI(state);
                        // fpC = true;
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    getMvpView().updateRightOneUI(state);
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            //fpC = true; //->
                            getMvpView().updateRightOneUI("match2");
                        }
                    }
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;

                    getMvpView().updateRightOneUI(state);
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            //fpC = true; //->
                            getMvpView().updateRightOneUI("low2");
                        }
                    }
                }
                break;

            case "rightTwo":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateRightTwoUI("nomatch2");
                        if (atnine) {
                            if (fcounter < 10) {
                                String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                                if (fcounter == 0)
                                    desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                            } else {
                                //fpC = true; //->
                                getMvpView().updateRightTwoUI("nomatch3");
                            }
                        }
                    } else {
                        getMvpView().updateRightTwoUI(state);
                        if (atnine) {
                            if (fcounter < 10) {
                                String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                                if (fcounter == 0)
                                    desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                            } else {
                                //fpC = true; //->
                                getMvpView().updateRightTwoUI("nomatch1");
                            }
                        }
                        //fpC = true;
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);

                    getMvpView().updateRightTwoUI(state);
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            //fpC = true; //->
                            getMvpView().updateRightTwoUI("match2");
                        }
                    }
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;

                    getMvpView().updateRightTwoUI(state);
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            //fpC = true; //->
                            getMvpView().updateRightTwoUI("low2");
                        }
                    }
                }
                break;

            case "rightIndex":
                if (state.equalsIgnoreCase("nomatch")) {
                    fcounter = fcounter + 1;
                    AppConstants.fpcount = fcounter;
                    fpQuality[fcount] = quality;
                    if (skippedFingers[fcount]) {
                        getMvpView().updateRightIndexUI("nomatch2");
                        if (atnine) {
                            if (fcounter < 10) {
                                String desc;
                                if (fcounter == 0) {
                                    //fpCount = 0;
                                    AppConstants.fpcount = fcounter;
                                    desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                                } else {
                                    desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                                }
                            } else {
                                //fpC = true; //->
                                AppConstants.fpQuality = fpQuality;
                                getMvpView().updateRightIndexUI("nomatch3");

                            }
                        }
                    } else {
                        if (atnine) {
                            if (fcounter < 10) {

                                String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                                if (fcounter == 0)
                                    desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                            } /*else {
                                fpC = true; //->
                            }*/
                        }
                        getMvpView().updateRightIndexUI(state);
                        //fpC = true;  //->
                    }
                } else if (state.equalsIgnoreCase("match")) {
                    getMvpView().showMessage(R.string.error_duplicate_title, R.string.msg_already_capture);
                    getMvpView().updateRightIndexUI(state);
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            //fpC = true;  //->
                            getMvpView().updateRightIndexUI("match2");
                        }
                    }
                } else if (state.equalsIgnoreCase("low")) {
                    showRecaptureDialog();
                    skippedFingers[fcount] = true;
                    getMvpView().updateRightIndexUI(state);
                    if (atnine) {
                        if (fcounter < 10) {
                            String desc = "" + (10 - fcounter) + "  fingerprint templates have been skipped.Save with the captured  finger printds templates?";
                            if (fcounter == 0)
                                desc = "No fingerprint template has been successfully captured.If saved in this state, a supervisor will be required to authenticate any card transaction?";
                        } else {
                            //fpC = true; //->
                            getMvpView().updateRightIndexUI("low2");
                        }
                    }
                }
                break;

            default:
                getMvpView().sweetAlert(R.string.error_capture_title, R.string.msg_recapture)
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            fpInstance = FingerprintReaderInit.getInstance(context);
                        }).show();
                break;
        }
    }

    private void showRecaptureDialog() {
        SweetAlertDialog alert = getMvpView().sweetAlert(R.string.error_quality_title, R.string.msg_recapture)
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
        alert.show();
    }

    @Override
    public void onFingerPrintCapture(String TAG, Bitmap bitmap, String encodedCaptureData, int captureQuality) {
        bfpC = true;
        switch (TAG) {
            case "leftThumb":
                state = "okay";
                memberInfo.setLeftThumb(encodedCaptureData);
                getMvpView().updateThumb(TAG, bitmap);
                updateUI(state, TAG, captureQuality);
                break;

            case "leftFront":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setLeftFront(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "leftOne":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setLeftOne(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "leftTwo":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftOne(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setLeftTwo(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "leftIndex":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftOne(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftTwo(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setLeftIndex(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "rightThumb":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftOne(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftTwo(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftIndex(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setRightThumb(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "rightFront":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftOne(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftTwo(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftIndex(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightThumb(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setRightFront(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "rightOne":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftOne(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftTwo(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftIndex(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightFront(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setRightOne(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "rightTwo":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftOne(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftTwo(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftIndex(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightOne(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setRightTwo(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;

            case "rightIndex":
                if (fpInstance.verifyFingerPrints(memberInfo.getLeftThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftOne(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftTwo(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getLeftIndex(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightThumb(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightFront(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightOne(), encodedCaptureData)
                        || fpInstance.verifyFingerPrints(memberInfo.getRightTwo(), encodedCaptureData)) {
                    state = "match";
                } else {
                    state = "nomatch";
                    memberInfo.setRightIndex(encodedCaptureData);
                    getMvpView().updateThumb(TAG, bitmap);
                }
                updateUI(state, TAG, captureQuality);
                break;
        }
    }

    @Override
    public void onFingerPrintQualityError(String TAG) {
        bfpC = true;
        state = "low";
        updateUI(state, TAG, 0);
    }
}
