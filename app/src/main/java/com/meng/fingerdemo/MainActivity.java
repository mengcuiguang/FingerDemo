package com.meng.fingerdemo;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.meng.fingerdemo.dialog.FingerDialog;
import com.meng.fingerdemo.helper.FingerHelper;
import com.meng.fingerdemo.util.ToastUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnMainFingerSupport, btnMainFingerSet, btnMainFingerVeri, btnMainFingerSdk, btnMainFingerPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
    }

    private void initView() {
        btnMainFingerSupport = findViewById(R.id.btn_main_finger_support);
        btnMainFingerSet = findViewById(R.id.btn_main_finger_set);
        btnMainFingerVeri = findViewById(R.id.btn_main_finger_veri);
        btnMainFingerSdk = findViewById(R.id.btn_main_finger_sdk);
        btnMainFingerPin = findViewById(R.id.btn_main_finger_pin);
    }

    private void initListener() {
        btnMainFingerSupport.setOnClickListener(this);
        btnMainFingerSet.setOnClickListener(this);
        btnMainFingerVeri.setOnClickListener(this);
        btnMainFingerSdk.setOnClickListener(this);
        btnMainFingerPin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_finger_sdk:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    ToastUtil.showToast(this, "大于22");
                } else {
                    ToastUtil.showToast(this, "小于22");
                }
                break;
            case R.id.btn_main_finger_pin:
                if (FingerHelper.isKeyguardSecure(this)) {
                    ToastUtil.showToast(this, "已设置图案锁");
                } else {
                    ToastUtil.showToast(this, "未设置图案锁");
                }
                break;
            case R.id.btn_main_finger_support:
                if (FingerHelper.isHardWareDetected(this)) {
                    ToastUtil.showToast(this, "支持");
                } else {
                    ToastUtil.showToast(this, "不支持");
                }
                break;
            case R.id.btn_main_finger_set:
                if (FingerHelper.hasEnrolledFingerPrint(this)) {
                    ToastUtil.showToast(this, "已设置");
                } else {
                    ToastUtil.showToast(this, "未设置");
                }
                break;

            case R.id.btn_main_finger_veri:
                if (!FingerHelper.isHardWareDetected(this)) {
                    ToastUtil.showToast(this, "暂不支持指纹");
                    return;
                }

                if (!FingerHelper.hasEnrolledFingerPrint(this)) {
                    ToastUtil.showToast(this, "请先设置指纹");
                    return;
                }

                new FingerDialog(this).showDialog();
                break;
        }
    }


}
