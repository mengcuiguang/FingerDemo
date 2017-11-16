package com.meng.fingerdemo.helper;

import android.app.KeyguardManager;
import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

/**
 * 描述：指纹帮助类
 * 作者：孟崔广
 * 时间：2017/11/15 16:05
 * 邮箱：mengcuiguang@cashbang.cn
 */
public class FingerHelper {

    /**
     * 官方写法，用于判断是否支持指纹识别
     *
     * @param context
     * @return
     */
    public static boolean isHardWareDetected(Context context) {
        return FingerprintManagerCompat.from(context).isHardwareDetected();
    }

    /**
     * 官方写法，当前手机是否设置过指纹
     *
     * @param context
     * @return
     */
    public static boolean hasEnrolledFingerPrint(Context context) {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints();
    }

    /**
     * 设备是否有屏幕锁保护 可以是password，PIN或者图案都行
     * google原生的逻辑就是：想要使用指纹识别的话，必须首先使能屏幕锁才行
     *
     * @param context
     * @return
     */
    public static boolean isKeyguardSecure(Context context) {
        return ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure();
    }

    /**
     * 开始进行指纹识别
     *
     * @param context
     * @param cancellationSignal 指纹识别取消的控制器
     * @param callback           指纹识别回调函数
     */
    public static void doFingerPrint(Context context, FingerprintManagerCompat.CryptoObject co, CancellationSignal cancellationSignal, FingerprintManagerCompat.AuthenticationCallback callback) {
        FingerprintManagerCompat managerCompat = FingerprintManagerCompat.from(context);
        managerCompat.authenticate(co, 0, cancellationSignal, callback, null);
    }
}
