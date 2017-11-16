package com.meng.fingerdemo;

import android.app.Application;
import android.content.Context;

import com.meng.fingerdemo.util.ContextUtil;

/**
 * 描述：
 * 作者：孟崔广
 * 时间：2017/11/15 17:23
 * 邮箱：mengcuiguang@cashbang.cn
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtil.init(getApplicationContext());
    }
}
