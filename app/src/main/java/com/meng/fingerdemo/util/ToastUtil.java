package com.meng.fingerdemo.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 描述：
 * 作者：孟崔广
 * 时间：2017/11/15 16:21
 * 邮箱：mengcuiguang@cashbang.cn
 */
public class ToastUtil {

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
