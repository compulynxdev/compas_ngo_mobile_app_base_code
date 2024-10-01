package com.compastbc.ui.login.fingerprint.imagecapture;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.core.utils.PermissionUtils;
import com.compastbc.ui.base.BaseFragment;
import com.compastbc.ui.dialog.ImagePickDialog;

public class ImageCaptureFragment extends BaseFragment implements ImageCaptureMvpView {

    private static final String TAG = ImageCaptureFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    public ImageCaptureFragment() {
        // Required empty public constructor
    }

    public static ImageCaptureFragment newInstance(OnFragmentInteractionListener mListener) {

        Bundle args = new Bundle();

        ImageCaptureFragment fragment = new ImageCaptureFragment();
        fragment.setCallback(mListener);
        fragment.setArguments(args);
        return fragment;
    }

    private void setCallback(OnFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImageCapturePresenter<ImageCaptureMvpView> mPresenter = new ImageCapturePresenter<>(getDataManager());
        mPresenter.onAttach(this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_capture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        ImageView img_picture = view.findViewById(R.id.img_picture);

        view.findViewById(R.id.btn_capture).setOnClickListener(clickView -> {
            if (PermissionUtils.RequestMultiplePermissionCamera(getBaseActivity())) {
                ImagePickDialog.newInstance(bitmap -> {
                    img_picture.setImageBitmap(bitmap);
                    if (mListener != null) mListener.onImageCapture(bitmap);
                }).show(getChildFragmentManager(), TAG);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onImageCapture(Bitmap bitmap);
    }
}
