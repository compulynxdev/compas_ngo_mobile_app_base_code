package com.compastbc.ui.login.fingerprint.fingercapture;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.ui.base.BaseFragment;

public class FingerCaptureFragment extends BaseFragment implements FingerCaptureMvpView, View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private ImageView sl0, sl1, sl2, sr1, sr0, r0, r1, r2, l0, l1, l2, sr2, l3, l4, sl3, sl4, r3, r4, sr3, sr4;
    private ImageView img0, img1, img2, img3, img4, img6, img7, img8, img9, img10, img5;
    private ImageView done1, done2;
    private Animation animation;

    private FingerCapturePresenter<FingerCaptureMvpView> mPresenter;

    public FingerCaptureFragment() {
        // Required empty public constructor
    }

    public static FingerCaptureFragment newInstance(OnFragmentInteractionListener mListener) {
        FingerCaptureFragment fragment = new FingerCaptureFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setCallback(mListener);
        return fragment;
    }

    private void setCallback(OnFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPresenter = new FingerCapturePresenter<>(getBaseActivity(), getDataManager());
        mPresenter.onAttach(this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finger_capture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        done1 = view.findViewById(R.id.donel);
        done2 = view.findViewById(R.id.done2);

        animation = new AlphaAnimation(1, 0);
        animation.setDuration(200);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);

        l0 = view.findViewById(R.id.l0);
        sl0 = view.findViewById(R.id.sl0);
        l1 = view.findViewById(R.id.l1);
        sl1 = view.findViewById(R.id.sl1);
        l2 = view.findViewById(R.id.l2);
        sl2 = view.findViewById(R.id.sl2);
        l3 = view.findViewById(R.id.l3);
        sl3 = view.findViewById(R.id.sl3);
        l4 = view.findViewById(R.id.l4);
        sl4 = view.findViewById(R.id.sl4);
        r0 = view.findViewById(R.id.r0);
        sr0 = view.findViewById(R.id.sr0);
        r1 = view.findViewById(R.id.r1);
        sr1 = view.findViewById(R.id.sr1);
        r2 = view.findViewById(R.id.r2);
        sr2 = view.findViewById(R.id.sr2);
        r3 = view.findViewById(R.id.r3);
        sr3 = view.findViewById(R.id.sr3);
        r4 = view.findViewById(R.id.r4);
        sr4 = view.findViewById(R.id.sr4);

        img0 = view.findViewById(R.id.img0);
        img1 = view.findViewById(R.id.img1);
        img2 = view.findViewById(R.id.img2);
        img3 = view.findViewById(R.id.img3);
        img4 = view.findViewById(R.id.img4);
        img5 = view.findViewById(R.id.img5);
        img5.setVisibility(View.GONE);
        img6 = view.findViewById(R.id.img6);
        img6.setVisibility(View.GONE);
        img7 = view.findViewById(R.id.img7);
        img7.setVisibility(View.GONE);
        img8 = view.findViewById(R.id.img8);
        img8.setVisibility(View.GONE);
        img9 = view.findViewById(R.id.img9);
        img9.setVisibility(View.GONE);
        img10 = view.findViewById(R.id.img10);
        img10.setVisibility(View.GONE);

        img1.setEnabled(false);
        img2.setEnabled(false);
        img3.setEnabled(false);
        img4.setEnabled(false);
        img5.setEnabled(false);
        img6.setEnabled(false);
        img7.setEnabled(false);
        img8.setEnabled(false);
        img9.setEnabled(false);
        img10.setEnabled(false);

        img0.setVisibility(View.VISIBLE);
        img1.setVisibility(View.VISIBLE);
        img2.setVisibility(View.VISIBLE);
        img3.setVisibility(View.VISIBLE);
        img4.setVisibility(View.VISIBLE);
        img5.setVisibility(View.GONE);
        img6.setVisibility(View.GONE);
        img7.setVisibility(View.GONE);
        img8.setVisibility(View.GONE);
        img9.setVisibility(View.GONE);
        img10.setVisibility(View.GONE);

        img0.setEnabled(true);
        l0.setVisibility(View.VISIBLE);

        setOnClickListener(img0, img1, img2, img3, img4, img5, img6, img7, img8, img9, img10);
        setAnimation(l0, l1, l2, l3, l4, r0, r1, r2, r3, r4);

        mPresenter.onViewLoaded();
    }

    private void setAnimation(View... views) {
        for (View view : views) {
            view.startAnimation(animation);
        }
    }

    private void setOnClickListener(View... views) {
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img0:
                mPresenter.onLeftThumbClick();
                break;

            case R.id.img1:
                mPresenter.onLeftFrontClick();
                break;

            case R.id.img2:
                mPresenter.onLeftOneCLick();
                break;

            case R.id.img3:
                mPresenter.onLeftTwoCLick();
                break;

            case R.id.img4:
                mPresenter.onLeftIndexClick();
                break;

            case R.id.img5:
                mPresenter.onRightThumbClick();
                break;

            case R.id.img6:
                mPresenter.onRightFrontClick();
                break;

            case R.id.img7:
                mPresenter.onRightOneCLick();
                break;

            case R.id.img8:
                mPresenter.onRightTwoCLick();
                break;

            case R.id.img9:
                mPresenter.onRightIndexClick();
                break;
        }
    }

    @Override
    public void updateLeftThumbUI(String state) {
        if (state.equalsIgnoreCase("low")) {
            img1.setEnabled(true);
            img0.setImageResource(R.drawable.bg_fp_no_img);
            l1.startAnimation(animation);
            l0.clearAnimation();
            l0.setVisibility(View.GONE);
            l1.setVisibility(View.VISIBLE);
        } else if (state.equalsIgnoreCase("okay")) {
            l0.clearAnimation();
            l0.setVisibility(View.GONE);
            sl0.setVisibility(View.VISIBLE);
            l1.startAnimation(animation);
            l1.setVisibility(View.VISIBLE);
            img0.setEnabled(false);
            img1.setEnabled(true);
        } else {
            l0.clearAnimation();
            l0.setVisibility(View.GONE);
            sl0.setVisibility(View.VISIBLE);
            img0.setEnabled(false);
        }
    }

    @Override
    public void updateLeftFrontUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            l1.clearAnimation();
            l1.setVisibility(View.GONE);
            sl1.setVisibility(View.VISIBLE);
            l2.startAnimation(animation);
            l2.setVisibility(View.VISIBLE);
            img1.setEnabled(false);
            img2.setEnabled(true);
        } else if (state.equalsIgnoreCase("nomatch2")) {
            l1.clearAnimation();
            l1.setVisibility(View.GONE);
            sl1.setVisibility(View.VISIBLE);
            img1.setEnabled(false);
        } else if (state.equalsIgnoreCase("match")) {
            img1.setImageResource(R.drawable.bg_fp_no_img);
        } else if (state.equalsIgnoreCase("low")) {
            img1.setImageResource(R.drawable.bg_fp_no_img);
            img2.setEnabled(true);
            l2.startAnimation(animation);
            l2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateLeftOneUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            l2.clearAnimation();
            l2.setVisibility(View.GONE);
            sl2.setVisibility(View.VISIBLE);
            l3.startAnimation(animation);
            l3.setVisibility(View.VISIBLE);
            img2.setEnabled(false);
            img3.setEnabled(true);
        } else if (state.equalsIgnoreCase("nomatch2")) {
            l2.clearAnimation();
            l2.setVisibility(View.GONE);
            sl2.setVisibility(View.VISIBLE);
            img2.setEnabled(false);
        } else if (state.equalsIgnoreCase("match")) {
            img2.setImageResource(R.drawable.bg_fp_no_img);
        } else if (state.equalsIgnoreCase("low")) {
            img2.setImageResource(R.drawable.bg_fp_no_img);
            l3.startAnimation(animation);
            l3.setVisibility(View.VISIBLE);
            img3.setEnabled(true);
        }
    }

    @Override
    public void updateLeftTwoUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            l3.clearAnimation();
            l3.setVisibility(View.GONE);
            sl3.setVisibility(View.VISIBLE);
            l4.startAnimation(animation);
            l4.setVisibility(View.VISIBLE);
            img3.setEnabled(false);
            img4.setEnabled(true);
        } else if (state.equalsIgnoreCase("nomatch2")) {
            l3.clearAnimation();
            l3.setVisibility(View.GONE);
            sl3.setVisibility(View.VISIBLE);
            img3.setEnabled(false);
        } else if (state.equalsIgnoreCase("match")) {
            img3.setImageResource(R.drawable.bg_fp_no_img);
        } else if (state.equalsIgnoreCase("low")) {
            img3.setImageResource(R.drawable.bg_fp_no_img);
            l4.startAnimation(animation);
            l4.setVisibility(View.VISIBLE);
            img4.setEnabled(true);
        }
    }

    @Override
    public void updateLeftIndexUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            l4.clearAnimation();
            l4.setVisibility(View.GONE);
            sl4.setVisibility(View.VISIBLE);
            r0.startAnimation(animation);
            r0.setVisibility(View.VISIBLE);
            img4.setEnabled(false);
            img5.setEnabled(true);
            img6.setVisibility(View.VISIBLE);
            img7.setVisibility(View.VISIBLE);
            img8.setVisibility(View.VISIBLE);
            img9.setVisibility(View.VISIBLE);
            img0.setVisibility(View.GONE);
            img1.setVisibility(View.GONE);
            img2.setVisibility(View.GONE);
            img3.setVisibility(View.GONE);
            img4.setVisibility(View.GONE);
            img5.setVisibility(View.VISIBLE);
            done1.setImageResource(R.drawable.ic_right);
        } else if (state.equalsIgnoreCase("nomatch2") || state.equalsIgnoreCase("nomatch3")) {
            l4.clearAnimation();
            l4.setVisibility(View.GONE);
            sl4.setVisibility(View.VISIBLE);

            if (state.equalsIgnoreCase("nomatch3")) img4.setEnabled(false);
        } else if (state.equalsIgnoreCase("match") || state.equalsIgnoreCase("low")) {
            img4.setImageResource(R.drawable.bg_fp_no_img);
        }
    }

    @Override
    public void updateRightThumbUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            r0.clearAnimation();
            r0.setVisibility(View.GONE);
            sr0.setVisibility(View.VISIBLE);
            r1.startAnimation(animation);
            r1.setVisibility(View.VISIBLE);
            img5.setEnabled(false);
            img6.setEnabled(true);
        } else if (state.equalsIgnoreCase("nomatch2")) {
            r0.clearAnimation();
            r0.setVisibility(View.GONE);
            sr0.setVisibility(View.VISIBLE);
            img5.setEnabled(false);
        } else if (state.equalsIgnoreCase("match")) {
            img5.setImageResource(R.drawable.bg_fp_no_img);
        } else if (state.equalsIgnoreCase("low")) {
            img5.setImageResource(R.drawable.bg_fp_no_img);
            r1.startAnimation(animation);
            r1.setVisibility(View.VISIBLE);
            img6.setEnabled(true);
        }
    }

    @Override
    public void updateRightFrontUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            r1.clearAnimation();
            r1.setVisibility(View.GONE);
            sr1.setVisibility(View.VISIBLE);
            r2.startAnimation(animation);
            r2.setVisibility(View.VISIBLE);
            img7.setEnabled(true);
            img6.setEnabled(false);
        } else if (state.equalsIgnoreCase("nomatch2")) {
            r1.clearAnimation();
            r1.setVisibility(View.GONE);
            sr1.setVisibility(View.VISIBLE);
            img6.setEnabled(false);
        } else if (state.equalsIgnoreCase("match")) {
            img6.setImageResource(R.drawable.bg_fp_no_img);
        } else if (state.equalsIgnoreCase("low") || state.equalsIgnoreCase("low2")) {
            img6.setImageResource(R.drawable.bg_fp_no_img);
            r2.startAnimation(animation);
            r2.setVisibility(View.VISIBLE);
            img7.setEnabled(true);

            if (state.equalsIgnoreCase("low2"))
                done2.setImageResource(R.drawable.ic_right);
        }
    }

    @Override
    public void updateRightOneUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            r2.clearAnimation();
            r2.setVisibility(View.GONE);
            sr2.setVisibility(View.VISIBLE);
            r3.startAnimation(animation);
            r3.setVisibility(View.VISIBLE);
            img7.setEnabled(false);
            img8.setEnabled(true);
        } else if (state.equalsIgnoreCase("nomatch2") || state.equalsIgnoreCase("nomatch3")) {
            r2.clearAnimation();
            r2.setVisibility(View.GONE);
            sr2.setVisibility(View.VISIBLE);
            img7.setEnabled(false);

            if (state.equalsIgnoreCase("nomatch3"))
                done2.setImageResource(R.drawable.ic_right);
        } else if (state.equalsIgnoreCase("match") || state.equalsIgnoreCase("match2")) {
            img7.setImageResource(R.drawable.bg_fp_no_img);

            if (state.equalsIgnoreCase("match2"))
                done2.setImageResource(R.drawable.ic_right);
        } else if (state.equalsIgnoreCase("low") || state.equalsIgnoreCase("low2")) {
            img7.setImageResource(R.drawable.bg_fp_no_img);
            r3.startAnimation(animation);
            r3.setVisibility(View.VISIBLE);
            img8.setEnabled(true);

            if (state.equalsIgnoreCase("low2"))
                done2.setImageResource(R.drawable.ic_right);
        }
    }

    @Override
    public void updateRightTwoUI(String state) {
        if (state.equalsIgnoreCase("nomatch") || state.equalsIgnoreCase("nomatch1")) {
            r3.clearAnimation();
            r3.setVisibility(View.GONE);
            sr3.setVisibility(View.VISIBLE);
            r4.startAnimation(animation);
            r4.setVisibility(View.VISIBLE);
            img8.setEnabled(false);
            img9.setEnabled(true);

            if (state.equalsIgnoreCase("nomatch1"))
                done2.setImageResource(R.drawable.ic_right);
        } else if (state.equalsIgnoreCase("nomatch2") || state.equalsIgnoreCase("nomatch3")) {
            r3.clearAnimation();
            r3.setVisibility(View.GONE);
            sr3.setVisibility(View.VISIBLE);
            img8.setEnabled(false);

            if (state.equalsIgnoreCase("nomatch3"))
                done2.setImageResource(R.drawable.ic_right);
        } else if (state.equalsIgnoreCase("match") || state.equalsIgnoreCase("match2")) {
            img8.setImageResource(R.drawable.bg_fp_no_img);

            if (state.equalsIgnoreCase("match2"))
                done2.setImageResource(R.drawable.ic_right);

        } else if (state.equalsIgnoreCase("low") || state.equalsIgnoreCase("low2")) {
            img8.setImageResource(R.drawable.bg_fp_no_img);
            r4.startAnimation(animation);
            r4.setVisibility(View.VISIBLE);
            img9.setEnabled(true);

            if (state.equalsIgnoreCase("low2"))
                done2.setImageResource(R.drawable.ic_right);

        }
    }

    @Override
    public void updateRightIndexUI(String state) {
        if (state.equalsIgnoreCase("nomatch")) {
            r4.clearAnimation();
            r4.setVisibility(View.GONE);
            sr4.setVisibility(View.VISIBLE);
            img9.setEnabled(false);
            done2.setImageResource(R.drawable.ic_right);
        } else if (state.equalsIgnoreCase("nomatch2") || state.equalsIgnoreCase("nomatch3")) {
            r4.clearAnimation();
            r4.setVisibility(View.GONE);
            sr4.setVisibility(View.VISIBLE);

            if (state.equalsIgnoreCase("nomatch3"))
                done2.setImageResource(R.drawable.ic_right);
        } else if (state.equalsIgnoreCase("match") || state.equalsIgnoreCase("match2")) {
            img9.setImageResource(R.drawable.bg_fp_no_img);

            if (state.equalsIgnoreCase("match2"))
                done2.setImageResource(R.drawable.ic_right);

        } else if (state.equalsIgnoreCase("low") || state.equalsIgnoreCase("low2")) {
            img9.setImageResource(R.drawable.bg_fp_no_img);

            if (state.equalsIgnoreCase("low2"))
                done2.setImageResource(R.drawable.ic_right);

        }
    }

    @Override
    public void displayNextHand() {
        r0.startAnimation(animation);
        r0.setVisibility(View.VISIBLE);
        img5.setEnabled(true);
        img6.setVisibility(View.VISIBLE);
        img7.setVisibility(View.VISIBLE);
        img8.setVisibility(View.VISIBLE);
        img9.setVisibility(View.VISIBLE);
        img0.setVisibility(View.GONE);
        img1.setVisibility(View.GONE);
        img2.setVisibility(View.GONE);
        img3.setVisibility(View.GONE);
        img4.setVisibility(View.GONE);
        img5.setVisibility(View.VISIBLE);
        done1.setImageResource(R.drawable.ic_right);
    }

    @Override
    public void updateThumb(String fingerName, Bitmap bitmap) {

        switch (fingerName) {
            case "leftThumb":
                img0.setImageBitmap(bitmap);
                break;

            case "leftFront":
                img1.setImageBitmap(bitmap);
                break;

            case "leftOne":
                img2.setImageBitmap(bitmap);
                break;

            case "leftTwo":
                img3.setImageBitmap(bitmap);
                break;

            case "leftIndex":
                img4.setImageBitmap(bitmap);
                break;

            case "rightThumb":
                img5.setImageBitmap(bitmap);
                break;

            case "rightFront":
                img6.setImageBitmap(bitmap);
                break;

            case "rightOne":
                img7.setImageBitmap(bitmap);
                break;

            case "rightTwo":
                img8.setImageBitmap(bitmap);
                break;

            case "rightIndex":
                img9.setImageBitmap(bitmap);
                break;

        }

        if (mListener != null) mListener.fingerPrintData(mPresenter.getAllFingerPrintData());
    }

    @Override
    public void reset(int counter) {
        switch (counter) {
            case 1:
                l0.startAnimation(animation);
                l0.setVisibility(View.VISIBLE);
                sl0.setVisibility(View.GONE);
                img0.setEnabled(true);
                img0.setImageBitmap(null);
                img0.setImageResource(R.drawable.bg_fp_no_img);
                /*counter = counter - 1;
                fpC = false;*/
                break;
            case 2:
                l1.startAnimation(animation);
                l1.setVisibility(View.VISIBLE);
                sl1.setVisibility(View.GONE);
                img1.setEnabled(true);
                img1.setImageBitmap(null);
                img1.setImageResource(R.drawable.bg_fp_no_img);
                /*counter = counter - 1;
                fpC = false;*/
                break;
            case 3:
                l2.startAnimation(animation);
                l2.setVisibility(View.VISIBLE);
                sl2.setVisibility(View.GONE);
                r0.clearAnimation();
                r0.setVisibility(View.GONE);
                img2.setEnabled(true);
                img3.setEnabled(false);
                img2.setImageBitmap(null);
                img2.setImageResource(R.drawable.bg_fp_no_img);
                //counter = counter - 1;
                break;

            case 4:
                img3.setEnabled(true);
                img3.setImageBitmap(null);
                img3.setImageResource(R.drawable.bg_fp_no_img);
                //counter = counter - 1;
                break;

            case 5:
                img4.setEnabled(true);
                img4.setImageBitmap(null);
                img4.setImageResource(R.drawable.bg_fp_no_img);
                //counter = counter - 1;
                break;

            case 6:
                img5.setEnabled(true);
                img5.setImageBitmap(null);
                img5.setImageResource(R.drawable.bg_fp_no_img);
                //counter = counter - 1;
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void fingerPrintData(MemberInfo memberInfo);
    }
}
