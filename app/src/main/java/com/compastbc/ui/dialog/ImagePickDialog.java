package com.compastbc.ui.dialog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.ui.base.BaseBottomSheetDialog;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;


/**
 * Created by hemant
 * Date: 25/5/18
 * Time: 2:10 PM
 */


public class ImagePickDialog extends BaseBottomSheetDialog implements View.OnClickListener, ImagePickDialogMvpView {

    private static final String TAG = ImagePickDialog.class.getSimpleName();

    private OnDialogFragmentInteractionListener callback;
    private ImagePickDialogMvpPresenter<ImagePickDialogMvpView> mPresenter;
    private String mCurrentPhotoPath = "";

    private ImagePickDialog() {

    }

    public static ImagePickDialog newInstance(OnDialogFragmentInteractionListener callback) {
        ImagePickDialog fragment = new ImagePickDialog();
        fragment.setCallback(callback);
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void setCallback(OnDialogFragmentInteractionListener callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPresenter = new ImagePickDialogPresenter<>(getBaseActivity(), getDataManager());
        mPresenter.onAttach(this);
        return inflater.inflate(R.layout.dialog_image_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvCancel:
                dismiss();
                break;

            case R.id.tvCamera:
                mPresenter.dispatchTakePictureIntent();
                break;

            case R.id.tvGallery:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, AppConstants.REQUEST_GALLERY);
                break;
        }
    }

    @Override
    protected void setUp(View view) {
        view.findViewById(R.id.tvCamera).setOnClickListener(this);
        view.findViewById(R.id.tvGallery).setOnClickListener(this);
        view.findViewById(R.id.tvCancel).setOnClickListener(this);
    }

    @Override
    public void getStartCameraIntent(Intent takePictureIntent) {
        startActivityForResult(takePictureIntent, AppConstants.REQUEST_TAKE_PHOTO);
    }

    @Override
    public void setImagePath(String path) {
        mCurrentPhotoPath = path;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstants.REQUEST_GALLERY:
                    try {
                        Uri uri = data.getData();
                        if (uri != null) {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getBaseActivity().getContentResolver(), uri);
                            callback.onImageReceived(bitmap);
                        } else {
                            sweetAlert(getString(R.string.alert), getString(R.string.some_error))
                                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }
                    } catch (OutOfMemoryError e) {
                        sweetAlert(getString(R.string.alert), getString(R.string.alert_out_of_memory)).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        e.printStackTrace();
                    } catch (Exception e) {
                        sweetAlert(getString(R.string.alert), getString(R.string.some_error)).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        e.printStackTrace();
                    }
                    break;

                case AppConstants.REQUEST_TAKE_PHOTO:
                    try {
                        Uri uri = Uri.fromFile(new File(mCurrentPhotoPath));
                        if (uri != null) {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getBaseActivity().getContentResolver(), uri);
                            callback.onImageReceived(bitmap);
                        } else {
                            sweetAlert(getString(R.string.alert), getString(R.string.some_error)).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        }

                    } catch (OutOfMemoryError e) {
                        sweetAlert(getString(R.string.alert), getString(R.string.alert_out_of_memory)).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        e.printStackTrace();
                    } catch (Exception e) {
                        sweetAlert(getString(R.string.alert), getString(R.string.some_error)).setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                        e.printStackTrace();
                    }
                    break;
            }
        }
        dismiss();
    }

    public interface OnDialogFragmentInteractionListener {
        void onImageReceived(Bitmap bitmap);
    }
}

