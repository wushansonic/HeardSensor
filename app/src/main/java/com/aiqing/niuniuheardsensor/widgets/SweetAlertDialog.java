package com.aiqing.niuniuheardsensor.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aiqing.niuniuheardsensor.R;

import java.util.List;

/**
 * Created by blue on 16/3/17.
 */
public class SweetAlertDialog extends Dialog implements View.OnClickListener {
    private View mDialogView;
    private AnimationSet mScaleInAnim;
    private AnimationSet mScaleOutAnim;
    //    private Animation mErrorInAnim;
    private AnimationSet mErrorXInAnim;
    private AnimationSet mSuccessLayoutAnimSet;
    private Animation mSuccessBowAnim;
    private TextView mTitleTextView;
    private TextView mContentTextView;
    private String mTitleText;
    private String mContentText;
    private boolean mShowCancel;
    private String mCancelText;
    private String mConfirmText;
    private String mConfirmText2;
    private int mAlertType;
    private FrameLayout mSuccessFrame;
    private View mSuccessLeftMask;
    private View mSuccessRightMask;
    private Drawable mCustomImgDrawable;
    private Button mConfirmButton;
    private Button mConfirmButton2;
    private Button mCancelButton;
    private FrameLayout mWarningFrame;
    private TextWatcher mTextWatcher;
    private OnSweetClickListener mCancelClickListener;
    private OnSweetClickListener mConfirmClickListener;
    private OnSweetClickListener mConfirmClickListener2;
    private ProgressBar progressBar;

    private EditText dialog_mobile_edit;
    private String hint;

    private int mContentTextGravity = Gravity.CENTER_HORIZONTAL;


    public static final int NORMAL_TYPE = 0;
    public static final int ERROR_TYPE = 1;
    public static final int SUCCESS_TYPE = 2;
    public static final int WARNING_TYPE = 3;

    private Context mContext;

    private boolean showEdit = true;

    public static interface OnSweetClickListener {
        public void onClick(SweetAlertDialog sweetAlertDialog);
    }

    public SweetAlertDialog(Context context) {
        this(context, NORMAL_TYPE);
        mContext = context;
    }

    public SweetAlertDialog(Context context, int alertType) {
        super(context, R.style.alert_dialog);
        mContext = context;

        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mAlertType = alertType;
        mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.sweet_error_x_in);
        // 2.3.x system don't support alpha-animation on layer-list drawable
        // remove it from animation set
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            List<Animation> childAnims = mErrorXInAnim.getAnimations();
            int idx = 0;
            for (; idx < childAnims.size(); idx++) {
                if (childAnims.get(idx) instanceof AlphaAnimation) {
                    break;
                }
            }
            if (idx < childAnims.size()) {
                childAnims.remove(idx);
            }
        }
        mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.sweet_success_bow_roate);
        mSuccessLayoutAnimSet = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.sweet_success_mask_layout);
        mScaleInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.sweet_dialog_scale_in);
        mScaleOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.sweet_dialog_scale_out);
        mScaleOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDialogView.setVisibility(View.GONE);
                mDialogView.post(new Runnable() {
                    @Override
                    public void run() {
                        SweetAlertDialog.super.dismiss();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sweet_alert_dialog);
        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mTitleTextView = (TextView) findViewById(R.id.title_text);
        mContentTextView = (TextView) findViewById(R.id.content_text);
        mSuccessFrame = (FrameLayout) findViewById(R.id.success_frame);
        mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
        mWarningFrame = (FrameLayout) findViewById(R.id.warning_frame);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mConfirmButton2 = (Button) findViewById(R.id.confirm_button_2);
        mCancelButton = (Button) findViewById(R.id.cancel_button);


        mConfirmButton.setOnClickListener(this);
        mConfirmButton2.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.pb);

        setTitleText(mTitleText);

        setTextChangedListener(mTextWatcher);

        setContentText(mContentText);
        setContentTextGravity(mContentTextGravity);
        showCancelButton(mShowCancel);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        setConfirmText2(mConfirmText2);
        changeAlertType(mAlertType, true);


        dialog_mobile_edit = (EditText) findViewById(R.id.dialog_mobile_edit);
        if (!TextUtils.isEmpty(hint))
            dialog_mobile_edit.setHint(hint);
        if (showEdit) {
            dialog_mobile_edit.setVisibility(View.VISIBLE);
        } else {
            dialog_mobile_edit.setVisibility(View.GONE);
        }
    }

    private void restore() {
        mSuccessFrame.setVisibility(View.GONE);
        mWarningFrame.setVisibility(View.GONE);

        mConfirmButton.setBackgroundResource(R.drawable.sweet_blue_button_background);
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();
    }

    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {
        } else if (mAlertType == SUCCESS_TYPE) {
            mSuccessRightMask.startAnimation(mSuccessBowAnim);
        }
    }

    private void changeAlertType(int alertType, boolean fromCreate) {
        mAlertType = alertType;
        // call after created views
        if (mDialogView != null) {
            if (!fromCreate) {
                // restore all of views state before switching alert type
                restore();
            }
            switch (mAlertType) {
                case SUCCESS_TYPE:
                    mSuccessFrame.setVisibility(View.VISIBLE);
                    // initial rotate layout of success mask
                    mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                    mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                    break;
                case WARNING_TYPE:
                    mConfirmButton.setBackgroundResource(R.drawable.sweet_red_button_background);
                    mWarningFrame.setVisibility(View.VISIBLE);
                    break;
            }
            if (!fromCreate) {
                playAnimation();
            }
        }
    }

    public int getAlerType() {
        return mAlertType;
    }

    public void changeAlertType(int alertType) {
        changeAlertType(alertType, false);
    }


    public String getTitleText() {
        return mTitleText;
    }

    public SweetAlertDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            mTitleTextView.setText(mTitleText);
        }
        return this;
    }

    public void setTextChangedListener(TextWatcher textWatcher) {
        mTextWatcher = textWatcher;
    }

    public String getContentText() {
        return mContentText;
    }

    public SweetAlertDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            mContentTextView.setVisibility(View.VISIBLE);
            mContentTextView.setText(mContentText);
        }
        return this;
    }

    public SweetAlertDialog setContentTextGravity(int gravity) {
        mContentTextGravity = gravity;
        if (mContentTextView != null) {
            mContentTextView.setGravity(gravity);
        }
        return this;
    }

    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    public SweetAlertDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }


    public String getCancelText() {
        return mCancelText;
    }

    public SweetAlertDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public String getConfirmText() {
        return mConfirmText;
    }

    public SweetAlertDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    public SweetAlertDialog setConfirmText2(String text) {
        mConfirmText2 = text;
        if (mConfirmButton2 != null && mConfirmText2 != null) {
            mConfirmButton2.setText(mConfirmText2);
            mConfirmButton2.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public SweetAlertDialog setCancelClickListener(OnSweetClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public SweetAlertDialog setConfirmClickListener(OnSweetClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    public SweetAlertDialog setConfirmClickListener2(OnSweetClickListener listener) {
        mConfirmClickListener2 = listener;
        return this;
    }

    public SweetAlertDialog setHint(String hint) {
        this.hint = hint;
        return this;
    }

    protected void onStart() {
        mDialogView.startAnimation(mScaleInAnim);
        playAnimation();
    }

    public void dismiss() {
        mDialogView.startAnimation(mScaleOutAnim);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                if (mCancelClickListener != null) {
                    mCancelClickListener.onClick(SweetAlertDialog.this);
                } else {
                    dismiss();
                }
                break;
            case R.id.confirm_button:
                if (mConfirmClickListener != null) {
                    mConfirmClickListener.onClick(SweetAlertDialog.this);
                } else {
                    dismiss();
                }
                break;
            case R.id.confirm_button_2:
                if (mConfirmClickListener2 != null) {
                    mConfirmClickListener2.onClick(SweetAlertDialog.this);
                } else {
                    dismiss();
                }
                break;

        }
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public String getMobile() {
        return dialog_mobile_edit.getText().toString();
    }

    public EditText getDialog_mobile_edit() {
        return dialog_mobile_edit;
    }


    public SweetAlertDialog hideEdit(boolean showEdit) {
        this.showEdit = showEdit;
        return this;
    }
}
