package com.meng.fingerdemo.dialog;

import android.content.Context;
import android.os.Handler;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.meng.fingerdemo.R;
import com.meng.fingerdemo.helper.CryptoObjectHelper;
import com.meng.fingerdemo.helper.FingerHelper;
import com.meng.fingerdemo.util.DrawableKit;

/**
 * 描述：指纹弹窗
 * 作者：孟崔广
 * 时间：2017/11/15 17:52
 * 邮箱：mengcuiguang@cashbang.cn
 */
public class FingerDialog {

    private Context context;

    private AlertDialog dialog;
    private ImageView ivDialogIcon;
    private TextView tvDialogText;
    private View btnDialogClose;

    // 指纹识别成功
    private static final int STYLE_SUCCESS = 1;
    // 指纹识别失败
    private static final int STYLE_ERROR = 2;
    // 指纹重试多次
    private static final int STYLE_WARN = 3;
    // 指纹验证
    private static final int STYLE_VERI = 4;

    private static final int COLOR_SUCCESS = R.color.green;
    private static final int COLOR_ERROR = R.color.colorAccent;
    private static final int COLOR_WARN = R.color.orange;

    private CancellationSignal cancellationSignal;
    private CryptoObjectHelper cryptoObjectHelper;

    public FingerDialog(Context context) {
        this.context = context;
    }

    public void showDialog() {
        dialogFinger();
    }

    /**
     * 指纹弹窗
     */
    private void dialogFinger() {
        View viewFinger = View.inflate(context, R.layout.view_dialog_finger, null);
        ivDialogIcon = viewFinger.findViewById(R.id.iv_dialog_finger_icon);
        tvDialogText = viewFinger.findViewById(R.id.tv_dialog_finger_text);
        btnDialogClose = viewFinger.findViewById(R.id.btn_dialog_finger_close);
        btnDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancellationSignal != null) {
                    cancellationSignal.cancel();
                    cancellationSignal = null;
                }
                dialog.dismiss();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.up_dialog);
        dialog = builder.create();
        dialog.setView(viewFinger);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // 清除样式
        DrawableKit.removeDrawableTintColor(ivDialogIcon.getDrawable());

        startFingerPrint();
    }

    /**
     * 指纹回调
     */
    private void startFingerPrint() {
        try {
            cancellationSignal = new CancellationSignal();
            cryptoObjectHelper = new CryptoObjectHelper();

            if (cancellationSignal == null) {
                cancellationSignal = new CancellationSignal();
            }
            FingerHelper.doFingerPrint(context, cryptoObjectHelper.buildCryptoObject(), cancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
                // 验证出错回调 指纹传感器会关闭一段时间,在下次调用authenticate时,会出现禁用期(时间依厂商不同30,1分都有)
                // 这个接口会再系统指纹认证出现不可恢复的错误的时候才会调用，并且参数errorCode就给出了错误码，标识了错误的原因。
                // 这个时候app能做的只能是提示用户重新尝试一遍。
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    super.onAuthenticationError(errMsgId, errString);
                    setStyle("请重新尝试指纹识别", STYLE_VERI);
//                ToastUtil.showToast(context,"禁用了");
                }

                // 验证帮助回调
                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    super.onAuthenticationHelp(helpMsgId, helpString);
                    setStyle(helpString.toString(), STYLE_WARN);
                }

                // 成功
                @Override
                public void onAuthenticationSucceeded(final FingerprintManagerCompat.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);

                    try {
                        //CryptoObject不是null的话，那么我们在这个方法中可以通过AuthenticationResult来获得Cypher对象然后调用它的doFinal方法。
                        // doFinal方法会检查结果是不是会拦截或者篡改过，如果是的话会抛出一个异常。
                        // 当我们发现这些异常的时候都应该将认证当做是失败来来处理
//                        result.getCryptoObject().getCipher().doFinal();

                        setStyle("指纹识别成功", STYLE_SUCCESS);

                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (cancellationSignal != null) {
                                    cancellationSignal.cancel();
                                    cancellationSignal = null;
                                }
                                dialog.dismiss();
                            }
                        }, 1000);
                    } catch (Exception e) {
//                        e.printStackTrace();
                        setStyle("指纹识别失败", STYLE_ERROR);
                    }
                }

                // 失败
                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    setStyle("指纹识别失败", STYLE_ERROR);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 弹窗样式
     *
     * @param msg
     * @param style
     */
    private void setStyle(String msg, int style) {
        tvDialogText.setText(msg);
        switch (style) {
            case STYLE_ERROR:
                tvDialogText.setTextColor(DrawableKit.getColorSrc(context, COLOR_ERROR));
                DrawableKit.setDrawableTintColor(ivDialogIcon.getDrawable(), COLOR_ERROR);
                break;
            case STYLE_WARN:
                tvDialogText.setTextColor(DrawableKit.getColorSrc(context, COLOR_WARN));
                DrawableKit.removeDrawableTintColor(ivDialogIcon.getDrawable());
                break;
            case STYLE_SUCCESS:
                tvDialogText.setTextColor(DrawableKit.getColorSrc(context, COLOR_SUCCESS));
                DrawableKit.setDrawableTintColor(ivDialogIcon.getDrawable(), COLOR_SUCCESS);
                break;
            case STYLE_VERI:
                tvDialogText.setTextColor(DrawableKit.getColorSrc(context, R.color.white_gray));
                DrawableKit.removeDrawableTintColor(ivDialogIcon.getDrawable());
                break;
            default:
                tvDialogText.setTextColor(DrawableKit.getColorSrc(context, R.color.white_gray));
                DrawableKit.removeDrawableTintColor(ivDialogIcon.getDrawable());
                break;
        }
    }
}
